package org.processmining.Guido.mapping;

import com.google.gson.JsonObject;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.util.ArrayUtils;
import org.processmining.log.utils.XUtils;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;
import static org.processmining.Guido.utils.Utils.findNewString;

/**
 * GUI to map event class (with any classifiers) to transitions of Petri net.
 * Alternative that allows to disable the approximate matching.
 *
 * @author aadrians
 * @author F. Mannhardt
 *
 */
public class InitialMapping  {

    // dummy event class (for unmapped transitions)
    public final static XEventClass DUMMY = new XEventClass("DUMMY", -1) {

        public boolean equals(Object o) {
            return this == o;
        }

        public int hashCode() {
            return System.identityHashCode(this);
        }

    };

    private String[] classifiers;
    private String defaultClassifier;
    private Object[] nameList;
    private Object[] resourceList;
    Map<String, List<Integer>> transitionNames;

    public InitialMapping(final XLog log, final PetrinetGraph net) {

//        final String invisibleTransitionRegEx = "[a-z][0-9]+|(tr[0-9]+)|(silent)|(tau)|(skip)|(invi)|(and)|(or)|(xor)|(start_event)|(end_event)";
//        final Pattern pattern = Pattern.compile(invisibleTransitionRegEx);

        classifiers = new String[]{"Event Name", "Resource"};
        defaultClassifier = "Event Name";

        nameList = extractEventClasses(log, XLogInfoImpl.NAME_CLASSIFIER);
        resourceList = extractEventClasses(log, XLogInfoImpl.RESOURCE_CLASSIFIER);

        // add mapping between transitions and selected event class
        transitionNames = new HashMap<>();

        for (Transition transition : net.getTransitions()) {

            List<Integer> list = new ArrayList<>();

            if (transition.isInvisible())
                list.add(0);
            else {
                list.add(preSelectOption(transition.getLabel(), nameList, true));
                list.add(preSelectOption(transition.getLabel(), nameList, false));
                list.add(preSelectOption(transition.getLabel(), resourceList, true));
                list.add(preSelectOption(transition.getLabel(), resourceList, false));
            }

            String label = findNewString(transitionNames, transition.getLabel());
            transitionNames.put(label, list);
        }
        transitionNames = transitionNames.entrySet().stream().sorted(comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

    }

    private JsonObject createJson(String s) {
        JsonObject json = new JsonObject();
        json.addProperty("label", s);
        json.addProperty("value", s);

        return json;
    }

    private String getLabel(Object obj) {
        return ((JsonObject) obj).get("label").getAsString();
    }

    /**
     * get all available event classes using the selected classifier, add with
     * NONE
     *
     * @param log
     * @param classifier
     * @return
     */
    private Object[] extractEventClasses(XLog log, XEventClassifier classifier) {
        // sort event class
        Collection<XEventClass> classes = XUtils.createEventClasses(classifier, log).getClasses();

        List<JsonObject> initial = new ArrayList<>();
        for(XEventClass xEventClass : classes) {
            initial.add(createJson(xEventClass.getId()));
        }

        initial.sort(new Comparator<JsonObject>() {
            private static final String KEY_NAME = "label";

            @Override
            public int compare(JsonObject o1, JsonObject o2) {
                String a = o1.get(KEY_NAME).toString();
                String b = o2.get(KEY_NAME).toString();
                return a.compareTo(b);
            }
        });

        // create possible event classes
        Object[] arrEvClass = initial.toArray();

        JsonObject first = createJson("NONE");
        Object[] notMappedAct = { first };
        Object[] boxOptions = ArrayUtils.concatAll(notMappedAct, arrEvClass);

        return boxOptions;
    }

    /**
     * Returns the Event Option Box index of the most similar event for the
     * transition.
     *
     * @param transition
     *            Name of the transitions, assuming low cases
     * @param events
     *            Array with the options for this transition
     * @return Index of option more similar to the transition
     */
    private int preSelectOption(String transition, Object[] events, boolean approximateMatch) {

        String transitionLabel = transition.toLowerCase();
        // try to find precise match
        for (int i = 1; i < events.length; i++) {
            String event = getLabel(events[i]).toLowerCase();
            if (event.equalsIgnoreCase(transitionLabel)) {
                return i;
            }
        }

//        Matcher matcher = pattern.matcher(transitionLabel);
//        if (matcher.find() && matcher.start() == 0) {
//            return 0;
//        }

        if (approximateMatch) {
            //The metric to get the similarity between strings
            AbstractStringMetric metric = new Levenshtein();

            int index = 0;
            float simOld = Float.MIN_VALUE;
            for (int i = 1; i < events.length; i++) {
                String event = getLabel(events[i]).toLowerCase();

                if (transitionLabel.startsWith(event)) {
                    index = i;
                    break;
                }

                float sim = metric.getSimilarity(transitionLabel, event);
                if (simOld < sim) {
                    simOld = sim;
                    index = i;
                }

            }

            return index;
        } else {
            return 0;
        }
    }

//    /**
//     * Generate the map between Transitions and Event according to the user
//     * selection.
//     *
//     * @return Map between Transitions and Events.
//     */
//    public TransEvClassMapping getMap() {
//        TransEvClassMapping map = new TransEvClassMapping(
//                (XEventClassifier) this.classifierSelectionCbBox.getSelectedItem(), DUMMY);
//        for (Transition trans : mapTrans2ComboBox.keySet()) {
//            Object selectedValue = mapTrans2ComboBox.get(trans).getSelectedItem();
//            if (selectedValue instanceof XEventClass) {
//                // a real event class
//                map.put(trans, (XEventClass) selectedValue);
//            } else {
//                // this is "NONE"
//                map.put(trans, DUMMY);
//            }
//        }
//        return map;
//    }

}


