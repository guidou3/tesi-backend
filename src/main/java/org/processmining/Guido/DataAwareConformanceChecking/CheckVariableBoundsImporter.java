package org.processmining.Guido.DataAwareConformanceChecking;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.deckfour.xes.model.*;
import org.processmining.Guido.InOut.VariableBoundsEntry;
import org.processmining.Guido.converters.DPNDataSettings;
import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author F. Mannhardt
 *
 */
public class CheckVariableBoundsImporter {

    private static Map<Class<?>, String> classToString;
    static {
        classToString = new HashMap<>();
        classToString.put(String.class, "String");
        classToString.put(Integer.class, "Integer");
        classToString.put(Long.class, "Long");
        classToString.put(Float.class, "Float");
        classToString.put(Date.class, "Date");
        classToString.put(Boolean.class, "Boolean");
        classToString.put(Double.class, "Double");
    }

    private static final int ROUND_TO = 1000;

    private List<String> columnNames = Arrays.asList("Variable Name", "Type", "Minimum Value", "Maximum Value");
    private List<VariableBoundsEntry> list;

    public CheckVariableBoundsImporter(DataPetriNet inputNet, XLog log, Map<String, String> variableMapping) {


        DataElement[] variables = inputNet.getVariables().toArray(new DataElement[0]);
        Arrays.sort(variables, Comparator.comparing(DataElement::getVarName));

        Map<DataElement, Number> minValueMap = new HashMap<>();
        Map<DataElement, Number> maxValueMap = new HashMap<>();
        Map<DataElement, Class<?>> typeMap = new HashMap<>();

        for (XTrace trace: log) {
            for (XEvent event: trace) {

                XAttributeMap attributes = event.getAttributes();

                for(DataElement elem : variables) {

                    XAttribute attribute = attributes.get(variableMapping.get(elem.getVarName()));
                    Number minValue = minValueMap.get(elem);
                    Number maxValue = maxValueMap.get(elem);

                    if (attribute != null) {
                        if (attribute instanceof XAttributeDiscrete) {
                            XAttributeDiscrete dAttr = (XAttributeDiscrete) attribute;
                            minValueMap.put(elem, minValue == null ? dAttr.getValue() : Math.min(minValue.longValue(), dAttr.getValue()));
                            maxValueMap.put(elem, maxValue == null ? dAttr.getValue() : Math.max(maxValue.longValue(), dAttr.getValue()));
                            if(elem.getType() == String.class)
                                typeMap.put(elem, Long.class);
                        } else if (attribute instanceof XAttributeContinuous) {
                            XAttributeContinuous cAttr = (XAttributeContinuous) attribute;
                            minValueMap.put(elem, minValue == null ? cAttr.getValue() : Math.min(minValue.doubleValue(), cAttr.getValue()));
                            maxValueMap.put(elem, maxValue == null ? cAttr.getValue() : Math.max(maxValue.doubleValue(), cAttr.getValue()));
                            if(elem.getType() == String.class)
                                typeMap.put(elem, Double.class);
                        } else if (attribute instanceof XAttributeTimestamp) {
                            XAttributeTimestamp tAttr = (XAttributeTimestamp) attribute;
                            minValueMap.put(elem, minValue == null ? tAttr.getValueMillis() : Math.min(minValue.doubleValue(), tAttr.getValueMillis()));
                            maxValueMap.put(elem, maxValue == null ? tAttr.getValueMillis() : Math.max(maxValue.doubleValue(), tAttr.getValueMillis()));
                            if(elem.getType() == String.class)
                                typeMap.put(elem, Date.class);
                        }
                    }
                }
            }
        }



        list = new ArrayList<>();
        for(DataElement elem : variables) {
            // TODO: create a new list of variables with the right type
//            setVariableType(elem, minValueMap.get(elem));

            Number minValue = determineMinValue(elem, minValueMap, typeMap);
            Number maxValue = determineMaxValue(elem, maxValueMap, typeMap);

            Class<?> type = elem.getType();
            if(type == String.class) {
                if(minValue != null)
                    type = minValue.getClass();
                else if(maxValue != null)
                    type = maxValue.getClass();
            }
            
            if (minValue != null && minValue.equals(maxValue))
                maxValue = minValue.longValue() + ROUND_TO;

            if(classToString.get(type) == null) {
                classToString.put(type, type.getSimpleName());
                VariableBoundsEntry.addClass(type.getSimpleName(), type);
            }

//            assert minValue != null;
            list.add(new VariableBoundsEntry(elem.getVarName(), classToString.get(type), minValue, maxValue));
        }
    }

