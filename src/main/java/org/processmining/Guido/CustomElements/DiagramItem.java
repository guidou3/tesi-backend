package org.processmining.Guido.CustomElements;

import java.io.Serializable;
import java.util.List;

public class DiagramItem implements Serializable {
    private String type;
    private String id;
    private Double width;
    private Double height;
    private Double x;
    private Double y;
    private String text;
    private String color;

    private List<Coordinates> waypoints;
    private String source;
    private String target;

    public String getId() {
        return id;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
