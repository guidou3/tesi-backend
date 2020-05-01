//package DataAwareConformanceChecking;
//
//// dataAwareReplayer package org.processmining.plugins.DataConformance.GUI;
//
//import org.processmining.Guido.utils.UI.ComboBox;
//import org.processmining.framework.util.LevenshteinDistance;
//
//import java.util.*;
//
//public class VariableMappingImporter<T,V> {
//    Map<T, ComboBox> comboBoxes=new HashMap<T,ComboBox>();
//    private final static float lowDistFact=0.3F;
//    private final static float highDistFact=0.7F;
//
//    public VariableMappingImporter(Collection<T> coll1, Collection<V> coll2, Boolean ask)
//    {
//        System.out.println("Please provide the mapping for the following:");
//        Set<Object> internalSet;
//
//        try {
//            internalSet=new TreeSet<Object>();
//            internalSet.addAll(coll2);
//            internalSet.add("");
//        }
//        catch(ClassCastException err) {
//            internalSet=new HashSet<Object>();
//            internalSet.addAll(coll2);
//            internalSet.add("");
//        }
//
//        for(T node : new TreeSet<T>(coll1)) {
//            ComboBox component =new ComboBox(node.toString(), internalSet.toArray());
//            Object[] retValue = getBestMatch(node.toString(), internalSet);
//
//            if (((Integer)retValue[1]) < highDistFact*node.toString().length()) {
//                component.setSelectedItem(retValue[0]);
//                if (((Integer)retValue[1]) > lowDistFact*node.toString().length()) {
//                    // TODO: here it should be put in resalt this combobox
//                    component.print();
//                }
//
//            }
//            if(ask) component.print();
//            comboBoxes.put(node, component);
//        }
//    }
//
//    private Object[] getBestMatch(String string, Set<Object> internalSet) {
//        LevenshteinDistance ld=new LevenshteinDistance();
//        int minDist= Integer.MAX_VALUE;
//        Object minObject=null;
//        for(Object internal : internalSet)
//        {
//            int ldist;
//            if(internal.toString().length() == 0)
//                ldist=string.length();
//            else
//                ldist=ld.getLevenshteinDistanceLinearSpace(string, internal.toString());
//
//            if (ldist<minDist)
//            {
//                minDist=ldist;
//                minObject=internal;
//            }
//        }
//        return new Object[]{minObject,minDist};
//    }
//
//    @SuppressWarnings("unchecked")
//    public Map<T,V> getMapping(boolean includeNotMapped)
//    {
//        Map<T,V> retValue=new HashMap<T,V>();
//        for(T node : comboBoxes.keySet())
//        {
//            Object selection=comboBoxes.get(node).getSelectedItem();
//            if (selection.equals(""))
//            {if (includeNotMapped) retValue.put(node, null);}
//            else
//                retValue.put(node, (V) selection);
//        }
//        return retValue;
//    }
//
//}
