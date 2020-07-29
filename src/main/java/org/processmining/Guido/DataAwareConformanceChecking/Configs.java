package org.processmining.Guido.DataAwareConformanceChecking;


import com.google.gson.JsonObject;
import nl.tue.astar.AStarThread;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration;
import org.processmining.plugins.balancedconformance.controlflow.adapter.SearchMethod;
import org.processmining.plugins.balancedconformance.dataflow.DataAlignmentAdapter;

import java.util.Map;

public class Configs {
    private static final int MAXLIMMAXNUMINSTANCES = 100_000_001;
    private static final int DEFLIMMAXNUMINSTANCES = 100_000_001;

    public static JsonObject getDefaultValues() {
        JsonObject json = new JsonObject();
        json.addProperty("maxThreads", Runtime.getRuntime().availableProcessors());
        json.addProperty("maxSearchSpace", MAXLIMMAXNUMINSTANCES);
        json.addProperty("defaultSearchSpace", DEFLIMMAXNUMINSTANCES);
        return json;
    }

    private String algorithm;
    private boolean balanced;
    private boolean cache;
    private boolean checked;
    private boolean evaluation;
    private double fitness;
    private boolean forced;
    private boolean keep_control;
    private boolean keep_data;
    private String milp;
    private String moves_ordering;
    private boolean optimization;
    private boolean partialOrder = false;
    private String queueing;
    int search_space;
    private boolean startComplete;
    int threads;
    private String unassigned;

    public Configs(String algorithm, boolean balanced, boolean cache, boolean checked, boolean evaluation, Double fitness, boolean forced,
                   boolean keep_control, boolean keep_data, String milp, String moves_ordering, boolean optimization,
                   boolean partialOrder, String queueing, int search_space, boolean startComplete, int threads,
                   String unassigned) {
        this.algorithm = algorithm;
        this.balanced = balanced;
        this.cache = cache;
        this.checked = checked;
        this.evaluation = evaluation;
        this.forced = forced;
        this.fitness = fitness;
        this.keep_control = keep_control;
        this.keep_data = keep_data;
        this.milp = milp;
        this.moves_ordering = moves_ordering;
        this.optimization = optimization;
        this.partialOrder = partialOrder;
        this.queueing = queueing;
        this.search_space = search_space;
        this.startComplete = startComplete;
        this.threads = threads;
        this.unassigned = unassigned;
    }

    public int getMaxQueuedStates() {
        return search_space == MAXLIMMAXNUMINSTANCES ? Integer.MAX_VALUE : search_space;
    }


    public ConstraintsBalancedConfiguration createConfig() {
        ConstraintsBalancedConfiguration config = new ConstraintsBalancedConfiguration();
        config.setActivateDataViewCache(cache);
        config.setUseOptimizations(optimization);
        config.setConcurrentThreads(threads);
        config.setSorting(AStarThread.ASynchronousMoveSorting.valueOf(moves_ordering));
        config.setIlpSolver(DataAlignmentAdapter.ILPSolver.valueOf(milp.toString()));
        config.setUsePartialDataAlignments(balanced);
        config.setVariablesUnassignedMode(BalancedProcessorConfiguration.UnassignedMode.valueOf(unassigned));
        config.setQueueingModel(AStarThread.QueueingModel.valueOf(queueing));
        config.setMaxCostFactor(fitness);
        config.setUsePartialOrders(partialOrder);

        config.setKeepControlFlowSearchSpace(keep_control);
        config.setKeepDataFlowSearchSpace(keep_data);
        config.setSearchMethod(SearchMethod.valueOf(algorithm));
        config.setMaxQueuedStates(getMaxQueuedStates());
        config.setEvaluationMode(evaluation);
        config.setStartComplete(startComplete);
        config.setEvaluationForced(forced);
        return config;
    }
}
