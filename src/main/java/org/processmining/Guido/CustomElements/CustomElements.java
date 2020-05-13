package org.processmining.Guido.CustomElements;

import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomElements implements Serializable {
    ArrayList<TimeDistance> timeDistances;
    ArrayList<Consequence> consequences;
    ArrayList<ConsequenceTimed> consequencesTimed;
    ArrayList<TaskDuration> taskDurations;
    ArrayList<TimeInstance> timeInstances;
    ArrayList<Resource> resources;
    ArrayList<Role> roles;
    ArrayList<Group> groups;

    Map<String, Constraint> idToConstraint;

//    public interface SortedCustomElements {
//        public ArrayList<TimeDistance> getTimeDistances();
//        public ArrayList<Consequence> getConsequences();
//        public ArrayList<ConsequenceTimed> getConsequencesTimed();
//        public ArrayList<TaskDuration> getTaskDurations();
//        public ArrayList<TimeInstance> getTimeInstances();
//        public ArrayList<Resource> getResources();
//        public ArrayList<Role> getRoles();
//        public ArrayList<Group> getGroups();
//
//    }

//    HashMap<String, CustomShape> id2shape;

    public void initialize(DataPetriNet dataPetriNet, Map<String, String> id2NewId) {

        HashMap<String, Transition> id2transition = new HashMap<>();
        idToConstraint = new HashMap<>();

        for (Transition transition : dataPetriNet.getTransitions())
            id2transition.put(transition.getId().toString(), transition);

        for (Consequence consequence : consequences) {
            consequence.initialize(id2transition, id2NewId);
            idToConstraint.put(consequence.id, consequence);
        }

        for (ConsequenceTimed consequence : consequencesTimed) {
            consequence.initialize(id2transition, id2NewId);
            idToConstraint.put(consequence.id, consequence);
        }

        for (TimeDistance timeDistance : timeDistances) {
            timeDistance.initialize(id2transition, id2NewId);
            idToConstraint.put(timeDistance.id, timeDistance);
        }

        for (TaskDuration taskDuration : taskDurations) {
            taskDuration.initialize(id2transition, id2NewId);
            idToConstraint.put(taskDuration.id, taskDuration);
        }

        for (TimeInstance timeInstance : timeInstances) {
            timeInstance.initialize(id2transition, id2NewId);
            idToConstraint.put(timeInstance.id, timeInstance);
        }

        for (Resource resource : resources) {
            resource.initialize(id2transition, id2NewId);
            idToConstraint.put(resource.id, resource);
        }

        for (Role role : roles) {
            role.initialize(id2transition, id2NewId);
            idToConstraint.put(role.id, role);
        }

        for (Group group : groups) {
            group.initialize(id2transition, id2NewId);
            idToConstraint.put(group.id, group);
        }
    }

    public ArrayList<TimeDistance> getTimeDistances() {
        return timeDistances;
    }
    public ArrayList<Consequence> getConsequences() {
        return consequences;
    }
    public ArrayList<ConsequenceTimed> getConsequencesTimed() {
        return consequencesTimed;
    }
    public ArrayList<TaskDuration> getTaskDurations() {
        return taskDurations;
    }
    public ArrayList<TimeInstance> getTimeInstances() {
        return timeInstances;
    }
    public ArrayList<Resource> getResources() {
        return resources;
    }
    public ArrayList<Role> getRoles() {
        return roles;
    }
    public ArrayList<Group> getGroups() {
        return groups;
    }

    public Constraint getConstraint(String id) {
        return idToConstraint.get(id);
    }
}
