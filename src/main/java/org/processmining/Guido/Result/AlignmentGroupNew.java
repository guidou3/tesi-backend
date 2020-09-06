package org.processmining.Guido.Result;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.Guido.CustomElements.ConsequenceTimed;
import org.processmining.Guido.CustomElements.CustomElements;
import org.processmining.Guido.converters.DPNDataSettings;
import org.processmining.Guido.utils.Utils;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView;
import org.processmining.plugins.DataConformance.visualization.alignment.AlignmentTrace;
import org.processmining.plugins.DataConformance.visualization.alignment.ColorTheme;
import org.processmining.plugins.DataConformance.visualization.grouping.GroupedAlignments;
import org.processmining.xesalignmentextension.XAlignmentExtension;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignment;
import org.processmining.xesalignmentextension.XAlignmentExtension.MoveType;
import org.processmining.xesalignmentextension.XDataAlignmentExtension;
import org.processmining.xesalignmentextension.XDataAlignmentExtension.XDataAlignmentMove;
import org.processmining.xesalignmentextension.XDataAlignmentExtension.IncorrectXAttribute;


import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import static org.processmining.Guido.utils.Utils.getHTMLColorString;

public class AlignmentGroupNew implements GroupedAlignments.AlignmentGroup {

    public final static class CompositeMoveType {

        private final XDataAlignmentExtension.DataMoveType dataMoveType;
        private final XAlignmentExtension.MoveType moveType;

