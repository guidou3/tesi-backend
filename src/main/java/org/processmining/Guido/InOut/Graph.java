package org.processmining.Guido.InOut;

import org.processmining.Guido.utils.Utils;

import java.util.List;

public class Graph {
    private String dot;
    private List<Utils.PairOfStrings> guards;

    public Graph(String dot, List<Utils.PairOfStrings> list) {
        this.dot = dot;
        guards = list;
    }
}
