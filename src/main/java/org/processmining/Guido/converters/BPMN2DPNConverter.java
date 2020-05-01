package org.processmining.Guido.converters;

import it.unimi.dsi.fastutil.Hash;
import org.processmining.Guido.DataPetriNetWithCustomElements;
import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.datapetrinets.exception.NonExistingVariableException;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.DataAssociation;
import org.processmining.models.graphbased.directed.bpmn.elements.DataObject;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.plugins.converters.bpmn2pn.BPMN2PetriNetWithDataConverter_Configuration;

import java.util.*;

/**
 * Conversion of BPMN to Petri net with data
 *
 *
 * @author Anna Kalenkova July 27, 2014
 */
public class BPMN2DPNConverter extends BPMN2PetriNetConverter {

    // Map from activities to transitions
    private Map<Activity, Transition> activitiesMap = new HashMap<Activity, Transition>();

    // Data objects map
    private Map<DataObject, DataElement> dataObjectMap = new HashMap<DataObject, DataElement>();

    // Data Petri net
    private DataPetriNetWithCustomElements dataPetriNet;

    private HashMap<Transition, Transition> transitionsMap;

    public BPMN2DPNConverter(BPMNDiagram bpmn, BPMN2PetriNetWithDataConverter_Configuration config) {
        super(bpmn, config);
    }

    public BPMN2DPNConverter(BPMNDiagram bpmn) {
        super(bpmn, new BPMN2PetriNetWithDataConverter_Configuration());
    }

    public boolean convertWithData() {

        // Call control-flow conversion
        super.convert();

        // Clone Petri net to Data Petri net
        clonePetriNetToDataPetriNet();

        // Construct activities map
        constructActivitiesMap();

        // Convert data objects
        convertDataObjects();

        // Convert associations
        convertAssociations();

        // Convert guards
        convertGuards();

        return errors.size() == 0;
    }

    public DataPetriNetWithCustomElements getDataPetriNet() {
        return dataPetriNet;
    }

    /**
     *
     * Construct a map from activities to transitions
     */
    private void constructActivitiesMap() {
        for(Activity activity : bpmn.getActivities()) {
            for(Transition transition : dataPetriNet.getTransitions()) {
                if (transition.getLabel() != null && activity.getLabel().contains(transition.getLabel())) {
                    activitiesMap.put(activity, transition);
                }
            }
        }
    }

    /**
     *
     * Convert data objects
     */
    private void convertDataObjects() {
        for(DataObject dataObject : bpmn.getDataObjects()) {
            DataElement dataElement
                    = dataPetriNet.addVariable(dataObject.getLabel(), java.lang.String.class, null, null);
            dataObjectMap.put(dataObject, dataElement);
        }
    }

    /**
     *
     * Convert associations
     */
    private void convertAssociations() {
        for (DataAssociation association : bpmn.getDataAssociations()) {
            BPMNNode source = association.getSource();
            BPMNNode target = association.getTarget();
            if ((source instanceof DataObject) && (target instanceof Activity))
                dataPetriNet.assignReadOperation(activitiesMap.get(target), dataObjectMap.get(source));
            if ((source instanceof Activity) && (target instanceof DataObject))
                dataPetriNet.assignWriteOperation(activitiesMap.get(source), dataObjectMap.get(target));
        }
    }

    /**
     *
     * Convert guards
     */
    private void convertGuards() {
        for (Flow sequenceFlow : bpmn.getFlows()) {
            String guard = sequenceFlow.getLabel();
            if ((guard != null) && (!guard.equals(""))) {
                if (sequenceFlow.getTarget() instanceof Activity) {
                    Activity activity = (Activity) (sequenceFlow.getTarget());
                    Transition transition = activitiesMap.get(activity);
                    try {
                        dataPetriNet.setGuardFromString(transition, guard);
                    }
                    catch (org.processmining.datapetrinets.expression.syntax.ParseException e) {
                        e.printStackTrace();
                        errors.add("Parse guard exception " + guard);
                    }
                    catch (NonExistingVariableException e) {
                        e.printStackTrace();
                        errors.add("Variable doesn't exist");
                    }
                }
                else {
                    Transition transition = transitionsMap.get(flowTransitionMap.get(sequenceFlow));
                    try {
                        dataPetriNet.setGuardFromString(transition, guard);
                    }
                    catch (org.processmining.datapetrinets.expression.syntax.ParseException e) {
                        e.printStackTrace();
                        errors.add("Parse guard exception " + guard);
                    }
                    catch (NonExistingVariableException e) {
                        e.printStackTrace();
                        errors.add("Variable doesn't exist");
                    }
                }
            }
        }
    }

//    private void removeUselessTransitions() {
//
//        for(Place p : dataPetriNet.getPlaces()) {
//            if(dataPetriNet.getInEdges(p).size() == 1 && dataPetriNet.getOutEdges(p).size() == 1) {
//                Transition before = (Transition) dataPetriNet.getInEdges(p).iterator().next().getSource();
//                if(before.isInvisible()) {
//                    //do something
//                }
//            }
//        }
//    }

