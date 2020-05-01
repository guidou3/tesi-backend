package org.processmining.Guido.CustomElements;

import org.processmining.Guido.CustomElements.enums.*;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.util.ArrayList;

public abstract class AbstractRRG extends Constraint {
    private String name;

    public abstract ResType getResType();

    public String getName() {
        return name;
    }

    public abstract ArrayList<Transition> getTransitions();
}
