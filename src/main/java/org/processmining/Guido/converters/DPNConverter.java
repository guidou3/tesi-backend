package org.processmining.Guido.converters;

import com.google.common.collect.*;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.*;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.Guido.CustomElements.*;
import org.processmining.Guido.CustomElements.enums.*;
import org.processmining.Guido.DataAwareConformanceChecking.ConstraintsBalancedConfiguration;
import org.processmining.Guido.DataPetriNetWithCustomElements;
import org.processmining.Guido.mapping.FinalMapping;
import org.processmining.Guido.utils.CyclesFinder;
import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.datapetrinets.DataPetriNetsWithMarkings;
import org.processmining.datapetrinets.dsl.DPNParser;
import org.processmining.datapetrinets.dsl.DPNParserConstants;
import org.processmining.datapetrinets.exception.NonExistingVariableException;
import org.processmining.datapetrinets.expression.GuardExpression;
import org.processmining.datapetrinets.expression.syntax.ParseException;
import org.processmining.log.utils.XUtils;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.processmining.Guido.mapping.FinalMapping.DUMMY;

public class DPNConverter {

    public interface CustomDPNtoDPNResult {
        DataPetriNet getOriginalDpn();
        DataPetriNet getNewDpn();
        XLog getOriginalLog();
        XLog getNewLog();
    }

    private BiMap<Place, Place> placesMap;
    private Map<Transition, Transition> transitionsMap;
    private Map<Transition, Transition> transitionsStartMap;
    private BiMap<DataElement, DataElement> variableMap;
//    private BiMap<Transition, Place> connectMap;
    private Map<Transition, ArrayList<Transition>> seqSyncMap;
    private Multimap<Transition, DataElement> relTimeVarMap;
    private Map<DataElement, TimeData> relTimeUnitMap;
    private Multimap<Transition, DataElement> consequenceSourceVarMap;
    private Multimap<Transition, DataElement> consequenceTargetVarMap;
    private BiMap<Transition, DataElement> timestampVarMap;
    private BiMap<Transition, DataElement> resourceVarMap;
    private BiMap<Transition, DataElement> roleVarMap;
    private BiMap<Transition, DataElement> groupVarMap;

    private Map<Transition, TimeUnit> transitionTimeUnitMap;

    private Map<String, Integer> varNameCounter;

    private Map<Transition, Collection<String>> logPositions;
    private BiMap<String, XEventClass> startEventClasses;

    private boolean evaluationMode;
    private boolean startComplete;

    public DPNConverter(ConstraintsBalancedConfiguration config) {
        placesMap = HashBiMap.create();
        transitionsMap = new HashMap<>();
        transitionsStartMap = new HashMap<>();
        variableMap = HashBiMap.create();
        seqSyncMap = new HashMap<>();
        relTimeVarMap = HashMultimap.create();
        relTimeUnitMap = new HashMap<>();
        consequenceSourceVarMap = HashMultimap.create();
        consequenceTargetVarMap = HashMultimap.create();
        timestampVarMap = HashBiMap.create();
        resourceVarMap = HashBiMap.create();
        roleVarMap = HashBiMap.create();
        groupVarMap = HashBiMap.create();

        varNameCounter = new HashMap<>();

        evaluationMode = config.isEvaluationMode();
        startComplete = config.isStartComplete();
        startEventClasses = HashBiMap.create();
    }


    public DataPetriNetWithCustomElements convertDpn(final DataPetriNetWithCustomElements originalDpn, final CustomElements ce)
            throws NonExistingVariableException {

        logPositions = originalDpn.getLogPositions();

        final DataPetriNetWithCustomElements dpn = cloneDataPetriNet(originalDpn);

        if(ce != null) {
            // convert time distances

            transitionTimeUnitMap = createTemporalUnitMap(ce);

            for (TimeDistance td: ce.getTimeDistances()) {
                addTimeDistance(dpn, td);
            }

            // convert sequence constraints
            for (Consequence consequence: ce.getConsequences()) {
                addConsequence(dpn, consequence);
            }

            // convert sequence constraints
            for (ConsequenceTimed consequence: ce.getConsequencesTimed()) {
                addConsequenceTimed(dpn, consequence);
            }

            // convert task duration
            for (TaskDuration tdn: ce.getTaskDurations()) {
                addTaskDuration(dpn, tdn);
            }

            // convert time instance
            for (TimeInstance ti: ce.getTimeInstances()) {
                addTimeInstance(dpn, ti);
            }

            // convert resource constraints
            for (Resource resource: ce.getResources()) {
                addRRG(dpn, resource, resourceVarMap, "Resource");
            }

            // convert role constraints
            for (Role role: ce.getRoles()) {
                addRRG(dpn, role, roleVarMap, "Role");
            }

            // convert group constraints
            for (Group group: ce.getGroups()) {
                addRRG(dpn, group, groupVarMap, "Group");
            }
        }

//        XLog newLog = logConversion(orgLog, map);
//
//        return new CustomDPNtoDPNResult() {
//            private DataPetriNet originalModel = originalDpn;
//            private DataPetriNet modifiedModel = dpn;
//
//            private XLog originalLog = orgLog;
//            private XLog modifiedLog = newLog;
//
//            public DataPetriNet getOriginalDpn() { return originalModel; }
//            public DataPetriNet getNewDpn() { return modifiedModel; }
//            public XLog getOriginalLog() { return originalLog; }
//            public XLog getNewLog() { return modifiedLog; }
//        };
        return dpn;
    }


