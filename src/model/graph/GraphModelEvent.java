package model.graph;

import java.util.Collection;
import java.util.EventObject;
import java.util.Set;

public class GraphModelEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4204385693013310645L;
	
    protected int   type;
    protected Object node;	// if single instances, let's not go chucking Sets about
    protected Edge edge;
    protected Set<Object> nodes;	// if multiple instances
    protected Collection<Edge> edges;
	
    /** Identifies the addition of new nodes or edges. */
    public static final int INSERT =  1;
    /** Identifies a change to existing data. */
    public static final int UPDATE =  0;
    /** Identifies the removal of nodes or edges. */
    public static final int DELETE = -1;
    /** Identifies the removal of the entire graph structure. */
    public static final int CLEAR = -2;

	public GraphModelEvent (final GraphModel source) {
		this (source, null, null, null, null, UPDATE);
	}
	
	public GraphModelEvent (final GraphModel source, final Set<Object> nodes, 
			final Collection<Edge> edges, final int type) {
		this (source, null, null, nodes, edges, type);
	}
	
	public GraphModelEvent (final GraphModel source, final Edge edge, final int type) {
		this (source, null, edge, null, null, type);
	}
	
	public GraphModelEvent (final GraphModel source, final Object node, final Edge edge, 
			final int type) {
		this (source, node, edge, null, null, type);
	}
	
	
	
	public GraphModelEvent (final GraphModel source, final Object node, final Edge edge, 
			final Set<Object> nodes, final Collection<Edge> edges, final int type) {
		super (source);
		this.node = node;
		this.edge = edge;
		this.nodes = nodes;
		this.edges = edges;
		this.type = type;
	}

	public final int getType() {
		return type;
	}

	public final Object getNode() {
		return node;
	}

	public final Edge getEdge() {
		return edge;
	}

	public final Set<Object> getNodes() {
		return nodes;
	}

	public final Collection<Edge> getEdges() {
		return edges;
	}
}
