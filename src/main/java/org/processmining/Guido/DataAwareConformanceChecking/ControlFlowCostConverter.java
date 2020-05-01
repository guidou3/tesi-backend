package org.processmining.Guido.DataAwareConformanceChecking;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.util.Map;

public class ControlFlowCostConverter {
    private Map<Transition, String> mapTrans2Label;
    private Map<XEventClass, String> mapXEvClass2Label;

    public void setMapTrans2Label(Map<Transition, String> mapTrans2Label) {
        this.mapTrans2Label = mapTrans2Label;
    }

    public void setMapXEvClass2Label(Map<XEventClass, String> mapXEvClass2Label) {
        this.mapXEvClass2Label = mapXEvClass2Label;
    }

    public Map<Transition, String> getMapTrans2Label() {
        return mapTrans2Label;
    }

    public Map<XEventClass, String> getMapXEvClass2Label() {
        return mapXEvClass2Label;
    }
}
