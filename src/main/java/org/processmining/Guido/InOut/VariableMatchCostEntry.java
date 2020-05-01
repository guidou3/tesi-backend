package org.processmining.Guido.InOut;

public class VariableMatchCostEntry {
    private String activity;
    private String attribute;
    private float nonWritingCost;
    private float faultyValueCost;
    private boolean finalVariable;

    public VariableMatchCostEntry() {
        activity = "*";
        attribute = "*";
        nonWritingCost = 1;
        faultyValueCost = 1;
        finalVariable = false;
    }

    public VariableMatchCostEntry(String activity, String attribute, float nonWritingCost,float faultyValueCost, boolean finalVariable) {
        this.activity = activity;
        this.attribute = attribute;
        this.nonWritingCost = nonWritingCost;
        this.faultyValueCost = faultyValueCost;
        this.finalVariable = finalVariable;
    }

    public String getActivity() {
        return activity;
    }

    public String getAttribute() {
        return attribute;
    }

    public float getNonWritingCost() {
        return nonWritingCost;
    }

    public float getFaultyValueCost() {
        return faultyValueCost;
    }

    public boolean isFinalVariable() {
        return finalVariable;
    }
}
