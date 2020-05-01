package org.processmining.Guido.DataAwareConformanceChecking;

/*
 * Mainly taken from PNetReplayer
 */

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.Guido.DataPetriNetWithCustomElements;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.datapetrinets.DataPetriNetsWithMarkings;
import org.processmining.datapetrinets.utils.MarkingsHelper;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.annotations.ConnectionObjectFactory;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.balancedconformance.ui.ControlFlowCostUI;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.utils.ConnectionManagerHelper;

import javax.swing.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * Borrowed from PNetReplayer
 */
public class PNReplayer {
    private ControlFlowCostConverter controlFlowCostConverter;
    private TransEvClassMapping mapping;
    private ControlFlowCost controlFlowCost;

    public PNReplayer(PetrinetGraph net) {
        setTemporaryInitialMarking(net);
        setTemporaryFinalMarking(net);
    }

    public void configureMarkings(UIPluginContext context, PetrinetGraph net) {
        configureInitialMarking(context, net);
        configureFinalMarking(context, net);
    }

    public ControlFlowCost getControlFlowCost(UIPluginContext context, PetrinetGraph net, XLog log, XEventClasses eventClasses) {
        // init local parameter
        EvClassLogPetrinetConnection conn;

        try {
            conn = context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class, context, net, log);
        } catch (Exception e) {
            System.out.println("Failed creating mapping");
            System.out.println("No mapping can be constructed between the net and the log. Caused by:\n\n" +
                    Throwables.getStackTraceAsString(e));
            return null;
        }

