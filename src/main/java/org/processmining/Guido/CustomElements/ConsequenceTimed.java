package org.processmining.Guido.CustomElements;

import org.processmining.Guido.CustomElements.enums.Ineq;
import org.processmining.Guido.CustomElements.enums.TimeData;
import org.processmining.Guido.CustomElements.enums.TimeUnit;

public class ConsequenceTimed extends Consequence {
    private TimeData timeData;
    private Ineq ineq;
    private boolean forced;

    public ConsequenceTimed() {

    }

    public ConsequenceTimed(ConsequenceTimed consequence) {
        super(consequence);
        this.timeData = consequence.timeData;
        this.ineq = consequence.ineq;
        this.forced = consequence.forced;
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

    public boolean isForced() {
        return forced;
    }
}
