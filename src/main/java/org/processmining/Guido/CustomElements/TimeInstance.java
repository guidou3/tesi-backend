package org.processmining.Guido.CustomElements;

import org.processmining.Guido.CustomElements.enums.Side;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TimeInstance extends Constraint {
    private Side transitionSide;
    private Side side;
    private Date timestamp;
    private String task;
    private Transition taskRef;

    public TimeInstance(TimeInstance timeInstance) {
        this.transitionSide = timeInstance.transitionSide;
        this.side = timeInstance.side;
        this.timestamp = timeInstance.timestamp;
        this.task = timeInstance.task;
    }

    public void initialize(Map<String, Transition> id2transition, Map<String, String> id2NewId) {
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
