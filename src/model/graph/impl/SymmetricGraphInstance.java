package model.graph.impl;

import java.util.HashSet;
import java.util.Set;

import model.graph.Edge;

public class SymmetricGraphInstance extends DirectedGraphInstance {

	/**
	 * If a symmetric graph add the edge to the sink node's collection as well
	 */
	boolean firelessAddEdge (final Edge edge) {	
		boolean addSuccess = super.firelessAddEdge (edge);
		if (addSuccess) {
			final Set<Edge> fromSet = graphData.get (edge.getNode2());
			addSuccess |= fromSet.add (edge);
			
			final Set<Edge> toSet = toGraphData.get (edge.getNode1());
			addSuccess |= toSet.add (edge);
		}
		return addSuccess;
	}
	
		
	/**
	 * If a symmetric graph remove the edge from the sink node's collection as well
	 */
	protected Edge firelessRemoveEdge (final Edge edge) {
		
		if (super.firelessRemoveEdge (edge) != null) {
			final Set<Edge> edges = graphData.get (edge.getNode2());
			if (edges != null) {
				edges.remove (edge);
				final Set<Edge> toEdges = toGraphData.get (edge.getNode1());
				toEdges.remove (edge);
			}
		} else {
			return null;
		}
		return edge;
	}
	
	
	@Override
	public Set<Edge> getEdges (final Object node1, final Object node2) {
		final Set<Edge> edges1 = graphData.get (node1);
		final Set<Edge> edges2 = graphData.get (node2);
		final Set<Edge> comboEdges = new HashSet<Edge> ();
		final Set<Edge> smallerSet = edges1.size() < edges2.size() ? edges1 : edges2;
		//final Set<Edge> largerSet = edges1.size() >= edges2.size() ? edges1 : edges2;
		for (Edge edge : smallerSet) {
			if (/*largerSet.contains (e) 
					&&*/ (edge.getNode1() == node1 || edge.getNode1().equals (node1)) 
					&& (edge.getNode2() == node2 || edge.getNode2().equals (node2))
					) {
				comboEdges.add (edge);
			}
			else if (/*largerSet.contains (e) 
					&&*/ (edge.getNode2() == node1 || edge.getNode2().equals (node1)) 
					&& (edge.getNode1() == node2 || edge.getNode1().equals (node2))
					) {
				comboEdges.add (edge);
			}
		}
		return comboEdges;
	}
}
