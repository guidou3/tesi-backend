package org.processmining.Guido.CustomElements.enums;

public class Prefixes {
    private static String consequencePrefix;
    private static String placePrefix;
    private static String transitionPrefix;

    static {
        consequencePrefix = "Consequence_";
        placePrefix = "place_";
        transitionPrefix = "transition_";
    }

    public static String conseqPlace() {
        return consequencePrefix + placePrefix;
    }

    public static String conseqTransition() {
        return consequencePrefix + transitionPrefix;
    }
}
