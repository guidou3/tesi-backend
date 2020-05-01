package org.processmining.graphvisualizers.plugins;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.VisualizeAcceptingPetriNetPlugin;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarray.models.graph.ActivityClusterArrayGraph;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.causalactivitygraph.models.graph.CausalActivityGraphGraph;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.datapetrinets.DataPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.graphvisualizers.algorithms.GraphVisualizerAlgorithm;
import org.processmining.graphvisualizers.parameters.GraphVisualizerParameters;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.bpmn.BPMNVisualization;
import org.processmining.plugins.petrinet.PetriNetVisualization;
import org.processmining.plugins.transitionsystem.MinedTSVisualization;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

public class GraphVisualizerPlugin extends GraphVisualizerAlgorithm {

	public static String runUI(PluginContext context, DataPetriNet net) {
		return runUI(context, net, new GraphVisualizerParameters());
	}

	public static String runUI(PluginContext context, BPMNDiagram net) {
		return runUI(context, net, new GraphVisualizerParameters());
	}

	private static String runUI(PluginContext context, PetrinetGraph graph, GraphVisualizerParameters parameters) {
		if (graph instanceof Petrinet) {
			/*
			 * It's a proper Petri net. Get a hold on the view specific
			 * attributes.
			 */
			PetriNetVisualization visualizer = new PetriNetVisualization();
			ProMJGraphPanel panel = (ProMJGraphPanel) visualizer.visualize(context, (Petrinet) graph);
			ProMJGraph jGraph = panel.getGraph();
			ViewSpecificAttributeMap map = jGraph.getViewSpecificAttributes();
			Petrinet net = (Petrinet) graph;
			for (Place place : net.getPlaces()) {
				map.putViewSpecific(place, GVPLACELABEL, place.getLabel());
			}
			/*
			 * Got it. Now create the dot panel.
			 */
			return apply(context, graph, map, parameters);
		}
		return apply(context, graph, parameters);
	}

	private static String runUI(PluginContext context, BPMNDiagram graph, GraphVisualizerParameters parameters) {

//		ProMJGraphPanel panel = (ProMJGraphPanel) BPMNVisualization.visualize(context, (BPMNDiagram) graph.getGraph());
//		ProMJGraphPanel panel = ProMJGraphVisualizer.instance().visualizeGraph(context, graph);
		ProMJGraph jGraph = BPMNGraphVisualizer.instance().visualizeGraph(context, graph);
		try {
			BufferedImage bi = jGraph.getImage(Color.WHITE, 5);
			ImageIO.write(bi, "jpg", new File("myImage.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//			ProMJGraph jGraph = panel.getGraph();
		ViewSpecificAttributeMap map = jGraph.getViewSpecificAttributes();

		for (Activity activity : graph.getActivities()) {
			map.putViewSpecific(activity, GVPLACELABEL, activity.getLabel());
		}
		/*
		 * Got it. Now create the dot panel.
		 */
		return apply(context, jGraph.getProMGraph(), map, parameters);
	}

	public static String runUI(PluginContext context, TransitionSystem graph) {
		/*
		 * Get a hold on the view specific attributes.
		 */
		ProMJGraphPanel panel = (ProMJGraphPanel) (new MinedTSVisualization()).visualize(context, graph);
		ProMJGraph jGraph = panel.getGraph();
		ViewSpecificAttributeMap map = jGraph.getViewSpecificAttributes();
		/*
		 * Got it. Now create the dot panel.
		 */
		return apply(context, graph, map);
	}

	public static String runUI(PluginContext context, AcceptingPetriNet graph) {
		/*
		 * Get a hold on the view specific attributes.
		 */
		ProMJGraphPanel panel = (ProMJGraphPanel) VisualizeAcceptingPetriNetPlugin.visualize(context, graph);
		ProMJGraph jGraph = panel.getGraph();
		ViewSpecificAttributeMap map = jGraph.getViewSpecificAttributes();
		/*
		 * Got it. Now create the dot panel.
		 */
		return apply(context, graph.getNet(), map);
	}

	public static String runUI(PluginContext context, ActivityClusterArray clusters) {
		return apply(context, new ActivityClusterArrayGraph(context, clusters));
	}

	public static String runUI(PluginContext context, CausalActivityGraph graph) {
		return apply(context, new CausalActivityGraphGraph(context, graph));
	}

	public static String runUI(PluginContext context, MutableFuzzyGraph graph) {
		return apply(context, graph);
	}

}
