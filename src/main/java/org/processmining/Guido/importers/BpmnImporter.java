package org.processmining.Guido.importers;

import org.processmining.datapetrinets.DataPetriNetsWithMarkings;
import org.processmining.datapetrinets.io.DPNIOException;
import org.processmining.datapetrinets.io.DataPetriNetImporter;
import org.processmining.datapetrinets.utils.MarkingsHelper;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Swimlane;
import org.processmining.plugins.bpmn.Bpmn;
import org.processmining.plugins.bpmn.BpmnProcess;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

//import org.processmining.contexts.uitopia.annotations.UIImportPlugin;


public class BpmnImporter {
    private Map<String, BPMNNode> id2node;
    private Map<String, Swimlane> id2lane;
    private Bpmn bpmn;
    BPMNDiagram newDiagram;


    public FileFilter getFileFilter() {
        return new FileNameExtensionFilter("BPMN 2.0 files", "bpmn", "xml");
    }

    public Bpmn importFromStream(PluginContext context, InputStream input, String filename) throws Exception {
        bpmn = importBpmnFromStream(input);
        if (bpmn == null) {
			/*
			 * No BPMN found in file. Fail.
		`	 */
            return null;
        }
        context.getFutureResult(0).setLabel(filename);
        return bpmn;
    }

    public Bpmn importBpmnFromStream(InputStream input)
            throws Exception {
        /*
         * Get an XML pull parser.
         */
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        /*
         * Initialize the parser on the provided input.
         */
        xpp.setInput(input, null);
        /*
         * Get the first event type.
         */
        int eventType = xpp.getEventType();
        /*
         * Create a fresh PNML object.
         */
        Bpmn bpmn = new Bpmn();

        /*
         * Skip whatever we find until we've found a start tag.
         */
        while (eventType != XmlPullParser.START_TAG) {
            eventType = xpp.next();
        }
        /*
         * Check whether start tag corresponds to PNML start tag.
         */
        if (xpp.getName().equals(bpmn.tag)) {
            /*
             * Yes it does. Import the PNML element.
             */
            bpmn.importElement(xpp, bpmn);
        } else {
            /*
             * No it does not. Return null to signal failure.
             */
            bpmn.log(bpmn.tag, xpp.getLineNumber(), "Expected " + bpmn.tag + ", got " + xpp.getName());
        }
        return bpmn;
    }

    public BPMNDiagram bpmnToDiagram() {
        if(newDiagram == null) {
            newDiagram = BPMNDiagramFactory.newBPMNDiagram("");
            id2node = new HashMap<>();
            id2lane = new HashMap<>();
            bpmn.unmarshall(newDiagram, id2node, id2lane);
        }

        return newDiagram;
    }

    public Map<String, String> getMap() {
        Map<String, String> map = new HashMap<>();
        for(Map.Entry<String, BPMNNode> entry : id2node.entrySet())
            if(entry.getValue() instanceof Activity && !entry.getKey().startsWith("output"))
                map.put(entry.getKey(), entry.getValue().getId().toString());

        return map;
    }

    public HashMap<String, String> getIdToNewId2() {
        HashMap<String, String> id2NewId = new HashMap<>();
        for (Map.Entry<String, Swimlane> entry : id2lane.entrySet())
            id2NewId.put(entry.getKey(), entry.getValue().getId().toString());

        return id2NewId;
    }


}
