package org.processmining.Guido.Result;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.util.List;
import java.util.Objects;

public class ConstraintResult {
    private String type;
    private String id;
    private List<String> transitions;
    private boolean result;
    private List<String> details;

    public ConstraintResult(String type, String id, List<String> transitions, boolean result, List<String> details) {
        this.type = type;
        this.id = id;
        this.transitions = transitions;
        this.result = result;
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstraintResult that = (ConstraintResult) o;
        return result == that.result &&
                Objects.equals(id, that.id) &&
                Objects.equals(type, that.type) &&
                Objects.equals(transitions, that.transitions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, transitions, result);
    }
}
