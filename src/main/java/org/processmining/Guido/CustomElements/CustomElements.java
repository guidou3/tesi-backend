package org.processmining.Guido.CustomElements;

import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomElements implements Serializable {
    Definitions definitions;
    List<DiagramItem> diagram;
    Map<String, List<String>> idMap;

    public CustomElements(Definitions definitions, List<DiagramItem> diagram, Map<String, List<String>> idMap) {
        this.definitions = definitions;
        this.diagram = diagram;
        this.idMap = idMap;
    }

    public void initialize(DataPetriNet dataPetriNet, Map<String, String> id2NewId) {
        definitions.initialize(dataPetriNet, id2NewId);
    }

    public ArrayList<TimeDistance> getTimeDistances() {
        return definitions.getTimeDistances();
    }
    public ArrayList<Consequence> getConsequences() {
        return definitions.getConsequences();
    }
    public ArrayList<ConsequenceTimed> getConsequencesTimed() {
        return definitions.getConsequencesTimed();
    }
    public ArrayList<TaskDuration> getTaskDurations() {
        return definitions.getTaskDurations();
    }
    public ArrayList<TimeInstance> getTimeInstances() {
        return definitions.getTimeInstances();
    }
    public ArrayList<Resource> getResources() {
        return definitions.getResources();
    }
    public ArrayList<Role> getRoles() {
        return definitions.getRoles();
    }
    public ArrayList<Group> getGroups() {
        return definitions.getGroups();
    }

    public Constraint getConstraint(String id) {
        return definitions.getConstraint(id);
    }

    public List<DiagramItem> getDiagram() {
        return diagram;
    }

    public Map<String, List<String>> getIdMap() {
        return idMap;
    }

    public CustomElements clone() {
        return new CustomElements(new Definitions(definitions), diagram, idMap);
    }

    public void setColors(Map<String, String> map) {
        for(DiagramItem item : diagram) {
            item.setColor(map.get(item.getId()));
        }
    }
}
