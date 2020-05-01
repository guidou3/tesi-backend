package org.processmining.Guido.DataAwareConformanceChecking;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.log.utils.XUtils;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.DataConformance.framework.ReplayableActivity;
import org.processmining.plugins.DataConformance.framework.VariableMatchCosts;
import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration;
import org.processmining.plugins.balancedconformance.config.DataConformancePlusConfiguration;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

import java.util.Map;
import java.util.Set;

public class ConstraintsBalancedConfiguration extends BalancedProcessorConfiguration {
    private boolean evaluationMode = false;
    private boolean startComplete = false;

//    public static BalancedProcessorConfiguration newDefaultInstance(PetrinetGraph net, Marking initialMarking,
//                                                                    Marking[] finalMarkings, XLog log, XEventClassifier classifier, int defaultMoveOnModelCost,
//                                                                    int defaultMoveOnLogCost, int defaultMissingWriteOpCost, int defaultIncorrectWriteOpCost,
//                                                                    boolean evaluationMode) {
//        return newDefaultInstance(net, initialMarking, finalMarkings, log, classifier, defaultMoveOnModelCost,
//                defaultMoveOnLogCost, defaultMissingWriteOpCost, defaultIncorrectWriteOpCost, evaluationMode, false);
//    }

    /**
     * Creates a {@link BalancedProcessorConfiguration} with reasonable default
     * settings, i.e. the name of attributes in the event log has to exactly
     * match variable names in the DPN-net, and the name of the event classes
     * has to exactly match the transition names in the DPN-net.
     *
     * @param net
     * @param initialMarking
     * @param finalMarkings
     * @param log
     * @param classifier
     * @param defaultMoveOnModelCost
     * @param defaultMoveOnLogCost
     * @param defaultMissingWriteOpCost
     * @param defaultIncorrectWriteOpCost
     * @return
     */
//    public static BalancedProcessorConfiguration newDefaultInstance(PetrinetGraph net, Marking initialMarking,
//                                                                    Marking[] finalMarkings, XLog log, XEventClassifier classifier, int defaultMoveOnModelCost,
//                                                                    int defaultMoveOnLogCost, int defaultMissingWriteOpCost, int defaultIncorrectWriteOpCost,
//                                                                    boolean evaluationMode, boolean usePartialorder) {
//
//        XEventClasses eventClasses = XUtils.createEventClasses(classifier, log);
//        Set<String> attributeNames = getAttributeNames(log, eventClasses.getClassifier());
//
//        return newDefaultInstance(net, initialMarking, finalMarkings, eventClasses, attributeNames,
//                defaultMoveOnModelCost, defaultMoveOnLogCost, defaultMissingWriteOpCost, defaultIncorrectWriteOpCost, evaluationMode,
//                usePartialorder);
//    }

//    /**
//     * Creates a {@link BalancedProcessorConfiguration} with reasonable default
//     * settings, i.e. the name of attributes in the event log has to exactly
//     * match variable names in the DPN-net, and the name of the event classes
//     * has to exactly match the transition names in the DPN-net.
//     *
//     * @param net
//     * @param initialMarking
//     * @param finalMarkings
//     * @param eventClasses
//     * @param attributeNames
//     * @param defaultMoveOnModelCost
//     * @param defaultMoveOnLogCost
//     * @param defaultMissingWriteOpCost
//     * @param defaultIncorrectWriteOpCost
//     * @return
//     */
//    public static BalancedProcessorConfiguration newDefaultInstance(PetrinetGraph net, Marking initialMarking,
//                                                                    Marking[] finalMarkings, XEventClasses eventClasses, Set<String> attributeNames,
//                                                                    int defaultMoveOnModelCost, int defaultMoveOnLogCost, int defaultMissingWriteOpCost,
//                                                                    int defaultIncorrectWriteOpCost, boolean evaluationMode) {
//        return newDefaultInstance(net, initialMarking, finalMarkings, eventClasses, attributeNames,
//                defaultMoveOnModelCost, defaultMoveOnLogCost, defaultMissingWriteOpCost, defaultIncorrectWriteOpCost, evaluationMode,
//                false);
//    }

//    public static BalancedProcessorConfiguration newDefaultInstance(PetrinetGraph net, Marking initialMarking,
//                                                                    Marking[] finalMarkings, XEventClasses eventClasses, Set<String> attributeNames,
//                                                                    int defaultMoveOnModelCost, int defaultMoveOnLogCost, int defaultMissingWriteOpCost,
//                                                                    int defaultIncorrectWriteOpCost, boolean evaluationMode, boolean usePartialorder) {
//
//        Set<String> variableNames;
//        if (net instanceof DataPetriNet) {
//            variableNames = PetriNetWithData.getAllVariableNames((DataPetriNet) net);
//        } else {
//            variableNames = ImmutableSet.of();
//        }
//
//        TransEvClassMapping activityMapping = createDefaultMappingTransitionsToEventClasses(net,
//                eventClasses.getClassifier(), eventClasses);
//        Map<XEventClass, Integer> mapEvClass2Cost = createDefaultLogMoveCost(eventClasses, defaultMoveOnLogCost);
//        Map<Transition, Integer> mapTrans2Cost = createDefaultModelMoveCost(net, defaultMoveOnModelCost);
//
//        Map<String, String> variableMapping;
//        if (!variableNames.isEmpty()) {
//            variableMapping = createDefaultVariableMapping(variableNames, attributeNames);
//        } else {
//            variableMapping = ImmutableMap.of();
//        }
//
//        VariableMatchCosts variableCost;
//        if (net instanceof DataPetriNet) {
//            variableCost = createDefaultVariableCost((DataPetriNet) net, variableNames, defaultMissingWriteOpCost,
//                    defaultIncorrectWriteOpCost);
//        } else {
//            variableCost = new VariableMatchCosts(0, 0, ImmutableSet.<ReplayableActivity>of(),
//                    ImmutableSet.<String>of());
//        }
//
//        return new ConstraintsBalancedConfiguration(initialMarking, finalMarkings, activityMapping, mapEvClass2Cost,
//                mapTrans2Cost, variableMapping, variableCost, evaluationMode, usePartialorder);
//    }

//    private static Set<String> getAttributeNames(XLog log, XEventClassifier classifier) {
//        Set<String> attributeNames;
//        if (log.getInfo(classifier) != null) {
//            attributeNames = ImmutableSet.copyOf(log.getInfo(classifier).getEventAttributeInfo().getAttributeKeys());
//        } else {
//            attributeNames = XUtils.getEventAttributeKeys(log);
//        }
//        return attributeNames;
//    }

