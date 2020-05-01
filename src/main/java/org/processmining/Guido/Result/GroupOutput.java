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
    private List<ConstraintResult> constraints;
    private int size;
    private double fitness;
    private float averageLength;

    public GroupOutput(List<AlignmentGroupStep> steps, List<ConstraintResult> constraints, int size, double fitness, float averageLength) {
        this.steps = steps;
        this.constraints = constraints;
        this.size = size;
        this.fitness = fitness;
        this.averageLength = averageLength;
    }
}
