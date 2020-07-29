package org.processmining.Guido.Result;

import org.processmining.Guido.CustomElements.*;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConstraintSingleResult {
    private String type;
    private String id;
    private boolean result;
    private List<String> details;
    private String source;
    private String target;
    private List<String> transitions;

    public ConstraintSingleResult(Constraint constraint, String type, String id, boolean result, List<String> details) {
        this.type = type;
        this.id = id;
        this.result = result;
        this.details = details;
        if(constraint instanceof CustomConnection) {
            CustomConnection connection = (CustomConnection) constraint;
            source = connection.getSourceRef().getLabel();
            target = connection.getTargetRef().getLabel();
        }
        else if(constraint instanceof TaskDuration){
            transitions = new ArrayList<>();
            transitions.add(((TaskDuration) constraint).getTransition().getLabel());
        }
        else if(constraint instanceof TimeInstance){
            transitions = new ArrayList<>();
            transitions.add(((TimeInstance) constraint).getTransition().getLabel());
        }
        else if(constraint instanceof Resource){
            transitions = new ArrayList<>();
            for(Transition t : ((Resource) constraint).getTransitions())
                transitions.add(t.getLabel());
        }
        else if(constraint instanceof Role){
            transitions = new ArrayList<>();
            for(Transition t : ((Role) constraint).getTransitions())
                transitions.add(t.getLabel());
        }
        else if(constraint instanceof Group){
            transitions = new ArrayList<>();
            for(Transition t : ((Group) constraint).getTransitions())
                transitions.add(t.getLabel());
        }
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public boolean getResult() {
        return result;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public List<String> getTransitions() {
        return transitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstraintSingleResult that = (ConstraintSingleResult) o;
        return result == that.result && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, result);
    }
}
