package org.processmining.Guido.InOut;

public class ViolationCostEntry {
    private String transition;
    private Integer cost;

    ViolationCostEntry(String s, Integer c) {
        transition = s;
        cost = c;
    }

    public String getTransition() {
        return transition;
    }

    public Integer getCost() {
        return cost;
    }
}
