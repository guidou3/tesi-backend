//package org.processmining.Guido.DataAwareConformanceChecking;
//
//import org.processmining.Guido.mapping.FinalMapping;
//import org.deckfour.xes.classification.XEventClassifier;
//import org.deckfour.xes.extension.std.XConceptExtension;
//import org.deckfour.xes.info.impl.XLogInfoImpl;
//import org.deckfour.xes.model.XLog;
//import org.processmining.contexts.uitopia.UIPluginContext;
//import org.processmining.framework.connections.Connection;
//import org.processmining.framework.connections.ConnectionCannotBeObtained;
//import org.processmining.framework.connections.ConnectionID;
//import org.processmining.framework.connections.ConnectionManager;
//import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
//import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
//import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
//import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
//import org.processmining.Guido.server.Database;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ConfigurationHelper {
//
//    public static TransEvClassMapping queryActivityEventClassMapping(UIPluginContext context, PetrinetGraph net,
//                                                                     XLog log) throws UserCancelledException {
//        try {
//            EvClassLogPetrinetConnection conn = getFirstConnectionWithoutAutoCreate(context.getConnectionManager(),
//                    EvClassLogPetrinetConnection.class, net, log);
//            return (TransEvClassMapping) conn.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);
//        } catch (ConnectionCannotBeObtained e) {
//            return doQueryNewActivityMapping(context, net, log);
//        }
//    }
//
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
//
//    // TODO: remove call to database
//    private static TransEvClassMapping doQueryNewActivityMapping(UIPluginContext context, PetrinetGraph net, XLog log)
//            throws UserCancelledException {
//        // list possible classifiers
////        List<XEventClassifier> classList = new ArrayList<>(log.getClassifiers());
////        // add default classifiers
////        if (!classList.contains(XLogInfoImpl.RESOURCE_CLASSIFIER)) {
////            classList.add(XLogInfoImpl.RESOURCE_CLASSIFIER);
////        }
////        if (!classList.contains(XLogInfoImpl.STANDARD_CLASSIFIER)) {
////            classList.add(XLogInfoImpl.STANDARD_CLASSIFIER);
////        }
////        if (!classList.contains(XLogInfoImpl.NAME_CLASSIFIER)) {
////            classList.add(0, XLogInfoImpl.NAME_CLASSIFIER);
////        }
////
////        Object[] availableEventClass = classList.toArray(new Object[classList.size()]);
////
////        MappingImporter mapping = new MappingImporter(log, net, availableEventClass);
//        FinalMapping mapping = Database.getConformanceChecker().getMapping();
//
//        String logName = XConceptExtension.instance().extractName(log);
//        String label = "Connection between " + net.getLabel() + " and " + (logName != null ? logName : "unnamed log");
//        EvClassLogPetrinetConnection con = new EvClassLogPetrinetConnection(label, net, log,
//                mapping.getClassifier(), mapping.getMap());
//        context.addConnection(con);
//
//        return mapping.getMap();
//    }
//
//}
//
