package org.processmining.Guido;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.google.gson.JsonObject;
import org.deckfour.xes.classification.*;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.jsoup.Jsoup;
import org.processmining.Guido.CustomElements.*;
import org.processmining.Guido.DataAwareConformanceChecking.*;
import org.processmining.Guido.InOut.ControlFlowViolationCosts;
import org.processmining.Guido.InOut.PnmlExporter;
import org.processmining.Guido.InOut.VariableBoundsEntry;
import org.processmining.Guido.InOut.VariableMatchCostEntry;
import org.processmining.Guido.Result.AlignmentGroupNew;
import org.processmining.Guido.Result.GroupOutput;
import org.processmining.Guido.converters.*;
import org.processmining.Guido.importers.*;
import org.processmining.Guido.mapping.*;
import org.processmining.contexts.uitopia.DummyUIPluginContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.datapetrinets.exception.NonExistingVariableException;
import org.processmining.datapetrinets.utils.MarkingsHelper;
import org.processmining.datapetrinets.visualization.graphviz.DPNGraphvizConverter;
import org.processmining.datapetrinets.visualization.graphviz.DPNGraphvizConverterPlugin;
import org.processmining.framework.connections.*;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.events.Logger;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.graphvisualizers.plugins.GraphVisualizerPlugin;
import org.processmining.log.utils.XUtils;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithDataFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.DataConformance.Alignment;
import org.processmining.plugins.DataConformance.DataAlignment.PetriNet.ReplayableTransition;
import org.processmining.plugins.DataConformance.ResultReplay;
import org.processmining.plugins.DataConformance.framework.VariableMatchCosts;
import org.processmining.plugins.DataConformance.visualization.alignment.XTraceResolver;
import org.processmining.plugins.DataConformance.visualization.grouping.GroupedAlignments;
import org.processmining.plugins.DataConformance.visualization.grouping.GroupedAlignments.AlignmentGroup;
import org.processmining.plugins.DataConformance.visualization.grouping.GroupedAlignmentsSimpleImpl;
import org.processmining.plugins.DataConformance.visualization.grouping.GroupedAlignmentsSimpleImpl.GroupingFunction;
import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration;
import org.processmining.plugins.balancedconformance.controlflow.ControlFlowAlignmentException;
import org.processmining.plugins.balancedconformance.dataflow.exception.DataAlignmentException;
import org.processmining.plugins.balancedconformance.export.XAlignmentConverter;
import org.processmining.plugins.balancedconformance.observer.DataConformancePlusObserverImpl;
import org.processmining.plugins.balancedconformance.result.AlignmentCollection;
import org.processmining.plugins.balancedconformance.result.BalancedReplayResult;
import org.processmining.plugins.bpmn.Bpmn;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.xesalignmentextension.XAlignmentExtension;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignment;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.List;

import static org.processmining.Guido.utils.Utils.*;
import static org.processmining.plugins.DataConformance.visualization.alignment.ColorTheme.createColorMap;

public class ConformanceChecker {
    private File modelFile;
    private File customElementsFile;
    private File logFile;

    private BPMNDiagram modelBpmn;
    private DataPetriNetWithCustomElements net;
    private DataPetriNetWithCustomElements customNet;
    private XLog log;
    private XLog customLog;
    private CustomElements customElements;

    private DummyUIPluginContext context;

    private DPNConverter dpnConverter;
    private BpmnToDpnMapping bpmnToDpnMapping;

    private FinalMapping finalMapping;
    private VariableMapping variableMapping;
    private ConstraintsBalancedConfiguration config;
    private PNReplayer replayer;
    private VariableMatchCostImporter matchCostImporter;
    private BalancedReplayResult result;

    private boolean hasCustomElements;

    public ConformanceChecker() {
        PluginManagerImpl.initialize(UIPluginContext.class);
        context = new DummyUIPluginContext();
    }

