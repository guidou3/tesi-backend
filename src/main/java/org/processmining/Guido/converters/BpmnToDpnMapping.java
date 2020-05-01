package org.processmining.Guido.converters;

import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BpmnToDpnMapping {
    HashMap<String, String> bpmnToDpn;
    HashMap<String, BPMNNode> dpnToBpmn;
    HashMap<Transition, BPMNNode> dpnToBpmnFinal;

    public BpmnToDpnMapping() {
        bpmnToDpn = new HashMap<>();
        dpnToBpmn = new HashMap<>();
    }

    public void initializeFirst(HashMap<String, BPMNNode> id2node) {
        for (Map.Entry<String, BPMNNode> entry : id2node.entrySet())
            bpmnToDpn.put(entry.getKey(), entry.getValue().getId().toString());

//        System.out.println("bpmnToDpn\tsize: " + bpmnToDpn.size());
//        System.out.println("dpnToBpmn\tsize: " + dpnToBpmn.size());
//        System.out.println("map\tsize: " + id2node.size() + "\n");
    }

    public void initializeSecond(Map<BPMNNode, Set<PetrinetNode>> map) {
        Map<String, String> tempMap = new HashMap<>();
        for(Map.Entry<BPMNNode, Set<PetrinetNode>> entry : map.entrySet()) {
            if(entry.getValue().size() > 1)  {
                if(entry.getKey() instanceof Event) {
                    Event e = (Event) entry.getKey();
                    if(e.getEventTrigger() != Event.EventTrigger.NONE) {
                        for(PetrinetNode item : entry.getValue()) {
                            if (item instanceof Transition || item instanceof DataElement) {
                                dpnToBpmn.put(item.getId().toString(), entry.getKey());
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
                dpnToBpmn.put(id, entry.getKey());
                tempMap.put(entry.getKey().getId().toString(), id);
            }
        }
        bpmnToDpn.replaceAll((k, v) -> tempMap.get(v));

//        System.out.println("bpmnToDpn\tsize: " + bpmnToDpn.size());
//        System.out.println("dpnToBpmn\tsize: " + dpnToBpmn.size());
//        System.out.println("map\tsize: " + map.size() + "\n");
    }

    public void nextStep(HashMap<String, String> map) {
//        bpmnToDpn.replaceAll((k, v) -> map.get(v));
        HashMap<String, String> bpmnToDpn2 = new HashMap<>();
        for(Map.Entry<String, String> entry : bpmnToDpn.entrySet()) {
            if(map.get(entry.getValue()) != null)
                bpmnToDpn2.put(entry.getKey(), map.get(entry.getValue()));
        }
        bpmnToDpn = bpmnToDpn2;

        HashMap<String, BPMNNode> dpnToBpmn2 = new HashMap<>();
        for(Map.Entry<String, String> entry : map.entrySet()) {
            if(dpnToBpmn.get(entry.getKey()) != null)
                dpnToBpmn2.put(entry.getValue(), dpnToBpmn.get(entry.getKey()));
        }
        dpnToBpmn = dpnToBpmn2;

//        System.out.println("Next step");
//        System.out.println("bpmnToDpn\tsize: " + bpmnToDpn.size());
//        System.out.println("dpnToBpmn\tsize: " + dpnToBpmn.size());
//        System.out.println("map\tsize: " + map.size() + "\n");

    }

    public void TransitionToBPMNNode(HashMap<Transition, String> map) {
        dpnToBpmnFinal = new HashMap<>();
        for(Map.Entry<Transition, String> entry : map.entrySet()) {
            if(dpnToBpmn.get(entry.getValue()) != null)
                dpnToBpmnFinal.put(entry.getKey(), dpnToBpmn.get(entry.getValue()));
        }


//        System.out.println("Final");
//        System.out.println("dpnToBpmnFinal\tsize: " + dpnToBpmnFinal.size());
//        System.out.println("map\tsize: " + map.size() + "\n");
    }

    public HashMap<String, String> getBpmnToDpn() {
        return bpmnToDpn;
    }

    public HashMap<Transition, BPMNNode> getDpnToBpmn() {
        return dpnToBpmnFinal;
    }
}
