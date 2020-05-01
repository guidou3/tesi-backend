//package DataAwareConformanceChecking;
//
//import com.google.common.collect.ImmutableSet;
//import com.google.gson.JsonObject;
//import nl.tue.astar.AStarThread.ASynchronousMoveSorting;
//import nl.tue.astar.AStarThread.QueueingModel;
//import org.deckfour.xes.classification.XEventClass;
//import org.deckfour.xes.classification.XEventClasses;
//import org.deckfour.xes.info.XLogInfo;
//import org.deckfour.xes.model.XLog;
//import org.processmining.Guido.utils.UI.CheckBox;
//import org.processmining.Guido.utils.UI.Select;
//import org.processmining.Guido.utils.UI.Selection;
//import org.processmining.contexts.uitopia.UIPluginContext;
//import org.processmining.datapetrinets.DataPetriNet;
//import org.processmining.framework.connections.Connection;
//import org.processmining.framework.connections.ConnectionCannotBeObtained;
//import org.processmining.framework.connections.ConnectionID;
//import org.processmining.framework.connections.ConnectionManager;
//import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
//import org.processmining.log.utils.XUtils;
//import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
//import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
//import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
//import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;
//import org.processmining.plugins.DataConformance.DataAlignment.PetriNet.ReplayableTransition;
//import org.processmining.plugins.DataConformance.framework.VariableMatchCosts;
//import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration;
//import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration.UnassignedMode;
//import org.processmining.plugins.balancedconformance.config.DataConformancePlusConfiguration;
//import org.processmining.plugins.balancedconformance.controlflow.adapter.SearchMethod;
//import org.processmining.plugins.balancedconformance.dataflow.DataAlignmentAdapter.ILPSolver;
//import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
//import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//import java.util.TreeSet;
//
///**
// * @author F. Mannhardt
// *
// */
//public class ConfigImporter2 {
//
//
//    private static final int MAXLIMMAXNUMINSTANCES = 100_000_001;
//    private static final int DEFLIMMAXNUMINSTANCES = 100_000_001;
//
//    private Select<Integer> concurrentThreads;
//
//    private CheckBox dataCacheActivated;
//    private CheckBox useOptimizations;
//    private CheckBox keepControlFlowSearchSpace;
//    private CheckBox keepDataFlowSearchSpace;
//    private CheckBox usePartialDataAlignments;
//
//    private Selection<SearchMethod> searchMethod;
//
//    private Selection<ASynchronousMoveSorting> sorting;
//    private Selection<QueueingModel> queueingModel;
//    private Selection<ILPSolver> ilpSolver;
//    private Selection<UnassignedMode> unassignedMode;
//
//    private Select<Double> maxCostInQueue;
//    private Select<Integer> limitQueuedStates;
//
//    private CheckBox usePartialOrder;
//
//    public ConfigImporter2(boolean def) {
//        int availableProcessors = Runtime.getRuntime().availableProcessors();
//
//        maxCostInQueue = new Select<>("Limit fitness", 0D, 1D, 0D);
//
//        limitQueuedStates = new Select<>("Limit search space", 1, MAXLIMMAXNUMINSTANCES, DEFLIMMAXNUMINSTANCES);
//
//        unassignedMode = new Selection<>(UnassignedMode.values(),"Unassigned variable mode");
//        unassignedMode.setSelectedItem(UnassignedMode.NULL);
//
//        ilpSolver = new Selection<>(ILPSolver.values(), "MILP Solver");
//        ilpSolver.setSelectedItem(ILPSolver.ILP_LPSOLVE);
//
//        searchMethod = new Selection<>(SearchMethod.values(), "Search Algorithm");
//        searchMethod.setSelectedItem(SearchMethod.ASTAR_GRAPH);
//
//        usePartialDataAlignments = new CheckBox("Compute Balanced Alignment", true);
//
//        keepControlFlowSearchSpace = new CheckBox("Keep Control Flow Search Space", true);
//
//        keepDataFlowSearchSpace = new CheckBox("Keep Data Flow Search Space", true);
//
//        dataCacheActivated = new CheckBox("Use Cache", true);
//
//        useOptimizations = new CheckBox("Use Optimization", true);
//
//        usePartialOrder = new CheckBox("Use PartialOrder", false);
//
//        sorting = new Selection<>(ASynchronousMoveSorting.values(), "Re-Ordering of Moves");
//        sorting.setSelectedItem(ASynchronousMoveSorting.LOGMOVEFIRST);
//
//        // Only for plain A*
//        queueingModel = new Selection<>(QueueingModel.values(), "Queueing Strategy");
//        queueingModel.setSelectedItem(QueueingModel.DEPTHFIRST);
//
//    }
//
//    public ConfigImporter2() {
//
////        JLabel introduction = new JLabel("<html>"
////                + "You are about to compute an alignment a Data Petri Net (DPN) (or plain Petri net) and an Event Log. "
////                + "Please note that a more user-friendly (and up-to-date) alternative to this plug-ins is the: <u>Multi-perspective Process Explorer</u>. "
////                + "For most parameters using the <u>default option</u> is a reasonable choice."
////                + "<ul>"
////                + "<li>Performance and Algorithm-related Parameters (this dialog: use defaults)</li>"
////                + "<li>Mapping between Events and Transitions (<ub>important</u>: check correct mapping)</li>"
////                + "<li>Cost for control-flow violations (default cost is 1)</li>"
////                + "<li>Mapping between variables in the DPN and attributes in the event log (<u>important</u>: check correct mapping)</li>"
////                + "<li>Cost for data violations (default cost is 1)</li>"
////                + "<li>Bounds for variables (use defaults)</li>"
////                + "</ul>"
////                + "The computation is computationally very intensive depending on the used model (degree of parallelism, complexity of guards) and log (length of traces). "
////                + "It is advised to have <b>at least 4GB of memory</b> allocated to ProM. Reducing the number of concurrent threads might help to reduce memory requirements. "
////                + "</html>");
//
//        // send availableProcessors, MAXLIMMAXNUMINSTANCES, DEFLIMMAXNUMINSTANCES
//
//        UnassignedMode.values() // UnassignedMode.NULL
//        ILPSolver.values() // ILPSolver.ILP_LPSOLVE
//        SearchMethod.values() //SearchMethod.ASTAR_GRAPH
//        ASynchronousMoveSorting.values() // ASynchronousMoveSorting.LOGMOVEFIRST
//        QueueingModel.values() // QueueingModel.DEPTHFIRST);
//
//
//    }
//
//    public static JsonObject getInitialData() {
//
//    }
//
//    public boolean isDataViewCacheActivated() {
//        return dataCacheActivated.isSelected();
//    }
//
//    public boolean getIsUseOptimizations() {
//        return useOptimizations.isSelected();
//    }
//
//    public boolean getIsUsePartialOrders() {
//        return usePartialOrder.isSelected();
//    }
//
//    public int getConcurrentThreads() {
//        return concurrentThreads.getValue();
//    }
//
//    public boolean getUseILP() {
//        return true;
//    }
//
//    public ASynchronousMoveSorting getSorting() {
//        return (ASynchronousMoveSorting) sorting.getSelectedItem();
//    }
//
//    public ILPSolver getILPSolver() {
//        return (ILPSolver) ilpSolver.getSelectedItem();
//    }
//
//    public QueueingModel getQueueingModel() {
//        return (QueueingModel) queueingModel.getSelectedItem();
//    }
//
//    public double getMaxCost() {
//        return maxCostInQueue.getValue();
//    }
//
//    public SearchMethod getSearchMethod() {
//        return (SearchMethod) searchMethod.getSelectedItem();
//    }
//
//    public int getMaxQueuedStates() {
//        return limitQueuedStates.getValue() == MAXLIMMAXNUMINSTANCES ? Integer.MAX_VALUE : limitQueuedStates.getValue();
//    }
//
//    public boolean getIsUsePartialDataAlignments() {
//        return usePartialDataAlignments.isSelected();
//    }
//
//    public UnassignedMode getUnassignedMode() {
//        return (UnassignedMode) unassignedMode.getSelectedItem();
//    }
//
//    public boolean getIsKeepControlFlowSearchSpace() {
//        return keepControlFlowSearchSpace.isSelected();
//    }
//
//    public boolean getIsKeepDataFlowSearchSpace() {
//        return keepDataFlowSearchSpace.isSelected();
//    }
//
//    public BalancedProcessorConfiguration createConfig() {
//        BalancedProcessorConfiguration config = new BalancedProcessorConfiguration();
//        config.setActivateDataViewCache(isDataViewCacheActivated());
//        config.setUseOptimizations(getIsUseOptimizations());
//        config.setConcurrentThreads(getConcurrentThreads());
//        config.setSorting(getSorting());
//        config.setIlpSolver(getILPSolver());
//        config.setUsePartialDataAlignments(getIsUsePartialDataAlignments());
//        config.setVariablesUnassignedMode(getUnassignedMode());
//        config.setQueueingModel(getQueueingModel());
//        config.setMaxCostFactor(getMaxCost());
//        config.setUsePartialOrders(getIsUsePartialOrders());
//
//        config.setKeepControlFlowSearchSpace(getIsKeepControlFlowSearchSpace());
//        config.setKeepDataFlowSearchSpace(getIsKeepDataFlowSearchSpace());
//        config.setSearchMethod(getSearchMethod());
//        config.setMaxQueuedStates(getMaxQueuedStates());
//        return config;
//    }
//
//    public static TransEvClassMapping queryActivityMapping(UIPluginContext context, DataPetriNet net, XLog log)
//            throws UserCancelledException {
//        try {
//            EvClassLogPetrinetConnection conn = getFirstConnection(context.getConnectionManager(),
//                    EvClassLogPetrinetConnection.class, net, log);
//            return (TransEvClassMapping) conn.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);
//        } catch (ConnectionCannotBeObtained e) {
//            return ConfigurationHelper.queryActivityEventClassMapping(context, net, log);
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
//
//    public static void queryControlFlowAlignmentConfig(UIPluginContext context, PetrinetGraph net, XLog log,
//                                                       DataConformancePlusConfiguration config, XEventClasses eventClasses) throws UserCancelledException {
//        CostBasedCompleteParam userConfig = new PNReplayer().getConfiguration(context, net, log, eventClasses);
//        if (userConfig != null) {
//            config.setInitialMarking(userConfig.getInitialMarking());
//            config.setFinalMarkings(userConfig.getFinalMarkings());
//            config.setMapEvClass2Cost(userConfig.getMapEvClass2Cost());
//            config.setMapTrans2Cost(userConfig.getMapTrans2Cost());
//        } else {
//            throw new UserCancelledException();
//        }
//    }
//
//    public static void queryControlFlowAlignmentConfig(final UIPluginContext context, PetrinetGraph net, XLog log,
//                                                       DataConformancePlusConfiguration config) throws UserCancelledException {
//        queryControlFlowAlignmentConfig(context, net, log, config,
//                XUtils.createEventClasses(config.getActivityMapping().getEventClassifier(), log));
//    }
//
//    public static class VariableBounds {
//
//        public Map<String, Object> upperBounds;
//        public Map<String, Object> lowerBounds;
//
//    }
//
//    public static void queryDataAlignmentConfig(final UIPluginContext context, DataPetriNet net, XLog log,
//                                                TransEvClassMapping activityMapping, DataConformancePlusConfiguration config)
//            throws UserCancelledException {
//
//        Set<String> attributeNames;
//        if (log.getInfo(activityMapping.getEventClassifier()) != null) {
//            XLogInfo info = log.getInfo(activityMapping.getEventClassifier());
//            attributeNames = ImmutableSet.copyOf(info.getEventAttributeInfo().getAttributeKeys());
//        } else {
//            attributeNames = XUtils.getEventAttributeKeys(log);
//        }
//
//        VariableMappingImporter<String, String> variableMapping = queryVariableMapping(context, net, attributeNames);
//        config.setVariableMapping(variableMapping.getMapping(false));
//
//        VariableMatchCosts variableCosts = queryVariableMatchCosts(context, activityMapping,
//                variableMapping.getMapping(true));
//        config.setVariableCost(variableCosts);
//
//        VariableBounds variableBounds = queryVariableBounds(context, net, log, config.getVariableMapping());
//
//        config.setUpperBounds(variableBounds.upperBounds);
//        config.setLowerBounds(variableBounds.lowerBounds);
//    }
//
//    public static VariableMappingImporter<String, String> queryVariableMapping(final UIPluginContext context,
//                                                                               final DataPetriNet net, final Set<String> eventEttributeKeys) throws UserCancelledException {
//
//        TreeSet<String> processAttributes = new TreeSet<>();
//        for (DataElement variable : net.getVariables()) {
//            processAttributes.add(variable.getVarName());
//        }
//
//        VariableMappingImporter<String, String> mapVariablePanel = new VariableMappingImporter<>(processAttributes, eventEttributeKeys, true);
////        TaskListener.InteractionResult result = context.showConfiguration("Setup Variable Mapping", mapVariablePanel);
//        return mapVariablePanel;
//    }
//
//    public static VariableMatchCosts queryVariableMatchCosts(final UIPluginContext context,
//                                                             final TransEvClassMapping activityMapping, final Map<String, String> variableMappingWithUnmapped)
//            throws UserCancelledException {
//
//        HashMap<ReplayableTransition, XEventClass> activityMapping2 = new HashMap<>();
//        for (Map.Entry<Transition, XEventClass> entry : activityMapping.entrySet()) {
//            activityMapping2.put(new ReplayableTransition(entry.getKey()), entry.getValue());
//        }
//
//        VariableMatchCostImporter<XEventClass> variablePanel = new VariableMatchCostImporter<>(activityMapping2,
//                variableMappingWithUnmapped);
//
////        TaskListener.InteractionResult result = context.showConfiguration("Cost of Deviations in Data Perspective", variablePanel);
////        if (result == TaskListener.InteractionResult.CANCEL) {
////            throw new UserCancelledException();
////        }
//
//        return variablePanel.getCosts();
//    }
//
//    public static VariableBounds queryVariableBounds(final UIPluginContext context, final DataPetriNet net,
//                                                     final XLog log, final Map<String, String> variableMapping) throws UserCancelledException {
//        context.log(
//                "Derive appropriate bounds for the ILP problems based on the values seen in the Event Log. This iterates through the whole Event log and may take a while ...");
//        CheckVariableBoundsImporter variableBoundsPanel = new CheckVariableBoundsImporter(net, log, variableMapping);
//        context.log("Finished deriving variable bounds.");
//
////        TaskListener.InteractionResult result = context.showConfiguration("Configure Variable Bounds", variableBoundsPanel);
////        if (result == TaskListener.InteractionResult.CANCEL) {
////            throw new UserCancelledException();
////        }
//
//        Map<String, Object> upperBounds = new HashMap<>();
//        Map<String, Object> lowerBounds = new HashMap<>();
//
//        for (DataElement elem : variableBoundsPanel.getDataElements()) {
//            Object value = elem.getMinValue();
//            if (value != null)
//                lowerBounds.put(elem.getVarName(), value);
//            value = elem.getMaxValue();
//            if (value != null)
//                upperBounds.put(elem.getVarName(), value);
//        }
//
//        VariableBounds variableBounds = new VariableBounds();
//        variableBounds.upperBounds = upperBounds;
//        variableBounds.lowerBounds = lowerBounds;
//        return variableBounds;
//    }
//
//
//}
