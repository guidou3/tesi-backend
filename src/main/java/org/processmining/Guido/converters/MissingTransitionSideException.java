package org.processmining.Guido.converters;

import org.processmining.Guido.CustomElements.enums.Side;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class MissingTransitionSideException extends Exception {

    public MissingTransitionSideException(Transition t, Side side) {
        super("The transition " + t.getLabel() + " doesn't possess the "+ side.toString() +" side");
    }
}
