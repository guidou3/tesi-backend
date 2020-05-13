package org.processmining.Guido.Result;

import java.util.Collection;

public class ActivityGraphDetails {
    private Collection<String> ids;
    private String label;
    private float value;
    private float[] cases;
    private String fillColor;
    private String textColor;

    public ActivityGraphDetails(Collection<String> ids, String label, float value, float[] cases, String fillColor, String textColor) {
        this.ids = ids;
        this.label = label;
        this.value = value;
        this.cases = cases;
        this.fillColor = fillColor;
        this.textColor = textColor;
    }
}