        // init gui for each step
        mapping = (TransEvClassMapping) conn.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);

        // check invisible transitions
        configureInvisibleTransitions(mapping);

        // populate event classes
        List<XEventClass> evClassCol = new ArrayList<>(eventClasses.getClasses());
        evClassCol.add(mapping.getDummyEventClass());
        ArrayList<Transition> transitionList = Lists.newArrayList(net.getTransitions());
        Collections.sort(evClassCol, new Comparator<XEventClass>() {
            public int compare(XEventClass o1, XEventClass o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        Collections.sort(transitionList, new Comparator<Transition>() {
            public int compare(Transition o1, Transition o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });

        controlFlowCostConverter = new ControlFlowCostConverter();
        return new ControlFlowCost(transitionList, evClassCol, mapping, controlFlowCostConverter);
    }

    public void setControlFlowCost(ControlFlowCost controlFlowCost) {
        this.controlFlowCost = controlFlowCost;
        this.controlFlowCost.setConverterMaps(controlFlowCostConverter);

    }

    public CostBasedCompleteParam getConfiguration(UIPluginContext context, PetrinetGraph net) {
        return createCostBasedParameters(controlFlowCost, mapping, getInitialMarking(context, net), getFinalMarkings(context, net));

    }

    private void configureInvisibleTransitions(TransEvClassMapping mapping) {
        Set<Transition> unmappedTrans = new HashSet<>();
        for (Entry<Transition, XEventClass> entry : mapping.entrySet()) {
            if (entry.getValue().equals(mapping.getDummyEventClass())) {
                if (!entry.getKey().isInvisible()) {
                    unmappedTrans.add(entry.getKey());
                }
            }
        }
        if (!unmappedTrans.isEmpty()) {
            JList list = new JList(unmappedTrans.toArray());
            JPanel panel = new JPanel();
            BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
            panel.setLayout(layout);
            panel.add(new JLabel("The following transitions are not mapped to any event class:"));

            JScrollPane sp = new JScrollPane(list);
            panel.add(sp);
            panel.add(new JLabel("Do you want to consider these transitions as invisible (unlogged activities)?"));

            Object[] options = { "Yes, set them to invisible", "No, keep them as they are" };

            if (0 == JOptionPane.showOptionDialog(null, panel, "Configure transition visibility",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0])) {
                for (Transition t : unmappedTrans) {
                    t.setInvisible(true);
                }
            }
            ;
        }
    }

    private void configureInitialMarking(UIPluginContext context, PetrinetGraph net) {

        if (net instanceof DataPetriNetsWithMarkings && ((DataPetriNetsWithMarkings) net).getInitialMarking() != null) {
            return;
        }

        // check existence of initial marking
        try {
            InitialMarkingConnection initCon = ConnectionManagerHelper
                    .safeGetFirstConnection(context.getConnectionManager(), InitialMarkingConnection.class, net);

            if (((Marking) initCon.getObjectWithRole(InitialMarkingConnection.MARKING)).isEmpty()) {
                JOptionPane.showMessageDialog(new JPanel(),
                        "The initial marking is an empty marking. If this is not intended, remove the currently existing InitialMarkingConnection object and, then, try again!",
                        "Empty Initial Marking", JOptionPane.WARNING_MESSAGE);
            }
        } catch (ConnectionCannotBeObtained exc) {
            //TODO: fix this
            Marking guessedInitialMarking = MarkingsHelper.guessInitialMarkingByStructure(net);
            if (guessedInitialMarking != null) {
//                String[] options = new String[] { "Keep guessed", "Create manually" };
//                System.out.println("<HTML>No initial marking is found for this model. Based on the net structure the initial marking should be: [<B>" +
//                        guessedInitialMarking.iterator().next() +
//                        "</B>].<BR/>Do you want to use the guessed marking, or manually create a new one?</HTML>");
//                int result = JOptionPane.showOptionDialog(context.getGlobalContext().getUI(),
//                        "<HTML>No initial marking is found for this model. Based on the net structure the initial marking should be: [<B>"
//                                + guessedInitialMarking.iterator().next()
//                                + "</B>].<BR/>Do you want to use the guessed marking, or manually create a new one?</HTML>",
//                        "No Initial Marking", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
//                        options[0]);
//                if (result == 1) {
//                    createMarking(context, net, InitialMarkingConnection.class);
//                } else {
//                    publishInitialMarking(context, net, guessedInitialMarking);
//                }
                publishInitialMarking(context, net, guessedInitialMarking);
            } else {
                createMarking(context, net, InitialMarkingConnection.class);
            }
        }
    }

    private void configureFinalMarking(UIPluginContext context, PetrinetGraph net) {

        if (net instanceof DataPetriNetsWithMarkings && ((DataPetriNetsWithMarkings) net).getFinalMarkings() != null
                && ((DataPetriNetsWithMarkings) net).getFinalMarkings().length > 0) {
            return;
        }

        // check existence of final marking
        try {
            FinalMarkingConnection finalConn = ConnectionManagerHelper
                    .safeGetFirstConnection(context.getConnectionManager(), FinalMarkingConnection.class, net);
            Marking finalMarking = finalConn.getObjectWithRole(FinalMarkingConnection.MARKING);
            if (finalMarking.isEmpty()) {
                System.out.println("WARNING_MESSAGE");
                System.out.println("Empty Final Marking");
                System.out.println("The final marking is an empty marking. If this is not intended, remove the currently existing FinalMarkingConnection object and, then, try again!");
            }
        } catch (ConnectionCannotBeObtained exc) {
            Marking guessedFinalMarking = MarkingsHelper.guessFinalMarkingByStructure(net);
            // TODO: fix this
            if (guessedFinalMarking != null) {
//                String[] options = new String[] { "Keep guessed", "Create manually" };
//                int result = JOptionPane.showOptionDialog(context.getGlobalContext().getUI(),
//                        "<HTML>No final marking is found for this model. Based on the net structure place the final marking should be: [<B>"
//                                + guessedFinalMarking.iterator().next()
//                                + "</B>].<BR/>Do you want to use the guessed marking, or manually create a new one?</HTML>",
//                        "No Final Marking", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
//                        options[0]);
//                if (result == 1) {
//                    createMarking(context, net, FinalMarkingConnection.class);
//                } else {
//                    publishFinalMarking(context, net, guessedFinalMarking);
//                }
                publishFinalMarking(context, net, guessedFinalMarking);
            } else {
                createMarking(context, net, FinalMarkingConnection.class);
            }
        }
    }

    private void setTemporaryInitialMarking(PetrinetGraph net) {
        Marking guessedInitialMarking = MarkingsHelper.guessInitialMarkingByStructure(net);
        if (guessedInitialMarking != null && net instanceof DataPetriNetWithCustomElements)
            ((DataPetriNetWithCustomElements) net).setInitialMarking(guessedInitialMarking);
        else
            throw new NullPointerException("Impossibile to guess the initial marking");
    }

    private void setTemporaryFinalMarking(PetrinetGraph net) {
        Marking guessedInitialMarking = MarkingsHelper.guessFinalMarkingByStructure(net);
        if (guessedInitialMarking != null && net instanceof DataPetriNetWithCustomElements)
            ((DataPetriNetWithCustomElements) net).setLastTransition(guessedInitialMarking);
        else
            throw new NullPointerException("Impossibile to guess the final marking");
    }

    private void publishInitialMarking(UIPluginContext context, PetrinetGraph net, Marking initialMarking) {
        context.getConnectionManager().addConnection(new InitialMarkingConnection(net, initialMarking));
//        ((DataPetriNetWithCustomElements) net).setInitialMarking(initialMarking);
    }

    private void publishFinalMarking(UIPluginContext context, PetrinetGraph net, Marking finalMarking) {
        context.getConnectionManager().addConnection(new FinalMarkingConnection(net, finalMarking));
//        ((DataPetriNetWithCustomElements) net).setLastTransition(finalMarking);
    }

    private CostBasedCompleteParam createCostBasedParameters(ControlFlowCost importer, TransEvClassMapping mapping,
                                                             Marking initialMarking, Marking[] finalMarkings) {
        CostBasedCompleteParam paramObj = new CostBasedCompleteParam(importer.getMapEvClassToCost(),
                importer.getTransitionWeight());
        paramObj.setMapSync2Cost(importer.getSyncCost());
        paramObj.setMaxNumOfStates(Integer.MAX_VALUE);
        paramObj.setInitialMarking(initialMarking);
        paramObj.setFinalMarkings(finalMarkings);
        paramObj.setUsePartialOrderedEvents(false);
        return paramObj;
    }

    private boolean createMarking(UIPluginContext context, PetrinetGraph net, Class<? extends Connection> classType) {
        boolean result = false;
        Collection<Pair<Integer, PluginParameterBinding>> plugins = context.getPluginManager()
                .find(ConnectionObjectFactory.class, classType, context.getClass(), true, false, false, net.getClass());
        PluginContext c2 = context.createChildContext("Creating connection of Type " + classType);
        Pair<Integer, PluginParameterBinding> pair = plugins.iterator().next();
        PluginParameterBinding binding = pair.getSecond();
        try {
            PluginExecutionResult pluginResult = binding.invoke(c2, net);
            pluginResult.synchronize();
            context.getProvidedObjectManager().createProvidedObjects(c2); // push the objects to main context
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c2.getParentContext().deleteChild(c2);
        }
        return result;
    }

    private Marking getInitialMarking(PluginContext context, PetrinetGraph net) {

        if (net instanceof DataPetriNetsWithMarkings && ((DataPetriNetsWithMarkings) net).getInitialMarking() != null) {
            return ((DataPetriNetsWithMarkings) net).getInitialMarking();
        }

        // check connection between petri net and marking
        Marking initMarking = null;
        try {
            initMarking = context.getConnectionManager()
                    .getFirstConnection(InitialMarkingConnection.class, context, net)
                    .getObjectWithRole(InitialMarkingConnection.MARKING);
        } catch (ConnectionCannotBeObtained exc) {
            initMarking = new Marking();
        }
        return initMarking;
    }

    private Marking[] getFinalMarkings(PluginContext context, PetrinetGraph net) {

        if (net instanceof DataPetriNetsWithMarkings && ((DataPetriNetsWithMarkings) net).getFinalMarkings() != null
                && ((DataPetriNetsWithMarkings) net).getFinalMarkings().length > 0) {
            return ((DataPetriNetsWithMarkings) net).getFinalMarkings();
        }

        // check if final marking exists
        Marking[] finalMarkings = null;
        try {
            Collection<FinalMarkingConnection> finalMarkingConnections = context.getConnectionManager()
                    .getConnections(FinalMarkingConnection.class, context, net);
            if (finalMarkingConnections.size() != 0) {
                Set<Marking> setFinalMarkings = new HashSet<>();
                for (FinalMarkingConnection conn : finalMarkingConnections) {
                    setFinalMarkings.add((Marking) conn.getObjectWithRole(FinalMarkingConnection.MARKING));
                }
                finalMarkings = setFinalMarkings.toArray(new Marking[setFinalMarkings.size()]);
            } else {
                finalMarkings = new Marking[0];
            }
        } catch (ConnectionCannotBeObtained exc) {
            // no final marking provided, give an empty marking
            finalMarkings = new Marking[0];
        }
        return finalMarkings;
    }

}
