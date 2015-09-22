package model.graph.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.graph.AbstractGraphModel;
import model.graph.Edge;
import model.graph.GraphModel;

public class DirectedGraphInstance extends AbstractGraphModel {

	Map<Object, Set<Edge>> graphData; 
	Map<Object, Set<Edge>> toGraphData; // List of edges that point to a given node for convenience
	int edgeCount = 0, nodeCount = 0;
	
	public DirectedGraphInstance () {
		super ();
		graphData = new HashMap<Object, Set<Edge>> ();
		toGraphData = new HashMap<Object, Set<Edge>> ();
	}
	
	@Override
	public boolean addGraph (final GraphModel otherGraph) {
		return addEdges (otherGraph.getEdges());
	}
	
	@Override
	public boolean addEdge (final Object node1, final Object node2, final Object obj) {	
		final Edge edge = new EdgeInstance (node1, node2, obj);		
		return addEdge (edge);
	}
		
	@Override
	public boolean addEdge (final Edge edge) {
		final boolean addSuccess = firelessAddEdge (edge);
		if (addSuccess) {
			this.fireGraphEdgeInserted (edge);
		}
		return addSuccess;
	}
	
	@Override
	public boolean addEdges (final Collection<Edge> edges) {
		boolean addSuccess = false;
		final Iterator<Edge> edgeI = edges.iterator();
    	while (edgeI.hasNext ()) {
    		final Edge edge = edgeI.next ();
    		addSuccess |= firelessAddEdge (edge);
    	}
    	if (addSuccess) {
    		this.fireGraphEdgesInserted (edges);
    	}
    	return addSuccess;
	}

	@Override
	public boolean addNode (final Object node) {
		boolean added = false;
		if (graphData.get (node) == null) {
			graphData.put (node, new HashSet<Edge> ());
			added = true;
			nodeCount++;
			this.fireGraphNodeInserted (node);
		}
		if (toGraphData.get (node) == null) {
			toGraphData.put (node, new HashSet<Edge> ());
		}
		return added;
	}
	
	/*
	 * Method that just adds an edge without calling edge event firing methods
	 * So that a addEdges call won't fire multiple single addEdge events
	 * Will still fire events in addNode if new nodes need to be instantiated
	 */
	boolean firelessAddEdge (final Edge edge) {
		boolean addSuccess = false;
		if (edge.getNode1() != null && edge.getNode2() != null) { // Don't add edges with a missing end
			addNode (edge.getNode1());
			addNode (edge.getNode2());
			
			final Set<Edge> fromSet = graphData.get (edge.getNode1());
			addSuccess |= fromSet.add (edge);

			if (addSuccess) {
				edgeCount++;
			}
			
			final Set<Edge> toSet = toGraphData.get (edge.getNode2());
			addSuccess |= toSet.add (edge);
		}
		return addSuccess;
	}
	
	
	
	@Override
	public Set<Object> getNodes () {
		return graphData.keySet();
	}
	
	@Override
	public Set<Edge> getEdges () {
		final Set<Edge> allEdges = new HashSet<Edge> ();
		final Collection<Set<Edge>> edgeSets = graphData.values();
		final Iterator<Set<Edge>> setIterator = edgeSets.iterator ();
		while (setIterator.hasNext()) {
			final Set<Edge> edgeSet = setIterator.next ();
			allEdges.addAll (edgeSet);
		}
		return allEdges;
	}

	
	/**
	 * returns edges that have this node as the source
	 */
	@Override
	public Set<Edge> getEdges (final Object node) {
		return graphData.get (node);
	}
	
