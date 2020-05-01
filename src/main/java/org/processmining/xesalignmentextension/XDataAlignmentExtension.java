/*
 * LICENSE:
 *
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package org.processmining.xesalignmentextension;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.processmining.xesalignmentextension.XAlignmentExtension.MoveType;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

/**
 * This extension allows to store an event log that has been aligned to a
 * data-aware model. Constraints on the values of attributes might have been
 * defined, therefore, mis-alignments in attributes are possible. It is supposed
 * to be used in conjunction with the extension for data-unaware alignments
 * {@link XAlignmentExtension}.
 *
 * It defines four attributes:
 * <ul>
 * <li>dataalignment:movetype - Type of the event ordinal value of (
 * {@link DataMoveType})</li>
 * <li>dataalignment:ismodelattribute - Is the attribute relevant for model,
 * i.e., has it been aligned</li>
 * <li>dataalignment:logattributeid - Name of the attribute in the original
 * event log. Aligned attributes are stored under the name used in the model.
 * </li>
 * <li>dataalignment:logvalue - Value of the attribute in the original event
 * log. Please not that the type might differ depending on the original type.
 * </li>
 * </ul>
 *
 * @author F. Mannhardt (f.mannhardt@tue.nl)
 *
 */
public final class XDataAlignmentExtension extends XExtension {

    public static class XDataAlignmentExtensionException extends Exception {

        private static final long serialVersionUID = -1198255012355536379L;

        public XDataAlignmentExtensionException() {
        }

        public XDataAlignmentExtensionException(String message) {
            super(message);
        }

        public XDataAlignmentExtensionException(Throwable cause) {
            super(cause);
        }

        public XDataAlignmentExtensionException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    public enum DataMoveType {
        CORRECT, INCORRECT, MISSING
    }

    private static final long serialVersionUID = 3877984638998557276L;

    /**
     * Unique URI of this extension.
     */
    public static final URI EXTENSION_URI = URI.create("http://www.xes-standard.org/dataalignment.xesext");

    /**
     * Keys for the attributes.
     */
    public static final String KEY_MOVE_TYPE = "dataalignment:movetype";
    public static final String KEY_IS_MODEL_ATTRIBUTE = "dataalignment:ismodelattribute";
    public static final String KEY_LOG_ATTRIBUTE_ID = "dataalignment:logattributeid";

    public static final String KEY_LOG_VALUE_DISCRETE = "dataalignment:logattribute:discrete";
    public static final String KEY_LOG_VALUE_CONTINOUS = "dataalignment:logattribute:continuous";
    public static final String KEY_LOG_VALUE_BOOLEAN = "dataalignment:logattribute:boolean";
    public static final String KEY_LOG_VALUE_LITERAL = "dataalignment:logattribute:literal";
    public static final String KEY_LOG_VALUE_TIMESTAMP = "dataalignment:logattribute:timestamp";

    public static XAttributeDiscrete ATTR_MOVE_TYPE;
    public static XAttributeLiteral ATTR_LOG_ATTRIBUTE_ID;
    public static XAttributeBoolean ATTR_IS_MODEL_ATTRIBUTE;

    /**
     * Singleton instance of this extension.
     */
    private final static XDataAlignmentExtension SINGLETON;

    private XFactory factory = XFactoryRegistry.instance().currentDefault();

    static {
        SINGLETON = new XDataAlignmentExtension();
        XExtensionManager.instance().register(SINGLETON);
    }

    /**
     * @return the singleton {@link XDataAlignmentExtension}.
     */
    public static XDataAlignmentExtension instance() {
        return SINGLETON;
    }

    private Object readResolve() {
        return SINGLETON;
    }

