package org.processmining.Guido.converters;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;

import java.util.*;

public class DPNDataSettings {
    public static class PairTE {
        private Transition transition;
        private DataElement dataElement;

        public PairTE(Transition transition, DataElement dataElement) {
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
            PairTE pairTE = (PairTE) o;
            return Objects.equals(transition, pairTE.transition) &&
                    Objects.equals(dataElement, pairTE.dataElement);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transition, dataElement);
        }
    }

    public static class PairTS {
        private Transition transition;
        private String string;

        public PairTS(Transition transition, String string) {
            this.transition = transition;
            this.string = string;
        }

        public Transition getTransition() {
            return transition;
        }

        public String getString() {
            return string;
        }
    }

    private List<PairTE> readOperations;
    private List<PairTE> writeOperations;
    private List<PairTS> guardsList;
    private Map<String, Class<?>> types;

    public DPNDataSettings() {
        readOperations = new ArrayList<>();
        writeOperations = new ArrayList<>();
        guardsList = new ArrayList<>();
        types = new HashMap<>();
    }

    public List<PairTE> getReadOperations() {
        return readOperations;
    }

    public List<PairTE> getWriteOperations() {
        return writeOperations;
    }

    public List<PairTS> getGuardsList() {
        return guardsList;
    }

    public Class<?> getType(String name) {
        return types.get(name);
    }

    public void addReadOperation(Transition t, DataElement de) {
        readOperations.add(new PairTE(t, de));
    }

    public void addWriteOperation(Transition t, DataElement de) {
        writeOperations.add(new PairTE(t, de));
    }

    public void addGuard(Transition t, String s) {
        guardsList.add(new PairTS(t, s));
    }

    public void addVariableType(String name, Class<?> type) {
        types.put(name, type);
    }

    public int size() {
        return readOperations.size() + writeOperations.size() + guardsList.size();
    }

    public void print() {
        System.out.println("\nRead operations: ("+ readOperations.size()  +")");
        for (PairTE item : readOperations) {
            System.out.println(item.getTransition().getLabel() + "\t\t" + item.getDataElement().getVarName());
        }
        System.out.println("\nWrite operations: ("+ writeOperations.size()  +")");
        for (PairTE item : writeOperations) {
            System.out.println(item.getTransition().getLabel() + "\t\t" + item.getDataElement().getVarName());
        }
        System.out.println("\nGuards: ("+ guardsList.size()  +")");
        for (PairTS item : guardsList) {
            System.out.println(item.getTransition().getLabel() + "\t\t" + item.getString());
        }
    }

    public void reset() {
        readOperations.clear();
        writeOperations.clear();
        guardsList.clear();
    }
}
