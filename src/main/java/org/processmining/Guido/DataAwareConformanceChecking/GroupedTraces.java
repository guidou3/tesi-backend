package org.processmining.Guido.DataAwareConformanceChecking;


import com.google.common.base.Function;
import com.google.common.collect.*;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.log.utils.XUtils;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.DataConformance.DataAlignment.GenericTrace;
import org.processmining.plugins.DataConformance.Utility;
import org.processmining.plugins.DataConformance.framework.ExecutionStep;
import org.processmining.plugins.DataConformance.framework.ExecutionTrace;
import org.processmining.plugins.balancedconformance.functions.VirtualVariable;
import org.processmining.plugins.balancedconformance.mapping.LogMapping;
import org.processmining.plugins.balancedconformance.mapping.Variable;
import org.processmining.plugins.balancedconformance.result.BalancedDataAlignmentState;
import org.processmining.plugins.balancedconformance.result.MaxAlignmentCostHelper;

import java.util.*;
import java.util.Map.Entry;

final class GroupedTraces {

    private static final class AbstractedEvent {

        private final String classId;
        private final Object[] relevantAttributes;
        private final int hashCode;

        public AbstractedEvent(XEvent event, String classId, SortedSet<String> consideredAttributes) {
            this.classId = classId;
            this.relevantAttributes = new Object[consideredAttributes.size()];
            int i = 0;
            for (String attributeKey : consideredAttributes) {
                XAttribute attr = event.getAttributes().get(attributeKey);
                if (attr != null) {
                    relevantAttributes[i++] = XUtils.getAttributeValue(attr);
                } else {
                    relevantAttributes[i++] = null;
                }
            }
            hashCode = calcHashCode(classId, relevantAttributes);
        }

        private static int calcHashCode(String classId, Object[] relevantAttribute) {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((classId == null) ? 0 : classId.hashCode());
            result = prime * result + Arrays.hashCode(relevantAttribute); // relies on sorted array of considered attributes
            return result;
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AbstractedEvent other = (AbstractedEvent) obj;
            if (classId == null) {
                if (other.classId != null)
                    return false;
            } else if (!classId.equals(other.classId))
                return false;
            if (!Arrays.equals(relevantAttributes, other.relevantAttributes))
                return false;
            return true;
        }

    }

    private static Date findStartTimeOfMappedTrace(LogMapping logMapping, XTrace trace) {
        for (XEvent event : trace) {
            if (!logMapping.getMappedTransitions(event).isEmpty()) {
                Date startTime = XUtils.getTimestamp(event);
                if (startTime != null) {
                    return startTime;
                }
            }
        }
        // trace without time information
        return null;
    }

    interface GroupedTrace {

        XTrace getRepresentativeTrace();

    }

    private static final class GroupedXTrace implements GroupedTrace {

        private final XTrace traceWithoutUnmapped;
        private final AbstractedEvent[] abstractTrace;
        private final int hashCode;

        public GroupedXTrace(XTrace trace, LogMapping logMapping,
                             Map<String, SortedSet<String>> consideredAttributesMap) {
            if (logMapping.hasVirtualVariables()) {
                //TODO only if relative time is needed & only consider relative time for grouping but avoid adding relative time
                addRelativeTime(trace, logMapping);
            }

            this.traceWithoutUnmapped = new XTraceImpl(new XAttributeMapImpl());
            AbstractedEvent[] tempTrace = new AbstractedEvent[trace.size()];
            int i = 0;
            for (XEvent event : trace) {
                if (!logMapping.getMappedTransitions(event).isEmpty()) {
                    // Only consider mapped events
                    String classId = logMapping.getEventClassifier().getClassIdentity(event);
                    SortedSet<String> consideredAttributes = consideredAttributesMap.get(classId);
                    if (consideredAttributes != null) {
                        tempTrace[i++] = new AbstractedEvent(event, classId, consideredAttributes);
                    } else {
                        // No attribute is relevant
                        tempTrace[i++] = new AbstractedEvent(event, classId, ImmutableSortedSet.<String>of());
                    }
                    traceWithoutUnmapped.add(event);
                }
            }
            abstractTrace = Arrays.copyOf(tempTrace, i);
            hashCode = Arrays.hashCode(abstractTrace);
        }

        private void addRelativeTime(XTrace trace, LogMapping logMapping) {
            for (XEvent event : trace) {
                if (!logMapping.getMappedTransitions(event).isEmpty()) {
                    Date startTime = findStartTimeOfMappedTrace(logMapping, trace);
                    Date currentTime = XUtils.getTimestamp(event);
                    if (currentTime != null && startTime != null) {
                        XUtils.putAttribute(event,
                                new XAttributeTimestampImpl(VirtualVariable.ATTRIBUTE_KEY_RELATIVE_TIME,
                                        currentTime.getTime() - startTime.getTime()));
                    }
                }
            }
        }

