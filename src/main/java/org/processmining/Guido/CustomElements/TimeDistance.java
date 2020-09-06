package org.processmining.Guido.CustomElements;

import org.processmining.Guido.CustomElements.enums.*;

public class TimeDistance extends CustomConnection {
    private Side targetSide;
    private Side sourceSide;
    private TimeData timeData;
    private Ineq ineq;

    public TimeDistance(TimeDistance timeDistance) {
        super(timeDistance);
        this.sourceSide = timeDistance.sourceSide;
        this.targetSide = timeDistance.targetSide;
        this.timeData = timeDistance.timeData;
        this.ineq = timeDistance.ineq;
    }

    public Side getSourceSide() {
        return sourceSide;
    }

    public Side getTargetSide() {
        return targetSide;
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
}
