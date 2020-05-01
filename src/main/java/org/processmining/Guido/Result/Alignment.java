package org.processmining.Guido.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.DataConformance.framework.ExecutionStep;
import org.processmining.plugins.DataConformance.framework.ReplayState;
import org.processmining.plugins.DataConformance.visualization.DataAwareStepTypes;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Floats;

/**
 * An Alignment with data. Use the {@link #alignmentStepIterator()} to iterate
 * over the {@link org.processmining.plugins.DataConformance.Alignment.AlignmentStep}s. Equals semantics of the alignment are based
 * on the {@link #traceName} and {@link #fitness} (TODO: why also fitness?).
 * Equals semantics of the contained {@link org.processmining.plugins.DataConformance.Alignment.AlignmentStep}s are based on the
 * actual alignment, so to compare if the alignment of two traces is the same
 * you should compare all steps.
 *
 */
public class Alignment implements Comparable<Alignment>, Iterable<Alignment.AlignmentStep> {

    private final static class AlignmentStepIterator extends AbstractIterator<AlignmentStep> {

        private Iterator<ExecutionStep> logIter;
        private Iterator<ExecutionStep> processIter;
        private Iterator<DataAwareStepTypes> typeIter;

        public AlignmentStepIterator(Alignment alignment) {
            logIter = alignment.logTrace.iterator();
            processIter = alignment.processTrace.iterator();
            typeIter = alignment.stepTypes.iterator();
        }

        protected AlignmentStep computeNext() {
            if (typeIter.hasNext()) {
                assert logIter.hasNext() && processIter.hasNext();
                final ExecutionStep logStep = logIter.next();
                final ExecutionStep processStep = processIter.next();
                final DataAwareStepTypes type = typeIter.next();
                return new AlignmentStepImpl(type, processStep, logStep);
            } else {
                return endOfData();
            }
        }
    }

    /**
     * A step (move) in the alignment, each step has a type {@link #getType()},
     * a log ({@link #getLogView()}), and a process component (
     * {@link #getProcessView()}.
     *
     * @author F. Mannhardt
     *
     */
    public interface AlignmentStep {

        /**
         * @return either the activity name for a move in both/model, or the
         *         event name for a log move.
         */
        String getLabel();

        /**
         * @return the type of the move
         */
        DataAwareStepTypes getType();

        /**
         * @return whether some of the required variables assignments are
         *         missing. This cannot be reliably deduced from the type.
         */
        boolean hasMissingVariableAssignment();

        /**
         * @return the process part of the move
         */
        ExecutionStep getProcessView();

        /**
         * @return the log part of the move
         */
        ExecutionStep getLogView();

    }

    public final static class AlignmentStepImpl implements AlignmentStep {

        private final ExecutionStep processStep;
        private final DataAwareStepTypes type;
        private final ExecutionStep logStep;

        public AlignmentStepImpl(DataAwareStepTypes type, ExecutionStep processStep, ExecutionStep logStep) {
            this.processStep = processStep;
            this.type = type;
            this.logStep = logStep;
        }

        public DataAwareStepTypes getType() {
            return type;
        }

        public ExecutionStep getProcessView() {
            return processStep;
        }

        public ExecutionStep getLogView() {
            return logStep;
        }

        public String getLabel() {
            return processStep.getActivity() != null ? processStep.getActivity() : logStep.getActivity();
        }

        public boolean hasMissingVariableAssignment() {
            if (processStep.isEmpty()) {
                return false;
            } else {
                for (String var : processStep.keySet()) {
                    if (!logStep.containsKey(var)) {
                        return true;
                    }
                }
                return false;
            }
        }

        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            result = prime * result + ((logStep == null) ? 0 : logStep.hashCode());
            result = prime * result + ((processStep == null) ? 0 : processStep.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AlignmentStepImpl other = (AlignmentStepImpl) obj;
            if (type != other.type)
                return false;
            if (logStep == null) {
                if (other.logStep != null)
                    return false;
            } else if (!logStep.equals(other.logStep))
                return false;
            if (processStep == null) {
                if (other.processStep != null)
                    return false;
            } else if (!processStep.equals(other.processStep))
                return false;
            return true;
        }

    }