    public CheckVariableBoundsImporter(List<VariableBoundsEntry> list) {
        this.list = list;
    }

    private Number determineMaxValue(DataElement elem, Map<DataElement, Number> maxValueMap,
                                     Map<DataElement, Class<?>> typeMap) {
        if (elem.getMaxValue() != null && elem.getMaxValue() instanceof Number) {
            return (Number) elem.getMaxValue();
        }
        else {
            return numCast(elem, maxValueMap.get(elem), typeMap);
        }
    }

    private Number determineMinValue(DataElement elem, Map<DataElement, Number> minValueMap,
                                     Map<DataElement, Class<?>> typeMap) {
        if (elem.getMinValue() != null && elem.getMinValue() instanceof Number) {
            return (Number) elem.getMinValue();
        }
        else
            return numCast(elem, minValueMap.get(elem), typeMap);
    }

    private Number numCast(DataElement elem, Number num, Map<DataElement, Class<?>> typeMap) {
        if (num != null) {
            double roundedValue = Math.floor((num.doubleValue()/ROUND_TO))*ROUND_TO;
            Class<?> type;
            if(typeMap.get(elem) != null && !elem.getType().equals(typeMap.get(elem)))
                type = typeMap.get(elem);
            else
                type = elem.getType();
            if (type == Long.class)
                return (long) roundedValue;
            else if (type == Integer.class)
                return (int) roundedValue;
            else if (type == Double.class)
                return roundedValue;
            else if (type == Float.class)
                return (float) roundedValue;
            else if (type == Date.class || type == SimpleDateFormat.class)
                return (long) roundedValue;
            else
                return null;
        }
        else
            return null;
    }

    public Set<DataElement> getDataElements() {
        Set<DataElement> retValue = new HashSet<>();
        for(VariableBoundsEntry el : list) {
            DataElement element;
            String varName = el.getVariable();
            Class<?> varType = el.getType();

            String minValueString = el.getMinimum();
            if (minValueString != null && minValueString.equals(""))
                minValueString = null;

            String maxValueString = el.getMaximum();
            if (maxValueString != null && maxValueString.equals(""))
                maxValueString = null;

            Comparable<?> minValue=null;
            Comparable<?> maxValue=null;

            try {
                if (varType == String.class) {
                    minValue = minValueString;
                    maxValue = maxValueString;
                } else if (varType == Long.class) {
                    minValue = minValueString != null ? Long.parseLong(minValueString) : null;
                    maxValue = maxValueString != null ? Long.parseLong(maxValueString) : null;
                } else if (varType == Integer.class) {
                    minValue = minValueString != null ? Integer.parseInt(minValueString) : null;
                    maxValue = maxValueString != null ? Integer.parseInt(maxValueString) : null;
                } else if (varType == Double.class) {
                    minValue = minValueString != null ? Double.parseDouble(minValueString) : null;
                    maxValue = maxValueString != null ? Double.parseDouble(maxValueString) : null;
                } else if (varType == Float.class) {
                    minValue = minValueString != null ? Float.parseFloat(minValueString) : null;
                    maxValue = maxValueString != null ? Float.parseFloat(maxValueString) : null;
                } else if (varType == Date.class) {
                    minValue = minValueString != null ? new Date(Long.parseLong(minValueString)) : null;
                    maxValue = maxValueString != null ? new Date(Long.parseLong(maxValueString)) : null;
                }
            } catch (NumberFormatException ignored) {
            }

            element = new DataElement(varName, varType, minValue, maxValue, null);
            retValue.add(element);
        }
        return retValue;

    }

}