    private XDataAlignmentExtension() {
        super("Data Alignment", "dataalignment", EXTENSION_URI);

        ATTR_MOVE_TYPE = factory.createAttributeDiscrete(KEY_MOVE_TYPE, -1, this);
        ATTR_IS_MODEL_ATTRIBUTE = factory.createAttributeBoolean(KEY_IS_MODEL_ATTRIBUTE, true, this);
        ATTR_LOG_ATTRIBUTE_ID = factory.createAttributeLiteral(KEY_LOG_ATTRIBUTE_ID, "__INVALID__", this);

        this.eventAttributes.add((XAttribute) ATTR_MOVE_TYPE.clone());
        this.metaAttributes.add((XAttribute) ATTR_MOVE_TYPE.clone());
        this.metaAttributes.add((XAttribute) ATTR_IS_MODEL_ATTRIBUTE.clone());
        this.metaAttributes.add((XAttribute) ATTR_LOG_ATTRIBUTE_ID.clone());

        //Cannot add the dynamically typed 'dataalignment:logvalue' attribute
    }

    public XFactory getFactory() {
        return factory;
    }

    public void setFactory(XFactory factory) {
        this.factory = factory;
    }

    public void assignDataMoveType(XAttributable a, DataMoveType dateMoveType) {
        a.getAttributes().put(KEY_MOVE_TYPE,
                factory.createAttributeDiscrete(KEY_MOVE_TYPE, dateMoveType.ordinal(), this));
    }

    public DataMoveType extractDataMoveType(XAttributable a) {
        XAttribute value = a.getAttributes().get(KEY_MOVE_TYPE);
        if (value != null && value instanceof XAttributeDiscrete) {
            return DataMoveType.values()[(int) (((XAttributeDiscrete) value).getValue())];
        } else if (value != null) {
            return DataMoveType.valueOf(((XAttributeLiteral) value).getValue());
        }
        return null;
    }

    public void assignIsModelAttribute(XAttribute attribute) {
        attribute.getAttributes().put(KEY_IS_MODEL_ATTRIBUTE,
                factory.createAttributeBoolean(KEY_IS_MODEL_ATTRIBUTE, true, this));
    }

    public Boolean extractIsModelAttribute(XAttribute attribute) {
        XAttribute value = attribute.getAttributes().get(KEY_IS_MODEL_ATTRIBUTE);
        if (value != null) {
            return ((XAttributeBoolean) value).getValue();
        }
        return false;
    }