    private static final Comparator<Alignment> NATURAL_ORDER = new Comparator<Alignment>() {

        public int compare(Alignment o1, Alignment o2) {
            return ComparisonChain
                    .start()
                    .compare(o2.getFitness(), o1.getFitness())
                    .compare(o1.getTraceName() != null ? o1.getTraceName() : "",
                            o2.getTraceName() != null ? o2.getTraceName() : "").result();
        }
    };

    public enum AlignmentOrdering implements Comparator<Alignment> {
        FITNESS_ASC("Fitness (Ascending)") {
            @Override
            public int compare(final Alignment a, final Alignment b) {
                return ComparisonChain.start().compare(a.getFitness(), b.getFitness()).result();
            }
        },
        FITNESS_DESC("Fitness (Descending)") {
            @Override
            public int compare(final Alignment a, final Alignment b) {
                return Collections.reverseOrder(FITNESS_ASC).compare(a, b);
            }
        },
        LENGTH_ASC("Length (Ascending)") {
            @Override
            public int compare(final Alignment a, final Alignment b) {
                return ComparisonChain.start().compare(a.getStepTypes().size(), b.getStepTypes().size()).result();
            }
        },
        LENGTH_DESC("Length (Descending)") {
            @Override
            public int compare(final Alignment a, final Alignment b) {
                return Collections.reverseOrder(LENGTH_ASC).compare(a, b);
            }
        },
        NAME_ASC("Trace Name (Ascending)") {
            @Override
            public int compare(final Alignment a, final Alignment b) {
                return ComparisonChain
                        .start()
                        .compare(a.getTraceName() != null ? a.getTraceName() : "",
                                b.getTraceName() != null ? b.getTraceName() : "").result();
            }
        },
        NAME_DESC("Trace Name (Descending)") {
            @Override
            public int compare(final Alignment a, final Alignment b) {
                return Collections.reverseOrder(NAME_ASC).compare(a, b);
            }
        };
        ;

        private String description;

        private AlignmentOrdering(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

    }

    private final String traceName;
    private final float fitness;
    private final float cost;
    private final List<String> stepLabels;
    private final List<DataAwareStepTypes> stepTypes;
    private final List<ExecutionStep> logTrace;
    private final List<ExecutionStep> processTrace;

    public Alignment(ReplayState state, float fitness, List<String> labelArray, List<DataAwareStepTypes> stepArray) {
        this.traceName = state.getTraceName();
        this.fitness = fitness;
        this.stepLabels = labelArray;
        this.stepTypes = stepArray;
        this.logTrace = new ArrayList<>(state.getLogTracePrefix());
        this.processTrace = new ArrayList<>(state.getProcessTracePrefix());
        this.cost = state.getCost();
    }

    public String getTraceName() {
        return traceName;
    }

    public float getFitness() {
        return fitness;
    }

    public float getCost() {
        return cost;
    }

    public List<? extends Object> getStepLabels() {
        return stepLabels;
    }

    public List<DataAwareStepTypes> getStepTypes() {
        return stepTypes;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Alignment other = (Alignment) obj;
        if (Float.compare(fitness, other.fitness) != 0)
            return false;
        if (traceName == null) {
            if (other.traceName != null)
                return false;
        } else if (!traceName.equals(other.traceName))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Floats.hashCode(fitness);
        result = prime * result + ((traceName == null) ? 0 : traceName.hashCode());
        return result;
    }

    public int compareTo(Alignment o) {
        return NATURAL_ORDER.compare(this, o);
    }

    /**
     * @return the log view
     */
    public List<ExecutionStep> getLogTrace() {
        return logTrace;
    }

    /**
     * @return the process view
     */
    public List<ExecutionStep> getProcessTrace() {
        return processTrace;
    }

    /**
     * @return iterator returning {@link AlignmentStep} objects
     */
    public Iterator<AlignmentStep> alignmentStepIterator() {
        return new AlignmentStepIterator(this);
    }

    public Iterator<AlignmentStep> iterator() {
        return alignmentStepIterator();
    }

}

