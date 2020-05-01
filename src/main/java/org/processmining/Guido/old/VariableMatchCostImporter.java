//package org.processmining.Guido.DataAwareConformanceChecking;
//
//import org.deckfour.xes.classification.XEventClass;
//import org.processmining.Guido.InOut.VariableMatchCostEntry;
//import org.processmining.plugins.DataConformance.DataAlignment.PetriNet.ReplayableTransition;
//import org.processmining.plugins.DataConformance.GUI.MatchingActivity;
//import org.processmining.plugins.DataConformance.GUI.VariableMatchCostUI;
//import org.processmining.plugins.DataConformance.GUI.VariableMatchingCostTable;
//import org.processmining.plugins.DataConformance.GUI.VariableMatchingTableModel;
//import org.processmining.plugins.DataConformance.framework.ReplayableActivity;
//import org.processmining.plugins.DataConformance.framework.VariableMatchCost;
//import org.processmining.plugins.DataConformance.framework.VariableMatchCosts;
//
//import java.util.*;
//
//
//public class VariableMatchCostImporter {
//    VariableMatchingCostTable variableCostTable;
//    private List<VariableMatchCostEntry> table;
//    private List<VariableMatchCostUI> table2;
//    private Set<? extends ReplayableActivity> activitySet;
//    private Set<String> variableSet;
//
//    private List<MatchingActivity<>> activityMatchingList
//
//    // activityMapping
//    // variableMapping normal...
//    public VariableMatchCostImporter(final Map<ReplayableTransition, XEventClass> activityMapping,
//                                  Map<String, String> variableMapping) {
//        variableCostTable = new VariableMatchingCostTable<XEventClass>(activityMapping, variableMapping);
//        table = new ArrayList<>();
//        table.add(new VariableMatchCostEntry());
//        activitySet = activityMapping.keySet(); // Replayable transition
//        variableSet = variableMapping.keySet(); // string
//        activityMatchingList
//
//        // mi servono 2 ulteriori liste con MatchActivity e MatchAttribute
//    }
//
//    public List<VariableMatchCostEntry>
//
//    public void setTable(List<VariableMatchCostEntry> newList) {
//        table = newList;
//        table2 = new ArrayList<>();
//        // devo convertire i valori della tabella in VariableMatchCostUI
//        // per fare questo mi servono le liste di MatchActivity e MatchAttribute e le funzioni per transformare label in oggetti
//        for (VariableMatchCostEntry entry : table) {
//
//        }
//    }
//
//    public VariableMatchCosts getCosts() {
//        VariableMatchingTableModel model = (VariableMatchingTableModel) variableCostTable.getModel();
//        List<VariableMatchCostUI> values = model.getValues();
//
//        Vector<VariableMatchCost> retValue = new Vector<VariableMatchCost>(values.size());
//
//        for (VariableMatchCostUI value : values) {
//            VariableMatchCost elem = new VariableMatchCost();
//            if (value.getActivity().getNode() != null)
//                elem.setActivity((value.getActivity().getNode()).getLabel());
//            if (value.getVariable().getProcessAttribute() != null)
//                elem.setVariable(value.getVariable().getProcessAttribute());
//            elem.setCostFaultyValue(value.getCostFaultyValue());
//            elem.setCostNotWriting(value.getCostNotWriting());
//            elem.setFinal(value.isFinal());
//            retValue.add(elem);
//        }
//        return new VariableMatchCosts(retValue, activitySet, variableSet);
//    }
//
//    public VariableMatchCosts getCosts2() {
//        Vector<VariableMatchCost> retValue = new Vector<VariableMatchCost>(table.size());
//
//        for (VariableMatchCostEntry entry : table) {
//            VariableMatchCost elem = new VariableMatchCost();
//            if (!entry.getActivity().equals("*"))
//                elem.setActivity(entry.getActivity());
//            if (value.getVariable().getProcessAttribute() != null)
//                elem.setVariable(value.getVariable().getProcessAttribute());
//            elem.setCostFaultyValue(value.getCostFaultyValue());
//            elem.setCostNotWriting(value.getCostNotWriting());
//            elem.setFinal(value.isFinal());
//            retValue.add(elem);
//        }
//        return new VariableMatchCosts(retValue, activitySet, variableSet);
//    }
//
//}
