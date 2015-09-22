package model.graph.impl;

import model.graph.Edge;

public class EdgeInstance implements Edge {

	private Object node1, node2, edgeObject;
	
	public EdgeInstance (final Object nodeTo, final Object nodeFrom, final Object obj) {
		node1 = nodeTo;
		node2 = nodeFrom;
		edgeObject = obj;	
	}
	
	@Override
	public Object getEdgeObject() {
		return edgeObject;
	}

	@Override
	public Object getNode1() {
		return node1;
	}

	@Override
	public Object getNode2() {
		return node2;
	}
	
	@Override
	public void setEdgeObject (final Object newObj) {
		edgeObject = newObj;
	}

	@Override
	public boolean equals (final Object obj) {
		boolean equal = false;
		
		if (obj instanceof Edge) {
			final Edge edge = (Edge)obj;
			equal = (edge == this || deepEquals (edge));
		}
		return equal;
	}
	
	
	boolean deepEquals (final Edge edge) {
		boolean match = (edge.getNode1() == this.getNode1());
		if (!match && edge.getNode1() != null) {
			match = edge.getNode1().equals (this.getNode1());
		}
		if (match) {
			match = (edge.getNode2() == this.getNode2());
			if (!match && edge.getNode2() != null) {
				match = edge.getNode2().equals (this.getNode2());
			}
			if (match) {
				match = (edge.getEdgeObject() == this.getEdgeObject());
				if (!match && edge.getEdgeObject() != null) {
					match = edge.getEdgeObject().equals (this.getEdgeObject());
				}
			}
		}
		return match;
	}
	
	@Override
	public int hashCode () {
		int hash = 7;
		int var_code = (null == node1 ? 0 : node1.hashCode()); 
		hash = 31 * hash + var_code; 
		var_code = (null == node2 ? 0 : node2.hashCode()); 
		hash = 31 * hash + var_code; 
		var_code = (null == edgeObject ? 0 : edgeObject.hashCode()); 
		hash = 31 * hash + var_code; 
		return hash;
	}
	
	
	@Override
	public String toString() {
		return (getNode1() == null ? "null" : getNode1().toString())
			+" --> "+(getNode2() == null ? "null" : getNode2().toString())
			+" by a "+getEdgeObject().toString();
	}
}