	/**
	 * returns edges that have this node as the sink
	 */
	@Override
	public Set<Edge> getCoincidentEdges (final Object node) {
		return toGraphData.get (node);
	}

	
	@Override
	public Set<Edge> getEdges (final Object node1, final Object node2) {
		final Set<Edge> edges = graphData.get (node1);
		final Set<Edge> comboEdges = new HashSet<Edge> ();
		//final Set<Edge> largerSet = edges1.size() >= edges2.size() ? edges1 : edges2;
		for (Edge edge : edges) {
			if (/*largerSet.contains (e) 
					&&*/ (edge.getNode1() == node1 || edge.getNode1().equals (node1)) 
					&& (edge.getNode2() == node2 || edge.getNode2().equals (node2))
					) {
				comboEdges.add (edge);
			}
		}
		return comboEdges;
	}

	
	
	
	
	@Override
	public Edge removeEdge (final Object node1, final Object node2, final Object obj) {
		final Edge edge = new EdgeInstance (node1, node2, obj);
		return removeEdge (edge);
	}

	@Override
	public Set<Edge> removeEdges (final Object node1, final Object node2) {
		final Set<Edge> toEdges = graphData.get (node1);
		final Set<Edge> removedEdges = new HashSet<Edge> ();
		for (Edge e : toEdges) {
			if (e.getNode2().equals (node2)) {
				firelessRemoveEdge (e);
				removedEdges.add (e);
			}
		}
		if (!removedEdges.isEmpty()) {
			this.fireGraphEdgesDeleted (removedEdges);
		}
		return removedEdges;
	}

	
	@Override
	public Edge removeEdge (final Edge edge) {
		final Edge removedEdge = firelessRemoveEdge (edge);
		if (removedEdge != null) {
			this.fireGraphEdgeDeleted (removedEdge);
		}
		return removedEdge;
	}
	
	
	@Override
	public Set<Edge> removeEdges (final Collection<Edge> edges) {
		final Set<Edge> removedEdges = new HashSet<Edge> ();
		for (Edge edge : edges) {
			if (firelessRemoveEdge (edge) != null) {
				removedEdges.add (edge);
			}
		}
		if (!removedEdges.isEmpty()) {
			this.fireGraphEdgesDeleted (removedEdges);
		}
		return removedEdges;
	}
	
	
	
	
	@Override
	public boolean removeNode (final Object node) {
		boolean removeSuccess = false;
		final Set<Edge> edges = graphData.get (node);

		// Remove edges emanating from this node
		if (edges != null) {
			final List<Edge> edgeCopy = new ArrayList<Edge> (edges);
			for (Edge edge : edgeCopy) {
				removeSuccess |= (firelessRemoveEdge (edge) != null);
			}
			nodeCount--;
		}
		
		// Remove edges that point to this node
		final Set<Edge> toEdges = toGraphData.get (node);
		if (toEdges != null) {
			final List<Edge> edgeCopy = new ArrayList<Edge> (toEdges);
			for (Edge edge : edgeCopy) {
				removeSuccess |= (firelessRemoveEdge (edge) != null);
			}
		}

		graphData.remove (node);
		
		this.fireGraphNodeDeleted (node);
		return removeSuccess;
	}
	
	

	
	/*
	 * Method that just removes an edge without calling event firing methods
	 * So that a removeEdges call won't fire multiple single removeEdge events
	 */
	protected Edge firelessRemoveEdge (final Edge edge) {
		final Set<Edge> edges = graphData.get (edge.getNode1());
		Edge returnEdge = edge;
		
		if (edges != null && edges.contains (edge)) {
			edges.remove (edge);
			final Set<Edge> toEdges = toGraphData.get (edge.getNode2());
			toEdges.remove (edge);
			edgeCount--;
		} else {
			returnEdge = null;
		}
		
		return returnEdge;
	}

	@Override
	public int getEdgeCount() {
		return edgeCount;
	}

	@Override
	public int getNodeCount() {
		return nodeCount;
	}
	
	
	public boolean containsNode (final Object node) {
		return graphData.containsKey (node);
	}
	
	public boolean containsEdge (final Edge edge) {
		final Set<Edge> edges = getEdges (edge.getNode1());
		return edges.contains (edge);
	}
	
	public void clear () {
		super.clear ();
		graphData.clear ();
		edgeCount = 0;
		nodeCount = 0;
	}
}