        public XTrace getRepresentativeTrace() {
            return traceWithoutUnmapped;
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            GroupedXTrace other = (GroupedXTrace) obj;
            if (!Arrays.equals(abstractTrace, other.abstractTrace))
                return false;
            return true;
        }

    }

    private final LogMapping logMapping;
    private final ListMultimap<GroupedTrace, XTrace> groupedTraces = Multimaps
            .synchronizedListMultimap(ArrayListMultimap.<GroupedTrace, XTrace>create());
    private final Map<String, SortedSet<String>> consideredAttributesMap;
    private final MaxAlignmentCostHelper maxCostHelper;

    public GroupedTraces(XLog log, final LogMapping logMapping, MaxAlignmentCostHelper maxCostHelper) {
        this.logMapping = logMapping;
        this.maxCostHelper = maxCostHelper;
        this.consideredAttributesMap = lookupConsideredAttributes(logMapping);
    }

    private static Map<String, SortedSet<String>> lookupConsideredAttributes(LogMapping logMapping) {
        Map<String, SortedSet<String>> consideredAttributesMap = new HashMap<String, SortedSet<String>>();
        for (Entry<String, Collection<Transition>> entry : logMapping.getEventIdentityToTransition().asMap()
                .entrySet()) {
            SortedSet<String> consideredAttributes = new TreeSet<>(); // Order is important!!
            String classId = entry.getKey();
            Set<Transition> possibleTransitions = logMapping.getEventIdentityToTransition().get(classId);
            // Consider attributes for all transitions this event class might be mapped to
            for (Transition t : possibleTransitions) {
                lookupVariables(logMapping, consideredAttributes, possibleTransitions, t);
            }
            consideredAttributesMap.put(classId, consideredAttributes);
        }
        return consideredAttributesMap;
    }

    private static void lookupVariables(LogMapping logMapping, SortedSet<String> consideredAttributes,
                                        Set<Transition> possibleTransitions, Transition t) {
        for (String var : logMapping.getVariablesToWrite().get(t)) {
            lookupVariable(logMapping, consideredAttributes, possibleTransitions, var);
        }
    }

    private static void lookupVariable(LogMapping logMapping, SortedSet<String> consideredAttributes,
                                       Set<Transition> possibleTransitions, String var) {
        Variable variable = logMapping.getVariables().get(var);
        if (variable.isVirtual()) {
            lookupVirtualVariable(consideredAttributes, possibleTransitions, variable);
        } else if (variable.isUsedInGuard()) {
            String attributeName = variable.getAttributeName();
            if (attributeName != null) {
                consideredAttributes.add(attributeName);
            } // otherwise unmapped so not relevant either
        }
    }

    private static void lookupVirtualVariable(SortedSet<String> consideredAttributes,
                                              Set<Transition> possibleTransitions, Variable variable) {
        VirtualVariable virtualVar = variable.getVirtualVariable();
        // add relevant attributes for current transition
        for (String attributeName : virtualVar.getRelevantAttributes()) {
            consideredAttributes.add(attributeName);
        }
        // also add all relevant attributes for further relevant transitions (the virtual variable might depend on the prefix)
        Set<Transition> relevantTransitions = virtualVar.getRelevantTransitions();
        if (!Sets.intersection(possibleTransitions, relevantTransitions).isEmpty()) {
            // this event is mapped to one of the transitions
            consideredAttributes.addAll(virtualVar.getRelevantAttributes());
        }
    }

    public void add(XTrace trace) {
        groupedTraces.put(new GroupedXTrace(trace, logMapping, consideredAttributesMap), trace);
    }

    public int size() {
        return groupedTraces.keySet().size();
    }

    public int groupSize(BalancedDataAlignmentState state) {
        return groupedTraces.keys()
                .count(new GroupedXTrace(state.getOriginalTrace(), logMapping, consideredAttributesMap));
    }

    public List<XTrace> getTracesInGroup(XTrace originalTrace) {
        return groupedTraces.get(new GroupedXTrace(originalTrace, logMapping, consideredAttributesMap));
    }

    public Map<GroupedTrace, Collection<XTrace>> asMap() {
        return groupedTraces.asMap();
    }

    public Multiset<GroupedTrace> asMultiset() {
        return groupedTraces.keys();
    }

    public List<BalancedDataAlignmentState> getExpandedStates(final BalancedDataAlignmentState state) {
        return Lists.transform(getTracesInGroup(state.getOriginalTrace()),
                new Function<XTrace, BalancedDataAlignmentState>() {

                    public BalancedDataAlignmentState apply(XTrace originalTrace) {
                        return expandStateUsingOriginalTrace(state, originalTrace);
                    }
                });
    }

