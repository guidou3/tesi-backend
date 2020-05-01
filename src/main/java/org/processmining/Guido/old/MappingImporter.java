//package DataAwareConformanceChecking;
//
//import org.deckfour.xes.classification.XEventClass;
//import org.deckfour.xes.classification.XEventClasses;
//import org.deckfour.xes.classification.XEventClassifier;
//import org.deckfour.xes.model.XLog;
//import org.processmining.Guido.utils.UI.CheckBox;
//import org.processmining.Guido.utils.UI.ComboBox;
//import org.processmining.framework.util.ArrayUtils;
//import org.processmining.log.utils.XUtils;
//import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
//import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
//import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
//import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
//import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
//
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * GUI to map event class (with any classifiers) to transitions of Petri net.
// * Alternative that allows to disable the approximate matching.
// *
// * @author aadrians
// * @author F. Mannhardt
// *
// */
//public class MappingImporter {
//    private static final long serialVersionUID = -699953189980632566L;
//
//    // dummy event class (for unmapped transitions)
//    public final static XEventClass DUMMY = new XEventClass("DUMMY", -1) {
//
//        public boolean equals(Object o) {
//            return this == o;
//        }
//
//        public int hashCode() {
//            return System.identityHashCode(this);
//        }
//
//    };
//
//    private Map<Transition, ComboBox> mapTrans2ComboBox = new HashMap<Transition, ComboBox>();
//    private ComboBox classifierSelectionCbBox;
//    private CheckBox useApproximateMatchingChkBox;
//
//    public MappingImporter(final XLog log, final PetrinetGraph net, Object[] availableClassifier) {
//
//        // label
//        System.out.println(
//                "<html><h1>Map Transitions to Event Classes</h1><p>First, select an appropriate classifier. "
//                        + "Unmapped transitions will be mapped to a dummy event class. "
//                        + "Approximate matches are highlighted in yellow and need to be reviewed for correctness. </p></html>");
//
//        final String invisibleTransitionRegEx = "[a-z][0-9]+|(tr[0-9]+)|(silent)|(tau)|(skip)|(invi)";
//        final Pattern pattern = Pattern.compile(invisibleTransitionRegEx);
//
//        useApproximateMatchingChkBox = new CheckBox("Use Approximate Matches", true);
//
//
//        // add classifier selection
//        classifierSelectionCbBox = new ComboBox("Choose classifier", availableClassifier);
//        classifierSelectionCbBox.setSelectedIndex(0);
//
//        useApproximateMatchingChkBox.print();
//        classifierSelectionCbBox.print();
//        refresh(log, pattern, useApproximateMatchingChkBox.isSelected());
//
//
//        // add mapping between transitions and selected event class
//        Object[] boxOptions = extractEventClasses(log, (XEventClassifier) classifierSelectionCbBox.getSelectedItem());
//        List<Transition> listTrans = new ArrayList<Transition>(net.getTransitions());
//        Collections.sort(listTrans, new Comparator<Transition>() {
//            public int compare(Transition o1, Transition o2) {
//                return o1.getLabel().compareTo(o2.getLabel());
//            }
//        });
//        for (Transition transition : listTrans) {
//            ComboBox cbBox = new ComboBox(transition.getLabel(), boxOptions);
//            mapTrans2ComboBox.put(transition, cbBox);
//            if (transition.isInvisible()) {
//                cbBox.setSelectedItem(DUMMY);
//            } else {
//                cbBox.setSelectedIndex(preSelectOption(transition, boxOptions, pattern,
//                        useApproximateMatchingChkBox.isSelected()));
//            }
//        }
//
//    }
//
//    private void refresh(final XLog log, final Pattern pattern, boolean useApproximateMatching) {
//        Object prov = classifierSelectionCbBox.getSelectedItem();
//        Object[] boxOptions = extractEventClasses(log, (XEventClassifier) classifierSelectionCbBox.getSelectedItem());
//
//        for (Transition transition : mapTrans2ComboBox.keySet()) {
//            ComboBox cbBox = mapTrans2ComboBox.get(transition);
//            cbBox.removeAllItems(); // remove all items
//
//            for (Object item : boxOptions) {
//                cbBox.addItem(item);
//            }
//            if (!transition.isInvisible()) {
//                cbBox.setSelectedIndex(preSelectOption(transition, boxOptions, pattern, useApproximateMatching));
//            } else {
//                cbBox.setSelectedItem(DUMMY);
//            }
//        }
//    }
//
//    /**
//     * get all available event classes using the selected classifier, add with
//     * NONE
//     *
//     * @param log
//     * @param selectedItem
//     * @return
//     */
//    private Object[] extractEventClasses(XLog log, XEventClassifier selectedItem) {
//
//        XEventClassifier classifier = (XEventClassifier) classifierSelectionCbBox.getSelectedItem();
//        XEventClasses eventClasses = XUtils.createEventClasses(classifier, log);
//
//        // sort event class
//        Collection<XEventClass> classes = eventClasses.getClasses();
//
//        // create possible event classes
//        Object[] arrEvClass = classes.toArray();
//        Arrays.sort(arrEvClass);
//        Object[] notMappedAct = { "NONE" };
//        Object[] boxOptions = ArrayUtils.concatAll(notMappedAct, arrEvClass);
//
//        return boxOptions;
//    }
//
//    /**
//     * Returns the Event Option Box index of the most similar event for the
//     * transition.
//     *
//     * @param transition
//     *            Name of the transitions, assuming low cases
//     * @param events
//     *            Array with the options for this transition
//     * @return Index of option more similar to the transition
//     */
//    private int preSelectOption(Transition transition, Object[] events, Pattern pattern, boolean approximateMatch) {
//
//        String transitionLabel = transition.getLabel().toLowerCase();
//
//        // try to find precise match
//        for (int i = 1; i < events.length; i++) {
//            String event = ((XEventClass) events[i]).toString().toLowerCase();
//            if (event.equalsIgnoreCase(transitionLabel)) {
//                return i;
//            }
//            ;
//        }
//
//        Matcher matcher = pattern.matcher(transitionLabel);
//        if (matcher.find() && matcher.start() == 0) {
//            return 0;
//        }
//
//        if (approximateMatch) {
//            //The metric to get the similarity between strings
//            AbstractStringMetric metric = new Levenshtein();
//
//            int index = 0;
//            float simOld = Float.MIN_VALUE;
//            for (int i = 1; i < events.length; i++) {
//                String event = ((XEventClass) events[i]).toString().toLowerCase();
//
//                if (transitionLabel.startsWith(event)) {
//                    index = i;
//                    break;
//                }
//
//                float sim = metric.getSimilarity(transitionLabel, event);
//                if (simOld < sim) {
//                    simOld = sim;
//                    index = i;
//                }
//
//            }
//
//            return index;
//        } else {
//            return 0;
//        }
//    }
//
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
//
//    /**
//     * Get the selected classifier
//     *
//     * @return
//     */
//    public XEventClassifier getSelectedClassifier() {
//        return (XEventClassifier) classifierSelectionCbBox.getSelectedItem();
//    }
//
//}
