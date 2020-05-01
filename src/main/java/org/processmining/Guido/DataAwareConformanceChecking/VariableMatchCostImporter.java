package org.processmining.Guido.DataAwareConformanceChecking;

import com.google.common.collect.Ordering;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.Guido.InOut.VariableMatchCostEntry;
import org.processmining.Guido.utils.Utils;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.DataConformance.DataAlignment.PetriNet.ReplayableTransition;
import org.processmining.plugins.DataConformance.GUI.*;
import org.processmining.plugins.DataConformance.framework.ReplayableActivity;
import org.processmining.plugins.DataConformance.framework.VariableMatchCost;
import org.processmining.plugins.DataConformance.framework.VariableMatchCosts;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.toMap;
import static org.processmining.Guido.utils.Utils.findNewString;


public class VariableMatchCostImporter {
    private List<VariableMatchCostUI> values;
    private Set<ReplayableTransition> activitySet;
    private Set<String> variableSet;

    private Map<String, MatchingActivity<ReplayableTransition, XEventClass>> activityMatchingList;
    private Map<String, MatchingXAttribute> attributeMatchingList;

    public static class Input {
        public List<String> activityMatchingList;
        public List<String> attributeMatchingList;
        public List<VariableMatchCostEntry> entryList;

        public Input(List<String> activityMatchingList, List<String> attributeMatchingList,
                     List<VariableMatchCostEntry> entryList) {
            this.activityMatchingList = activityMatchingList;
            this.attributeMatchingList = attributeMatchingList;
            this.entryList = entryList;
        }
    }

    // activityMapping
    // variableMapping normal...
    public VariableMatchCostImporter(final Map<ReplayableTransition, XEventClass> activityMapping,
                                      Map<String, String> variableMapping) {
        activitySet = activityMapping.keySet();
        variableSet = variableMapping.keySet();
        activityMatchingList = getActivityList(activityMapping);
        attributeMatchingList = getVariableList(variableMapping);

        // mi servono 2 ulteriori liste con MatchActivity e MatchAttribute
    }

    public Input generateFrontEndContent() {
        List<String> activityMatchingLabels = activityMatchingList.keySet()
                .stream()
                .filter(string-> !string.startsWith("custom:") && !string.startsWith("invisible:"))
                .sorted()
                .collect(Collectors.toList());
        List<String> attributeMatchingLabels = attributeMatchingList.keySet()
                .stream()
                .filter(string-> !string.startsWith("custom:"))
                .sorted()
                .collect(Collectors.toList());

        List<VariableMatchCostEntry> list = new ArrayList<>();
        list.add(new VariableMatchCostEntry());

        return new Input(activityMatchingLabels, attributeMatchingLabels, list);
    }

    private Map<String, MatchingActivity<ReplayableTransition, XEventClass>> getActivityList(Map<ReplayableTransition, XEventClass> activityMapping) {
        Map<String, MatchingActivity<ReplayableTransition, XEventClass>> activities = new HashMap<>();

        for (Map.Entry<ReplayableTransition, XEventClass> entry : activityMapping.entrySet()) {
            if (entry.getValue() != null) {
                MatchingActivity<ReplayableTransition, XEventClass> matchNode = new MatchingActivity<>(entry.getKey(), entry.getValue());
                activities.put(findNewString(activities, matchNode.toString()), matchNode);
            }
        }

        activities.put("*", MatchingActivity.ALLFLEXNODES);
        return activities;
    }

    private Map<String, MatchingXAttribute> getVariableList(Map<String, String> variableMapping) {
        Map<String, MatchingXAttribute> variables = new HashMap<>();

        for (Map.Entry<String, String> entry : variableMapping.entrySet()) {
            MatchingXAttribute match;

            if (entry.getValue() != null)
                match = new MatchingXAttribute(entry.getValue(), entry.getKey());
            else
                match = new MatchingXAttribute("", entry.getKey());

            variables.put(findNewString(variables, match.toString()), match);
        }

        variables.put("*", MatchingXAttribute.ALLATTRIBUTES);
        return variables;
    }

    public void setTable(List<VariableMatchCostEntry> newList) {
        values = new ArrayList<>();
        // devo convertire i valori della tabella in VariableMatchCostUI
        // per fare questo mi servono le liste di MatchActivity e MatchAttribute e le funzioni per transformare label in oggetti
        for (VariableMatchCostEntry entry : newList) {
            VariableMatchCostUI item = new VariableMatchCostUI();
            item.setActivity(activityMatchingList.get(entry.getActivity()));
            item.setVariable(attributeMatchingList.get(entry.getAttribute()));
            item.setCostFaultyValue(entry.getFaultyValueCost());
            item.setCostNotWriting(entry.getNonWritingCost());
            item.setFinal(entry.isFinalVariable());

            values.add(item);
        }
    }

    public VariableMatchCosts getCosts() {
        Vector<VariableMatchCost> retValue = new Vector<VariableMatchCost>(values.size());

        for (VariableMatchCostUI value : values) {
            VariableMatchCost elem = new VariableMatchCost();
            if (value.getActivity().getNode() != null)
                elem.setActivity((value.getActivity().getNode()).getLabel());
            if (value.getVariable().getProcessAttribute() != null)
                elem.setVariable(value.getVariable().getProcessAttribute());
            elem.setCostFaultyValue(value.getCostFaultyValue());
            elem.setCostNotWriting(value.getCostNotWriting());
            elem.setFinal(value.isFinal());
            retValue.add(elem);
        }
        return new VariableMatchCosts(retValue, activitySet, variableSet);
    }

}
