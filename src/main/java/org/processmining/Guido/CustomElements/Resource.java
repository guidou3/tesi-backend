package org.processmining.Guido.CustomElements;


import org.processmining.Guido.CustomElements.enums.*;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.util.ArrayList;
import java.util.HashMap;

public class Resource extends AbstractRRG {
    private ArrayList<String> transitions;
    private ArrayList<Transition> transitionsRef;
    private ResType resType;

    public void initialize(HashMap<String, Transition> id2transition, HashMap<String, String> id2NewId) {
        transitionsRef = new ArrayList<>();
        for(String id : transitions) {
            transitionsRef.add(id2transition.get(id2NewId.get(id)));
        }
    }

    public ArrayList<Transition> getTransitions() {
        return transitionsRef;
    }

    public ResType getResType() {
        return resType;
    }
}
