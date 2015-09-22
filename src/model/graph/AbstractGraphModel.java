package model.graph;

import java.util.Collection;
import java.util.EventListener;
import java.util.Set;

import javax.swing.event.EventListenerList;

public abstract class AbstractGraphModel implements GraphModel {

	/** List of listeners */
    protected EventListenerList listenerList = new EventListenerList ();
	
    
	@Override
	public abstract boolean addEdge (Object node1, Object node2, Object obj);

	@Override
	public abstract boolean addEdge (Edge edge);

	@Override
	public abstract boolean addEdges (Collection<Edge> edges);

	@Override
	public abstract boolean addNode (Object node);

	
	@Override
	public abstract Set<Edge> getEdges();

	@Override
	public abstract Set<Edge> getEdges (Object node);

	@Override
	public abstract Set<Edge> getEdges (Object node1, Object node2);

	@Override
	public abstract Set<Object> getNodes();

	
	
	@Override
	public abstract Edge removeEdge (Edge edge);
	
	@Override
	public abstract Set<Edge> removeEdges (Collection<Edge> edges);
	
	@Override
	public abstract Edge removeEdge (Object node1, Object node2, Object obj);

	@Override
	public abstract Set<Edge> removeEdges (Object node1, Object node2);

	@Override
	public abstract boolean removeNode (Object node);
	
	
	//
	//  Managing Listeners
	//

    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     *
     * @param	listener		the GraphModelListener
     */
    public void addGraphModelListener (final GraphModelListener listener) {
    	listenerList.add (GraphModelListener.class, listener);
    }

    /**
     * Removes a listener from the list that's notified each time a
     * change to the data model occurs.
     *
     * @param	listener		the GraphModelListener
     */
    public void removeGraphModelListener (final GraphModelListener listener) {
    	listenerList.remove (GraphModelListener.class, listener);
    }

    /**
     * Returns an array of all the GraphModel model listeners 
     * registered on this model.
     *
     * @return all of this model's <code>GraphModelListener</code>s 
     *         or an empty
     *         array if no GraphModel model listeners are currently registered
     *
     * @see #addGraphModelListener
     * @see #removeGraphModelListener
     *
     * @since 1.4
     */
    public GraphModelListener[] getGraphModelListeners() {
        return (GraphModelListener[])listenerList.getListeners(
                GraphModelListener.class);
    }
    
    
    public boolean doListenersExist () {
    	return listenerList.getListenerCount() > 0;
    }
    
    public boolean doListenersExist (final Class<?> listenerType) {
    	return listenerList.getListenerCount (listenerType) > 0;
    }

//
//  Fire methods
//

    /**
     * Notifies all listeners that nodes in the GraphModel
     * may have changed. The <code>JGraph</code> should redraw the
     * GraphModel from scratch.
     *
     * @see GraphModelEvent
     * @see EventListenerList
     * @see javax.swing.JGraph#GraphChanged(GraphModelEvent)
     */
    public void fireGraphDataChanged() {
    	if (doListenersExist ()) { // Saves making new GraphModelEvent if no-one cares
    		fireGraphChanged (new GraphModelEvent(this));
    	}
    }

    /**
     * Notifies all listeners that the GraphModel's structure has changed.
     * insets.e. an indeterminate number of Edges have been added
     *
     * @see GraphModelEvent
     * @see EventListenerList
     */
    public void fireGraphStructureChanged() {
       	if (doListenersExist ()) { // Saves making new GraphModelEvent if no-one cares
       		fireGraphChanged (new GraphModelEvent (this));
       	}
    }

    
    /**
    *
    * @param  insertedNode  inserted Object
    *
    * @see GraphModelEvent
    * @see EventListenerList
    *
    */
    public void fireGraphNodeInserted (final Object insertedNode) {
       	if (doListenersExist ()) { // Saves making new GraphModelEvent if no-one cares     	 
       		fireGraphChanged (new GraphModelEvent (this, insertedNode, (Edge)null, GraphModelEvent.INSERT));
       	}
    }
    
    
    /**
     *
     * @param  insertedNodes  Set of inserted Objects
     *
     * @see GraphModelEvent
     * @see EventListenerList
     *
     */
    public void fireGraphNodesInserted (final Set<Object> insertedNodes) {
       	if (doListenersExist ()) { // Saves making new GraphModelEvent if no-one cares     	 
       		fireGraphChanged (new GraphModelEvent (this, insertedNodes, (Set<Edge>)null, GraphModelEvent.INSERT));
       	}
    }
    
    
    /**
     *
     * @param insertedEdge  inserted Edge
     *
     * @see GraphModelEvent
     * @see EventListenerList
     */
    public void fireGraphEdgeInserted (final Edge insertedEdge) {
       	if (doListenersExist ()) { // Saves making new GraphModelEvent if no-one cares	 
       		fireGraphChanged (new GraphModelEvent (this, insertedEdge, GraphModelEvent.INSERT));
       	}
    }
    
