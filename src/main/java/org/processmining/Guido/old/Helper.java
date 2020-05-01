//package org.processmining.Guido.DataAwareConformanceChecking;
//
//import org.deckfour.uitopia.api.event.TaskListener;
//import org.deckfour.xes.classification.XEventClassifier;
//import org.deckfour.xes.extension.std.XConceptExtension;
//import org.deckfour.xes.info.impl.XLogInfoImpl;
//import org.deckfour.xes.model.XLog;
//import org.processmining.contexts.uitopia.UIPluginContext;
//import org.processmining.datapetrinets.DataPetriNet;
//import org.processmining.datapetrinets.ui.ConfigurationUIHelper;
//import org.processmining.datapetrinets.ui.ImprovedEvClassLogMappingUI;
//import org.processmining.framework.connections.Connection;
//import org.processmining.framework.connections.ConnectionCannotBeObtained;
//import org.processmining.framework.connections.ConnectionID;
//import org.processmining.framework.connections.ConnectionManager;
//import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
//import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
//import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
//import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class Helper {
//    @SuppressWarnings("unchecked")
//    private static <T extends Connection> T getFirstConnectionWithoutAutoCreate(ConnectionManager connectionManager,
//                                                                                Class<T> connectionType, Object... objects) throws ConnectionCannotBeObtained {
//        for (ConnectionID connID : connectionManager.getConnectionIDs()) {
//            Connection c = connectionManager.getConnection(connID);
//            if (((connectionType == null) || connectionType.isAssignableFrom(c.getClass()))
//                    && c.containsObjects(objects)) {
//                return (T) c;
//            }
//        }
//        throw new ConnectionCannotBeObtained("Connection not found", connectionType, objects);
//    }
//
//    public static TransEvClassMapping queryActivityEventClassMapping(UIPluginContext context, PetrinetGraph net,
//                                                                     XLog log) {
//        try {
//            EvClassLogPetrinetConnection conn = getFirstConnectionWithoutAutoCreate(context.getConnectionManager(),
//                    EvClassLogPetrinetConnection.class, net, log);
//            return (TransEvClassMapping) conn.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);
//        } catch (ConnectionCannotBeObtained e) {
//            return doQueryNewActivityMapping(context, net, log);
//        }
//    }
//
//    private static TransEvClassMapping doQueryNewActivityMapping(UIPluginContext context, PetrinetGraph net, XLog log)
//            throws UserCancelledException {
//        // list possible classifiers
//        List<XEventClassifier> classList = new ArrayList<>(log.getClassifiers());
//        // add default classifiers
//        if (!classList.contains(XLogInfoImpl.RESOURCE_CLASSIFIER)) {
//            classList.add(XLogInfoImpl.RESOURCE_CLASSIFIER);
//        }
//        if (!classList.contains(XLogInfoImpl.STANDARD_CLASSIFIER)) {
//            classList.add(XLogInfoImpl.STANDARD_CLASSIFIER);
//        }
//        if (!classList.contains(XLogInfoImpl.NAME_CLASSIFIER)) {
//            classList.add(0, XLogInfoImpl.NAME_CLASSIFIER);
//        }
//
//        Object[] availableEventClass = classList.toArray(new Object[classList.size()]);
//
//        ImprovedEvClassLogMappingUI mappingUi = new ImprovedEvClassLogMappingUI(log, net, availableEventClass);
//        TaskListener.InteractionResult result = context.showConfiguration("Mapping between Events from Log and Transitions of the Data Petri Net", mappingUi);
//
//        if (result == TaskListener.InteractionResult.CANCEL) {
//            throw new UserCancelledException();
//        }
//
//        String logName = XConceptExtension.instance().extractName(log);
//        String label = "Connection between " + net.getLabel() + " and " + (logName != null ? logName : "unnamed log");
//        EvClassLogPetrinetConnection con = new EvClassLogPetrinetConnection(label, net, log,
//                mappingUi.getSelectedClassifier(), mappingUi.getMap());
//        context.addConnection(con);
//
//        return mappingUi.getMap();
//    }
//
//    public static TransEvClassMapping queryActivityMapping(UIPluginContext context, DataPetriNet net, XLog log) {
//        try {
//            EvClassLogPetrinetConnection conn = getFirstConnection(context.getConnectionManager(),
//                    EvClassLogPetrinetConnection.class, net, log);
//            return (TransEvClassMapping) conn.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);
//        } catch (ConnectionCannotBeObtained e) {
//            return queryActivityEventClassMapping(context, net, log);
//        }
//    }
//
//    private static <T extends Connection> T getFirstConnection(ConnectionManager connectionManager,
//                                                               Class<T> connectionType, Object... objects) throws ConnectionCannotBeObtained {
//        for (ConnectionID connID : connectionManager.getConnectionIDs()) {
//            Connection c = connectionManager.getConnection(connID);
//            if (((connectionType == null) || connectionType.isAssignableFrom(c.getClass()))
//                    && c.containsObjects(objects)) {
//                return (T) c;
//            }
//        }
//        throw new ConnectionCannotBeObtained("Connection not found", connectionType, objects);
//    }
//}
