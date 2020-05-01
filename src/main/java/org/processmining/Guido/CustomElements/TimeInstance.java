package org.processmining.Guido.CustomElements;

import org.processmining.Guido.CustomElements.enums.Side;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class TimeInstance extends Constraint {
    private Side transitionSide;
    private Side side;
    private Date timestamp;
    private String task;
    private Transition taskRef;

    public void initialize(HashMap<String, Transition> id2transition, HashMap<String, String> id2NewId) {
        taskRef = id2transition.get(id2NewId.get(task));
    }

    public Side getTransitionSide() {
        return transitionSide;
    }

    public Long getTime() {
        return timestamp.getTime();
    }

    public Transition getTransition() {
        return taskRef;
    }

    public String getPosition() {
        if(side == Side.START)
            return "<";
        else
            return ">";
    }

    public String getOpposite() {
        if(side == Side.START)
            return ">=";
        else
            return "<=";
    }
}
