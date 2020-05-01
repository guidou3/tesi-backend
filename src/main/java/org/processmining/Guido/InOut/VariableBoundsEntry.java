package org.processmining.Guido.InOut;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VariableBoundsEntry {
    private static Map<String, Class<?>> labelToClass = new HashMap<>();

    static {
        labelToClass.put("String", String.class);
        labelToClass.put("Double", Double.class);
        labelToClass.put("Long", Long.class);
        labelToClass.put("Integer", Integer.class);
        labelToClass.put("Float", Float.class);
        labelToClass.put("Date", Date.class);
        labelToClass.put("Boolean", Boolean.class);
    }

    String variable;
    String type;
    String minimum;
    String maximum;

    public VariableBoundsEntry(String variable, String type, String minimum, String maximum) {
        this.variable = variable;
        this.type = type;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public VariableBoundsEntry(String variable, String type, Number minimum, Number maximum) {
        this.variable = variable;
        this.type = type;
        this.minimum = minimum != null ? minimum.toString() : null;
        this.maximum = maximum != null ? maximum.toString() : null;
    }

    public String getVariable() {
        return variable;
    }

    public Class<?> getType() {
        return labelToClass.get(type);
    }

    public String getTypeAsString() {
        return type;
    }

    public String getMinimum() {
        return minimum;
    }

    public String getMaximum() {
        return maximum;
    }

    public static void addClass(String s, Class<?> type) {
        labelToClass.put(s, type);
    }
}
