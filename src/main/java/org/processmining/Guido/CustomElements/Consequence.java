package org.processmining.Guido.CustomElements;

import org.processmining.Guido.CustomElements.enums.*;

public class Consequence extends CustomConnection {
    private Side sourceSide;
    private Side targetSide;

    public Consequence() {

    }

    public Consequence(Consequence consequence) {
        super(consequence);
        this.sourceSide = consequence.sourceSide;
        this.targetSide = consequence.targetSide;
    }

    public Side getSourceSide() {
        return sourceSide;
    }

    public Side getTargetSide() {
        return targetSide;
    }
}
