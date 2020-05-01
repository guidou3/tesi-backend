package org.processmining.Guido.converters;

import org.processmining.Guido.InOut.VariableBoundsEntry;
import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.DataObject;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;

import java.util.*;

public class CloneDPNWithNewData {
    private DataPetriNet original_dpn;
    private DataPetriNet dataPetriNet;

    // Data objects map
    private Map<DataElement, DataElement> dataElementMap = new HashMap<DataElement, DataElement>();
    private HashMap<Transition, Transition> transitionsMap;

    public CloneDPNWithNewData(DataPetriNet dpn, DPNDataSettings dataSettings, Set<DataElement> set) {

        original_dpn = dpn;
        transitionsMap = new HashMap<>();

        dataPetriNet = new PetriNetWithData(original_dpn.getLabel());

        Map<Place, Place> placesMap = new HashMap();
        for(Place place : original_dpn.getPlaces()) {
            Place newPlace = dataPetriNet.addPlace(place.getLabel());
            placesMap.put(place, newPlace);
        }

        for(Transition transition : original_dpn.getTransitions()) {
            Transition newTransition = dataPetriNet.addTransition(transition.getLabel());
            newTransition.setInvisible(transition.isInvisible());
            transitionsMap.put(transition, newTransition);
        }

        Map<String, DataElement> map = new HashMap<>();

        for(DataElement entry : set) {
            map.put(entry.getVarName(), entry);
        }

        for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : original_dpn.getEdges()) {
            if ((edge.getSource() instanceof Transition) && (edge.getTarget() instanceof Place))
                dataPetriNet.addArc(transitionsMap.get(edge.getSource()), placesMap.get(edge.getTarget()));
            else if ((edge.getSource() instanceof Place) && (edge.getTarget() instanceof Transition))
                dataPetriNet.addArc(placesMap.get(edge.getSource()), transitionsMap.get(edge.getTarget()));
        }

        for (DataElement de : original_dpn.getVariables()) {
            DataElement entry = map.get(de.getVarName());
            DataElement dataElement = dataPetriNet.addVariable(entry.getVarName(), entry.getType(),
                    entry.getMinValue(), entry.getMaxValue());
            dataElementMap.put(de, dataElement);
        }

        for (DPNDataSettings.PairTE pair : dataSettings.getReadOperations()) {
            dataPetriNet.assignReadOperation(transitionsMap.get(pair.getTransition()), dataElementMap.get(pair.getDataElement()));
        }

        for (DPNDataSettings.PairTE pair : dataSettings.getWriteOperations()) {
            dataPetriNet.assignWriteOperation(transitionsMap.get(pair.getTransition()), dataElementMap.get(pair.getDataElement()));
        }

        for (DPNDataSettings.PairTS pair : dataSettings.getGuardsList()) {
            try {
                dataPetriNet.setGuardFromString(transitionsMap.get(pair.getTransition()), pair.getString());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public DataPetriNet getDataPetriNet() {
        return dataPetriNet;
    }
}
