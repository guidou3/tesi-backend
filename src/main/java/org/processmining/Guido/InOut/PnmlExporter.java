package org.processmining.Guido.InOut;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.datapetrinets.utils.MarkingsHelper;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.base.PnmlElementFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;

public class PnmlExporter {

    public void exportPetriNetToPNMLFile(PluginContext context, DataPetriNet net, File file) throws Exception {
        exportPetriNetToPNMLOrEPNMLFile(context, net, file, Pnml.PnmlType.PNML);
    }

    protected void exportPetriNetToPNMLOrEPNMLFile(PluginContext context, DataPetriNet net, File file,
                                                   Pnml.PnmlType type) throws IOException, SAXException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException {

        GraphLayoutConnection layout = getLayout(context, net);
        Pnml pnml = createPnml(context, net, type, layout);

        Document document = parsePnmlToDocument(pnml);

        // We only process the first final marking ignoring all others
        Marking finalMarkings = MarkingsHelper.getFinalMarkingsOrEmpty(context, net)[0];

        addVariables(net, layout, document);
        addGuardExpressionsAndWriteOps(net, document);
        addFinalMarkings(net, finalMarkings, document);

        Source documentSource = new DOMSource(document);

        // Write the DOM document to the file
        Result result = new StreamResult(file);
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.setOutputProperty(OutputKeys.INDENT, "yes");
        xformer.transform(documentSource, result);
    }

    private void addFinalMarkings(DataPetriNet net, Marking finalMarkings, Document document) {
        NodeList placeNodes = document.getElementsByTagName(PnmlHelper.PLACE);
        for (int i = 0; i < placeNodes.getLength(); i++) {
            Element placeNode = (Element) placeNodes.item(i);
            String localId = PnmlHelper.getLocalId(placeNode);
            if (localId != null) {
                for (Place p : finalMarkings) {
                    if (localId.equals(p.getLocalID().toString())) {
                        placeNode.appendChild(createFinalMarkingNode(document, finalMarkings.occurrences(p)));
                    }
                }
            }
        }
    }

    private Node createFinalMarkingNode(Document document, Integer numTokens) {
        Element finalMarkingNode = document.createElement(PnmlHelper.FINAL_MARKING_NODE);
        Element numTokensNode = document.createElement(PnmlHelper.TEXT);
        numTokensNode.appendChild(document.createTextNode(String.valueOf(numTokens)));
        finalMarkingNode.appendChild(numTokensNode);
        return finalMarkingNode;
    }

    private void addGuardExpressionsAndWriteOps(DataPetriNet net, Document document) {
        NodeList transitionNodes = document.getElementsByTagName(PnmlHelper.TRANSITION);
        for (int i = 0; i < transitionNodes.getLength(); i++) {
            Element transitionNode = (Element) transitionNodes.item(i);
            String localId = PnmlHelper.getLocalId(transitionNode);
            for (Transition t : net.getTransitions()) {
                PNWDTransition trans = (PNWDTransition) t;
                if (trans.getLocalID().toString().equals(localId)) {
                    String guard = trans.getGuardAsString();
                    if (guard != null) {
                        transitionNode.setAttribute(PnmlHelper.GUARD, guard);
                    }
                    if (trans.isInvisible())
                        transitionNode.setAttribute(PnmlHelper.INVISIBLE, PnmlHelper.TRUE);
                    for (DataElement writeOperation : trans.getWriteOperations()) {
                        Element writeNode = document.createElement(PnmlHelper.WRITE_VARIABLE);
                        writeNode.appendChild(document.createTextNode(writeOperation.getVarName()));
                        transitionNode.appendChild(writeNode);
                    }
                    for (DataElement readOperation : trans.getReadOperations()) {
                        Element readNode = document.createElement(PnmlHelper.READ_VARIABLE);
                        readNode.appendChild(document.createTextNode(readOperation.getVarName()));
                        transitionNode.appendChild(readNode);
                    }
                    break;
                }
            }
        }
    }

