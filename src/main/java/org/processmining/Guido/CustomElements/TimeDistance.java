package org.processmining.Guido.CustomElements;

import org.processmining.Guido.CustomElements.enums.*;

public class TimeDistance extends CustomConnection {
    private Side side;
    private TimeData timeData;
    private Ineq ineq;

    public TimeDistance(TimeDistance timeDistance) {
        super(timeDistance);
        this.side = timeDistance.side;
        this.timeData = timeDistance.timeData;
        this.ineq = timeDistance.ineq;
    }

    public Side getSide() {
        return side;
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
