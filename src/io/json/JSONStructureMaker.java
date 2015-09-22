package io.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.table.TableModel;

import model.graph.Edge;
import model.graph.GraphModel;
import model.graph.impl.SymmetricGraphInstance;
import model.parcoord.JSONBasedTableModel;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.MissingNode;


public class JSONStructureMaker extends AbstractJSONMaker {

	static final Logger LOGGER = Logger.getLogger (JSONStructureMaker.class);
	
	
	
	public JSONStructureMaker (final JsonNode root) {
		super (root);
	}
	
	public GraphModel makeGraph (final String[] nodeTypes, final String[] edgeTypes) {
		
		final GraphModel graph  = new SymmetricGraphInstance ();	
		
		// Look through the rootnode for fields named 'nodeType'
		// Add that nodeTypes' subfields as nodes to a graph
		for (String nodeType : nodeTypes) {
			final JsonNode nodeCollection = rootNode.get (nodeType);
			final Iterator<Entry<String, JsonNode>> nodeIter = nodeCollection.getFields();
			while (nodeIter.hasNext()) {
				final Entry<String, JsonNode> node = nodeIter.next();
				LOGGER.debug (node);
				graph.addNode (node.getValue());
			}
		}
		
		// Look through the rootnode for fields named 'edgeType'
		// Iterate through all the subfields 
		// At each subfiled check to see if there is a field named after one of the existing
		// nodeTypes, then connect those references together, either via the edgeType or
		// directly to each other
		for (String edgeType : edgeTypes) {
			final JsonNode edgeCollection = rootNode.get (edgeType);
			final Iterator<Entry<String, JsonNode>> edgeIter = edgeCollection.getFields();
			while (edgeIter.hasNext()) {
				final Entry<String, JsonNode> edgeEntry = edgeIter.next();
				final JsonNode edge = edgeEntry.getValue();
				
				for (String nodeType : nodeTypes) {
					final JsonNode nodes = edge.get (nodeType);
					final JsonNode origNodeCollection = rootNode.get (nodeType);
					if (nodes != null) {
						final Iterator<JsonNode> nodeIter = nodes.getElements();
						
						// If the 'edge' - the link between the node types - is a node in the graph
						// itself then connect node types via this existing node
						if (graph.containsNode (edge)) {
							while (nodeIter.hasNext()) {
								final JsonNode nodeID = nodeIter.next();	
								final JsonNode nodeRecord = origNodeCollection.get (nodeID.getTextValue());
								if (!(nodeRecord instanceof MissingNode)) {
									graph.addEdge (edge, nodeRecord, Integer.valueOf (1));
								}
								
		
							}
						}
						// otherwise connect the collection of node references to
						else {
							final List<JsonNode> cycleList = new ArrayList<JsonNode> ();
							while (nodeIter.hasNext()) {
								final JsonNode nodeID = nodeIter.next();
								cycleList.add (origNodeCollection.get (nodeID.getTextValue()));
							}
							fullCycle (cycleList, graph);
						}
					}
				}
			}
		}
		
		return graph;
	}
	
	protected void fullCycle (final List<JsonNode> nodes, final GraphModel graph) {
		for (int n = 0; n < nodes.size() - 1; n++) {
			for (int m = n + 1; m < nodes.size(); m++) {
				final JsonNode node1 = nodes.get(n);
				final JsonNode node2 = nodes.get(m);
				final Set<Edge> edges = graph.getEdges (node1, node2);
				if (edges.isEmpty()) {
					graph.addEdge (node1, node2, Integer.valueOf (1));
				} else {
					final Iterator<Edge> edgeIter = edges.iterator();
					final Edge firstEdge = edgeIter.next();
					final Integer val = (Integer)firstEdge.getEdgeObject();
					firstEdge.setEdgeObject (Integer.valueOf (val.intValue() + 1));
					//graph.removeEdge (firstEdge);
					//graph.addEdge (node1, node2, Integer.valueOf (val.intValue() + 1));
				}
			}
		}
	}
	
	public TableModel makeTable (final String rowType) {
		final JsonNode rowCollection = rootNode.get (rowType);
		return new JSONBasedTableModel (rowCollection);
	}
	
	/**
	 * make a map of json node types that occur as key/value pairs under a given fieldname
	 * So '"publication": {254006: {pubdatanode}}' would have pubdatanode linked to "publication"
	 * @param nodeTypes
	 * @return map of node types for JsonNodes
	 */
	public Map<JsonNode, String> makeNodeTypeMap (final String[] nodeTypes) {
		final Map<JsonNode, String> nodeTypeMap = new HashMap<JsonNode, String> ();
		
		for (String nodeType : nodeTypes) {
			final JsonNode nodeCollection = rootNode.get (nodeType);
			final Iterator<Entry<String, JsonNode>> nodeIter = nodeCollection.getFields();
			while (nodeIter.hasNext()) {
				final Entry<String, JsonNode> node = nodeIter.next();
				nodeTypeMap.put (node.getValue(), nodeType);
			}
		}
		
		return nodeTypeMap;
	}
}
