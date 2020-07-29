package org.processmining.Guido.Result;
import org.deckfour.xes.model.XAttribute;
import org.processmining.Guido.Result.AlignmentGroupNew.*;
import org.processmining.Guido.utils.Utils;
import org.processmining.xesalignmentextension.XDataAlignmentExtension;
import org.processmining.xesalignmentextension.XDataAlignmentExtension.IncorrectXAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupOutput {

    private List<AlignmentGroupStep> steps;
    private List<ConstraintSingleResult> constraints;
    private int size;
    private double fitnessValue;
    private String fitness;
    private float averageLength;
    double relevance;

    public GroupOutput(List<AlignmentGroupStep> steps, List<ConstraintSingleResult> constraints, int size, double fitness, float averageLength) {
        this.steps = steps;
        this.constraints = constraints;
        this.size = size;
        this.fitness = getPercentage(fitness);
        this.fitnessValue = fitness;
        this.averageLength = averageLength;
        this.relevance = size * (1 - fitness);
    }

    public List<ConstraintSingleResult> getConstraints() {
        return constraints;
    }

    public int getSize() {
        return size;
    }

    private String getPercentage(double num) {
        return Math.round(num * 10000) / 100 + "%";
    }
}