    private BalancedDataAlignmentState expandStateUsingOriginalTrace(BalancedDataAlignmentState state,
                                                                     XTrace originalTrace) {

        // Remove virtual variable
        if (logMapping.hasVirtualVariables()) {
            for (XEvent event : originalTrace) {
                event.getAttributes().remove(VirtualVariable.ATTRIBUTE_KEY_RELATIVE_TIME);
            }
        }

        String traceName = XUtils.getConceptName(originalTrace);
        int traceSize = originalTrace.size();

        ExecutionTrace adaptedProcessTrace = new GenericTrace(traceSize, traceName);
        ExecutionTrace adaptedLogTrace = new GenericTrace(traceSize, traceName);
        int adjustedCost = (int) state.getCost();

        ListIterator<XEvent> originalTraceIter = originalTrace.listIterator();
        Iterator<ExecutionStep> logIter = state.getLogTracePrefix().iterator();
        Iterator<ExecutionStep> processIter = state.getProcessTracePrefix().iterator();

        XEvent currentEvent = null;
        if (originalTraceIter.hasNext()) {
            currentEvent = originalTraceIter.next();
        }

        while (logIter.hasNext() && processIter.hasNext()) {
            ExecutionStep logStep = logIter.next();
            ExecutionStep processStep = processIter.next();

            if (originalTraceIter.hasNext() && currentEvent == null) {
                currentEvent = originalTraceIter.next();
            }

            if (logStep == ExecutionStep.bottomStep
                    // Model move with data assignments from artificial variables
                    || (logStep.getActivity() == null && processStep != ExecutionStep.bottomStep)) {
                // Model move 
                while (isUnmapped(currentEvent)) {
                    adjustedCost += expandUnmappedEvent(adaptedProcessTrace, adaptedLogTrace, currentEvent);
                    currentEvent = advanceIteratorOrNull(originalTraceIter);
                }
                adaptedLogTrace.add(logStep);
                adaptedProcessTrace.add(processStep);
            } else if (processStep == ExecutionStep.bottomStep) {
                // Log move
                while (isUnmapped(currentEvent)) {
                    adjustedCost += expandUnmappedEvent(adaptedProcessTrace, adaptedLogTrace, currentEvent);
                    currentEvent = advanceIteratorOrNull(originalTraceIter);
                }
                adaptedLogTrace.add(new ExecutionStep(logStep.getActivity(), currentEvent));
                adaptedProcessTrace.add(processStep);
                currentEvent = null; // consume event
            } else {
                // Synchronous move
                while (isUnmapped(currentEvent)) {
                    adjustedCost += expandUnmappedEvent(adaptedProcessTrace, adaptedLogTrace, currentEvent);
                    currentEvent = advanceIteratorOrNull(originalTraceIter);
                }
                expandSynchronousMove(state, logStep, processStep, currentEvent, adaptedProcessTrace, adaptedLogTrace);
                currentEvent = null; // consume event
            }
        }

        // Consume rest of the trace
        if (currentEvent != null) { // there might be an unmapped event already present
            adjustedCost += expandUnmappedEvent(adaptedProcessTrace, adaptedLogTrace, currentEvent);
        }
        while (originalTraceIter.hasNext()) { // check for the remainder of the unmapped events
            currentEvent = originalTraceIter.next();
            adjustedCost += expandUnmappedEvent(adaptedProcessTrace, adaptedLogTrace, currentEvent);
        }

        int maxCost = state.getMaxCost();
        // recalculate maxCost if there are unmapped events
        if (adjustedCost != state.getCost()) {
            maxCost = (int) maxCostHelper.getMaxCost(originalTrace);
        }

        return new BalancedDataAlignmentState(adaptedLogTrace, adaptedProcessTrace, adjustedCost, state.getDataCost(),
                maxCost, originalTrace);
    }

    private boolean isUnmapped(XEvent currentEvent) {
        return currentEvent != null && logMapping.getMappedTransitions(currentEvent).isEmpty();
    }

    private XEvent advanceIteratorOrNull(ListIterator<XEvent> originalTraceIter) {
        XEvent currentEvent;
        if (originalTraceIter.hasNext()) {
            currentEvent = originalTraceIter.next();
        } else {
            currentEvent = null;
        }
        return currentEvent;
    }

