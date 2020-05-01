package org.processmining.Guido.InOut;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

public class ControlFlowViolationCosts {
    private List<ViolationCostEntry> modelTable;
    private List<ViolationCostEntry> logTable;

    public ControlFlowViolationCosts() {
        modelTable = new ArrayList<>();
        logTable = new ArrayList<>();
    }

    public ControlFlowViolationCosts(List<ViolationCostEntry> modelTable, List<ViolationCostEntry> logTable) {
        this.modelTable = modelTable;
        this.logTable = logTable;
    }

    public ControlFlowViolationCosts(Map<String, Integer> tableContent, Map<String, Integer> evClassTableContent) {
        modelTable = new ArrayList<>();
        logTable = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : tableContent.entrySet()) {
            modelTable.add(new ViolationCostEntry(entry.getKey(), entry.getValue()));
        }

        for(Map.Entry<String, Integer> entry : evClassTableContent.entrySet()) {
            logTable.add(new ViolationCostEntry(entry.getKey(), entry.getValue()));
        }

        modelTable.sort((Comparator.comparing(ViolationCostEntry::getTransition)));
        logTable.sort((Comparator.comparing(ViolationCostEntry::getTransition)));
    }

    public Map<String, Integer> getModelTable() {
        Map<String, Integer> modelMap = new HashMap<>();
        for(ViolationCostEntry element : modelTable) {
            modelMap.put(element.getTransition(), element.getCost());
        }
        return modelMap;
    }

    public Map<String, Integer> getLogTable() {
        Map<String, Integer> logMap = new HashMap<>();
        for(ViolationCostEntry element : logTable) {
            logMap.put(element.getTransition(), element.getCost());
        }
        return logMap;
    }
}