    public ConformanceChecker(boolean bool) {
        this();
        try {
//            setModelDpn(new File("./data/models/prova.pnml"));
            setModelBpmn(new File("./data/models/prova.bpmn"));
            convertBpmnToDpn();
            hasCustomElements = bool;

            setLog(new File("./data/logs/prova.xes"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setModelBpmn(String s) throws Exception {
        setModelBpmn(new File(s));
    }

    public void setModelBpmn(File m) throws Exception {
        modelFile = m;
        BpmnImporter bpmnImporter = new BpmnImporter();
        Bpmn bpmn = bpmnImporter.importFromStream(context, new FileInputStream(m), m.getName());

        if(bpmn == null) throw new Exception("Bpmn not loaded");

        modelBpmn = bpmnImporter.bpmnToDiagram();
        bpmnToDpnMapping = new BpmnToDpnMapping();
        bpmnToDpnMapping.initializeFirst(bpmnImporter.getMap());
    }

    public void setLog(String s) throws Exception {
        setLog(new File(s));
    }

    public void setLog(File l) throws Exception {
        logFile = l;
        log = LogImporter.importLog(context, logFile);
    }

    public void exportLog(String name) {
        try {
            // name = "newLog.xes"
            File file = new File(name);
            FileOutputStream out = new FileOutputStream(file);
            XSerializer logSerializer = new XesXmlSerializer();
            logSerializer.serialize(log, out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void extractLogPositions() {
        Multimap<Transition, String> logPositions = HashMultimap.create();
        XEventClassifier classifier = finalMapping.getClassifier();

        Map<String, Transition> idToTran = new HashMap<>();

        for(Map.Entry<Transition, XEventClass> entry : finalMapping.getMap().entrySet())
            idToTran.put(entry.getValue().getId(), entry.getKey());

        for (XTrace oldTrace : log) {
            for (XEvent event : oldTrace) {
                Transition t = idToTran.get(classifier.getClassIdentity(event));
                String lifecycleState = DPNConverter.getLifecycle(event);
                if(!logPositions.containsEntry(t, lifecycleState))
                    logPositions.put(t, lifecycleState);
            }
        }

        net.setTransitionLogPositions(logPositions.asMap());
    }

    public void setCustomElements(String s) {
        setCustomElements(new File(s));
    }

    public void setCustomElements(File ce) {
        customElementsFile = ce;
        customElements = JsonImporter.importJson(ce);

        if(net == null)
            convertBpmnToDpn();

        assert customElements != null;
        customElements.initialize(net, bpmnToDpnMapping.getBpmnToDpn());
    }

    public void convertBpmnToDpn() {
        Bpmn2Dpn bpmn2DpnConverter = new Bpmn2Dpn();
        // convert bpmn to data petrinet
        net = bpmn2DpnConverter.convert(context, modelBpmn);

        bpmn2DpnConverter.updateMapping(bpmnToDpnMapping);
    }

    public void exportDpn(DataPetriNet dataPetriNet, String name) {
        if(dataPetriNet == null) return;

        try {
            new PnmlExporter().exportPetriNetToPNMLFile(context, dataPetriNet, new File(name));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void customDpnConversion() throws NonExistingVariableException {
        dpnConverter = new DPNConverter(config);
        customNet = dpnConverter.convertDpn(net, customElements);
    }

    public void updateFinalMapping1() {
        dpnConverter.updateFinalMapping1(finalMapping);
    }

    public void updateFinalMapping2() {
        dpnConverter.updateFinalMapping2(finalMapping);
    }

    public void convertLog() {
        customLog = dpnConverter.convertLog(log, finalMapping.getMap());
    }

    // ----------------------------------------
    // * * * QUERY AND POST CONFIGURATION * * *
    // ----------------------------------------

    public JsonObject getDefaultParameters() {
        return Configs.getDefaultValues();
    }

    public void setConfigs(Configs c) throws Exception {
        config = c.createConfig();


    }

    public InitialMapping getInitialMapping() {
        return new InitialMapping(log, net);
    }

    public void setMapping(FinalMapping mapping) throws NonExistingVariableException {
        finalMapping = mapping;

        finalMapping.setLabelToTransition(getLabelToTransition());
        finalMapping.labelToXEventClasses(log);

        replayer = new PNReplayer(net);

        if(hasCustomElements) {
            extractLogPositions();
            exportDpn(net, "prova1.pnml");
            setCustomElements("./data/customElements/customElements.json");
            customDpnConversion();

//            CyclesFinder cf = new CyclesFinder(net);
//            cf.findCycles();

            updateFinalMapping1();
            convertLog();
//            exportLog();

            updateFinalMapping2();
        }

        String logName = XConceptExtension.instance().extractName(customLog);
        String label = "Connection between " + customNet.getLabel() + " and " + (logName != null ? logName : "unnamed log");
        EvClassLogPetrinetConnection con = new EvClassLogPetrinetConnection(label, customNet, customLog, finalMapping.getClassifier(), finalMapping.getMap());
        context.addConnection(con);

    }

    public ControlFlowViolationCosts queryControlFlowCost() {
        TransEvClassMapping activityMapping = queryActivityMapping(customNet, customLog);
        config.setActivityMapping(activityMapping);
        config.setObserver(new DataConformancePlusObserverImpl(context));

        XEventClassifier eventClassifier = activityMapping.getEventClassifier();
        XEventClasses eventClasses = XUtils.createEventClasses(eventClassifier, customLog);

        replayer.configureMarkings(context, customNet);

        return replayer.getControlFlowCost(context, customNet, customLog, eventClasses).toControlFlowViolationCosts();

//        Configs.queryControlFlowAlignmentConfig(context, net, log, config, eventClasses);
    }

    public void postControlFlowCost(ControlFlowViolationCosts costs) {
        replayer.setControlFlowCost(new ControlFlowCost(costs.getModelTable(), costs.getLogTable()));

        CostBasedCompleteParam userConfig = replayer.getConfiguration(context, customNet);
        config.setInitialMarking(userConfig.getInitialMarking());
        config.setFinalMarkings(userConfig.getFinalMarkings());
        config.setMapEvClass2Cost(userConfig.getMapEvClass2Cost());
        config.setMapTrans2Cost(userConfig.getMapTrans2Cost());
    }

    // -------------------------------------
    // * * * * * DATA PERSPECTIVE * * * * *
    // -------------------------------------

    // TODO: from front-end use this method to skip (or not) the following steps
    public boolean queryDataPerspective() {
        return !customNet.getVariables().isEmpty();
    }

    public InitialVariableMapping getInitialVariableMapping() {

        // if there are no variables in the net return null
        if (customNet.getVariables().isEmpty()) {
            return null;
        }

        TransEvClassMapping activityMapping = finalMapping.getMap();

        Set<String> attributeNames;
        if (customLog.getInfo(activityMapping.getEventClassifier()) != null) {
            XLogInfo info = customLog.getInfo(activityMapping.getEventClassifier());
            attributeNames = ImmutableSet.copyOf(info.getEventAttributeInfo().getAttributeKeys());
        } else {
            attributeNames = XUtils.getEventAttributeKeys(customLog);
        }

        TreeSet<String> processAttributes = new TreeSet<>();
        for (DataElement variable : customNet.getVariables())
            processAttributes.add(variable.getVarName());

        InitialVariableMapping variableMapping = new InitialVariableMapping(processAttributes, attributeNames);
        customNet.setCustomVariableMapping(variableMapping.extractCustomVariableMapping());
        return variableMapping;
    }

    public void setVariableMapping(Map<String, String> map) {
        map.putAll(customNet.getCustomVariableMapping());
        variableMapping = new VariableMapping(map);
        config.setVariableMapping(variableMapping.getMapping(false));
    }

    // only if !net.getVariables().isEmpty()
    public VariableMatchCostImporter.Input queryVariableMathCost() {
        HashMap<ReplayableTransition, XEventClass> activityMapping = new HashMap<>();
        for (Map.Entry<Transition, XEventClass> entry : finalMapping.getMap().entrySet()) {
            activityMapping.put(new ReplayableTransition(entry.getKey()), entry.getValue());
        }

        matchCostImporter = new VariableMatchCostImporter(activityMapping, variableMapping.getMapping(true));
        return matchCostImporter.generateFrontEndContent();
    }

    // only if !net.getVariables().isEmpty()
    public void postVariableMatchCost(List<VariableMatchCostEntry> entries) {
        matchCostImporter.setTable(entries);
        config.setVariableCost(matchCostImporter.getCosts());
    }

    // only if !net.getVariables().isEmpty()
    public CheckVariableBoundsImporter queryVariableBounds() {
        return new CheckVariableBoundsImporter(customNet, customLog, config.getVariableMapping());
    }

    // only if !net.getVariables().isEmpty()
    public void postVariableBounds(List<VariableBoundsEntry> list) {
//        Configs.VariableBounds variableBounds = queryVariableBounds(context, net, log, config.getVariableMapping());
        CheckVariableBoundsImporter importer = new CheckVariableBoundsImporter(list);

        Map<String, Object> upperBounds = new HashMap<>();
        Map<String, Object> lowerBounds = new HashMap<>();
        Set<DataElement> dataElements = importer.getDataElements();

        customNet.fixDataElementsTypes(dataElements);
        exportDpn(customNet, "prova2.pnml");
//        fixDataElements(dataElements);

        for (DataElement elem : dataElements) {
            Object value = elem.getMinValue();
            if (value != null)
                lowerBounds.put(elem.getVarName(), value);
            value = elem.getMaxValue();
            if (value != null)
                upperBounds.put(elem.getVarName(), value);
        }

        config.setUpperBounds(upperBounds);
        config.setLowerBounds(lowerBounds);
    }

    // ------------------------------------
    // * * * * * * FINAL STEPS * * * * * *
    // ------------------------------------

    public void queryConfiguration() throws Exception {
        if (net.getVariables().isEmpty())  {
            VariableMatchCosts variableCost = BalancedProcessorConfiguration.createDefaultVariableCost(customNet,
                    Collections.<String>emptySet(), 0, 0);
            config.setVariableCost(variableCost);
            config.setVariableMapping(Collections.<String, String>emptyMap());

            if (!BalancedDataConformance.hasGuards(customNet))
                throw new Exception("Variables/guards missing\nSelected DPN-net does not define variables/guards. Alignment will not consider data!");
        }

    }

    public Progress dobBlancedDataConformance() throws ControlFlowAlignmentException, DataAlignmentException {
        result = new BalancedDataConformance().balancedAlignmentPluginHeadless(context, customNet, customLog, config);
        return context.getProgress();
    }

    public List<GroupOutput> getGroups() {
        if(result == null) return null;
        XAlignmentConverter converter = new XAlignmentConverter();
        XTraceResolver traceResolver = buildTraceMap(result);
        converter.setClassifier(result.getClassifier());
        converter.setVariableMapping(result.getVariableMapping());
        Iterable<XAlignmentExtension.XAlignment> xAlignments = convertToXAlignment(result, converter, traceResolver);

        // crea funzione di grouping (mi serve per poter utilizzare AlignmentGroupNew come definizione dei gruppi)
        Map<String, Color> activityColorMap = createColorMap(xAlignments);
        GroupingFunction<XAlignment, AlignmentGroup> function = (a, groupedAlignments) ->
                new AlignmentGroupNew(a, groupedAlignments, activityColorMap, config.isEvaluationMode());

        GroupedAlignments<XAlignment> groupedAlignments = new GroupedAlignmentsSimpleImpl(function, xAlignments);
        Set<AlignmentGroup> groups = groupedAlignments.getAlignmentGroups();

        List<GroupOutput> output = new ArrayList<>();
        for(AlignmentGroup group : groups) {
            output.add(((AlignmentGroupNew) group).getOutput());
        }

        return output;
    }

    private XTraceResolver buildTraceMap(ResultReplay logReplayResult) {
        final Map<String, XTrace> traceMap = new HashMap<>();
        for (XTrace trace : logReplayResult.getAlignedLog()) {
            traceMap.put(XConceptExtension.instance().extractName(trace), trace);
        }
        return new XTraceResolver() {

            public boolean hasOriginalTraces() {
                return true;
            }

            public XTrace getOriginalTrace(String name) {
                return traceMap.get(name);
            }
        };
    }

    private Iterable<XAlignment> convertToXAlignment(AlignmentCollection alignments, final XAlignmentConverter converter,
                                                     final XTraceResolver resolver) {
        return ImmutableList.copyOf(Iterables.transform(alignments.getAlignments(), new Function<Alignment, XAlignment>() {

            public XAlignmentExtension.XAlignment apply(Alignment a) {
                return converter.viewAsXAlignment(a, resolver.getOriginalTrace(a.getTraceName()));
            }
        }));
    }

    // -------------------------------------
    // * * * * * GRAPHIC RENDERERS * * * * *
    // -------------------------------------

    public String renderDot(int i) {
        if(i==2 &&result != null) {
            DataPetriNet original = result.getNet();
            PetriNetWithDataFactory factory=new PetriNetWithDataFactory(original, original.getLabel());

            final DataPetriNet cloneNet=factory.getRetValue();
            for(DataElement elem : original.getVariables()) {
                cloneNet.addVariable(elem.getVarName(), elem.getType(), elem.getMinValue(), elem.getMaxValue());
            }
            for(Transition tOrig : original.getTransitions()) {
                PNWDTransition tNew = (PNWDTransition) factory.getTransMapping().get(tOrig);
                if (tOrig instanceof PNWDTransition) {
                    for(DataElement elem : ((PNWDTransition)tOrig).getReadOperations()) {
                        cloneNet.assignReadOperation(tNew, cloneNet.getVariable(elem.getVarName()));
                    }
                    for(DataElement elem : ((PNWDTransition)tOrig).getWriteOperations()) {
                        cloneNet.assignWriteOperation(tNew, cloneNet.getVariable(elem.getVarName()));
                    }
                }
            }
            for(Transition node : cloneNet.getTransitions()) {
                if (node.isInvisible()) {
                    node.getAttributeMap().remove(AttributeMap.TOOLTIP);
                    node.getAttributeMap().put(AttributeMap.FILLCOLOR, new Color(0,0,0,127));
                }
                else {
                    float[] actArray =result.actArray.get(node.getLabel());
                    if (actArray!=null) {
                        float value;
                        if (actArray[0]+actArray[1]==0 || ((PNWDTransition)node).getWriteOperations().size()==0)
                            value=(actArray[0]+actArray[1])/(actArray[0]+actArray[1]+actArray[2]+actArray[3]);
                        else {
                            float dataFlowValue=actArray[0]/(actArray[0]+actArray[1]);
                            float controlFlowValue=(actArray[0]+actArray[1])/(actArray[0]+actArray[1]+actArray[2]+actArray[3]);
                            value=2*dataFlowValue*controlFlowValue/(dataFlowValue+controlFlowValue);
                        }
                        Color fillColor=getColorForValue(value);
                        Color textColor=new Color(255-fillColor.getRed(),255-fillColor.getGreen(),255-fillColor.getBlue());
                        node.getAttributeMap().put(AttributeMap.FILLCOLOR, fillColor);
                        node.getAttributeMap().put(AttributeMap.LABELCOLOR, textColor);
                        String tooltip = "<html><table><tr><td><b>Number moves in both without incorrect write operations:</b> " + (int) actArray[0] +
                                "</td></tr><tr><td><b>Number moves in both with incorrect write operations:</b> " +
                                (int) actArray[1] +
                                "</td></tr><tr><td><b>Number moves in log:</b> " +
                                (int) actArray[2] +
                                "</td></tr><tr><td><b>Number moves in model:</b> " +
                                (int) actArray[3] +
                                "</td></tr></table></html>";
                        node.getAttributeMap().put(AttributeMap.TOOLTIP, tooltip);
                    }
                }
            }
            for(DataElement node : cloneNet.getVariables()) {
                float[] attrArray = result.attrArray.get(node.getVarName());
                if (attrArray != null) {
                    float value=1-(attrArray[1]+attrArray[4])/(attrArray[0]+attrArray[1]+attrArray[4]);
                    Color fillColor=getColorForValue(value);
                    node.getAttributeMap().put(AttributeMap.FILLCOLOR, fillColor);
                    String tooltip = "<html><table><tr><td><b>Number of correct write operations:</b> " + (int) attrArray[0] +
                            "</td></tr><tr><td><b>Number of wrong write operations:</b> " +
                            (int) attrArray[4] +
                            "</td></tr><tr><td><b>Number of missing write operations:</b> " +
                            (int) attrArray[1] +
                            "</td></tr></table></html>";
                    node.getAttributeMap().put(AttributeMap.TOOLTIP, tooltip);
                }
                else
                    context.log("Could not get statistics for variable "+node.getVarName(), Logger.MessageLevel.WARNING);
            }
            return GraphVisualizerPlugin.runUI(context, cloneNet);
        }
        if(i>=1 && customNet != null)
            return GraphVisualizerPlugin.runUI(context, customNet);
        else if(i>=0 && net != null)
            return GraphVisualizerPlugin.runUI(context, net);
        else
            return "";

    }

    public String renderDot2() {
        Marking initialMarking = MarkingsHelper.getInitialMarkingOrEmpty(context, net);
        Marking[] finalMarkings = MarkingsHelper.getFinalMarkingsOrEmpty(context, net);
        DPNGraphvizConverter.DPNAsDot dpnAsDot = DPNGraphvizConverter.convertDPN(net, initialMarking, finalMarkings,
                DPNGraphvizConverterPlugin.GuardDisplayMode.EDGES, DPNGraphvizConverterPlugin.VariableDisplayMode.AUTO_LAYOUT,
                DPNGraphvizConverterPlugin.PlaceDisplayMode.BASIC, Dot.GraphDirection.leftRight,
                Collections.<AbstractGraphElement, Map<DPNGraphvizConverter.DecorationKey, Object>>emptyMap());
        String s = dpnAsDot.getDot().toString()
                .replaceAll("label=<", "label = \"")
                .replaceAll("<BR/>", " ")
                .replaceAll("> id", "\" id")
                .replaceAll(">, id", "\", id")
                .replaceAll("<B>", "<b>")
                .replaceAll("</B>", "</b>");
        return Jsoup.parse(s).text();
    }

    public String renderBpmnDot() {
        if(result != null) {
            HashMap<Transition, BPMNNode> map = bpmnToDpnMapping.getDpnToBpmn();
            for(Transition node : net.getTransitions()) {
                if (!node.isInvisible()){
                    float[] actArray =result.actArray.get(node.getLabel());
                    if (actArray!=null) {
                        float value;
                        if (actArray[0]+actArray[1]==0 || ((PNWDTransition)node).getWriteOperations().size()==0)
                            value=(actArray[0]+actArray[1])/(actArray[0]+actArray[1]+actArray[2]+actArray[3]);
                        else {
                            float dataFlowValue=actArray[0]/(actArray[0]+actArray[1]);
                            float controlFlowValue=(actArray[0]+actArray[1])/(actArray[0]+actArray[1]+actArray[2]+actArray[3]);
                            value=2*dataFlowValue*controlFlowValue/(dataFlowValue+controlFlowValue);
                        }
                        Color fillColor=getColorForValue(value);
                        Color textColor=new Color(255-fillColor.getRed(),255-fillColor.getGreen(),255-fillColor.getBlue());
                        BPMNNode bpmnNode = map.get(node);
                        bpmnNode.getAttributeMap().put(AttributeMap.FILLCOLOR, fillColor);
                        bpmnNode.getAttributeMap().put(AttributeMap.LABELCOLOR, textColor);
                        String tooltip = "<html><table><tr><td><b>Number moves in both without incorrect write operations:</b> " + (int) actArray[0] +
                                "</td></tr><tr><td><b>Number moves in both with incorrect write operations:</b> " +
                                (int) actArray[1] +
                                "</td></tr><tr><td><b>Number moves in log:</b> " +
                                (int) actArray[2] +
                                "</td></tr><tr><td><b>Number moves in model:</b> " +
                                (int) actArray[3] +
                                "</td></tr></table></html>";
                        bpmnNode.getAttributeMap().put(AttributeMap.TOOLTIP, tooltip);
                    }
                }
            }
        }

        return GraphVisualizerPlugin.runUI(context, modelBpmn);
    }

    // -----------------------------------
    // * * * * * PRIVATE METHODS * * * * *
    // -----------------------------------

    private Map<String, Transition> getLabelToTransition() {
        Map<String, Transition> labelToTransition = new HashMap<>();
        for(Transition t : net.getTransitions()) {
            labelToTransition.put(findNewString(labelToTransition, t.getLabel()), t);
        }

        return labelToTransition;
    }

    private TransEvClassMapping queryActivityMapping(DataPetriNet net, XLog log) {
        try {
            EvClassLogPetrinetConnection conn = getFirstConnection(net, log);
            return (TransEvClassMapping) conn.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);
        } catch (ConnectionCannotBeObtained e) {
            return finalMapping.getMap();

        }
    }

    private EvClassLogPetrinetConnection getFirstConnection(Object... objects) throws ConnectionCannotBeObtained {
        ConnectionManager connectionManager = context.getConnectionManager();
        for (ConnectionID connID : connectionManager.getConnectionIDs()) {
            Connection c = connectionManager.getConnection(connID);
            if (EvClassLogPetrinetConnection.class.isAssignableFrom(c.getClass()) && c.containsObjects(objects)) {
                return (EvClassLogPetrinetConnection) c;
            }
        }
        throw new ConnectionCannotBeObtained("Connection not found", EvClassLogPetrinetConnection.class, objects);
    }

}