    private void addVariables(DataPetriNet net, GraphLayoutConnection layout, Document document) {
        Node netElement = document.getElementsByTagName(PnmlHelper.NET).item(0);
        Element variablesElem = document.createElement(PnmlHelper.VARIABLES);
        netElement.appendChild(variablesElem);
        for (DataElement dataElem : net.getVariables()) {
            Element singleVariableElem = document.createElement(PnmlHelper.VARIABLE);

            //Create tag's attributes
            singleVariableElem.setAttribute(PnmlHelper.TYPE, dataElem.getType().getCanonicalName());
            if (dataElem.getMinValue() != null)
                singleVariableElem.setAttribute(PnmlHelper.MIN_VALUE, String.valueOf(dataElem.getMinValue()));
            if (dataElem.getMaxValue() != null)
                singleVariableElem.setAttribute(PnmlHelper.MAX_VALUE, String.valueOf(dataElem.getMaxValue()));

            //Create the name sub-tag
            Element createNameElem = document.createElement(PnmlHelper.NAME);
            createNameElem.appendChild(document.createTextNode(dataElem.getVarName()));
            singleVariableElem.appendChild(createNameElem);

            //Create the position tag
            Element createPositionElem = document.createElement(PnmlHelper.POSITION);
            Point2D pos = layout.getPosition(dataElem);
            if (pos == null)
                pos = new Point(0, 0);
            createPositionElem.setAttribute(PnmlHelper.X, String.valueOf((int) pos.getX()));
            createPositionElem.setAttribute(PnmlHelper.Y, String.valueOf((int) pos.getY()));
            singleVariableElem.appendChild(createPositionElem);

            //Create the dimension tag
            Element createDimensionElem = document.createElement(PnmlHelper.DIMENSION);
            Dimension dim = layout.getSize(dataElem);
            if (dim == null)
                dim = new Dimension(50, 50);
            createDimensionElem.setAttribute(PnmlHelper.WIDTH, String.valueOf((int) dim.getWidth()));
            createDimensionElem.setAttribute(PnmlHelper.HEIGHT, String.valueOf((int) dim.getHeight()));
            singleVariableElem.appendChild(createDimensionElem);

            variablesElem.appendChild(singleVariableElem);
        }
    }

    private Document parsePnmlToDocument(Pnml pnml) throws SAXException, IOException, ParserConfigurationException {
        String pnmlAsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + pnml.exportElement(pnml);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        InputSource source = new InputSource(new StringReader(pnmlAsXml));
        Document document = factory.newDocumentBuilder().parse(source);
        return document;
    }

    private GraphLayoutConnection getLayout(PluginContext context, DataPetriNet net) {
        GraphLayoutConnection layout;
        try {
            if (context != null) {
                layout = context.getConnectionManager().getFirstConnection(GraphLayoutConnection.class, context, net);
            } else {
                layout = new GraphLayoutConnection(net);
            }
        } catch (ConnectionCannotBeObtained e) {
            layout = new GraphLayoutConnection(net);
        }
        return layout;
    }

    private Pnml createPnml(PluginContext context, DataPetriNet net, Pnml.PnmlType type, GraphLayoutConnection layout) {
        HashMap<PetrinetGraph, Marking> netWithInitialMarking = new HashMap<PetrinetGraph, Marking>();
        Marking initialMarking = MarkingsHelper.getInitialMarkingOrEmpty(context, net);
        netWithInitialMarking.put(net, initialMarking);
        Map<PetrinetGraph, Collection<Marking>> netWithFinalMarking = new HashMap<PetrinetGraph, Collection<Marking>>();
        //TODO for now just ad dummy to avoid NPE, later adopt the storage mechanism of normal petri net
        netWithFinalMarking.put(net, ImmutableList.of(new Marking()));
        PnmlElementFactory factory = new FullPnmlElementFactory();
        Pnml pnml = new Pnml();
        synchronized (factory) {
            pnml.setFactory(factory);
            pnml.convertFromNet(netWithInitialMarking, netWithFinalMarking, layout);
            pnml.setType(type);
        }
        return pnml;
    }

}

