package model.graph;

import java.util.Collection;
import java.util.Set;

/**
 * 
 * @author cs22
 *
 * Interface for constructing and interrogating a graph
 * 
 * For directed graphs, read node1 as the source/from
 * and node2 as the destination/to
 * 
 * Undirected graphs can interpret the two as interchangeable
 */
public interface GraphModel {
	
	boolean addGraph (GraphModel otherGraph);
	
	boolean addNode (Object node);
	public boolean addEdge (Object node1, Object node2, Object obj);
	public boolean addEdge (Edge edge);
	public boolean addEdges (Collection<Edge> edges);
	
	public boolean removeNode (Object node);
	public Edge removeEdge (Edge edge);
	public Set<Edge> removeEdges (Collection<Edge> edges);
	public Edge removeEdge (Object node1, Object node2, Object obj);
	public Set<Edge> removeEdges (Object node1, Object node2);
	
	/**
	 * return the Set of all Edges in the GraphModel
	 * @return Set of Edges
	 */
	public Set<Edge> getEdges ();
	
	/**
	 * Returns the Set of all Edges associated with this node Object
	 * In a symmetric graph this should be all edges
	 * In a directed graph these might only be the edges with this node as a source
	 * @param node
	 * @return Set of Edges
	 */
	public Set<Edge> getEdges (Object node);
	
	/**
	 * Complements the previous method.
	 * Returns the Set of all Edges associated with this node Object as it's sink/target
	 * In a symmetric graph this should be all edges i.e. the same as getEdges (node)
	 * In a directed graph these might only be the edges with this node as a sink
	 * @param node
	 * @return Set of Edges
	 */
	public Set<Edge> getCoincidentEdges (Object node);
	public Set<Edge> getEdges (Object node1, Object node2);
	public Set<Object> getNodes ();
	
	public int getNodeCount ();
	public int getEdgeCount ();
	
	public boolean containsNode (Object node);
	public boolean containsEdge (Edge edge);
	
	/**
	 * Delete the entire graph structure
	 */
	public void clear ();
	
    /**
     * Adds a listener that is notified each time a change
     * to the data model occurs.
     *
     * @param	listener		the GraphModelListener
     */
    public void addGraphModelListener (GraphModelListener listener);

    /**
     * Removes a listener that is notified each time a
     * change to the data model occurs.
     *
     * @param	listener		the GraphModelListener
     */
    public void removeGraphModelListener (GraphModelListener listener);
}
