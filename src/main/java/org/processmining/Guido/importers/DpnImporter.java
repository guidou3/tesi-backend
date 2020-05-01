package org.processmining.Guido.importers;

import org.processmining.datapetrinets.DataPetriNetsWithMarkings;
import org.processmining.datapetrinets.io.DPNIOException;
import org.processmining.datapetrinets.io.DataPetriNetImporter;
import org.processmining.datapetrinets.utils.MarkingsHelper;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class DpnImporter {
    public static DataPetriNetImporter.DPNWithLayout importFromStream(InputStream input) throws DPNIOException {
//        DataPetriNetImporter dataPetriNetImporter = new DataPetriNetImporter();
//        DataPetriNet prova = dataPetriNetImporter.importFromStream(input).getDPN();
        return new DataPetriNetImporter().importFromStream(input);
    }

    public static DataPetriNetsWithMarkings importDpn(PluginContext context, File input)
            throws Exception {

        DataPetriNetImporter.DPNWithLayout dpnWithLayout = importFromStream(new FileInputStream(input));
        context.addConnection(dpnWithLayout.getLayout());

        DataPetriNetsWithMarkings dpnWithMarkings = dpnWithLayout.getDPN();

        if (MarkingsHelper.hasNonEmptyInitialMarking(context, dpnWithMarkings)) {
            context.addConnection(new InitialMarkingConnection(dpnWithMarkings, dpnWithMarkings.getInitialMarking()));
        }

        if (MarkingsHelper.hasNonEmptyFinalMarking(context, dpnWithMarkings)) {
            context.addConnection(new FinalMarkingConnection(dpnWithMarkings, dpnWithMarkings.getFinalMarkings()[0]));
        }

//        context.getFutureResult(0).setLabel(dpnWithMarkings.getLabel());
        return dpnWithMarkings;
    }
}
