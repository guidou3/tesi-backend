package org.processmining.Guido.DataAwareConformanceChecking;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.Guido.InOut.ControlFlowViolationCosts;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.processmining.Guido.utils.Utils.findNewStringBis;

public class ControlFlowCost {

    // default value
    private static final int DEFCOSTMOVEONLOG = 1;
    private static final int DEFCOSTMOVEONMODEL = 1;

    // parameter-related GUI
    private Map<Transition, String> mapTrans2Label;
    private Map<String, Integer> tableContent;

    private Map<XEventClass, String> mapXEvClass2Label;
    private Map<String, Integer> evClassTableContent;

    private List<Transition> transitions;

    public ControlFlowCost(List<Transition> transitions, List<XEventClass> evClassCol, TransEvClassMapping mapping, ControlFlowCostConverter conv) {
        this(transitions, evClassCol, mapping, DEFCOSTMOVEONLOG, DEFCOSTMOVEONMODEL, conv);
    }

    public ControlFlowCost(List<Transition> transitions, List<XEventClass> eventClasses, TransEvClassMapping mapping,
                                   int defaultCostMoveOnLog, int defaultCostMoveOnModel, ControlFlowCostConverter conv) {
        this.transitions = transitions;

        Map<Transition, String> mapTrans2Label = new HashMap<>();
        Map<XEventClass, String> mapXEvClass2Label = new HashMap<>();

        tableContent = new HashMap<>();
        for (Transition trans : transitions) {
            String label = findNewStringBis(mapTrans2Label, trans.getLabel());
            mapTrans2Label.put(trans, label);
            tableContent.put(label, trans.isInvisible() ? 0 : defaultCostMoveOnModel);
        }

        // move on log cost
        // TODO: in org.processmining.plugins.balancedconformance.ui there is a boolean that is never initialized used in the following segment of code... why?
        evClassTableContent = new HashMap<>();
        for (XEventClass evClass : eventClasses) {
            evClassTableContent.put(evClass.getId(), !mapping.containsValue(evClass) ? 0 : defaultCostMoveOnLog);
            mapXEvClass2Label.put(evClass, evClass.getId());
        }

        conv.setMapTrans2Label(mapTrans2Label);
        conv.setMapXEvClass2Label(mapXEvClass2Label);
    }

    public ControlFlowCost(Map<String, Integer> tableContent, Map<String, Integer> evClassTableContent) {
        this.tableContent = tableContent;
        for(String s : this.tableContent.keySet()) {
            if(s.contains("custom:") && s.contains("Wrong"))
                this.tableContent.put(s, 1);
//            else
//                this.tableContent.put(s, this.tableContent.get(s)*10);
        }

        this.evClassTableContent = evClassTableContent;
    }

    public void setConverterMaps(ControlFlowCostConverter converter) {
        mapTrans2Label = converter.getMapTrans2Label();
        mapXEvClass2Label = converter.getMapXEvClass2Label();

        transitions = mapTrans2Label.keySet().parallelStream().collect(Collectors.toList());
    }

    public ControlFlowViolationCosts toControlFlowViolationCosts() {
        return new ControlFlowViolationCosts(tableContent, evClassTableContent);
    }

    /**
     * Get map from event class to cost of move on log
     *
     * @return
     */
    public Map<XEventClass, Integer> getMapEvClassToCost() {
        Map<XEventClass, Integer> mapEvClass2Cost = new HashMap<>();
        for (Map.Entry<XEventClass, String> entry : mapXEvClass2Label.entrySet()) {
            mapEvClass2Cost.put(entry.getKey(), evClassTableContent.get(entry.getValue()));
        }

        return mapEvClass2Cost;
    }

    /**
     * get penalty when move on model is performed
     *
     * @return
     */
    public Map<Transition, Integer> getTransitionWeight() {
        Map<Transition, Integer> costs = new HashMap<>();
        for (Map.Entry<Transition, String> entry : mapTrans2Label.entrySet()) {
            costs.put(entry.getKey(), tableContent.get(entry.getValue()));
        }

        return costs;
    }

    /**
     * get cost of doing synchronous moves
     *
     * @return
     */
    public Map<Transition, Integer> getSyncCost() {
        Map<Transition, Integer> costs = new HashMap<>(1);
        for (Transition transition : transitions) {
            costs.put(transition, 0);
        }
        return costs;
    }
}
