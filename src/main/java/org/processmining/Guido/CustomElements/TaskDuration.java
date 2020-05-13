package org.processmining.Guido.CustomElements;

import org.processmining.Guido.CustomElements.enums.*;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.util.HashMap;
import java.util.Map;

public class TaskDuration extends Constraint {
    private TimeData timeData;
    private Ineq ineq;
    private String task;
    private Transition taskRef;

    public void initialize(Map<String, Transition> id2transition, Map<String, String> id2NewId) {
        taskRef = id2transition.get(id2NewId.get(task));
    }

    public float getTime() {
        return timeData.getTime();
    }

    public TimeUnit getTimeUnit() {
        return timeData.getTimeUnit();
    }

    public TimeData getTimeData() {
        return timeData;
    }

    public String getIneq() {
        return ineq.toString();
    }

    public String getOppositeIneq() {
        return ineq.getOpposite();
    }

    public Transition getTransition() {
        return taskRef;
    }
}