    /**
     * Creates an {@link ConstraintsBalancedConfiguration} with reasonable
     * defaults, you still need to set the mandatory parameters (see other
     * constructor) manually.
     */
    public ConstraintsBalancedConfiguration() {
        super();
    }

    /**
     * Creates a new configuration with the supplied mandatory parameters. All
     * other configuration options are set to reasonable defaults.
     *
     * @param initialMarking
     * @param finalMarkings
     * @param activityMapping
     *            Mapping of Transition to Event Class
     * @param mapEvClass2Cost
     * @param mapTrans2Cost
     *            Configuration of the PNReplayer
     * @param variableMapping
     *            Mapping from variable name in DPN-net to variable name in
     *            event log
     * @param variableCost
     *            Mapping from variable name in DPN-net to costss
     */
    public ConstraintsBalancedConfiguration(Marking initialMarking, Marking[] finalMarkings,
                                          TransEvClassMapping activityMapping, Map<XEventClass, Integer> mapEvClass2Cost,
                                          Map<Transition, Integer> mapTrans2Cost, Map<String, String> variableMapping,
                                          VariableMatchCosts variableCost, boolean evaluationMode, boolean startComplete,
                                            boolean usePartialOrder) {
        super(initialMarking, finalMarkings, activityMapping, mapEvClass2Cost, mapTrans2Cost, variableMapping,
                variableCost, usePartialOrder);
        this.evaluationMode = evaluationMode;
        this.startComplete = startComplete;
    }

    /**
     * Copy constructor that takes its parameters from the supplied
     * configuration.
     *
     * @param config
     *            to copy (shallow-clone) from
     */
    public ConstraintsBalancedConfiguration(DataConformancePlusConfiguration config) {
        super(config);
    }

    /**
     * Copy constructor that takes its parameters from the supplied
     * configuration.
     *
     * @param config
     *            to copy (shallow-clone) from
     */
    public ConstraintsBalancedConfiguration(BalancedProcessorConfiguration config) {
        super(config);
        evaluationMode = false;
    }

    public ConstraintsBalancedConfiguration(ConstraintsBalancedConfiguration config) {
        super(config);
        evaluationMode = config.isEvaluationMode();
        startComplete = config.isStartComplete();
    }

    public boolean isEvaluationMode() {
        return evaluationMode;
    }

    public void setEvaluationMode(boolean evaluationMode) {
        this.evaluationMode = evaluationMode;
    }

    public boolean isStartComplete() {
        return startComplete;
    }

    public void setStartComplete(boolean startComplete) {
        this.startComplete = startComplete;
    }
}