        public CompositeMoveType(XDataAlignmentExtension.XDataAlignmentMove move) {
            dataMoveType = move.getDataMoveType();
            moveType = move.getType();
        }

        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((dataMoveType == null) ? 0 : dataMoveType.hashCode());
            result = prime * result + ((moveType == null) ? 0 : moveType.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof CompositeMoveType))
                return false;
            CompositeMoveType other = (CompositeMoveType) obj;
            if (dataMoveType != other.dataMoveType)
                return false;
            if (moveType != other.moveType)
                return false;
            return true;
        }

    }

    public static class IncorrectData {
        private String variable;
        private String modelValue;
        private String logValue;

        public IncorrectData(IncorrectXAttribute attr) {
            variable = attr.getModelAttribute().getKey();
            modelValue = attr.getModelAttribute().toString();
            logValue = attr.getLogAttribute().toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IncorrectData that = (IncorrectData) o;
            return variable.equals(that.variable);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variable) +24;
        }
    }

    public final static class AlignmentGroupStep {
        private String label;
        private String id;
        private CompositeMoveType moveType;
        private boolean invisible;
        private List<Utils.PairOfStrings> missingVariables;
        private List<IncorrectData> incorrectVariables;
        private String transitionColor;

        public AlignmentGroupStep(String label, CompositeMoveType moveType, boolean invisible,
                                  Color color, List<Utils.PairOfStrings> missingVariables,
                                  List<IncorrectData> incorrectVariables) {
            this.label = label;
            this.moveType = moveType;
            this.invisible = invisible;
            this.transitionColor = getHTMLColorString(color);
            this.missingVariables = missingVariables;
            this.incorrectVariables = incorrectVariables;
        }

        public AlignmentGroupStep(XDataAlignmentMove move, final Map<String, Color> activityColorMap) {
            this.label = move.getType() == MoveType.MODEL ?  move.getModelMove() : move.getLogMove();;
            this.moveType = new CompositeMoveType(XDataAlignmentExtension.instance().extendXAlignmentMove(move));
            this.invisible = !move.isObservable();
            this.id = move.getActivityId();
            this.transitionColor = getHTMLColorString(activityColorMap.get(move.getActivityId()));
            if(transitionColor == null && !invisible)
                transitionColor = "lightgray";
            try {
                List<XAttribute> missing = move.getMissingAttributes();
                List<Utils.PairOfStrings> list = new ArrayList<>();
                for (XAttribute attr : missing)
                    list.add(new Utils.PairOfStrings(attr.getKey(), attr.toString()));
                this.missingVariables = list.size() > 0 ? list : null;
                List<IncorrectXAttribute> incorrect = move.getIncorrectAttributes();
                List<IncorrectData> list2 = new ArrayList<>();
                for (IncorrectXAttribute attr : incorrect)
                    list2.add(new IncorrectData(attr));
                this.incorrectVariables = list2.size() > 0 ? list2 : null;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getLabel() {
            return label;
        }

        public CompositeMoveType getMoveType() {
            return moveType;
        }

        public boolean isInvisible() {
            return invisible;
        }

        public List<Utils.PairOfStrings> getMissingVariables() {
            return missingVariables;
        }

        public List<IncorrectData> getIncorrectVariables() {
            return incorrectVariables;
        }

        public boolean hasVariableErrors() {
            return missingVariables != null || incorrectVariables != null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AlignmentGroupStep that = (AlignmentGroupStep) o;
            if(label.equals(that.label) && moveType.equals(that.moveType)) {
                if(!(Objects.equals(missingVariables, that.missingVariables) &&
                        Objects.equals(incorrectVariables, that.incorrectVariables))) {
                    System.out.println("New Group (Data)");
                    return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(label, moveType, invisible, missingVariables, incorrectVariables);
        }
    }

    private final GroupedAlignments<XAlignment> groupedAlignments;

    private XAlignment alignment;

    private List<AlignmentGroupStep> stepsList;

    private List<ConstraintSingleResult> constraintResults;

    public AlignmentGroupNew(XAlignment a, GroupedAlignments<XAlignment> groupedAlignments,
                             final Map<String, Color> activityColorMap, boolean evaluationMode, CustomElements ce) {
        this.alignment = a;
        this.groupedAlignments = groupedAlignments;
        this.stepsList = new ArrayList<>();
        constraintResults = new ArrayList<>();

        //TODO
        // multimap with costraint -> List of costraint steps
        // list of costraints (only if they satisfy the basic requirement) with the result, type and detailed data

        Map<String, List<String>> constraintToResults = new HashMap<>();
        for (XAlignmentExtension.XAlignmentMove move : a) {
            if(evaluationMode && !move.isObservable() && move.getModelMove().contains("custom:")) {
                String label = move.getModelMove();
                String[] data = label.split("<\\|>");
                constraintToResults.computeIfAbsent(data[1], k -> new ArrayList<>());

                constraintToResults.get(data[1]).add(label);
            }
            stepsList.add(new AlignmentGroupStep((XDataAlignmentMove) move, activityColorMap));
        }

        for(Map.Entry<String, List<String>> entry : constraintToResults.entrySet()) {
            String type = null;
            int result = 0;
            List<String> details  = new ArrayList<>();

            for(String label : entry.getValue()) {
                String[] data = label.split("<\\|>");

                if(type == null) type = data[0].substring(7);
                if(type.equals("TimeDistance") || type.equals("ConsequenceTimed") || type.equals("Resource") ||
                        type.equals("Role") || type.equals("Group")) {
                    if(data[3].equals("Wrong")) result = 1;
                    else if(data[3].equals("Missing")) result = 2;

                    details.add("Guard on " + data[2] + " was " + data[3]);
                }
                else {
                    if(data[2].equals("Wrong")) result = 1;
                    else if(data[2].equals("Missing")) result = 2;
                }
            }

            if(type == null) continue;
            else if(type.equals("TimeDistance") || type.equals("Resource") || type.equals("Role") ||
                    type.equals("Group")) {
                if(details.size() < 2 && result == 0) continue;
            }
            else if(type.equals("ConsequenceTimed")) {
                if(((ConsequenceTimed) ce.getConstraint(entry.getKey())).isForced() && details.size() < 2 && result == 0)
                    continue;
            }

            constraintResults.add(new ConstraintSingleResult(ce.getConstraint(entry.getKey()), type, entry.getKey(), result, details));
        }
    }

    public String getName() {
        return null;
    }

    public int size() {
        return groupedAlignments.getAlignmentCount(this);
    }

    public GroupOutput getOutput() {
        return new GroupOutput(
                stepsList,
                constraintResults,
                groupedAlignments.getAlignmentCount(this),
                alignment.getFitness(),
                alignment.size()
        );
    }

    public ProMTraceView.Trace<? extends ProMTraceView.Event> getRepresentative(final AlignmentTrace.InvisibleSetting invisibleSetting,
                                                                                final AlignmentTrace.DeviationsSetting deviationsSetting, final boolean colorCodeActivities) {
        return null;
    }

    public double getAverageFitness() {
        return alignment.getFitness();
    }

    public float getAverageLength() {
        return alignment.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlignmentGroupNew groupNew = (AlignmentGroupNew) o;
        return confrontSteps(groupNew.stepsList) && constraintResults.equals(groupNew.constraintResults);
    }

    private boolean confrontSteps(List<AlignmentGroupStep> otherSteps) {
        List<AlignmentGroupStep> newSteps = new ArrayList<>();
        for(AlignmentGroupStep step : stepsList) {
            if(!step.isInvisible() || step.hasVariableErrors())
                newSteps.add(step);
        }

        List<AlignmentGroupStep> newOtherSteps = new ArrayList<>();
        for(AlignmentGroupStep step : otherSteps) {
            if(!step.isInvisible() || step.hasVariableErrors())
                newOtherSteps.add(step);
        }

        return newSteps.equals(newOtherSteps);
    }

    @Override
    public int hashCode() {
        return 31 + Objects.hash(stepsList, constraintResults);
    }
}
