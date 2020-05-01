package org.processmining.Guido.CustomElements;

import org.processmining.Guido.CustomElements.enums.*;

public class TimeDistance extends CustomConnection {
    private Side side;
    private TimeData timeData;
    private Ineq ineq;

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