    /**
     * 
     *
     * @param insertedEdges  Set of inserted Edges
     *
     * @see GraphModelEvent
     * @see EventListenerList
     */
    public void fireGraphEdgesInserted (final Collection<Edge> insertedEdges) {
       	if (doListenersExist ()) { // Saves making new GraphModelEvent if no-one cares	 
       		fireGraphChanged (new GraphModelEvent (this, null, insertedEdges, GraphModelEvent.INSERT));
       	}
    }

    
    /**
     *
     * @param deletedNode  deleted Object
     *
     * @see GraphModelEvent
     * @see EventListenerList
     */
    public void fireGraphNodeDeleted (final Object deletedNode) {
       	if (doListenersExist ()) { // Saves making new GraphModelEvent if no-one cares    	 
       		fireGraphChanged (new GraphModelEvent (this, deletedNode, (Edge)null, GraphModelEvent.DELETE));
       	}
    }
    
    /**
     *
     * @param deletedNodes  Set of deleted Objects
     *
     * @see GraphModelEvent
     * @see EventListenerList
     */
    public void fireGraphNodesDeleted (final Set<Object> deletedNodes) {
       	if (doListenersExist ()) { // Saves making new GraphModelEvent if no-one cares    	 
       		fireGraphChanged (new GraphModelEvent (this, deletedNodes, (Set<Edge>)null, GraphModelEvent.DELETE));
       	}
    }
    
    /**
     *
     * @param deletedEdge  deleted Edge
     * 
     * @see GraphModelEvent
     * @see EventListenerList
     */
    public void fireGraphEdgeDeleted (final Edge deletedEdge) {
       	if (doListenersExist ()) { // Saves making new GraphModelEvent if no-one cares  	 
       		fireGraphChanged (new GraphModelEvent (this, deletedEdge, GraphModelEvent.DELETE));
       	}
    }

    /**
     *
     * @param deletedEdges  Set of deleted Edges
     * 
     * @see GraphModelEvent
     * @see EventListenerList
     */
    public void fireGraphEdgesDeleted (final Set<Edge> deletedEdges) {
       	if (doListenersExist ()) { // Saves making new GraphModelEvent if no-one cares  	 
       		fireGraphChanged (new GraphModelEvent (this, null, deletedEdges, GraphModelEvent.DELETE));
       	}
    }
    
    /**
     * Forwards the given notification event to all
     * <code>GraphModelListeners</code> that registered
     * themselves as listeners for this GraphModel model.
     *
     * @param event  the event to be forwarded
     *
     * @see #addGraphModelListener
     * @see GraphModelEvent
     * @see EventListenerList
     */
    public void fireGraphChanged (final GraphModelEvent event) {
		// Guaranteed to return a non-null array
    	final Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
		    if (listeners[i] == GraphModelListener.class) {
		    	((GraphModelListener)listeners[i+1]).graphChanged(event);
		    }
		}
    }
    
    @Override
    public void clear () {
    	if (doListenersExist ()) { // Saves making new GraphModelEvent if no-one cares  	 
       		fireGraphChanged (new GraphModelEvent (this, getNodes(), getEdges(), GraphModelEvent.CLEAR));
       	}
    }

    public <T extends EventListener> T[] getListeners (final Class<T> listenerType) { 
    	return listenerList.getListeners (listenerType); 
    }
}