    // determine the smaller timeUnit to link to a transition
    // this timeUnit has to consider all the transitions linked to the current through a guard
    private Map<Transition, TimeUnit> createTemporalUnitMap(CustomElements ce) {
        Map<Integer, TimeUnit> map = new HashMap<>();
        Map<Transition, Integer> mapToGroup = new HashMap<>();
        int count = -1;
        for (TimeDistance td: ce.getTimeDistances()) {
            Transition t1 = transitionsMap.get((Transition) td.getSourceRef()),
                    t2 = transitionsMap.get((Transition) td.getTargetRef());

            if(mapToGroup.get(t1) == null && mapToGroup.get(t2) == null) {
                count++;
                mapToGroup.put(t1, count);
                mapToGroup.put(t2, count);
                map.put(count, td.getTimeUnit());
            }
            else if(mapToGroup.get(t1) != null && mapToGroup.get(t2) != null) {
                int group1 = mapToGroup.get(t1);
                int group2 = mapToGroup.get(t2);

                for(Map.Entry<Transition, Integer> entry : mapToGroup.entrySet()) {
                    if(entry.getValue() == group2)
                        mapToGroup.put(entry.getKey(), group1);
                }
            }
            else if(mapToGroup.get(t1) == null)
                mapToGroup.put(t1, mapToGroup.get(t2));
            else
                mapToGroup.put(t2, mapToGroup.get(t1));

            if(map.get(mapToGroup.get(t1)).moreThan(td.getTimeUnit()))
                map.put(mapToGroup.get(t1), td.getTimeUnit());
        }

        for (ConsequenceTimed ct: ce.getConsequencesTimed()) {
            Transition t1 = transitionsMap.get((Transition) ct.getSourceRef()),
                    t2 = transitionsMap.get((Transition) ct.getTargetRef());

            if(mapToGroup.get(t1) == null && mapToGroup.get(t2) == null) {
                count++;
                mapToGroup.put(t1, count);
                mapToGroup.put(t2, count);
                map.put(count, ct.getTimeUnit());
            }
            else if(mapToGroup.get(t1) != null && mapToGroup.get(t2) != null) {
                int group1 = mapToGroup.get(t1);
                int group2 = mapToGroup.get(t2);

                for(Map.Entry<Transition, Integer> entry : mapToGroup.entrySet()) {
                    if(entry.getValue() == group2)
                        mapToGroup.put(entry.getKey(), group1);
                }
            }
            else if(mapToGroup.get(t1) == null)
                mapToGroup.put(t1, mapToGroup.get(t2));
            else
                mapToGroup.put(t2, mapToGroup.get(t1));

            if(map.get(mapToGroup.get(t1)).moreThan(ct.getTimeUnit()))
                map.put(mapToGroup.get(t1), ct.getTimeUnit());

        }

        for (TaskDuration tdn: ce.getTaskDurations()) {
            Transition t = transitionsMap.get(tdn.getTransition());

            if(mapToGroup.get(t) == null) {
                count++;
                mapToGroup.put(t, count);
                map.put(count, tdn.getTimeUnit());
            }
            else if(map.get(mapToGroup.get(t)).moreThan(tdn.getTimeUnit()))
                map.put(mapToGroup.get(t), tdn.getTimeUnit());
        }

        Map<Transition, TimeUnit> result = new HashMap<>();
        for(Map.Entry<Transition, Integer> entry : mapToGroup.entrySet()) {
            result.put(entry.getKey(), map.get(entry.getValue()));
        }

        return result;
    }

    DataPetriNetWithCustomElements cloneDataPetriNet(final DataPetriNetWithCustomElements originalDpn)
            throws NonExistingVariableException {
        final DataPetriNetWithCustomElements dpn = new DataPetriNetWithCustomElements(originalDpn.getLabel());

        // add places
        for (Place place: originalDpn.getPlaces()) {
            Place dpnPlace = dpn.addPlace(place.getLabel());
            placesMap.put(place, dpnPlace);
        }

        // add transitions
        for(Transition transition : originalDpn.getTransitions()) {
            String label = (transition.isInvisible() ? "invisible:" : "") + transition.getLabel();
            Transition newTransition = dpn.addTransition(label);
            newTransition.setInvisible(transition.isInvisible());

            transitionsMap.put(transition, newTransition);
        }

        // add arcs
        for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : originalDpn.getEdges()) {
            if ((edge.getSource() instanceof Transition) && (edge.getTarget() instanceof Place)) {
                dpn.addArc(transitionsMap.get(edge.getSource()), placesMap.get(edge.getTarget()));
            }
            if ((edge.getSource() instanceof Place) && (edge.getTarget() instanceof Transition)) {
                dpn.addArc(placesMap.get(edge.getSource()), transitionsMap.get(edge.getTarget()));
            }
        }

        // add variables
        // TODO: look better
        for (DataElement variable: originalDpn.getVariables()) {
            DataElement newVar = dpn.addVariable(variable.getVarName(), variable.getType(), variable.getMinValue(), variable.getMaxValue());
            variableMap.put(variable, newVar);
            varNameCounter.put(variable.getVarName(), 1);

            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc: originalDpn.getInEdges(variable))
                dpn.assignWriteOperation(transitionsMap.get((Transition) arc.getSource()), newVar);


            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc: originalDpn.getOutEdges(variable))
                dpn.assignReadOperation(transitionsMap.get((Transition) arc.getTarget()), newVar);
        }

        // add guards to cloned DataPetriNet
        for(Transition transition : originalDpn.getTransitions()) {
            // copy existing guard
            if (((PNWDTransition) transition).hasGuardExpression())
                dpn.setGuard(transitionsMap.get(transition), ((PNWDTransition) transition).getGuardExpression());
        }

        // convert initial and final marking
        if(dpn.getInitialMarking() != null) {
            Marking initialMarking = new Marking();
            for (Place initPlace: dpn.getInitialMarking().toList()) {
                initialMarking.add(placesMap.get(initPlace));
            }
            dpn.setInitialMarking(initialMarking);
        }

        if(dpn.getFinalMarkings() != null) {
            Marking[] finalMarking = new Marking[dpn.getFinalMarkings().length];
            for (Marking marking: dpn.getFinalMarkings()) {
                Marking dpnMarking = new Marking();
                for (Place finalPlace: marking.toList()) {
                    dpnMarking.add(placesMap.get(finalPlace));
                }
                finalMarking[Arrays.asList(dpn.getFinalMarkings()).indexOf(marking)] = dpnMarking;
            }
            dpn.setFinalMarkings(finalMarking);
        }
        dpn.setLastTransition(transitionsMap.get(originalDpn.getLastTransition()));