    /**
     *
     * Clone Petri net to data Petri net
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void clonePetriNetToDataPetriNet() {

        // Map for plain and data Petri nets places
        Map<Place, Place> placesMap = new HashMap();

        // Map for plain and data Petri nets transitions
        transitionsMap = new HashMap<>();

        dataPetriNet = new DataPetriNetWithCustomElements(net.getLabel());
        for(Place place : net.getPlaces()) {
            Place newPlace = dataPetriNet.addPlace(place.getLabel());
            placesMap.put(place, newPlace);
        }
        for(Transition transition : net.getTransitions()) {
            Transition newTransition = dataPetriNet.addTransition(transition.getLabel());
            newTransition.setInvisible(transition.isInvisible());
            transitionsMap.put(transition, newTransition);
        }
        for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getEdges()) {
            if ((edge.getSource() instanceof Transition) && (edge.getTarget() instanceof Place))
                dataPetriNet.addArc(transitionsMap.get(edge.getSource()), placesMap.get(edge.getTarget()));
            else if ((edge.getSource() instanceof Place) && (edge.getTarget() instanceof Transition))
                dataPetriNet.addArc(placesMap.get(edge.getSource()), transitionsMap.get(edge.getTarget()));
        }
    }

    public void updateMapping(BpmnToDpnMapping bpmnToDpnMapping) {
        bpmnToDpnMapping.initializeSecond(super.getNodeMap());

        // pn id to dpn id
        HashMap<String, String> oldDpnId2NewId = new HashMap<>();
        HashMap<Transition, String> transitionToId = new HashMap<>();
        for(Map.Entry<Transition, Transition> entry : transitionsMap.entrySet()) {
            oldDpnId2NewId.put(entry.getKey().getId().toString(), entry.getValue().getId().toString());
            transitionToId.put(entry.getValue(), entry.getValue().getId().toString());
        }
        bpmnToDpnMapping.nextStep(oldDpnId2NewId);
        bpmnToDpnMapping.TransitionToBPMNNode(transitionToId);


//        HashMap<String, String> id2NewId = new HashMap<>();
//        for(Map.Entry<String, String> entry : id2NewId2.entrySet()) {
//            id2NewId.put(entry.getKey(), oldDpnId2NewId.get(entry.getValue()));
//        }

    }

//    public HashMap<String, String> getId2NewId(HashMap<String, String> impoterIdMap) {
//        // bpmn id to pn id
//        HashMap<String, String> bpmnIdToDPNId = new HashMap<>();
//        for (Map.Entry<BPMNNode, Set<PetrinetNode>> entry : super.getNodeMap().entrySet()) {
//            if(entry.getValue().size() == 1) {
//                PetrinetNode first = entry.getValue().iterator().next();
//                bpmnIdToDPNId.put(entry.getKey().getId().toString(), first.getId().toString());
//            }
//            else System.out.println("doh2");
//        }
//
//        // initial bpmn id to imported id -> initial bpmn id to pn id
//        HashMap<String, String> id2NewId2 = new HashMap<>(impoterIdMap);
//        id2NewId2.replaceAll((k, v) -> bpmnIdToDPNId.get(v));
//
////        bpmnToDpnMapping.nextStep(bpmnIdToDPNId);
//
//        // pn id to dpn id
//        HashMap<String, String> oldDpnId2NewId = new HashMap<>();
//        for(Map.Entry<Transition, Transition> entry : transitionsMap.entrySet()) {
//            oldDpnId2NewId.put(entry.getKey().getId().toString(), entry.getValue().getId().toString());
//        }
//
//        HashMap<String, String> id2NewId = new HashMap<>();
//        id2NewId2.replaceAll((k, v) -> oldDpnId2NewId.get(v));
//
//        return id2NewId;
//    }
}

