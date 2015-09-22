package model.graph;

public interface Edge {

	public Object getNode1 ();
	public Object getNode2 ();
	public Object getEdgeObject ();
	
	/**
	 * Useful for when an edge object needs updated
	 * i.e. the edge object is an Integer that needs incremented
	 * otherwise the old edge must be removed and a new edge added to the graph
	 * @param newObj
	 */
	public void setEdgeObject (final Object newObj);
}
