package org.processmining.Guido.mapping;

import org.processmining.framework.util.LevenshteinDistance;

import java.util.*;

public class VariableMapping {
    Map<String, String> mapping;

    public VariableMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getMapping(boolean includeNotMapped) {
        Map<String, String> retValue=new HashMap<String, String>();
        for(Map.Entry<String, String> couple : mapping.entrySet()) {
            String selection=couple.getValue();
            if (selection.equals("")) {
                if (includeNotMapped)
                    retValue.put(couple.getKey(), null);
            }
            else
                retValue.put(couple.getKey(), selection);
        }
        return retValue;
    }
}
