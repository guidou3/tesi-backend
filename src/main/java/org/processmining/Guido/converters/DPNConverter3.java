//package org.processmining.Guido.converters;
//
//import com.google.common.collect.*;
//import org.processmining.Guido.CustomElements.enums.TimeUnit;
//import org.deckfour.xes.classification.XEventClass;
//import org.deckfour.xes.classification.XEventClassifier;
//import org.deckfour.xes.extension.std.XLifecycleExtension;
//import org.deckfour.xes.extension.std.XOrganizationalExtension;
//import org.deckfour.xes.factory.XFactory;
//import org.deckfour.xes.factory.XFactoryRegistry;
//import org.deckfour.xes.model.*;
//import org.processmining.Guido.CustomElements.*;
//import org.processmining.Guido.CustomElements.enums.*;
//import org.processmining.datapetrinets.DataPetriNet;
//import org.processmining.datapetrinets.DataPetriNetsWithMarkings;
//import org.processmining.datapetrinets.dsl.DPNParserConstants;
//import org.processmining.datapetrinets.exception.NonExistingVariableException;
//import org.processmining.datapetrinets.expression.GuardExpression;
//import org.processmining.datapetrinets.expression.syntax.ParseException;
//import org.processmining.log.utils.XUtils;
//import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
//import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
//import org.processmining.models.graphbased.directed.petrinet.elements.Place;
//import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
//import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;
//import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
//import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
//import org.processmining.models.semantics.petrinet.Marking;
//import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
//
//import java.text.SimpleDateFormat;
//import java.time.ZonedDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//
//public class DPNConverter {
//
//    public interface CustomDPNtoDPNResult {
//        DataPetriNet getOriginalDpn();
//        DataPetriNet getNewDpn();
//        XLog getOriginalLog();
//        XLog getNewLog();
//    }
//
//    private BiMap<Place, Place> placesMap;
//    private Map<Transition, Transition> transitionsMap;
//    private BiMap<DataElement, DataElement> variableMap;
////    private BiMap<Transition, Place> connectMap;
//    private Map<Transition, ArrayList<Transition>> seqSyncMap;
//    private Multimap<Transition, DataElement> relTimeVarMap;
//    private Map<DataElement, TimeUnit> relTimeUnitMap;
//    private BiMap<Transition, DataElement> timestampVarMap;
//    private BiMap<Transition, DataElement> resourceVarMap;
//    private BiMap<Transition, DataElement> roleVarMap;
//    private BiMap<Transition, DataElement> groupVarMap;
//
//    private Map<String, Integer> varNameCounter;
//
//    DPNDataSettings dataSettings;
//
//
//    public DPNConverter(DPNDataSettings dataSettings) {
//        placesMap = HashBiMap.create();
//        transitionsMap = new HashMap<>();
//        variableMap = HashBiMap.create();
//        seqSyncMap = new HashMap<>();
//        relTimeVarMap = HashMultimap.create();
//        relTimeUnitMap = new HashMap<>();
//        timestampVarMap = HashBiMap.create();
//        resourceVarMap = HashBiMap.create();
//        roleVarMap = HashBiMap.create();
//        groupVarMap = HashBiMap.create();
//
//        varNameCounter = new HashMap<>();
//        this.dataSettings = dataSettings;
//    }
//
//
//    public DataPetriNet convertDpn(final DataPetriNet dpn, final CustomElements ce)
//            throws NonExistingVariableException {
//
////        dataSettings.reset();
////        final DataPetriNet dpn = cloneDataPetriNet(originalDpn);
////        System.out.println("----------------------------------------------");
////        dataSettings.print();
//
//        if(ce != null) {
//            // convert time distances
//            for (TimeDistance td: ce.getTimeDistances()) {
//                addTimeDistance(dpn, td);
//            }
//
//            // convert sequence constraints
//            for (Consequence consequence: ce.getConsequences()) {
//                addConsequence(dpn, consequence);
//            }
//
//            // convert task duration
//            for (TaskDuration tdn: ce.getTaskDurations()) {
//                addTaskDuration(dpn, tdn);
//            }
//
//            // convert time instance
//            for (TimeInstance ti: ce.getTimeInstances()) {
//                addTimeInstance(dpn, ti);
//            }
//
//            // convert resource constraints
//            for (Resource resource: ce.getResources()) {
//                addRRG(dpn, resource, resourceVarMap, "Resource");
//            }
//
//
//            // convert role constraints
//            for (Role role: ce.getRoles()) {
//                addRRG(dpn, role, roleVarMap, "Role");
//            }
//
//            // convert group constraints
//            for (Group group: ce.getGroups()) {
//                addRRG(dpn, group, groupVarMap, "Group");
//            }
//        }
//
////        XLog newLog = logConversion(orgLog, map);
////
////        return new CustomDPNtoDPNResult() {
////            private DataPetriNet originalModel = originalDpn;
////            private DataPetriNet modifiedModel = dpn;
////
////            private XLog originalLog = orgLog;
////            private XLog modifiedLog = newLog;
////
////            public DataPetriNet getOriginalDpn() { return originalModel; }
////            public DataPetriNet getNewDpn() { return modifiedModel; }
////            public XLog getOriginalLog() { return originalLog; }
////            public XLog getNewLog() { return modifiedLog; }
////        };
//        return dpn;
//    }
//
////    DataPetriNet cloneDataPetriNet(final DataPetriNet dpn) throws NonExistingVariableException {
////        final DataPetriNetsWithMarkings dpn = new PetriNetWithData(originalDpn.getLabel());
//////        XFactory factory = XFactoryRegistry.instance().currentDefault();
//////        final XLog modLog = factory.createLog();
////
////        // convert places
////        for (Place place: originalDpn.getPlaces()) {
////            Place dpnPlace = dpn.addPlace(place.getLabel());
////            placesMap.put(place, dpnPlace);
////        }
////
////        // convert transitions
////        for(Transition transition : originalDpn.getTransitions()) {
////            Transition newTransition = dpn.addTransition(transition.getLabel());
////            newTransition.setInvisible(transition.isInvisible());
////
//////            // copy existing guard
//////            if (((PNWDTransition) transition).hasGuardExpression()) {
//////                ((PNWDTransition) newTransition).setGuard(dpn, ((PNWDTransition) transition).getGuardExpression());
//////            }
////
////            transitionsMap.put(transition, newTransition);
////
////        }
////
////        // convert arcs
////        for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : originalDpn.getEdges()) {
////            if ((edge.getSource() instanceof Transition) && (edge.getTarget() instanceof Place)) {
////                dpn.addArc(transitionsMap.get(edge.getSource()), placesMap.get(edge.getTarget()));
////            }
////            if ((edge.getSource() instanceof Place) && (edge.getTarget() instanceof Transition)) {
////                dpn.addArc(placesMap.get(edge.getSource()), transitionsMap.get(edge.getTarget()));
////            }
////        }
////
////        // convert variables
////        // TODO: look better
////        for (DataElement variable: originalDpn.getVariables()) {
////            DataElement newVar = dpn.addVariable(variable.getVarName(), variable.getType(), variable.getMinValue(), variable.getMaxValue());
////            variableMap.put(variable, newVar);
////            varNameCounter.put(variable.getVarName(), 1);
////
////            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc: originalDpn.getInEdges(variable)) {
////                dpn.assignWriteOperation(transitionsMap.get((Transition) arc.getSource()), newVar);
////                dataSettings.addWriteOperation(transitionsMap.get((Transition) arc.getSource()), newVar);
////            }
////
////            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc: originalDpn.getOutEdges(variable)) {
////                dpn.assignReadOperation(transitionsMap.get((Transition) arc.getTarget()), newVar);
////                dataSettings.addReadOperation(transitionsMap.get((Transition) arc.getTarget()), newVar);
////            }
////        }
////
////        for(Transition transition : originalDpn.getTransitions()) {
////            Transition newTransition = transitionsMap.get(transition);
////
////            // copy existing guard
////            if (((PNWDTransition) transition).hasGuardExpression()) {
////                ((PNWDTransition) newTransition).setGuard(dpn, ((PNWDTransition) transition).getGuardExpression());
////                dataSettings.addGuard(newTransition, ((PNWDTransition) transition).getGuardExpression().toCanonicalString());
////            }
////        }
////
////        // convert initial and final marking
////        if(dpn.getInitialMarking() != null) {
////            Marking initialMarking = new Marking();
////            for (Place initPlace: dpn.getInitialMarking().toList()) {
////                initialMarking.add(placesMap.get(initPlace));
////            }
////            dpn.setInitialMarking(initialMarking);
////        }
////
////        if(dpn.getFinalMarkings() != null) {
////            Marking[] finalMarking = new Marking[dpn.getFinalMarkings().length];
////            for (Marking marking: dpn.getFinalMarkings()) {
////                Marking dpnMarking = new Marking();
////                for (Place finalPlace: marking.toList()) {
////                    dpnMarking.add(placesMap.get(finalPlace));
////                }
////                finalMarking[Arrays.asList(dpn.getFinalMarkings()).indexOf(marking)] = dpnMarking;
////            }
////            dpn.setFinalMarkings(finalMarking);
////        }
////
//////		dpn.setInitialMarking(placesMap.get(dpn.getInitialMarkings()));
//////		dpn.setFinalMarkings(dpn.getFinalMarkings());
////        return dpn;
////    }
//
////    public XLog convertLog(final XLog orgLog, TransEvClassMapping map) {
////        XFactory factory = XFactoryRegistry.instance().currentDefault();
////        final XLog modLog = factory.createLog();
////
////        // LOG CONVERSION
////        BiMap<String, XEventClass> eventClassMap = HashBiMap.create();
////        for (XEventClass ec: map.values())
////            eventClassMap.put(ec.getId(), ec);
////
////        XEventClassifier eventClassifier = map.getEventClassifier();
////        Multimap<XEventClass, Transition> invertedMap = Multimaps.invertFrom(Multimaps.forMap(map), HashMultimap.<XEventClass, Transition>create());
////
////        for (XTrace oldTrace : orgLog) {
////            XTrace newTrace = factory.createTrace(oldTrace.getAttributes());
////            Date firstEventDate = null;
////
////            HashMap<String, XEvent> lastSeenEventMap = new HashMap<>();
////
////            for (XEvent event : oldTrace) {
////                String eventClassId = eventClassifier.getClassIdentity(event);
////
////                if (firstEventDate == null)  firstEventDate = XUtils.getTimestamp(event);
////
////                lastSeenEventMap.putIfAbsent(eventClassId, event);
////                XEvent lastSeen = lastSeenEventMap.get(eventClassId);
////                if (lastSeen.equals(event)) {
////                    // se c'è solo complete crea una copia dell'evento e gli da tipo start
////                    if (getLifecycle(event).equals("complete")) {
////                        XEvent clone = (XEvent) event.clone();
////                        invertLifecycle(clone);
////                        addAttributesToEvent(eventClassMap, invertedMap, firstEventDate,
////                                clone, eventClassifier.getClassIdentity(clone));
////                        newTrace.add(clone);
////                    }
////                } else {
////                    if (getLifecycle(event).equals(getLifecycle(lastSeen))) {
////                        XEvent clone;
////                        switch (getLifecycle(event)) { // crea manualmente gli start/complete mancanti quando essi mancano... ... ...
////                            case "start":
////                                clone = (XEvent) lastSeenEventMap.get(eventClassId).clone();
////                                invertLifecycle(clone);
////                                addAttributesToEvent(eventClassMap, invertedMap, firstEventDate, clone, eventClassifier.getClassIdentity(clone));
////                                newTrace.add(newTrace.indexOf(lastSeenEventMap.get(eventClassId)) + 1, clone);
////                                break;
////                            case "complete":
////                                clone = (XEvent) event.clone();
////                                invertLifecycle(clone);
////                                addAttributesToEvent(eventClassMap, invertedMap, firstEventDate, clone, eventClassifier.getClassIdentity(clone));
////                                newTrace.add(clone);
////                                break;
////                            default:
////                                throw new IllegalStateException("Unsupported lifecycle transition: " + getLifecycle(event));
////                        }
////                    }
////                }
////
////                addAttributesToEvent(eventClassMap, invertedMap, firstEventDate, event, eventClassId);
////
////                lastSeenEventMap.put(eventClassId, event);
////                newTrace.add(event);
////            }
////
////            // check for "open" events (events that have not been completed)
////            for (XEvent event : lastSeenEventMap.values()) {
////                if (getLifecycle(event).equals("start")) {
////                    XEvent clone = (XEvent) lastSeenEventMap.get(eventClassifier.getClassIdentity(event)).clone();
////                    invertLifecycle(clone);
////                    newTrace.add(newTrace.indexOf(lastSeenEventMap.get(eventClassifier.getClassIdentity(event))) + 1, clone);
////                }
////            }
////
////            modLog.add(newTrace);
////        }
////        return modLog;
////    }
//
//    void addTimeDistance(DataPetriNet dataPetriNet, TimeDistance timeDistance) {
//        Transition t1 = timeDistance.getSourceRef(), t2 = timeDistance.getTargetRef();
//
//        Class<?> varType = Date.class;
//
//        DataElement var1 = addNonExistingTimeVarToMap(t1, dataPetriNet, varType, "RelTime", timeDistance.getTimeUnit());
//        DataElement var2 = addNonExistingTimeVarToMap(t2, dataPetriNet, varType, "RelTime", timeDistance.getTimeUnit());
//
//        long time = timeDistance.getTime() * timeDistance.getTimeUnit().getFactor();
//
////        String guard1 = "((" + var2 + "==null)||(" + var1 + "'" + timeDistance.getIneq() + "(" + var2 + "+" + time + ")))";
////        String guard2 = "((" + var1 + "==null)||(" + var2 + "'" + timeDistance.getIneq() + "(" + var1 + "+" + time + ")))";
////
////        addGuardExpression(dataPetriNet, t1, guard1);
////        addGuardExpression(dataPetriNet, t2, guard2);
//    }
//
//    void addConsequence(DataPetriNet dpn, Consequence sequence) {
//        Transition source = transitionsMap.get((Transition) sequence.getSourceRef()),
//                   target = transitionsMap.get((Transition) sequence.getTargetRef());
//
//        // TODO: check that this is the only consequence between source and target
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
//
//        //TODO: implement time constraint
//        if (sequence.getTime() > 0) {
//            DataElement sourceVar = addNonExistingTimeVarToMap(source, dpn, convertType(DPNParserConstants.DATE), "RelTime", sequence.getTimeUnit());
//            DataElement targetVar = addNonExistingTimeVarToMap(target, dpn, convertType(DPNParserConstants.DATE), "RelTime", sequence.getTimeUnit());
//
//            dpn.assignReadOperation(target, sourceVar);
//            dataSettings.addReadOperation(target, sourceVar);
//
//            long time = sequence.getTime() * sequence.getTimeUnit().getFactor();
//
//            String guard = targetVar + "'" + sequence.getIneq() + sourceVar + "+" + time + "||" + sourceVar + "==null";
//
//            addGuardExpression(dpn, target, guard);
//        }
//    }
//
//    void addTaskDuration(DataPetriNet dpn, TaskDuration taskDuration) {
//        Transition t = transitionsMap.get(taskDuration.getTransition());
//
//        // TODO: change the following to identify the time (start, end)
//        DataElement startVar = addNonExistingTimeVarToMap(t, dpn, convertType(DPNParserConstants.CONTINUOUS), "RelTime", taskDuration.getTimeUnit());
//        DataElement complVar = addNonExistingTimeVarToMap(t, dpn, convertType(DPNParserConstants.CONTINUOUS), "RelTime", taskDuration.getTimeUnit());
//
//        String guardAsString = "(" + complVar + "'" +
//                taskDuration.getIneq() + startVar + "+" + taskDuration.getTime() + ")";
//
//        addGuardExpression(dpn, t, guardAsString);
//    }
//
//    void addTimeInstance(DataPetriNet dpn, TimeInstance timeInstance) {
//        Transition transition = transitionsMap.get((Transition) timeInstance.getTransition());
//
//        //TODO: consider transition side
//
//        addNonExistingVarToMap(transition, timestampVarMap, dpn, convertType(DPNParserConstants.DATE), "TimeInstance");
//        String ineq = timeInstance.getPosition();
//        SimpleDateFormat guardFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
//        String guard = timestampVarMap.get(transition) + "'" + ineq + '"' + guardFormatter.format(timeInstance.getTimestamp()) + '"';
//        addGuardExpression(dpn, transition, guard);
//    }
//
//    void addRRG(DataPetriNet dpn, AbstractRRG rrg, BiMap<Transition, DataElement> map, String prefix) {
//        for (Transition transition: rrg.getTransitions()) {
//            Transition t1 = transitionsMap.get(transition);
//            addNonExistingVarToMap(t1, map, dpn, convertType(DPNParserConstants.LITERAL), prefix);
//        }
//
//        for (Transition transition: rrg.getTransitions()) {
//            Transition t1 = transitionsMap.get(transition);
//            addGuardExpression(dpn, t1, createGuard(rrg, map, t1));
//        }
//    }
//
//    private String createGuard(AbstractRRG constraint, BiMap<Transition, DataElement> map,
//                               Transition t1) {
//        ArrayList<Transition> transitions = constraint.getTransitions();
//        if(transitions.size() == 0)
//            return null;
//        else if(transitions.size() == 1) {
//            if(constraint.getResType() == ResType.INSTANCE)
//                return map.get(t1) + "'==" + constraint.getName();
//            else
//                return null;
//        }
//        else {
//            StringBuilder guard = new StringBuilder();
//            boolean first = true;
//            switch (constraint.getResType()) {
//                case OCCURRENCE :
//                    for(Transition transition2 : transitions) {
//                        Transition t2 = transitionsMap.get(transition2);
//                        if(t1.equals(t2)) continue;
//                        else if(first)
//                            first = false;
//                        else
//                            guard.append("&&");
//
//                        guard.append("(").append(map.get(t1)).append("'==").append(map.get(t2)).append("||").append(map.get(t2)).append("==null)");
//                    }
//                    break;
//                case ABSENCE :
//                    for(Transition transition2 : transitions) {
//                        Transition t2 = transitionsMap.get(transition2);
//                        if(t1.equals(t2)) continue;
//                        else if(first)
//                            first = false;
//                        else
//                            guard.append("&&");
//                        guard.append("(").append(map.get(t1)).append("'!=").append(map.get(t2)).append("||").append(map.get(t2)).append("==null)");
//                    }
//                    break;
//                case INSTANCE :
//                    guard = new StringBuilder(map.get(t1) + "'==" + constraint.getName());
//                    break;
//                default :
//                    break;
//            }
//            return guard.toString();
//        }
//    }
//
//    private long zonedDateTimeDifference(ZonedDateTime d1, ZonedDateTime d2){
//        return ChronoUnit.MILLIS.between(d1, d2);
//    }
//
//    private static String checkForQuotes(String name) {
//        String value = name;
//        if (!name.startsWith("\"") || !name.endsWith("\"")) {
//            value = '"' + name + '"';
//        }
//        return value;
//    }
//
////    private void addAttributesToEvent(BiMap<String, XEventClass> eventClassMap, Multimap<XEventClass,
////            Transition> invertedMap, Date firstEventDate, XEvent event, String eventClassId) {
////        Date thisEventDate = XUtils.getTimestamp(event);
////        long timeDiff = thisEventDate.getTime() - firstEventDate.getTime();
////
////        Collection<Transition> edpnTransitions = invertedMap.get(eventClassMap.get(eventClassId));
////        for (Transition t: edpnTransitions) {
////            Transition transition = transitionsMap.get(t);
////            // relTime
////            for (DataElement timeVar: relTimeVarMap.get(transition)) {
////                double relDiff = timeDiff / (double) relTimeUnitMap.get(timeVar).getFactor();
////                setContinuousAttr(event, timeVar.getVarName(), relDiff);
////            }
////            // time instances
////            if (timestampVarMap.containsKey(transition)) {
////                setDateAttr(event, timestampVarMap.get(transition).getVarName(), XUtils.getTimestamp(event));
////            }
////            // resource
////            if (resourceVarMap.containsKey(transition) && XOrganizationalExtension.instance().extractResource(event) != null) {
////                setLiteralAttr(event, resourceVarMap.get(transition).getVarName(), XOrganizationalExtension.instance().extractResource(event));
////            }
//////			// role
////            if (roleVarMap.containsKey(transition) && XOrganizationalExtension.instance().extractRole(event) != null) {
////                setLiteralAttr(event, roleVarMap.get(transition).getVarName(), XOrganizationalExtension.instance().extractRole(event));
////            }
//////			// group
////            if (groupVarMap.containsKey(transition) && XOrganizationalExtension.instance().extractGroup(event) != null) {
////                setLiteralAttr(event, groupVarMap.get(transition).getVarName(), XOrganizationalExtension.instance().extractGroup(event));
////            }
////
////        }
////    }
////
////    private static String getLifecycle(XEvent event) {
////        XAttribute attribute = event.getAttributes().get("lifecycle:transition");
////        return attribute != null ? attribute.toString() : "complete";
////    }
////
////    private static void invertLifecycle(XEvent event) {
////        switch (getLifecycle(event)) {
////            case "start" :
////                setLifecycle(event, "complete");
////                break;
////            case "complete" :
////                setLifecycle(event, "start");
////                break;
////            default :
////                throw new IllegalStateException("Unsupported lifecycle transition: " + getLifecycle(event));
////        }
////    }
////
////    private static void setLifecycle(XEvent event, String lifecycle) {
////        XAttributeMap attributes = event.getAttributes();
////        XFactory factory = XFactoryRegistry.instance().currentDefault();
////        XAttribute attribute = factory.createAttributeLiteral("lifecycle:transition", lifecycle, XLifecycleExtension.instance());
////        attributes.put("lifecycle:transition", attribute);
////        event.setAttributes(attributes);
////    }
////
////    private static void setContinuousAttr(XEvent event, String varName, double value) {
////        XAttributeMap attributes = event.getAttributes();
////        XFactory factory = XFactoryRegistry.instance().currentDefault();
////        XAttribute attribute = factory.createAttributeContinuous(varName, value, null);
////        attributes.put(varName, attribute);
////        event.setAttributes(attributes);
////    }
////
////    private static void setDateAttr(XEvent event, String varName, Date value) {
////        XAttributeMap attributes = event.getAttributes();
////        XFactory factory = XFactoryRegistry.instance().currentDefault();
////        XAttribute attribute = factory.createAttributeTimestamp(varName, value, null);
////        attributes.put(varName, attribute);
////        event.setAttributes(attributes);
////    }
////
////    private static void setLiteralAttr(XEvent event, String varName, String value) {
////        XAttributeMap attributes = event.getAttributes();
////        XFactory factory = XFactoryRegistry.instance().currentDefault();
////        XAttribute attribute = factory.createAttributeLiteral(varName, value, null);
////        attributes.put(varName, attribute);
////        event.setAttributes(attributes);
////    }
//
//    private void addGuardExpression(DataPetriNet dpn, Transition transition, String guard) {
//        GuardExpression newGuard;
//        if (guard != null) {
//            try {
//                newGuard = GuardExpression.Factory.newInstance(guard);
//                if (((PNWDTransition) transition).getGuardExpression() == null) {
//                    ((PNWDTransition) transition).setGuard(dpn, newGuard);
//                    dataSettings.addGuard(transition, guard);
//                }
//
//                else {
//                    ((PNWDTransition) transition).setGuard(dpn, GuardExpression.Operation.and(((PNWDTransition) transition).getGuardExpression(), newGuard));
//                    String guardL = ((PNWDTransition) transition).getGuardExpression().toCanonicalString();
//                    dataSettings.addGuard(transition, ((PNWDTransition) transition).getGuardExpression().toCanonicalString());
//                }
//            }
//            catch (ParseException | NonExistingVariableException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
////    String getUniqueVarName(String varName) {
////        if(varNameCounter.get(varName) != null) {
////            int index = varNameCounter.get(varName);
////            varNameCounter.put(varName, index+1);
////            return varName+index;
////        }
////
////        varNameCounter.put(varName, 1);
////        return varName;
////    }
//
//    private String normalize(String s) {
//        String[] list = s.split(" +");
//        StringBuilder newString = new StringBuilder();
//        for(String word : list) {
//            newString.append(word.substring(0, 1).toUpperCase());
//            newString.append(word.substring(1));
//        }
//        return newString.toString();
//    }
//
//    DataElement addNonExistingTimeVarToMap(Transition t, DataPetriNet dpn, Class<?> varType, String prefix, TimeUnit timeUnit) {
//        String varName = normalize(t.getLabel()) + "_Timestamp";
////        String varName = prefix + "_" + t.getLabel().replaceAll(" ", "_") + "_" + timeUnit.toString();
////        String uniqueVarName = getUniqueVarName(varName);
//        if (!relTimeVarMap.containsEntry(t, varName)) {
//            DataElement var = dpn.addVariable(varName, varType, null, null);
//            dpn.assignWriteOperation(t, var);
//            dataSettings.addWriteOperation(t, var);
////            relTimeUnitMap.put(var, timeUnit);
////            relTimeVarMap.put(t, var);
//        }
//
//        // TODO: hashmap<transition, hashmap<string, DataElement>
//        DataElement timeVar = null;
//        for (DataElement element: relTimeVarMap.get(t)) {
//            if (element.getVarName().equals(varName)){
//                timeVar = element;
//                break;
//            }
//        }
//        return timeVar;
//    }
//
//    private void addNonExistingVarToMap(Transition t, BiMap<Transition, DataElement> map,
//                                               DataPetriNet dpn, Class<?> varType, String prefix) {
//        String varName = prefix + "_" + t.getLabel().replaceAll(" ", "_");
//        if (!map.containsKey(t)) {
//            DataElement var = dpn.addVariable(varName, varType, null, null);
//            dpn.assignWriteOperation(t, var);
//            dataSettings.addWriteOperation(t, var);
//            map.put(t, var);
//        }
//    }
//
//    private static Class<?> convertType(int type) {
//        switch (type) {
//            case DPNParserConstants.LITERAL :
//                return String.class;
//
//            case DPNParserConstants.DISCRETE :
//                return Long.class;
//
//            case DPNParserConstants.BOOLEAN :
//                return Boolean.class;
//
//            case DPNParserConstants.CONTINUOUS :
//                return Double.class;
//
//            case DPNParserConstants.DATE :
//                return Date.class;
//
//            default :
//                throw new IllegalStateException();
//        }
//    }
//
//}
//
