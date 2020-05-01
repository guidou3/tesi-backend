package org.processmining.Guido;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.Guido.CustomElements.Constraint;
import org.processmining.Guido.CustomElements.CustomElements;
import org.processmining.datapetrinets.exception.NonExistingVariableException;
import org.processmining.datapetrinets.expression.GuardExpression;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.models.semantics.petrinet.Marking;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class DataPetriNetWithCustomElements extends PetriNetWithData {
    public static class PairTransitionDataElement {
        private Transition transition;
        private DataElement dataElement;

        public PairTransitionDataElement(Transition transition, DataElement dataElement) {
            this.transition = transition;
            this.dataElement = dataElement;
        }

        public Transition getTransition() {
            return transition;
        }

        public DataElement getDataElement() {
            return dataElement;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PairTransitionDataElement pairTE = (PairTransitionDataElement) o;
            return Objects.equals(transition, pairTE.transition) &&
                    Objects.equals(dataElement, pairTE.dataElement);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transition, dataElement);
        }
    }

    private List<PairTransitionDataElement> readOperations;
    private List<PairTransitionDataElement> writeOperations;
    private Map<Transition, String> guardsMap;
    private Map<String, Class<?>> types;

    private Map<Transition, Collection<String>> logPositions;

    private Transition last;

    private boolean isResetting = false;

    private Map<String, String> customVariableMapping;

    private CustomElements customElements;

    public DataPetriNetWithCustomElements(String name) {
        super(name);
        readOperations = new ArrayList<>();
        writeOperations = new ArrayList<>();
        guardsMap = new HashMap<>();
        types = new HashMap<>();
    }

    @Override
    public void assignReadOperation(Transition t, DataElement v) {
        super.assignReadOperation(t, v);
        readOperations.add(new PairTransitionDataElement(t, v));
    }

    @Override
    public void assignWriteOperation(Transition t, DataElement v) {
        super.assignWriteOperation(t, v);
        writeOperations.add(new PairTransitionDataElement(t, v));
    }

    @Override
    public void setGuard(Transition t, String guard) throws ParseException {
        super.setGuard(t, guard);
        if(!isResetting)
            guardsMap.put(t, guard);
    }

    @Override
    public void setGuardFromString(Transition transition, String guardAsString)
            throws org.processmining.datapetrinets.expression.syntax.ParseException, NonExistingVariableException {
        super.setGuardFromString(transition, guardAsString);
        if(!isResetting)
            guardsMap.put(transition, guardAsString);
    }

    @Override
    public void setGuard(Transition transition, GuardExpression guard) throws NonExistingVariableException {
        super.setGuard(transition, guard);
        if(!isResetting)
            guardsMap.put(transition, ((PNWDTransition) transition).getGuardAsString());
    }

    public void fixDataElementsTypes(Set<DataElement> dataElements) {
        removeAllVariables();
        Map<String, DataElement> nameToDataElement = new HashMap<>();

        for(DataElement d : dataElements)
            nameToDataElement.put(d.getVarName(), addVariable(d.getVarName(), d.getType(), d.getMinValue(), d.getMaxValue()));


        for(PairTransitionDataElement pair : readOperations) {
            super.assignReadOperation(pair.getTransition(), nameToDataElement.get(pair.getDataElement().getVarName()));
        }

        for(PairTransitionDataElement pair : writeOperations) {
            super.assignWriteOperation(pair.getTransition(), nameToDataElement.get(pair.getDataElement().getVarName()));
        }

        for(Map.Entry<Transition, String> entry : guardsMap.entrySet()) {
            try {
                super.setGuardFromString(entry.getKey(), entry.getValue());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        print();
    }

    public void removeAllVariables() {
        isResetting = true;
        super.removeAllVariables();
        isResetting = false;
    }

    public void print() {
        System.out.println("\nRead operations: ("+ readOperations.size()  +")");
        for (PairTransitionDataElement item : readOperations) {
            System.out.println(item.getTransition().getLabel() + "\t\t" + item.getDataElement().getVarName());
        }
        System.out.println("\nWrite operations: ("+ writeOperations.size()  +")");
        for (PairTransitionDataElement item : writeOperations) {
            System.out.println(item.getTransition().getLabel() + "\t\t" + item.getDataElement().getVarName());
        }
        System.out.println("\nGuards: ("+ guardsMap.size()  +")");
        for (Map.Entry<Transition, String> item : guardsMap.entrySet()) {
            System.out.println(item.getKey().getLabel() + "\t\t" + item.getKey().getId() + "\t\t" + item.getValue());
        }
    }

    public Class<?> getType(String name) {
        return types.get(name);
    }

    public void addVariableType(String name, Class<?> type) {
        types.put(name, type);
    }

    public void setLastTransition(Marking finalMarking) {
        List<Place> mark = new ArrayList<>(finalMarking);

        for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : getInEdges(mark.get(0)))
            if(arc.getSource() instanceof Transition) last = (Transition) arc.getSource();
    }

    public void setLastTransition(Transition last) {
        this.last = last;
    }

    public Transition getLastTransition() {
        return last;
    }

    public void setTransitionLogPositions(Map<Transition, Collection<String>> logPositions) {
        this.logPositions = logPositions;
    }

    public Map<Transition, Collection<String>> getLogPositions() {
        return logPositions;
    }

    public Constraint getCustomElement(String id) {
        return customElements.getConstraint(id);
    }

    public void setCustomVariableMapping(Map<String, String> customVariableMapping) {
        this.customVariableMapping = customVariableMapping;
    }

    public Map<String, String> getCustomVariableMapping() {
        return customVariableMapping;
    }
}
