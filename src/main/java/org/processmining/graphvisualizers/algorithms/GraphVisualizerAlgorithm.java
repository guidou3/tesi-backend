package org.processmining.graphvisualizers.algorithms;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.xml.crypto.Data;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.graphvisualizers.parameters.GraphVisualizerParameters;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.AttributeMap.ArrowType;
import org.processmining.models.graphbased.AttributeMapOwner;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;
import org.processmining.models.shapes.*;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class GraphVisualizerAlgorithm {

	public static String GVPLACELABEL = "[GV]PlaceLabel";

	/**
	 * Create a JComponent using dot from the given graph.
	 * 
	 * @param context
	 *            The plug-in context. Not that relevant.
	 * @param graph
	 *            The graph to visualize using dot.
	 * @return The JComponent containing the dot visualization of the graph.
	 */
	public static String apply(UIPluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph, Map<String, String> map) {
		return apply(((PluginContext) context), graph, map);

	}

	public static String apply(PluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph, Map<String, String> map) {
		return apply(context, graph, new ViewSpecificAttributeMap(), new GraphVisualizerParameters(), map);

	}

	public static String apply(UIPluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph,
			GraphVisualizerParameters parameters, Map<String, String> map) {
		return apply(((PluginContext) context), graph, parameters, map);

	}

	public static String apply(PluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph,
			GraphVisualizerParameters parameters, Map<String, String> map) {
		return apply(context, graph, new ViewSpecificAttributeMap(), parameters, map);

	}

	public static String apply(UIPluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph,
			ViewSpecificAttributeMap map, Map<String, String> mapL) {
		return apply(((PluginContext) context), graph, map, mapL);
	}

	public static String apply(PluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph,
			ViewSpecificAttributeMap map, Map<String, String> mapL) {
		return apply(context, graph, map, new GraphVisualizerParameters(), mapL);
	}

	public static String apply(UIPluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph,
			ViewSpecificAttributeMap map, GraphVisualizerParameters parameters, Map<String, String> mapL) {
		return apply(((PluginContext) context), graph, map, parameters, mapL);
	}

	public static String apply(PluginContext context,
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<?, ?>> graph,
			ViewSpecificAttributeMap map, GraphVisualizerParameters parameters, Map<String, String> mapL) {
		Dot dot = new Dot();
		Map<DirectedGraphNode, DotNode> nodeMap = new HashMap<DirectedGraphNode, DotNode>();
		for (DirectedGraphNode node : graph.getNodes()) {
			DotNode dotNode = dot.addNode(node.getLabel());
			nodeMap.put(node, dotNode);
			apply(node, dotNode, map, parameters, mapL);
		}
		for (DirectedGraphEdge<? extends DirectedGraphNode, ? extends DirectedGraphNode> edge : graph.getEdges()) {
			DotEdge dotEdge = dot.addEdge(nodeMap.get(edge.getSource()), nodeMap.get(edge.getTarget()));
			apply(edge, dotEdge, map, parameters);
		}

		return dot.toString();
	}

	/*
	 * Copy (as much as possible) the attributes from the JGraph node to the dot
	 * node.
	 */
	private static void apply(DirectedGraphNode node, DotNode dotNode, ViewSpecificAttributeMap map,
			GraphVisualizerParameters parameters, Map<String, String> mapL) {
		AttributeMap attMap = node.getAttributeMap();
		Shape shape = getShape(attMap, AttributeMap.SHAPE, null, node, map);
		String style = "filled";
		if (shape != null) {
			if(node instanceof DataElement) {
				dotNode.setOption("shape", "hexagon");
			}
			else if (shape instanceof RoundedRect) {
				dotNode.setOption("shape", "box");
				style = style + ",rounded";
			} else if (shape instanceof Rectangle) {
				dotNode.setOption("shape", "box");
			} else if (shape instanceof Ellipse) {
				Boolean isSquare = getBoolean(attMap, AttributeMap.SQUAREBB, false, node, map);
				dotNode.setOption("shape", isSquare ? "circle" : "ellipse");
			} else if (shape instanceof Diamond) {
				dotNode.setOption("shape", "diamond");
			} else if (shape instanceof Hexagon) {
				dotNode.setOption("shape", "hexagon");
			} else if (shape instanceof Octagon) {
				dotNode.setOption("shape", "octagon");
			} else if (shape instanceof Polygon) {
				//				attMap.get(AttributeMap.POLYGON_POINTS);
				dotNode.setOption("shape", "polygon");
			}
			dotNode.setOption("style", style);
		}
		Boolean showLabel = getBoolean(attMap, AttributeMap.SHOWLABEL, true, node, map);
		Boolean invisiblePlusGuard = false;
		// HV: Setting a tooltip seems to have no effect.
		String label = getString(attMap, AttributeMap.LABEL, "", node, map);
		String placeLabel = getString(attMap, GVPLACELABEL, "", node, map);
		String tooltip = getString(attMap, AttributeMap.TOOLTIP, "", node, map);
		String internalLabel = getFormattedString(parameters.getInternalLabelFormat(), label, placeLabel, tooltip);
		String externalLabel = getFormattedString(parameters.getExternalLabelFormat(), label, placeLabel, tooltip);
		tooltip = getFormattedString(parameters.getToolTipFormat(), label, placeLabel, tooltip);
		if (showLabel) {
			dotNode.setLabel(internalLabel);
		} else {
			if(mapL.get(node.getId().toString()) != null) {
				dotNode.setLabel(mapL.get(node.getId().toString()));
				invisiblePlusGuard = true;
			}
			else
				dotNode.setLabel("");
		}
		if(node instanceof Gateway) {
			String image;
			Gateway gateway = (Gateway) node;
			Gateway.GatewayType type = gateway.getGatewayType();
			if(type == Gateway.GatewayType.DATABASED)
				image = "x.svg";
			else if(type == Gateway.GatewayType.PARALLEL)
				image = "plus.svg";
			else {
				image = "";
			}
			dotNode.setOption("image", image);
		}
		dotNode.setOption("xlabel", externalLabel);
		dotNode.setOption("tooltip", tooltip);

		Float penWidth = getFloat(attMap, AttributeMap.LINEWIDTH, 1.0F, node, map);
		dotNode.setOption("penwidth", "" + penWidth);
		Color strokeColor = getColor(attMap, AttributeMap.STROKECOLOR, Color.BLACK, node, map);
		dotNode.setOption("color", ColourMap.toHexString(strokeColor));
		Color labelColor = getColor(attMap, AttributeMap.LABELCOLOR, Color.BLACK, node, map);
		dotNode.setOption("fontcolor", ColourMap.toHexString(labelColor));
		Color fillColor = getColor(attMap, AttributeMap.FILLCOLOR, Color.WHITE, node, map);

		if(invisiblePlusGuard)
			dotNode.setOption("fontcolor", ColourMap.toHexString(Color.WHITE));
		
		if(node instanceof DataElement)
			fillColor = Color.LIGHT_GRAY;

		Color gradientColor = getColor(attMap, AttributeMap.GRADIENTCOLOR, fillColor, node, map);
		if (gradientColor == null || gradientColor.equals(fillColor)) {
			dotNode.setOption("fillcolor", ColourMap.toHexString(fillColor));
		} else {
			dotNode.setOption("fillcolor",
					ColourMap.toHexString(fillColor) + ":" + ColourMap.toHexString(gradientColor));
		}

	}

	/*
	 * Copy (as much as possible) the attributes from the JGraph edge to the dot
	 * edge.
	 */
	private static void apply(DirectedGraphEdge<?, ?> edge, DotEdge dotEdge, ViewSpecificAttributeMap map,
			GraphVisualizerParameters parameters) {
		AttributeMap attMap = edge.getAttributeMap();
		Boolean showLabel = getBoolean(attMap, AttributeMap.SHOWLABEL, false, edge, map);
		String label = getString(attMap, AttributeMap.LABEL, "", edge, map);
		dotEdge.setLabel(showLabel ? label : "");
		dotEdge.setOption("dir", "both");
		ArrowType endArrowType = getArrowType(attMap, AttributeMap.EDGEEND, ArrowType.ARROWTYPE_CLASSIC, edge, map);
		Boolean endIsFilled = getBoolean(attMap, AttributeMap.EDGEENDFILLED, false, edge, map);
		switch (endArrowType) {
			case ARROWTYPE_SIMPLE :
			case ARROWTYPE_CLASSIC :
				dotEdge.setOption("arrowhead", "open");
				break;
			case ARROWTYPE_TECHNICAL :
				dotEdge.setOption("arrowhead", endIsFilled ? "normal" : "empty");
				break;
			case ARROWTYPE_CIRCLE :
				dotEdge.setOption("arrowhead", endIsFilled ? "dot" : "odot");
				break;
			case ARROWTYPE_LINE :
				dotEdge.setOption("arrowhead", "tee");
				break;
			case ARROWTYPE_DIAMOND :
				dotEdge.setOption("arrowhead", endIsFilled ? "diamond" : "odiamond");
				break;
			case ARROWTYPE_NONE :
				dotEdge.setOption("arrowhead", "none");
				break;
			default :
				dotEdge.setOption("arrowhead", endIsFilled ? "box" : "obox");
				break;
		}
		ArrowType startArrowType = getArrowType(attMap, AttributeMap.EDGESTART, ArrowType.ARROWTYPE_NONE, edge, map);
		Boolean startIsFilled = getBoolean(attMap, AttributeMap.EDGESTARTFILLED, false, edge, map);
		dotEdge.setOption("arrowtail", "none");
		switch (startArrowType) {
			case ARROWTYPE_SIMPLE :
			case ARROWTYPE_CLASSIC :
				dotEdge.setOption("arrowtail", "open");
				break;
			case ARROWTYPE_TECHNICAL :
				dotEdge.setOption("arrowtail", startIsFilled ? "normal" : "empty");
				break;
			case ARROWTYPE_CIRCLE :
				dotEdge.setOption("arrowtail", startIsFilled ? "dot" : "odot");
				break;
			case ARROWTYPE_LINE :
				dotEdge.setOption("arrowtail", "tee");
				break;
			case ARROWTYPE_DIAMOND :
				dotEdge.setOption("arrowtail", startIsFilled ? "diamond" : "odiamond");
				break;
			case ARROWTYPE_NONE :
				dotEdge.setOption("arrowtail", "none");
				break;
			default :
				dotEdge.setOption("arrowtail", startIsFilled ? "box" : "obox");
				break;
		}
		Float penWidth = getFloat(attMap, AttributeMap.LINEWIDTH, 1.0F, edge, map);
		dotEdge.setOption("penwidth", "" + penWidth);
		Color edgeColor = getColor(attMap, AttributeMap.EDGECOLOR, Color.BLACK, edge, map);
		dotEdge.setOption("color", ColourMap.toHexString(edgeColor));
		Color labelColor = getColor(attMap, AttributeMap.LABELCOLOR, Color.BLACK, edge, map);
		dotEdge.setOption("fontcolor", ColourMap.toHexString(labelColor));
	}

	/*
	 * The following methods get the attribute value from the JGraph object with
	 * a given default value. If the object has no such attribute, the default
	 * value will be returned.
	 */

	private static Boolean getBoolean(AttributeMap map, String key, Boolean value, AttributeMapOwner owner,
			ViewSpecificAttributeMap m) {
		Object obj = m.get(owner, key, null);
		if (obj != null && obj instanceof Boolean) {
			return (Boolean) obj;
		}
		obj = map.get(key);
		if (obj != null && obj instanceof Boolean) {
			return (Boolean) obj;
		}
		return value;
	}

	private static Float getFloat(AttributeMap map, String key, Float value, AttributeMapOwner owner,
			ViewSpecificAttributeMap m) {
		Object obj = m.get(owner, key, null);
		if (obj != null && obj instanceof Float) {
			return (Float) obj;
		}
		obj = map.get(key);
		if (obj != null && obj instanceof Float) {
			return (Float) obj;
		}
		return value;
	}

	private static String getString(AttributeMap map, String key, String value, AttributeMapOwner owner,
			ViewSpecificAttributeMap m) {
		Object obj = m.get(owner, key, null);
		if (obj != null && obj instanceof String) {
			/*
			 * Some labels contain HTML mark-up. Remove as much as possible.
			 */
			String s1 = ((String) obj).replaceAll("<br>", "\\\\n");
			String s2 = s1.replaceAll("<[^>]*>", "");
			return s2;
		}
		obj = map.get(key);
		if (obj != null && obj instanceof String) {
			/*
			 * Some labels contain HTML mark-up. Remove as much as possible.
			 */
			String s1 = ((String) obj).replaceAll("<br>", "\\\\n");
			String s2 = s1.replaceAll("<[^>]*>", "");
			return s2;
		}
		return value;
	}

	private static Color getColor(AttributeMap map, String key, Color value, AttributeMapOwner owner,
			ViewSpecificAttributeMap m) {
		Object obj = m.get(owner, key, null);
		if (obj != null && obj instanceof Color) {
			return (Color) obj;
		}
		obj = map.get(key);
		if (obj != null && obj instanceof Color) {
			return (Color) obj;
		}
		return value;
	}

	private static ArrowType getArrowType(AttributeMap map, String key, ArrowType value, AttributeMapOwner owner,
			ViewSpecificAttributeMap m) {
		Object obj = m.get(owner, key, null);
		if (obj != null && obj instanceof ArrowType) {
			return (ArrowType) obj;
		}
		obj = map.get(key);
		if (obj != null && obj instanceof ArrowType) {
			return (ArrowType) obj;
		}
		return value;
	}

	private static Shape getShape(AttributeMap map, String key, Shape value, AttributeMapOwner owner,
			ViewSpecificAttributeMap m) {
		Object obj = m.get(owner, key, null);
		if (obj != null && obj instanceof Shape) {
			return (Shape) obj;
		}
		obj = map.get(key);
		if (obj != null && obj instanceof Shape) {
			return (Shape) obj;
		}
		return value;
	}

	private static String getFormattedString(String format, String label, String placeLabel, String tooltip) {
		String shortPlaceLabel = (placeLabel.length() > 5 ? placeLabel.substring(0, 4) : placeLabel);
		return format.replace("%l", label).replace("%p", placeLabel).replace("%s", shortPlaceLabel).replace("%t",
				tooltip);
	}
}
