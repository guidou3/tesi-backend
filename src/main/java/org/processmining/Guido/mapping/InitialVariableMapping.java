package org.processmining.Guido.mapping;

import org.processmining.framework.util.LevenshteinDistance;

import java.util.*;

import org.processmining.framework.util.LevenshteinDistance;

import java.util.*;

public class InitialVariableMapping {
    Map<String, String> mapping;
    Set<String> internalSet;

    private final static float lowDistFact=0.3F;
    private final static float highDistFact=0.7F;

    private class NameValue {
        String s;
        int i;

        public NameValue(String s, int i) {
            this.s = s;
            this.i = i;
        }

        public String getName() {
            return s;
        }

        public int getValue() {
            return i;
        }
    }

    public InitialVariableMapping(Collection<String> coll1, Collection<String> coll2) {

        try {
            internalSet = new TreeSet<>(coll2);
            internalSet.add("");
        }
        catch(ClassCastException err) {
            internalSet = new HashSet<>(coll2);
            internalSet.add("");
        }

        mapping = new HashMap<>();

        for(String s : new TreeSet<>(coll1)) {
            if(s.startsWith("custom:"))
                mapping.put(s, s); // custom variable automatically created, it is useless to check
            else {
                NameValue pair = getBestMatch(s, internalSet);
                String match = "";

                if (pair.getValue() < highDistFact*s.length()) {
                    match = pair.getName();
                    if (pair.getValue() > lowDistFact*s.length()) {
                        // if the value is higher than the lower bound, highlight the association as possible (not sure)
                        match +="<*>";
                    }
                }
                mapping.put(s, match);
            }
        }
    }

    private NameValue getBestMatch(String string, Set<String> internalSet) {
        LevenshteinDistance ld = new LevenshteinDistance();
        int minValue = Integer.MAX_VALUE;
        String minName = null;

        for(String internal : internalSet) {
            int ldist;

            if(internal.length() == 0)
                ldist = string.length();
            else
                ldist = ld.getLevenshteinDistanceLinearSpace(string, internal);

            if (ldist < minValue) {
                minValue = ldist;
                minName = internal;
            }
        }
        return new NameValue(minName, minValue);
    }

    public Map<String, String> extractCustomVariableMapping() {
        Map<String, String> newMapping = new HashMap<>();
        Map<String, String> customMapping = new HashMap<>();

        mapping.forEach((var1, var2) -> {
            if(var1.startsWith("custom:"))
                customMapping.put(var1, var2);
            else
                newMapping.put(var1, var2);
        });

        mapping = newMapping;
        return customMapping;
    }
}
