package org.processmining.Guido.Result;

import java.util.List;

public class ConstraintsResult {
    private String id;
    private String type;
    private int correct;
    private int wrong;
    private int missing;
    private float fitnessValue;
    private String fitness;
    private String source;
    private String target;
    private List<String> transitions;


    public ConstraintsResult(ConstraintSingleResult constraint) {
        this.id = constraint.getId();
        this.type = constraint.getType();
        correct = 0;
        wrong = 0;
        missing = 0;
        this.source = constraint.getSource();
        this.target = constraint.getTarget();
        this.transitions = constraint.getTransitions();
    }

    public void addResult(int result, int cases) {
        if(result == 0)
            correct += cases;
        else if(result == 1)
            wrong += cases;

        calculateFitness();
    }

    public void fixMissing(int missing) {
        wrong -= missing;
        if(wrong < 0) wrong = 0;
        this.missing = missing;

        calculateFitness();
    }

    public float getFitness() {
        return fitnessValue;
    }

    private void calculateFitness() {
        fitnessValue = (float) correct/(correct+wrong+missing);
        fitness = Math.round(fitnessValue * 10000)/100 + "%";
    }
}
