package org.processmining.Guido.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.*;

public class CyclesFinder {
    private DataPetriNet dpn;
    private Multimap<PetrinetNode, List<PetrinetNode>> nodeToFollowing;

    private Multimap<PetrinetNode, Integer> nodeToCycleId;
    private Map<Integer, List<Transition>> idToCycle;
    private Map<Integer, Transition> idToCycleFirst;

    private Collection<List<PetrinetNode>> cycles;

    private int countCycles;

    public CyclesFinder(DataPetriNet dpn) {
        this.dpn = dpn;
        nodeToFollowing = HashMultimap.create();

        nodeToCycleId = HashMultimap.create();
        idToCycle = new HashMap<>();
        idToCycleFirst = new HashMap<>();

        countCycles = 0;
    }

    public void findCycles() {
        List<Place> mark = new ArrayList<>(((PetriNetWithData) dpn).getInitialMarking());

        for(PetrinetNode node : mark)
            findCycles(node, new ArrayList<>());
    }

    private void findCycles(PetrinetNode current, List<PetrinetNode> path) {
        PetrinetNode first = current;
        List<PetrinetNode> nextNodes = findNextNodes(current);
        do {
            if(path.contains(current)) {
                List<PetrinetNode> cycle = path.subList(path.indexOf(current), path.size()-1);

                addCycle(cycle);
                return;
                // taglia path dalla prima posizione di current e inserisci gli id in idToCycles
                // bisogna considerare che in questa maniera si trova l'inizio e la fine del ciclo (dopo bisogna creare una multimappa che collega ogni node ai cicli ai quali appartiene
                // interrompi qui l'esecuzione
            }
            else if(previous(current) > 1 && nodeToFollowing.get(current) != null) { // XOR join
                Collection<List<PetrinetNode>> paths = nodeToFollowing.get(current);
                for(List<PetrinetNode> following : paths) {
                    for(PetrinetNode node : path) {
                        if(following.contains(node)) {
                            List<PetrinetNode> cycle = path.subList(path.indexOf(node), path.size()-1);
                            cycle.addAll(following.subList(0, following.indexOf(node)-1));

                            addCycle(cycle);
                        }
                    }
                }


            }
            else
                path.add(current);
            current = nextNodes.get(0);
            nextNodes = findNextNodes(current);
        } while (nextNodes.size() == 1);

        if(nextNodes.size() == 0) {
            int size = path.size() -1;
            for (int i=0; i<size; i++)
                nodeToFollowing.put(path.get(i), path.subList(i+1, size));
        }

        for(PetrinetNode node : nextNodes)
            findCycles(node, new ArrayList<>(path));

    }

    private void addCycle(List<PetrinetNode> cycle) {
        List<Transition> newCycle = new ArrayList<>();
        for(PetrinetNode node : cycle)
            if(node instanceof Transition)
                newCycle.add((Transition) node);


        Collection<Integer> ids = nodeToCycleId.get(cycle.get(0));
        for(int id : ids) {
            List<Transition> oldCycle = idToCycle.get(id);
            if(oldCycle.size() == newCycle.size()) {
                boolean same = true;
                int index = oldCycle.indexOf(newCycle.get(0));
                for(Transition t : newCycle)
                    if(oldCycle.get(index).equals(t))
                        index = (index < oldCycle.size() -1) ? index +1 : 0;
                    else {
                        same = false;
                        break;
                    }

                if(same) return;
            }
        }

        for (PetrinetNode node1 : cycle)
            nodeToCycleId.put(node1, countCycles);

        idToCycle.put(countCycles, newCycle);
        idToCycleFirst.put(countCycles, newCycle.get(0));
        countCycles++;
    }

    private List<PetrinetNode> findNextNodes(PetrinetNode node) {
        List<PetrinetNode> list = new ArrayList<>();

        for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : dpn.getOutEdges(node))
            if(arc.getTarget() instanceof Place || arc.getTarget() instanceof Transition)
                list.add(arc.getTarget());

        return list;
    }

    private int previous(PetrinetNode node) {
        int prev = 0;
        for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : dpn.getInEdges(node))
            if(arc.getTarget() instanceof Place || arc.getTarget() instanceof Transition)
                prev++;

        return prev;
    }

//    public void removeCopies() {
//        Map<String, List<PetrinetNode>> cyclesMap = new HashMap<>();
//        for(List<PetrinetNode> cycle : idToCycle.values()) {
//            StringBuilder ids = new StringBuilder();
//            for (PetrinetNode node : cycle)
//                if(node instanceof Transition)
//                    ids.append(node.getId().toString());
//
//            cyclesMap.putIfAbsent(ids.toString(), cycle);
//        }
//
//        cycles = cyclesMap.values();
//    }

    // nuova versione: nella versione attuale è considerato un solo punto di ingresso, ma ciò non è necessariamente vero per tutti i casi.
    // modifica la versione attuale per considerare tutti i possibili punti di ingresso
}