//		dpn.setInitialMarking(placesMap.get(dpn.getInitialMarkings()));
//		dpn.setFinalMarkings(dpn.getFinalMarkings());
        return dpn;
    }

    public void updateIdMapping(ActivityTransitionMapping mapping) {
        Map<String, String> idToId = new HashMap<>();
        for(Map.Entry<Transition, Transition> entry : transitionsMap.entrySet()) {
            idToId.put(entry.getValue().getId().toString(), entry.getKey().getId().toString());
        }

        mapping.fourthStep(idToId);
    }

    private boolean keepEventByLifecycle(Collection<Transition> transitions) {
        boolean necessary = false;
        for(Transition t : transitions)
            if(transitionsStartMap.get(t) != null) {
                necessary = true;
                break;
            }
        return necessary;
    }

    public XLog convertLog(final XLog orgLog, TransEvClassMapping map) {
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        final XLog modLog = factory.createLog();

        // LOG CONVERSION
        BiMap<String, XEventClass> eventClassMap = HashBiMap.create();
        for (XEventClass ec: map.values())
            eventClassMap.put(ec.getId(), ec);

        XEventClassifier eventClassifier = map.getEventClassifier();
        Multimap<XEventClass, Transition> invertedMap = Multimaps.invertFrom(Multimaps.forMap(map), HashMultimap.<XEventClass, Transition>create());

        for (XTrace oldTrace : orgLog) {
            XTrace newTrace = factory.createTrace(oldTrace.getAttributes());
            Date firstEventDate = null;


//            HashMap<String, XEvent> lastSeenEventMap = new HashMap<>();

            for (XEvent event : oldTrace) {
                String eventClassId = eventClassifier.getClassIdentity(event);

                if (firstEventDate == null)  {
                    firstEventDate = XUtils.getTimestamp(event);
                    firstEventDate.setTime(firstEventDate.getTime() - 1000*60*60*24);
                }
//                XEvent clone = (XEvent) event.clone();
//                addAttributesToEvent(eventClassMap, invertedMap, firstEventDate, clone, eventClassifier.getClassIdentity(clone));
//                newTrace.add(clone);
//
//                lastSeenEventMap.putIfAbsent(eventClassId, event);
//                XEvent lastSeen = lastSeenEventMap.get(eventClassId);
//                if (lastSeen.equals(event)) {
//                    // se c'è solo complete crea una copia dell'evento e gli da tipo start
//                    if (getLifecycle(event).equals("complete")) {
//                        XEvent clone = (XEvent) event.clone();
//                        invertLifecycle(clone);
//                        addAttributesToEvent(eventClassMap, invertedMap, firstEventDate, clone, eventClassifier.getClassIdentity(clone));
//                        newTrace.add(clone);
//                    }
//                } else {
//                    if (getLifecycle(event).equals(getLifecycle(lastSeen))) {
//                        XEvent clone;
//                        switch (getLifecycle(event)) { // crea manualmente gli start/complete mancanti quando essi mancano... ... ...
//                            case "start":
//                                clone = (XEvent) lastSeenEventMap.get(eventClassId).clone();
//                                invertLifecycle(clone);
//                                addAttributesToEvent(eventClassMap, invertedMap, firstEventDate, clone, eventClassifier.getClassIdentity(clone));
//                                newTrace.add(newTrace.indexOf(lastSeenEventMap.get(eventClassId)) + 1, clone);
//                                break;
//                            case "complete":
//                                clone = (XEvent) event.clone();
//                                invertLifecycle(clone);
//                                addAttributesToEvent(eventClassMap, invertedMap, firstEventDate, clone, eventClassifier.getClassIdentity(clone));
//                                newTrace.add(clone);
//                                break;
//                            default:
//                                throw new IllegalStateException("Unsupported lifecycle transition: " + getLifecycle(event));
//                        }
//                    }
//                }

                if(getLifecycle(event).equals("start")) {
                    // se lo stato corrente è start e viene utilizzato modifico il suo "nome" per rendere chiaro che
                    // si tratta dell'evento start
                    if(keepEventByLifecycle(invertedMap.get(eventClassMap.get(eventClassId)))) {
                        XAttributeMap attributes = event.getAttributes();
                        String classifier = map.getEventClassifier().equals(XLogInfoImpl.NAME_CLASSIFIER) ? "concept:name" :
                                "org:resource";

                        XAttribute oldAttribute = attributes.get(classifier);
                        XAttribute newAttribute = factory.createAttributeLiteral(classifier, oldAttribute.toString() + "_start",
                                oldAttribute.getExtension());

                        attributes.put(classifier, newAttribute);
                        event.setAttributes(attributes);

                    }
                    else // se lo stato corrente è start e non viene utilizzato non viene inserito nel nuovo log
                        continue;
                }

                addAttributesToEvent(invertedMap.get(eventClassMap.get(eventClassId)), firstEventDate, event);
                newTrace.add(event);
            }

            // check for "open" events (events that have not been completed)
//            for (XEvent event : lastSeenEventMap.values()) {
//                if (getLifecycle(event).equals("start")) {
//                    XEvent clone = (XEvent) lastSeenEventMap.get(eventClassifier.getClassIdentity(event)).clone();
//                    invertLifecycle(clone);
//                    newTrace.add(newTrace.indexOf(lastSeenEventMap.get(eventClassifier.getClassIdentity(event))) + 1, clone);
//                }
//            }

            modLog.add(newTrace);
        }

        Collection<XEventClass> classes = XUtils.createEventClasses(eventClassifier, orgLog).getClasses();
        for (XEventClass ec: classes)
            if(ec.getId().endsWith("_start")) startEventClasses.put(ec.getId(), ec);
        return modLog;
    }

    public void updateFinalMapping1(FinalMapping finalMapping) {
        TransEvClassMapping oldMapping = finalMapping.getMap();
        TransEvClassMapping newMapping = new TransEvClassMapping(finalMapping.getClassifier(), DUMMY);

        for(Map.Entry<Transition, XEventClass> entry : oldMapping.entrySet())
            newMapping.put(transitionsMap.get(entry.getKey()), entry.getValue());

        finalMapping.setMapping(newMapping);
    }

    public void updateFinalMapping2(FinalMapping finalMapping) {
        TransEvClassMapping mapping = finalMapping.getMap();

        Map<Transition, Transition> inverted = transitionsMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        for(Map.Entry<Transition, XEventClass> entry : mapping.entrySet()) {
            Transition t = transitionsStartMap.get(inverted.get(entry.getKey()));
            if(t != null) {
                XEventClass eventClass = startEventClasses.get(t.getLabel()+ "_start");
                if(eventClass != null)
                    mapping.put(t, eventClass);
                else
                    throw new NullPointerException();
            }
        }

        finalMapping.setMapping(mapping);
    }

    void addTimeDistance(DataPetriNet dpn, TimeDistance timeDistance) {
        try {
            Transition t1 = getTransition(dpn, timeDistance.getSourceRef(), timeDistance.getSide()),
                    t2 = getTransition(dpn, timeDistance.getTargetRef(), timeDistance.getSide());

            Class<?> varType = convertType(DPNParserConstants.CONTINUOUS);

            DataElement var1 = getTimeVar(t1, dpn, varType, false);
            DataElement var2 = getTimeVar(t2, dpn, varType, false);

            double time = timeDistance.getTimeData().unitTo(transitionTimeUnitMap.get(t1));

            String expression = "((%s%s%s(%s+%s))||(%s==null))";

            String guard1 = String.format(expression, var1, evaluationMode ? "": "'", timeDistance.getIneq(), var2, time, var2);
            String guard2 = String.format(expression, var2, evaluationMode ? "": "'", timeDistance.getIneq(), var1, time, var1);

            if(evaluationMode) {
                String expression2 = "((%s==null)||((%s!=null)&&(%s%s(%s+%s))))";

                String guard3 = String.format(expression2, var1, var2, var1, timeDistance.getOppositeIneq(), var2, time);
                String guard4 = String.format(expression2, var2, var1, var2, timeDistance.getOppositeIneq(), var1, time);

                String label = "custom:TimeDistance<|>" + timeDistance.getId() + "<|>";

                addInvisibleControls(t1, dpn, label + t1.getLabel(), guard1, guard3, true);
                addInvisibleControls(t2, dpn, label + t2.getLabel(), guard2, guard4, true);
            }
            else {
                addGuardExpression(dpn, t1, guard1);
                addGuardExpression(dpn, t2, guard2);
            }
        }
        catch (MissingTransitionSideException ex) {
            System.out.println(ex.getMessage());
        }
    }

    void addConsequence(DataPetriNetWithCustomElements dpn, Consequence sequence) {
        try {
            Transition source = getTransition(dpn, sequence.getSourceRef(), sequence.getSourceSide()),
                    target = getTransition(dpn, sequence.getTargetRef(), sequence.getTargetSide()),
                    last = dpn.getLastTransition();

            String label = "custom:Consequence<|>" + sequence.getId();

            // TODO
            //  al posto di fare il controllo su last, trovare nel modello il punto di convergenza più vicino tra source
            //  e target e porre lì il controllo


            // TODO: check that this is the only consequence between source and target

    //        String seqLabel = source.getLabel() + "_" + target.getLabel();
    //
    //        ArrayList<Transition> list = seqSyncMap.get(target);
    //
    //        if (list != null) {
    //            Place p1 = dpn.addPlace(Prefixes.conseqPlace() + seqLabel + "_" + (list.size()+1));
    //            Transition t1 = dpn.addTransition(Prefixes.conseqTransition() + seqLabel + "_" + (list.size()+1));
    //            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc: dpn.getInEdges(list.get(list.size()-1))) {
    //                dpn.addArc((Place) arc.getSource(), t1);
    //            }
    //            list.add(t1);
    ////            seqSyncMap.put(target, list); // serve o side-effect?
    //            dpn.addArc(source, p1);
    //            dpn.addArc(p1, t1);
    //        } else {
    //            Place p1 = dpn.addPlace(Prefixes.conseqPlace() + seqLabel + "_1");
    //            Place p2 = dpn.addPlace(Prefixes.conseqPlace() + seqLabel + "_2");
    //
    //            Transition t1 = dpn.addTransition(Prefixes.conseqTransition() + seqLabel + "_1");
    //            Transition t2 = dpn.addTransition(Prefixes.conseqTransition() + seqLabel + "_2");
    //
    //            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc: dpn.getInEdges(target)) {
    //                dpn.addArc((Place) arc.getSource(), t1);
    //                dpn.addArc((Place) arc.getSource(), t2);
    //                dpn.removeEdge(arc);
    //            }
    //
    //            t1.setInvisible(true);
    //            t2.setInvisible(true);
    //
    //            seqSyncMap.put(target, new ArrayList<>(Arrays.asList(t1, t2)));
    //
    //            dpn.addArc(p1, t2);
    //            dpn.addArc(t1, p2);
    //            dpn.addArc(t2, p2);
    //            dpn.addArc(source, p1);
    //            dpn.addArc(p2, target);
    //        }

            //TODO: implement time constraint
            String varName = "custom:ConsequenceOpen" + "_" + sequence.getId();
            DataElement var = dpn.addVariable(varName, convertType(DPNParserConstants.BOOLEAN), null, null);
            dpn.assignWriteOperation(source, var);
            dpn.assignWriteOperation(target, var);
            // eventuale operazione di scrittura all'interno dei cicli
            consequenceSourceVarMap.put(source, var);
            consequenceTargetVarMap.put(target, var);

            String guard = String.format("((%s==false)||(%s==null))", var, var);

            if(evaluationMode) {
                String guard1 = String.format("(%s==true)", var);

                // in questo caso i controlli vengono effettuati prima della transizione
                //                addInvisibleControls(source, dpn, label, guard, guard1, false);
                addInvisibleControls(last, dpn, label, guard, guard1, false);
            }
            else {
                //                addGuardExpression(dpn, source, guard);
                addInvisibleControl(last, dpn, label, guard, false);
            }
        }
        catch (MissingTransitionSideException ex) {
            System.out.println(ex.getMessage());
        }
    }

    void addConsequenceTimed(DataPetriNetWithCustomElements dpn, ConsequenceTimed consequenceTimed) {
        try {
            Transition source = getTransition(dpn, consequenceTimed.getSourceRef(), consequenceTimed.getSourceSide()),
                    target = getTransition(dpn, consequenceTimed.getTargetRef(), consequenceTimed.getTargetSide()),
                    last = dpn.getLastTransition();

            String label = "custom:ConsequenceTimed<|>" + consequenceTimed.getId();

            Class<?> varType = convertType(DPNParserConstants.CONTINUOUS);
            DataElement sourceVar = getTimeVar(source, dpn, varType, false);
            DataElement targetVar = getTimeVar(target, dpn, varType, false);

            // aggiungi timeData ad una Map<Transition, List<TimeData>>
            // una volta inseriti tutti determino la timeUnit minore e creo le guardie messe temporaneamente in sospeso
            // nel frattempo metto le guardie da creare in una lista corrispondente al tipo di guardia corrispettivo

            float time = consequenceTimed.getTimeData().unitTo(transitionTimeUnitMap.get(source));

            String guard = String.format("((%s==null)||(%s%s%s(%s+%s)))", sourceVar, targetVar, evaluationMode ? "": "'",
                    consequenceTimed.getIneq(), sourceVar, time);

            if(evaluationMode) {
                String expression1 = "((%s==null)||((%s!=null)&&(%s%s(%s+%s))))";
                String guard1 = String.format(expression1, targetVar, sourceVar, targetVar, consequenceTimed.getOppositeIneq(), sourceVar, time);

                addInvisibleControls(target, dpn, label + "<|>TimeCheck", guard, guard1, true);
            }
            else
                addGuardExpression(dpn, target, guard);

            // if the consequenceTimed is forced add the control at the end of the model
            // TODO: to obtain better performances find the closest sure meeting point of the path of both source and
            //  target. This should cut down the time to find the optimal path.
            if(consequenceTimed.isForced()) {
                String guard2 = String.format("((%s==null)||(%s!=null))", sourceVar, targetVar);

                if(evaluationMode) {
                    String guard3 = String.format("(((%s!=null)&&(%s==null)))", sourceVar, targetVar);
                    addInvisibleControls(last, dpn, label+ "<|>ConsequenceCheck", guard2, guard3, false);
                }
                else
                    addInvisibleControl(last, dpn, label, guard2, false);
            }
        }
        catch (MissingTransitionSideException ex) {
            System.out.println(ex.getMessage());
        }
    }

    void addTaskDuration(DataPetriNet dpn, TaskDuration taskDuration) {
        try {
            Transition t1 = getTransition(dpn, taskDuration.getTransition(), Side.START);
            Transition t2 = getTransition(dpn, taskDuration.getTransition(), Side.END);

            // if there is no "start" event, it is useless to use this constraint
            if(t1.equals(t2))
                return;

            DataElement var1 = getTimeVar(t1, dpn, convertType(DPNParser.CONTINUOUS), false);
            DataElement var2 = getTimeVar(t2, dpn, convertType(DPNParser.CONTINUOUS), false);

            double time = taskDuration.getTimeData().unitTo(transitionTimeUnitMap.get(t2));

            if(evaluationMode) {
                String guard = "(" + var2 + taskDuration.getIneq() + var1 + "+" + time + ")";
                String guard1 = "((("+var1+"==null)||("+var2 +"==null))||(" + var2 + taskDuration.getOppositeIneq() + var1 + "+" + time + "))";

                String label = "custom:TaskDuration<|>" + taskDuration.getId();

                addInvisibleControls(t2, dpn, label, guard, guard1, true);
            }
            else {
                String guard = "(" + var2 + "'" + taskDuration.getIneq() + var1 + "+" + time + ")";
                addGuardExpression(dpn, t2, guard);
            }
        }
        catch (MissingTransitionSideException ex) {
            System.out.println(ex.getMessage());
        }
    }

    void addTimeInstance(DataPetriNet dpn, TimeInstance timeInstance) {
        try {
            Transition transition = getTransition(dpn, timeInstance.getTransition(), timeInstance.getTransitionSide());

            transitionTimeUnitMap.putIfAbsent(transition, TimeUnit.MINUTES);

            addNonExistingVarToMap(transition, timestampVarMap, dpn, convertType(DPNParserConstants.CONTINUOUS), "TimeInstance");

            double time = (double) timeInstance.getTime() / TimeUnit.DAYS.getFactor();

            DataElement timeVar = timestampVarMap.get(transition);

            String expression = "(%s%s%s%s)";

            String which = evaluationMode ? "" : "'";
            String guard = String.format(expression, timeVar, which, timeInstance.getPosition(), time);

            String label = "custom:TimeInstance<|>" + timeInstance.getId();

            if(evaluationMode) {
                String expression1 = "((%s==null)||(%s%s%s))";
                String guard1 = String.format(expression1, timeVar, timeVar, timeInstance.getOpposite(), time);
                addInvisibleControls(transition, dpn, label, guard, guard1, true);
            }
            else {
                addGuardExpression(dpn, transition, guard);
            }
        }
        catch (MissingTransitionSideException ex) {
            System.out.println(ex.getMessage());
        }
    }

    void addRRG(DataPetriNet dpn, AbstractRRG rrg, BiMap<Transition, DataElement> map, String prefix) {
        for (Transition transition: rrg.getTransitions()) {
            Transition t1 = transitionsMap.get(transition);
            addNonExistingVarToMap(t1, map, dpn, convertType(DPNParserConstants.LITERAL), prefix);
        }

        for (Transition transition: rrg.getTransitions()) {
            Transition t1 = transitionsMap.get(transition);

            createGuards(dpn, rrg, map, t1);
        }
    }

    private void createGuards(DataPetriNet dpn, AbstractRRG constraint, BiMap<Transition, DataElement> map,
                              Transition t1) {
        ArrayList<Transition> transitions = constraint.getTransitions();
        String guard1, guard2;
        String prime = evaluationMode ? "" : "'";
        if(transitions.size() == 0) return;
        else if(transitions.size() == 1) {
            if(constraint.getResType() == ResType.INSTANCE) {
                guard1 = map.get(t1) + prime + "==" + constraint.getName();
                guard2 = "(" + map.get(t1) + "==null)||("+map.get(t1) + "!=" + constraint.getName()+")";
            }
            else return;
        }
        else {
            StringBuilder guard1b = new StringBuilder();
            StringBuilder guard2b = new StringBuilder();
            guard1b.append("(");
            guard2b.append("((").append(map.get(t1)).append("==null)||");

            String expression = "((%s==null)||(%s%s%s%s))";
            String expression1 = "((%s!=null)&&(%s%s%s))";

            boolean first = true;
            switch (constraint.getResType()) {
                case OCCURRENCE :
                    for(Transition transition2 : transitions) {
                        Transition t2 = transitionsMap.get(transition2);
                        if(t1.equals(t2)) continue;
                        else if(first)
                            first = false;
                        else {
                            guard1b.append("&&");
                            guard2b.append("||");
                        }

                        guard1b.append(String.format(expression, map.get(t2), map.get(t1), prime, "==", map.get(t2)));
                        guard2b.append(String.format(expression1, map.get(t2), map.get(t1), "!=", map.get(t2)));
                    }
                    guard1b.append(")");
                    guard2b.append(")");
                    break;
                case ABSENCE :
                    for(Transition transition2 : transitions) {
                        Transition t2 = transitionsMap.get(transition2);
                        if(t1.equals(t2)) continue;
                        else if(first)
                            first = false;
                        else {
                            guard1b.append("&&");
                            guard2b.append("||");
                        }

                        guard1b.append(String.format(expression, map.get(t2), map.get(t1), prime, "!=", map.get(t2)));
                        guard2b.append(String.format(expression1, map.get(t2), map.get(t1), "==", map.get(t2)));
                    }
                    guard1b.append(")");
                    guard2b.append(")");
                    break;
                case INSTANCE :
                    guard1b = new StringBuilder(map.get(t1) + prime + "==" + constraint.getName());
                    guard2b = new StringBuilder(map.get(t1) + "!=" + constraint.getName());
                    break;
                default :
                    break;
            }
            guard1 = guard1b.toString();
            guard2 = guard2b.toString();
        }
        String label = "custom:";
        if(constraint instanceof Resource)
            label += "Resource";
        else if(constraint instanceof Role)
            label += "Role";
        else
            label += "Group";

        label += "<|>" + constraint.getId() + "<|>" + t1.getLabel();

        if(evaluationMode)
            addInvisibleControls(t1, dpn, label, guard1, guard2, true);
        else
            addGuardExpression(dpn, t1, guard1);
    }

    // MODEL METHODS

    private void addInvisibleControl(Transition t, DataPetriNet dpn, String costraint, String guard, boolean after) {
        Place p = dpn.addPlace("Time_Distance_Place_" + t.getLabel());
        Transition t1 = dpn.addTransition(costraint + " " + t.getLabel());

        t1.setInvisible(true);

        if(after) {
            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc: dpn.getOutEdges(t)) {
                if(arc.getTarget() instanceof Place) {
                    dpn.addArc(t1, (Place) arc.getTarget());
                    dpn.removeEdge(arc);
                }
            }

            dpn.addArc(t, p);
            dpn.addArc(p, t1);
        }
        else {
            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc: dpn.getInEdges(t)) {
                if(arc.getSource() instanceof Place) {
                    dpn.addArc((Place) arc.getSource(), t1);
                    dpn.removeEdge(arc);
                }
            }

            dpn.addArc(t1, p);
            dpn.addArc(p, t);
        }

        addGuardExpression(dpn, t1, guard);
    }

    private void addInvisibleControls(Transition t, DataPetriNet dpn, String label, String guard1, String guard2, boolean after) {
        Place p = dpn.addPlace(label + " Place " + t.getLabel());
        Transition tc = dpn.addTransition(label + "<|>Correct" );
        Transition tw = dpn.addTransition(label + "<|>Wrong");

        tc.setInvisible(true);
        tw.setInvisible(true);

        if(after) {
            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc: dpn.getOutEdges(t)) {
                if(arc.getTarget() instanceof Place) {
                    dpn.addArc(tc, (Place) arc.getTarget());
                    dpn.addArc(tw, (Place) arc.getTarget());
                    dpn.removeEdge(arc);
                }
            }

            dpn.addArc(t, p);
            dpn.addArc(p, tc);
            dpn.addArc(p, tw);
        }
        else {
            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc: dpn.getInEdges(t)) {
                if(arc.getSource() instanceof Place) {
                    dpn.addArc((Place) arc.getSource(), tc);
                    dpn.addArc((Place) arc.getSource(), tw);
                    dpn.removeEdge(arc);
                }
            }

            dpn.addArc(p, t);
            dpn.addArc(tc, p);
            dpn.addArc(tw, p);
        }


        addGuardExpression(dpn, tc, guard1);
        addGuardExpression(dpn, tw, guard2);
    }

    private Transition getTransition(DataPetriNet dpn, Transition t, Side side) throws MissingTransitionSideException {
        Transition t1;
        if(side.equals(Side.START)) {
            if(logPositions.get(t).contains("start"))
                t1 = getStartTransition(dpn, t);
            else if(startComplete)
                t1 = getCompleteTransition(t);
            else
                throw new MissingTransitionSideException(t, side);
        }
        else {
            t1 = getCompleteTransition(t);
        }
        return t1;
    }

    private Transition getStartTransition(DataPetriNet dpn, Transition t1) {
        return Optional.ofNullable(transitionsStartMap.get(t1)).orElse(createNewTransitionPosition(dpn, t1));
    }

    private Transition getCompleteTransition(Transition t) {
        return transitionsMap.get(t);
    }

    private Transition createNewTransitionPosition(DataPetriNet dpn, Transition t) {
        Transition t1 = transitionsMap.get(t);
        Transition t2 = dpn.addTransition(t1.getLabel() + "_start");
        Place newPlace = dpn.addPlace(t1.getLabel() + "_startToEnd");

        // TODO: gestione delle variabili in transizioni "sdoppiate"
        for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : dpn.getInEdges(t1)) {
            if(edge.getSource() instanceof Place) {
                dpn.addArc((Place) edge.getSource(), t2);
                dpn.removeEdge(edge);
            }
        }

        dpn.addArc(t2, newPlace);
        dpn.addArc(newPlace, t1);

        transitionsStartMap.put(t, t2);

        return t2;
    }

    private void addGuardExpression(DataPetriNet dpn, Transition transition, String guard) {
        if (guard == null) return;
        try {
            GuardExpression originalGuard = ((PNWDTransition) transition).getGuardExpression();
            GuardExpression newGuard = GuardExpression.Factory.newInstance(guard);

            if (originalGuard == null)
                dpn.setGuard(transition, newGuard);
            else
                dpn.setGuard(transition, GuardExpression.Operation.and(originalGuard, newGuard));
        }
        catch (ParseException | NonExistingVariableException e) {
            e.printStackTrace();
        }
    }

    private String normalize(String s) {
        String[] list = s.split(" +");
        StringBuilder newString = new StringBuilder();
        for(String word : list) {
            newString.append(word.substring(0, 1).toUpperCase());
            newString.append(word.substring(1));
        }
        return newString.toString();
    }

    private DataElement getTimeVar(Transition t, DataPetriNet dpn, Class<?> varType, boolean forceNew) {
        String varName = "custom:" + normalize(t.getLabel()) + "_TimeVar";
        if(forceNew) {
            Integer index = varNameCounter.get(varName);
            if(index == null)
                index = 1;

            varNameCounter.put(varName, index+1);
            varName += "_" + index;
        }

        for (DataElement element: relTimeVarMap.get(t))
            if (element.getVarName().equals(varName))
                return element;

        DataElement var = dpn.addVariable(varName, varType, null, null);
        dpn.assignWriteOperation(t, var);
        relTimeVarMap.put(t, var);
        return var;
//        if (!relTimeVarMap.containsEntry(t, varName)) {
//            DataElement var = dpn.addVariable(varName, varType, null, null);
//            dpn.assignWriteOperation(t, var);
//            relTimeVarMap.put(t, var);
//            return var;
//        }
//        else {
//            for (DataElement element: relTimeVarMap.get(t))
//                if (element.getVarName().equals(varName))
//                    return element;
//
//            return null;
//        }

    }

    private void addNonExistingVarToMap(Transition t, BiMap<Transition, DataElement> map,
                                        DataPetriNet dpn, Class<?> varType, String prefix) {
        String varName = "custom:" + prefix + "_" + t.getLabel().replaceAll(" ", "_");
        if (!map.containsKey(t)) {
            DataElement var = dpn.addVariable(varName, varType, null, null);
            dpn.assignWriteOperation(t, var);
            map.put(t, var);
        }
    }

