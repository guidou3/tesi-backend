package org.processmining.Guido.converters;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;

import java.util.*;
import java.util.stream.Collectors;

public class ActivityTransitionMapping {
    Map<String, String> bpmnToDpn;
    Multimap<String, String> dpnToBpmn;

    public ActivityTransitionMapping() {
        bpmnToDpn = new HashMap<>();
        dpnToBpmn = HashMultimap.create();
    }

    // initialize bpmnToDpn map
    public void firstStep(Map<String, String> id2node) {
        bpmnToDpn = new HashMap<>(id2node);

        for(Map.Entry<String, String> entry : bpmnToDpn.entrySet())
            dpnToBpmn.put(entry.getValue(), entry.getKey());

    }

    //initialize dpnToBpmn and update bpmnToDpn
    public void secondStep(Map<BPMNNode, Set<PetrinetNode>> map) {
        Map<String, String> tempMap = new HashMap<>();
        Map<String, String> tempMap1 = new HashMap<>();

        for(Map.Entry<BPMNNode, Set<PetrinetNode>> entry : map.entrySet()) {
            if(entry.getValue().size() > 1)  {
                if(entry.getKey() instanceof Event) {
                    Event e = (Event) entry.getKey();
                    if(e.getEventTrigger() != Event.EventTrigger.NONE) {
                        for(PetrinetNode item : entry.getValue()) {
                            if (item instanceof Transition || item instanceof DataElement) {
                                tempMap1.put(item.getId().toString(), entry.getKey().getId().toString());
                                // non serve inserire questi dati nella mappa bpmn -> dpn in quanto non esistono vincoli
                                // che si "attaccano" a eventi di questo tipo
                            }
                        }
                    }
                }
                else if(!(entry.getKey() instanceof Gateway)) {
                    System.out.println(entry.getKey().getClass().toString());
                }
                else {
                    for(PetrinetNode item : entry.getValue()) {
                        if (item instanceof Transition) {
                            tempMap.put(entry.getKey().getId().toString(), item.getId().toString());
                        }
                    }
                }
//                    dpnToBpmn.put(entry.getKey(), entry.getValue());

            }
            else {
                String id = entry.getValue().iterator().next().getId().toString();
                tempMap1.put(id, entry.getKey().getId().toString());
                tempMap.put(entry.getKey().getId().toString(), id);
            }
        }
        bpmnToDpn.replaceAll((k, v) -> tempMap.get(v));
        Multimap<String, String> tempMultiMap = HashMultimap.create();
        tempMap1.forEach((k,v) -> {
            tempMultiMap.putAll(k, dpnToBpmn.get(v));
        });
        dpnToBpmn = tempMultiMap;
    }

    public void thirdStep(Map<String, String> map) {
//        bpmnToDpn.replaceAll((k, v) -> map.get(v));
        HashMap<String, String> bpmnToDpn2 = new HashMap<>();
        for(Map.Entry<String, String> entry : bpmnToDpn.entrySet()) {
            if(map.get(entry.getValue()) != null)
                bpmnToDpn2.put(entry.getKey(), map.get(entry.getValue()));
        }
        bpmnToDpn = bpmnToDpn2;

        Multimap<String, String> tempMultiMap = HashMultimap.create();
        map.forEach((k,v) -> {
            tempMultiMap.putAll(v, dpnToBpmn.get(k));
        });
        dpnToBpmn = tempMultiMap;

//        System.out.println("Next step");
//        System.out.println("bpmnToDpn\tsize: " + bpmnToDpn.size());
//        System.out.println("dpnToBpmn\tsize: " + dpnToBpmn.size());
//        System.out.println("map\tsize: " + map.size() + "\n");

    }

    public void fourthStep(Map<String, String> map) {
        Multimap<String, String> tempMultiMap = HashMultimap.create();
        map.forEach((k,v) -> {
            tempMultiMap.putAll(k, dpnToBpmn.get(v));
        });
        dpnToBpmn = tempMultiMap;
    }

//    public void TransitionToBPMNNode(HashMap<Transition, String> map) {
//        dpnToBpmnFinal = new HashMap<>();
//        for(Map.Entry<Transition, String> entry : map.entrySet()) {
//            if(dpnToBpmn.get(entry.getValue()) != null)
//                dpnToBpmnFinal.put(entry.getKey(), dpnToBpmn.get(entry.getValue()));
//        }
//
//
////        System.out.println("Final");
////        System.out.println("dpnToBpmnFinal\tsize: " + dpnToBpmnFinal.size());
////        System.out.println("map\tsize: " + map.size() + "\n");
//    }

    public Map<String, String> getBpmnToDpn() {
        return bpmnToDpn;
    }

    public Map<String, Collection<String>> getDpnToBpmn() {
        return dpnToBpmn.asMap();
    }
}
