package org.processmining.Guido.CustomElements;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CustomConnection extends Constraint implements Serializable {
    private String source;
    private String target;

    private Transition sourceRef;
    private Transition targetRef;

    public CustomConnection() {}

    public CustomConnection(CustomConnection customConnection) {
        this.source = customConnection.source;
        this.target = customConnection.target;
    }

    public void initialize(Map<String, Transition> id2transition, Map<String, String> id2NewId) {
        sourceRef = id2transition.get(id2NewId.get(source));
        targetRef = id2transition.get(id2NewId.get(target));
    }

    public String getSource() { return source; }

    public String getTarget() { return target; }

    public Transition getSourceRef() { return sourceRef; }

    public Transition getTargetRef() { return targetRef; }

}