    /**
     * @param adaptedProcessTrace
     * @param adaptedLogTrace
     * @param currentEvent
     * @return additional cost of unmapped event
     */
    private int expandUnmappedEvent(ExecutionTrace adaptedProcessTrace, ExecutionTrace adaptedLogTrace,
                                    XEvent currentEvent) {
        XEventClass currentClass = logMapping.getEventClasses().getClassOf(currentEvent);
        adaptedLogTrace.add(new ExecutionStep(currentClass.getId(), currentEvent));
        adaptedProcessTrace.add(ExecutionStep.bottomStep);
        return logMapping.getEventClass2Cost().get(currentClass);
    }

    private void expandSynchronousMove(BalancedDataAlignmentState state, ExecutionStep logStep,
                                       ExecutionStep processStep, XEvent currentEvent, ExecutionTrace adaptedProcessTrace,
                                       ExecutionTrace adaptedLogTrace) {

        ExecutionStep expandedLogStep = new ExecutionStep(logStep.getActivity(), currentEvent);
        ExecutionStep expandedProcessStep = new ExecutionStep(processStep.getActivity(),
                processStep.getActivityObject());
        for (String variableName : processStep.keySet()) {
            Variable var = logMapping.getVariables().get(variableName);
            if (var != null) {
                if (var.isUsedInGuard()) {
                    // Variable is used in a guard representative value is correct given the grouping was correct
                    assert logValueEqualsRepresentativeValue(logStep, currentEvent, variableName,
                            var) : "Original value for " + variableName
                            + " differs from representative value! Invalid grouping.";
                    assignRepresentativeValue(var, logStep, processStep, expandedLogStep, expandedProcessStep);
                } else {
                    /*
                     * Variable is not constraint by a guard, a free variable ->
                     * different values are grouped together. We need to
                     * retrieve the original value from the event
                     */
                    expandVariable(var, logStep, processStep, currentEvent, expandedLogStep, expandedProcessStep);
                }

            } else {
                throw new RuntimeException("Unkown variable returned " + variableName + " in " + state);
            }
        }
        adaptedLogTrace.add(expandedLogStep);
        adaptedProcessTrace.add(expandedProcessStep);
    }

    private boolean logValueEqualsRepresentativeValue(ExecutionStep logStep, XEvent currentEvent, String variableName,
                                                      Variable var) {
        XAttribute originalEventAttribute = currentEvent.getAttributes().get(var.getAttributeName());
        if (logStep.get(variableName) != null) {
            if (originalEventAttribute != null) {
                Object processValue = logStep.get(variableName);
                Object originalValue = Utility.convertToExpectedType(Utility.getValue(originalEventAttribute),
                        processValue.getClass());
                boolean isSame = processValue.equals(originalValue);
                if (!isSame) {
                    System.err.println("Value returned for variable " + variableName + ": " + processValue + " != "
                            + originalValue);
                }
                return isSame;
            } else {
                return false;
            }
        } else {
            boolean isPresent = originalEventAttribute == null;
            if (!isPresent) {
                System.err.println("Value returned for variable " + variableName + ": is missing");
            }
            return isPresent;
        }
    }

    private void expandVariable(Variable var, ExecutionStep logStep, ExecutionStep processStep, XEvent currentEvent,
                                ExecutionStep expandedLogStep, ExecutionStep expandedProcessStep) {
        Object value = Utility.getValue(currentEvent.getAttributes().get(var.getAttributeName()));
        Class<?> expectedType = var.getType();
        String varName = var.getName();
        if (value == null) {
            // Value is missing in original trace, use representative values
            assignRepresentativeValue(var, logStep, processStep, expandedLogStep, expandedProcessStep);
        } else if (Utility.isAssignableToExpectedType(value, expectedType)) {
            // Use value from original event 
            assignOriginalEventValue(var, value, expandedLogStep, expandedProcessStep);
        } else {
            throw new RuntimeException("Reading event " + logMapping.getEventClassifier().getClassIdentity(currentEvent)
                    + " failed. Attribute " + var.getAttributeName() + " with value " + value
                    + " is not of expected type " + expectedType.getSimpleName() + " for variable: " + varName + ".");
        }
    }

    private void assignOriginalEventValue(Variable var, Object value, ExecutionStep expandedLogStep,
                                          ExecutionStep expandedProcessStep) {
        expandedLogStep.put(var.getAttributeName(), value);
        expandedProcessStep.put(var.getAttributeName(), value);
    }

    private void assignRepresentativeValue(Variable var, ExecutionStep logStep, ExecutionStep processStep,
                                           ExecutionStep expandedLogStep, ExecutionStep expandedProcessStep) {
        Object logValue = logStep.get(var.getName());
        if (logValue != null) {
            expandedLogStep.put(var.getName(), logValue);
        }
        Object processValue = processStep.get(var.getName());
        if (processValue != null) {
            expandedProcessStep.put(var.getName(), processValue);
        }
    }

}

