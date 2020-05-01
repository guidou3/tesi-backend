//package org.processmining.Guido.old;
//
//import org.deckfour.uitopia.api.event.TaskListener;
//import org.deckfour.xes.classification.XEventClasses;
//import org.deckfour.xes.classification.XEventClassifier;
//import org.deckfour.xes.model.XLog;
//import org.processmining.contexts.uitopia.UIPluginContext;
//import org.processmining.datapetrinets.DataPetriNet;
//import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
//import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
//import org.processmining.log.utils.XUtils;
//import org.processmining.plugins.DataConformance.framework.VariableMatchCosts;
//import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration;
//import org.processmining.plugins.balancedconformance.observer.DataConformancePlusObserverImpl;
//import org.processmining.plugins.balancedconformance.ui.BalancedAlignmentConfigPanel;
//import org.processmining.plugins.balancedconformance.ui.DataConformanceConfigUIUtils;
//import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
//
//import java.util.Collections;
//
//public class Temp {
//
//    public static BalancedProcessorConfiguration queryConfiguration(final UIPluginContext context, DataPetriNet net,
//                                                                    XLog log) throws UserCancelledException {
//
//        BalancedProcessorConfiguration config = queryBalancedAlignmentConfig(context);
//
//        TransEvClassMapping activityMapping = DataConformanceConfigUIUtils.queryActivityMapping(context, net, log);
//
//        config.setActivityMapping(activityMapping);
//        config.setObserver(new DataConformancePlusObserverImpl(context));
//        XEventClassifier eventClassifier = activityMapping.getEventClassifier();
//
//        XEventClasses eventClasses = XUtils.createEventClasses(eventClassifier, log);
//
//        DataConformanceConfigUIUtils.queryControlFlowAlignmentConfig(context, net, log, config, eventClasses);
//
//        if (!net.getVariables().isEmpty()) {
//            DataConformanceConfigUIUtils.queryDataAlignmentConfig(context, net, log, activityMapping, config);
//        } else {
//
//            VariableMatchCosts variableCost = BalancedProcessorConfiguration.createDefaultVariableCost(net,
//                    Collections.<String>emptySet(), 0, 0);
//            config.setVariableCost(variableCost);
//            config.setVariableMapping(Collections.<String, String>emptyMap());
//
//            if (!hasGuards(net)) {
//                ProMUIHelper.showWarningMessage(context,
//                        "Selected DPN-net does not define variables/guards. Alignment will not consider data!",
//                        "Variables/guards missing");
//            }
//        }
//
//        return config;
//    }
//
//    public static BalancedProcessorConfiguration queryBalancedAlignmentConfig(UIPluginContext context)
//            throws UserCancelledException {
//        BalancedProcessorConfiguration config = new BalancedProcessorConfiguration();
//        BalancedAlignmentConfigPanel balancedAlignmentConfigPanel = new BalancedAlignmentConfigPanel();
//        TaskListener.InteractionResult balancedAlignmentResult = context
//                .showConfiguration("Performance & Alignment-related Parameters", balancedAlignmentConfigPanel);
//        if (balancedAlignmentResult == TaskListener.InteractionResult.CANCEL) {
//            throw new UserCancelledException();
//        } else {
//            config.setActivateDataViewCache(balancedAlignmentConfigPanel.isDataViewCacheActivated());
//            config.setUseOptimizations(balancedAlignmentConfigPanel.getIsUseOptimizations());
//            config.setConcurrentThreads(balancedAlignmentConfigPanel.getConcurrentThreads());
//            config.setSorting(balancedAlignmentConfigPanel.getSorting());
//            config.setIlpSolver(balancedAlignmentConfigPanel.getILPSolver());
//            config.setUsePartialDataAlignments(balancedAlignmentConfigPanel.getIsUsePartialDataAlignments());
//            config.setVariablesUnassignedMode(balancedAlignmentConfigPanel.getUnassignedMode());
//            config.setQueueingModel(balancedAlignmentConfigPanel.getQueueingModel());
//            config.setMaxCostFactor(balancedAlignmentConfigPanel.getMaxCost());
//            config.setUsePartialOrders(balancedAlignmentConfigPanel.getIsUsePartialOrders());
//
//            config.setKeepControlFlowSearchSpace(balancedAlignmentConfigPanel.getIsKeepControlFlowSearchSpace());
//            config.setKeepDataFlowSearchSpace(balancedAlignmentConfigPanel.getIsKeepDataFlowSearchSpace());
//            config.setSearchMethod(balancedAlignmentConfigPanel.getSearchMethod());
//            config.setMaxQueuedStates(balancedAlignmentConfigPanel.getMaxQueuedStates());
//            return config;
//        }
//    }
//}