//    private void addRRGVarToMap(Transition t, BiMap<Transition, DataElement> map,
//                                DataPetriNet dpn, String prefix) {
//        String varName = "custom-" + prefix + "_" + t.getLabel().replaceAll(" ", "_");
//        if (!map.containsKey(t)) {
//            DataElement var = dpn.addVariable(varName, convertType(DPNParserConstants.LITERAL), null, null);
//            dpn.assignWriteOperation(t, var);
//            map.put(t, var);
//        }
//    }

    private static Class<?> convertType(int type) {
        switch (type) {
            case DPNParserConstants.LITERAL :
                return String.class;

            case DPNParserConstants.DISCRETE :
                return Long.class;

            case DPNParserConstants.BOOLEAN :
                return Boolean.class;

            case DPNParserConstants.CONTINUOUS :
                return Double.class;

            case DPNParserConstants.DATE :
                return Date.class;

            default :
                throw new IllegalStateException();
        }
    }

    // LOG METHODS

    private static String checkForQuotes(String name) {
        String value = name;
        if (!name.startsWith("\"") || !name.endsWith("\"")) {
            value = '"' + name + '"';
        }
        return value;
    }

    private void addAttributesToEvent(Collection<Transition> transitions,  Date first, XEvent event) {
        Date eventDate = XUtils.getTimestamp(event);

        for (Transition t: transitions) {
            // relTime
            for (DataElement timeVar: relTimeVarMap.get(t)) {
                double diff = (double) (eventDate.getTime() - first.getTime()) / transitionTimeUnitMap.get(t).getFactor();
//                double relDiff = getDifference(first, thisEventDate, transitionTimeUnitMap.get(transition));

                setContinuousAttr(event, timeVar.getVarName(), diff);
            }

            // consequences sources
            for (DataElement boolVar: consequenceSourceVarMap.get(t))
                setBooleanAttr(event, boolVar.getVarName(), true);

            // consequences targets
            for (DataElement boolVar: consequenceTargetVarMap.get(t))
                setBooleanAttr(event, boolVar.getVarName(), false);

            // time instances
            if (timestampVarMap.containsKey(t)) {
                double diff = (double) (eventDate.getTime()) / TimeUnit.DAYS.getFactor();
                setContinuousAttr(event, timestampVarMap.get(t).getVarName(), diff);
//                setDateAttr(event, timestampVarMap.get(t).getVarName(), XUtils.getTimestamp(event));
            }

            // resource
            String val = XOrganizationalExtension.instance().extractResource(event);
            if (resourceVarMap.containsKey(t) && val != null)
                setLiteralAttr(event, resourceVarMap.get(t).getVarName(), val);

            // role
            val = XOrganizationalExtension.instance().extractRole(event);
            if (roleVarMap.containsKey(t) && val != null)
                setLiteralAttr(event, roleVarMap.get(t).getVarName(), val);

            // group
            val = XOrganizationalExtension.instance().extractGroup(event);
            if (groupVarMap.containsKey(t) && val != null)
                setLiteralAttr(event, groupVarMap.get(t).getVarName(), val);

        }
    }

