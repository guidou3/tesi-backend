package org.processmining.Guido.InOut;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final class PnmlHelper {

    static final String DIMENSION = "dimension";
    static final String POSITION = "position";
    static final String NAME = "name";
    static final String MAX_VALUE = "maxValue";
    static final String TYPE = "type";
    static final String VARIABLES = "variables";
    static final String VARIABLE = "variable";
    static final String TRUE = "true";
    static final String READ_VARIABLE = "readVariable";
    static final String WRITE_VARIABLE = "writeVariable";
    static final String INVISIBLE = "invisible";
    static final String GUARD = "guard";
    static final String TEXT = "text";
    static final String TRANSITION = "transition";
    static final String LOCAL_NODE_ID = "localNodeID";
    static final String TOOLSPECIFIC = "toolspecific";
    static final String PLACE = "place";
    static final String MIN_VALUE = "minValue";
    static final String FINAL_MARKING_NODE = "finalMarking";
    static final String NET = "net";
    static final String ID = "id";
    static final String WIDTH = "width";
    static final String HEIGHT = "height";
    static final String X = "x";
    static final String Y = "y";

    private PnmlHelper() {
        super();
    }

    static String getLocalId(Element node) {
        NodeList toolspecifics = node.getElementsByTagName(TOOLSPECIFIC);
        if (toolspecifics.getLength() > 0) {
            Node toolspecific = toolspecifics.item(0);
            Node localIdNode = toolspecific.getAttributes().getNamedItem(LOCAL_NODE_ID);
            if (localIdNode != null) {
                return localIdNode.getTextContent();
            }
        }
        return null;
    }

    static String getLabel(Element node) {
        NodeList textNodes = node.getElementsByTagName(PnmlHelper.TEXT);
        if (textNodes.getLength() == 1) {
            Node text = textNodes.item(0);
            if (text != null) {
                return text.getTextContent();
            }
        }
        return null;
    }

}

