package org.processmining.Guido.mapping;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.log.utils.XUtils;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * GUI to map event class (with any classifiers) to transitions of Petri net.
 * Alternative that allows to disable the approximate matching.
 *
 * @author aadrians
 * @author F. Mannhardt
 *
 */
public class FinalMapping  {

    // dummy event class (for unmapped transitions)
    public final static XEventClass DUMMY = new XEventClass("DUMMY", -1) {

        public boolean equals(Object o) {
            return this == o;
        }

        public int hashCode() {
            return System.identityHashCode(this);
        }

    };

    private Object classifier;
    private Map<String, Object> list;
    private Map<String, Transition> labelToTransition;

    private TransEvClassMapping mapping;

    public FinalMapping(XEventClassifier classifier, Map<String, Object> list) {
        this.classifier = classifier;
        this.list = list;

    }

    public XEventClassifier getClassifier() {
        return (XEventClassifier) classifier;
    }

    public void labelToXEventClasses(XLog log) {
        if(classifier.equals("Event Name"))
            classifier = XLogInfoImpl.NAME_CLASSIFIER;
        else
            classifier = XLogInfoImpl.RESOURCE_CLASSIFIER;

        Collection<XEventClass> classes = XUtils.createEventClasses((XEventClassifier) classifier, log).getClasses();
        Map<String, XEventClass> labelToXEvent = new HashMap<>();
        for(XEventClass xEventClass : classes) {
            labelToXEvent.put(xEventClass.getId(), xEventClass);
        }
        Map<String, Object> newMap = new HashMap<>();
        for(Map.Entry<String, Object> entry : list.entrySet()) {
            if(entry.getValue() == "NONE")
                newMap.put(entry.getKey(), "NONE");
            else
                newMap.put(entry.getKey(), labelToXEvent.get((String) entry.getValue()));
        }

        list = newMap;
        mapping = null;
    }

    public void setLabelToTransition(Map<String, Transition> labelToTransition) {
        this.labelToTransition = labelToTransition;
        mapping = null;
    }

    public void setMapping(TransEvClassMapping map) {
        this.mapping = map;
    }


    /**
     * Generate the map between Transitions and Event according to the user
     * selection.
     *
     * @return Map between Transitions and Events.
     */
    public TransEvClassMapping getMap() {
        if (mapping == null) {
            TransEvClassMapping map = new TransEvClassMapping((XEventClassifier) classifier, DUMMY);
            for (String trans : list.keySet()) {
                Object selectedValue = list.get(trans);
                if (selectedValue instanceof XEventClass) {
                    // a real event class
                    map.put(labelToTransition.get(trans), (XEventClass) selectedValue);
                } else {
                    // this is "NONE"
                    map.put(labelToTransition.get(trans), DUMMY);
                }
            }
            mapping = map;
        }

        return mapping;

    }

}


