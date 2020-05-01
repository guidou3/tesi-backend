package org.processmining.Guido.converters;

import org.processmining.Guido.DataPetriNetWithCustomElements;
import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.converters.bpmn2pn.BPMN2PetriNetWithDataConverter_Configuration;

import java.util.HashMap;

public class Bpmn2Dpn {
    BPMN2DPNConverter conv;

    public DataPetriNetWithCustomElements convert(PluginContext context, BPMNDiagram bpmn) {
        BPMN2PetriNetWithDataConverter_Configuration config = new BPMN2PetriNetWithDataConverter_Configuration();
        conv = new BPMN2DPNConverter(bpmn, config);

        Progress progress = context.getProgress();
        progress.setCaption("Converting BPMN diagram to Data Petri net");

        boolean success = conv.convertWithData();

        if (success) {
            DataPetriNetWithCustomElements net = conv.getDataPetriNet();
            Marking m = conv.getMarking();
            context.getConnectionManager().addConnection(new InitialMarkingConnection(net, m));

            if (!conv.getWarnings().isEmpty())
                showWarningsandErrors(conv);

            context.getFutureResult(0).setLabel("Data Petri net from " + bpmn.getLabel());
            context.getFutureResult(1).setLabel("Initial marking of the PN from " + bpmn.getLabel());

            return net;
        } else {
            if (!conv.getErrors().isEmpty() || !conv.getWarnings().isEmpty())
                showWarningsandErrors(conv);

            System.out.println("Could not translate BPMN diagram");
            return null;
        }
    }


    private static void showWarningsandErrors(BPMN2PetriNetConverter conv) {
        StringBuffer errors_warnings = new StringBuffer();
        for (String error : conv.getErrors()) {
            errors_warnings.append("Error: ").append(error).append('\n');
        }
        for (String warning : conv.getWarnings()) {
            errors_warnings.append("Warning: ").append(warning).append('\n');
        }
        System.out.println(errors_warnings.toString());
    }

    public void updateMapping(BpmnToDpnMapping mapping) {
        conv.updateMapping(mapping);
    }
}