//    private static double getDifference(LocalDateTime date1, LocalDateTime date2, TimeUnit unit) {
//        if(unit.ordinal() <= TimeUnit.HOURS.ordinal()) {
//            Duration diff = Duration.between(date1, date2);
//
//            if(unit == TimeUnit.MILLISECONDS)
//                return diff.toMillis();
//            else if(unit == TimeUnit.SECONDS)
//                return diff.getSeconds();
//            else if(unit == TimeUnit.MINUTES)
//                return diff.toMinutes();
//            else
//                return diff.toHours();
//        }
//        else {
//            Period diff = Period.between(date1.toLocalDate(), date2.toLocalDate());
//
//            if(unit == TimeUnit.DAYS)
//                return diff.getDays();
//            else if(unit == TimeUnit.WEEKS)
//                return (double) diff.getDays() / 7;
//            else if(unit == TimeUnit.MONTHS)
//                return diff.getMonths();
//            else
//                return diff.getYears();
//        }
//    }

    public static String getLifecycle(XEvent event) {
        XAttribute attribute = event.getAttributes().get("lifecycle:transition");
        return attribute != null ? attribute.toString() : "complete";
    }

    private static void invertLifecycle(XEvent event) {
        switch (getLifecycle(event)) {
            case "start" :
                setLifecycle(event, "complete");
                break;
            case "complete" :
                setLifecycle(event, "start");
                break;
            default :
                throw new IllegalStateException("Unsupported lifecycle transition: " + getLifecycle(event));
        }
    }

    private static void setLifecycle(XEvent event, String lifecycle) {
        XAttributeMap attributes = event.getAttributes();
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        XAttribute attribute = factory.createAttributeLiteral("lifecycle:transition", lifecycle, XLifecycleExtension.instance());
        attributes.put("lifecycle:transition", attribute);
        event.setAttributes(attributes);
    }

    private static void setContinuousAttr(XEvent event, String varName, double value) {
        XAttributeMap attributes = event.getAttributes();
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        XAttribute attribute = factory.createAttributeContinuous(varName, value, null);
        attributes.put(varName, attribute);
        event.setAttributes(attributes);
    }

    private static void setDateAttr(XEvent event, String varName, Date value) {
        XAttributeMap attributes = event.getAttributes();
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        XAttribute attribute = factory.createAttributeTimestamp(varName, value, null);
        attributes.put(varName, attribute);
        event.setAttributes(attributes);
    }

    private static void setLiteralAttr(XEvent event, String varName, String value) {
        XAttributeMap attributes = event.getAttributes();
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        XAttribute attribute = factory.createAttributeLiteral(varName, value, null);
        attributes.put(varName, attribute);
        event.setAttributes(attributes);
    }

    private static void setBooleanAttr(XEvent event, String varName, boolean value) {
        XAttributeMap attributes = event.getAttributes();
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        XAttribute attribute = factory.createAttributeBoolean(varName, value, null);
        attributes.put(varName, attribute);
        event.setAttributes(attributes);
    }
}