    public boolean isCorrectAttribute(XAttribute attribute) throws XDataAlignmentExtensionException {
        if (extractIsModelAttribute(attribute)) {
            DataMoveType dataMoveType = extractDataMoveType(attribute);
            if (dataMoveType == null) {
                throw new XDataAlignmentExtensionException("Missing meta-attribute " + KEY_MOVE_TYPE);
            } else if (dataMoveType == DataMoveType.CORRECT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of {@link XAttribute} that are aligned to the model as
     * attributes with correct values.
     *
     * @param event
     * @return
     * @throws XDataAlignmentExtensionException
     *             in case of in-consistent meta-data
     */
    public List<XAttribute> extractCorrectAttributes(XEvent event) throws XDataAlignmentExtensionException {
        List<XAttribute> correctAttributesMap = new ArrayList<>();
        for (XAttribute a : event.getAttributes().values()) {
            if (isCorrectAttribute(a)) {
                correctAttributesMap.add(a);
            }
        }
        return correctAttributesMap;
    }

    public boolean isIncorrectAttribute(XAttribute attribute) throws XDataAlignmentExtensionException {
        if (extractIsModelAttribute(attribute)) {
            DataMoveType dataMoveType = extractDataMoveType(attribute);
            if (dataMoveType == null) {
                throw new XDataAlignmentExtensionException("Missing meta-attribute " + KEY_MOVE_TYPE);
            } else if (dataMoveType == DataMoveType.INCORRECT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of {@link IncorrectXAttribute} that are aligned to the
     * model as attributes with incorrect values.
     *
     * @param event
     * @return
     * @throws XDataAlignmentExtensionException
     */
    public List<IncorrectXAttribute> extractIncorrectAttributes(XEvent event) throws XDataAlignmentExtensionException {
        List<IncorrectXAttribute> incorrectAttributes = new ArrayList<>();
        for (final XAttribute a : event.getAttributes().values()) {
            if (isIncorrectAttribute(a)) {
                String logValueKey = decideLogAttributeKey(a);
                final XAttribute logAttribute = a.getAttributes().get(logValueKey);
                if (logAttribute != null) {
                    incorrectAttributes.add(new IncorrectXAttribute() {

                        public XAttribute getModelAttribute() {
                            return a;
                        }

                        public XAttribute getLogAttribute() {
                            return logAttribute;
                        }
                    });
                } else {
                    throw new XDataAlignmentExtensionException("Missing meta-attribute " + logValueKey);
                }
            }
        }
        return incorrectAttributes;
    }

    private String decideLogAttributeKey(XAttribute a) {
        Class<? extends XAttribute> attributeClass = a.getClass();
        if (XAttributeLiteral.class.isAssignableFrom(attributeClass)) {
            return KEY_LOG_VALUE_LITERAL;
        } else if (XAttributeDiscrete.class.isAssignableFrom(attributeClass)) {
            return KEY_LOG_VALUE_DISCRETE;
        } else if (XAttributeContinuous.class.isAssignableFrom(attributeClass)) {
            return KEY_LOG_VALUE_CONTINOUS;
        } else if (XAttributeTimestamp.class.isAssignableFrom(attributeClass)) {
            return KEY_LOG_VALUE_TIMESTAMP;
        } else if (XAttributeBoolean.class.isAssignableFrom(attributeClass)) {
            return KEY_LOG_VALUE_BOOLEAN;
        } else {
            throw new UnsupportedOperationException(
                    "Does not support attributes of type " + a.getClass().getSimpleName());
        }
    }

    public boolean isMissingAttribute(XAttribute attribute) throws XDataAlignmentExtensionException {
        if (extractIsModelAttribute(attribute)) {
            DataMoveType dataMoveType = extractDataMoveType(attribute);
            if (dataMoveType == null) {
                throw new XDataAlignmentExtensionException("Missing meta-attribute " + KEY_MOVE_TYPE);
            } else if (dataMoveType == DataMoveType.MISSING) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of {@link XAttribute} that are aligned to the model as
     * attributes missing in the original event log.
     *
     * @param event
     * @return
     * @throws XDataAlignmentExtensionException
     */
    public List<XAttribute> extractMissingAttributes(XEvent event) throws XDataAlignmentExtensionException {
        List<XAttribute> missingAttributesMap = new ArrayList<>();
        for (XAttribute a : event.getAttributes().values()) {
            if (isMissingAttribute(a)) {
                missingAttributesMap.add(a);
            }
        }
        return missingAttributesMap;
    }

    public boolean isUnmappedAttribute(XAttribute attribute) {
        if (!(extractIsModelAttribute(attribute))) {
            return true;
        }
        return false;
    }

    public List<XAttribute> extractUnmappedAttributes(XEvent event) {
        List<XAttribute> unmappedAttributes = new ArrayList<>();
        for (XAttribute a : event.getAttributes().values()) {
            if (isUnmappedAttribute(a)) {
                unmappedAttributes.add(a);
            }
        }
        return unmappedAttributes;

    }

    public interface IncorrectXAttribute {

        /**
         * @return attribute value according to the model
         */
        XAttribute getModelAttribute();

        /**
         * @return attribute value according to the original event log
         */
        XAttribute getLogAttribute();

    }

    /**
     * Wrapper for an {@link XEvent} providing easy access to the information
     * stored in the {@link XDataAlignmentExtension}.
     *
     * @author F. Mannhardt
     *
     */
    public interface XDataAlignmentMove extends XAlignmentMove {

        DataMoveType getDataMoveType();

        /**
         * @return {@link XDataAlignmentExtension#extractCorrectAttributes(XEvent)}
         * @throws XDataAlignmentExtensionException
         */
        List<XAttribute> getCorrectAttributes() throws XDataAlignmentExtensionException;

        /**
         * @return {@link XDataAlignmentExtension#extractIncorrectAttributes(XEvent)}
         * @throws XDataAlignmentExtensionException
         */
        List<IncorrectXAttribute> getIncorrectAttributes() throws XDataAlignmentExtensionException;

        /**
         * @return {@link XDataAlignmentExtension#extractMissingAttributes(XEvent)}
         * @throws XDataAlignmentExtensionException
         */
        List<XAttribute> getMissingAttributes() throws XDataAlignmentExtensionException;

        /**
         * @return {@link XDataAlignmentExtension#extractUnmappedAttributes(XEvent)}
         */
        List<XAttribute> getUnmappedAttributes();

    }

    /**
     * Wraps the {@link XAlignmentMove} into a {@link XDataAlignmentMove} with
     * additional information about the alignment of attributes.
     *
     * @param move
     * @return
     */
    public XDataAlignmentMove extendXAlignmentMove(final XAlignmentMove move) {
        if (move instanceof XDataAlignmentMove) {
            return (XDataAlignmentMove) move;
        } else {
            return new XDataAlignmentMove() {

                public boolean isObservable() {
                    return move.isObservable();
                }

                public MoveType getType() {
                    return move.getType();
                }

                public String getModelMove() {
                    return move.getModelMove();
                }

                public String getLogMove() {
                    return move.getLogMove();
                }

                public String getEventClassId() {
                    return move.getEventClassId();
                }

                public XEvent getEvent() {
                    return move.getEvent();
                }

                public String getActivityId() {
                    return move.getActivityId();
                }

                public DataMoveType getDataMoveType() {
                    DataMoveType dataMoveType = SINGLETON.extractDataMoveType(move.getEvent());
                    return dataMoveType != null ? dataMoveType : DataMoveType.CORRECT;
                }

                public List<XAttribute> getCorrectAttributes() throws XDataAlignmentExtensionException {
                    return SINGLETON.extractCorrectAttributes(move.getEvent());
                }

                public List<IncorrectXAttribute> getIncorrectAttributes() throws XDataAlignmentExtensionException {
                    return SINGLETON.extractIncorrectAttributes(move.getEvent());
                }

                public List<XAttribute> getMissingAttributes() throws XDataAlignmentExtensionException {
                    return SINGLETON.extractMissingAttributes(move.getEvent());
                }

                public List<XAttribute> getUnmappedAttributes() {
                    return SINGLETON.extractUnmappedAttributes(move.getEvent());
                }
            };
        }
    }

    public XAttribute assignModelAttribute(XEvent event, String attributeName, Object value) {
        XAttribute modelAttribute = createAttribute(attributeName, value, SINGLETON);
        assignIsModelAttribute(modelAttribute);
        event.getAttributes().put(modelAttribute.getKey(), modelAttribute);
        return modelAttribute;
    }

    public void assignLogAttribute(XAttribute modelAttribute, String logAttributeId, Object value) {
        assignLogValue(modelAttribute, value);
        assignLogAttributeId(modelAttribute, logAttributeId);
    }

    private void assignLogAttributeId(XAttribute modelAttribute, String logAttributeId) {
        XAttributeLiteral attr = (XAttributeLiteral) ATTR_LOG_ATTRIBUTE_ID.clone();
        attr.setValue(logAttributeId);
        attr.getAttributes().put(attr.getKey(), attr);
    }

    public void assignLogValue(XAttribute attribute, Object logValue) {
        XAttribute metaAttribute = createAttribute(decideLogAttributeKey(attribute), logValue, SINGLETON);
        attribute.getAttributes().put(metaAttribute.getKey(), metaAttribute);
    }

    public XAttribute extractLogValue(XAttribute attribute) {
        return attribute.getAttributes().get(decideLogAttributeKey(attribute));
    }

    private XAttribute createAttribute(String attributeName, Object value, XExtension extension) {
        XFactory f = factory;
        if (value instanceof Double || value instanceof Float) {
            return f.createAttributeContinuous(attributeName, ((Number) value).doubleValue(), extension);
        } else if (value instanceof Integer || value instanceof Long) {
            return f.createAttributeDiscrete(attributeName, ((Number) value).longValue(), extension);
        } else if (value instanceof Date) {
            return f.createAttributeTimestamp(attributeName, ((Date) value), extension);
        } else if (value instanceof Boolean) {
            return f.createAttributeBoolean(attributeName, ((Boolean) value), extension);
        } else {
            return f.createAttributeLiteral(attributeName, value.toString(), extension);
        }
    }

}