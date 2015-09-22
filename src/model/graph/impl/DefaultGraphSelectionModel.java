package model.graph.impl;

import java.util.Collection;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;

import model.graph.GraphSelectionEvent;
import model.graph.GraphSelectionListener;
import model.graph.GraphSelectionModel;

public class DefaultGraphSelectionModel implements GraphSelectionModel {

	static final Logger LOGGER = Logger.getLogger (DefaultGraphSelectionModel.class);

	
	/** List of listeners */
    protected EventListenerList listenerList = new EventListenerList ();

	Set<Object> selection;
	Set<Object> preNewSelection;
	boolean isAdjusting;
	
	
	public DefaultGraphSelectionModel () {
		selection = new HashSet<Object> ();
		preNewSelection = new HashSet<Object> ();
		isAdjusting = false;
	}
	
	@Override
	public void clearSelection() {
		selection.clear ();
		fireGraphSelectionCleared ();
	}

	@Override
	public Collection<Object> getAllSelected() {
		return selection;
	}

	@Override
	public boolean isSelected (final Object obj) {
		return selection.contains (obj);
	}

	@Override
	public void setSelected (final Object obj, final boolean selected) {
		if (selected) {
			if (selection.add (obj)) {
				fireGraphSelectionAdded (obj);
			}
		} else {
			if (selection.remove (obj)) {
				fireGraphSelectionRemoved (obj);
			}
		}
	}

	@Override
	public void setSelected (final Collection<Object> objs, final boolean selected) {
		if (selected) {
			if (selection.addAll (objs)) {
				fireGraphSelectionAdded (objs);
			}
		} else {	
			if (selection.removeAll (objs)) {
				fireGraphSelectionRemoved (objs);
			}	
		}
	}
	

	
    /**
     *
     * @param selectedObjects  Set of selected nodes
     *
     * @see GraphSelectionEvent
     * @see EventListenerList
     */
    public void fireGraphSelectionAdded (final Collection<Object> selectedObjects) {
       	if (listenerList.getListenerCount() > 0 && !isValueAdjusting ()) { // Saves making new GraphModelEvent if no-one cares	 
       		fireGraphSelectionChanged (new GraphSelectionEvent (this, selectedObjects, GraphSelectionEvent.SELECTED, isAdjusting));
       	}
    }
    
    public void fireGraphSelectionAdded (final Object selectedObject) {
       	if (listenerList.getListenerCount() > 0 && !isValueAdjusting ()) { // Saves making new GraphModelEvent if no-one cares	 
       		fireGraphSelectionChanged (new GraphSelectionEvent (this, selectedObject, GraphSelectionEvent.SELECTED, isAdjusting));
       	}
    }
    
    public void fireGraphSelectionRemoved (final Collection<Object> selectedObjects) {
       	if (listenerList.getListenerCount() > 0 && !isValueAdjusting ()) { // Saves making new GraphModelEvent if no-one cares	 
       		fireGraphSelectionChanged (new GraphSelectionEvent (this, selectedObjects, GraphSelectionEvent.UNSELECTED, isAdjusting));
       	}
    }
    
    public void fireGraphSelectionRemoved (final Object selectedObject) {
       	if (listenerList.getListenerCount() > 0 && !isValueAdjusting ()) { // Saves making new GraphModelEvent if no-one cares	 
       		fireGraphSelectionChanged (new GraphSelectionEvent (this, selectedObject, GraphSelectionEvent.UNSELECTED, isAdjusting));
       	}
    }
    
    public void fireGraphSelectionChanged () { // Everything may have changed i.e. a clear all
       	if (listenerList.getListenerCount() > 0 && !isValueAdjusting ()) { // Saves making new GraphModelEvent if no-one cares	 
       		fireGraphSelectionChanged (new GraphSelectionEvent (this, null, GraphSelectionEvent.CHANGED, isAdjusting));
       	}
    }
    
    public void fireGraphSelectionCleared () { // Everything may have changed i.e. a clear all
       	if (listenerList.getListenerCount() > 0 && !isValueAdjusting ()) { // Saves making new GraphModelEvent if no-one cares	 
       		fireGraphSelectionChanged (new GraphSelectionEvent (this, null, GraphSelectionEvent.CLEAR, isAdjusting));
       	}
    }
    

	@Override
	public void addGraphSelectionListener (final GraphSelectionListener gsl) {
		listenerList.add (GraphSelectionListener.class, gsl);
	}

	@Override
	public void removeGraphSelectionListener (final GraphSelectionListener gsl) {
		listenerList.remove (GraphSelectionListener.class, gsl);
	}
	
    /**
     * Returns an array of all the GraphSelectionModel model listeners 
     * registered on this model.
     *
     * @return all of this model's <code>GraphSelectionListener</code>s 
     *         or an empty
     *         array if no GraphSelectionModel model listeners are currently registered
     *
     * @see #addGraphSelectionListener
     * @see #removeGraphSelectionListener
     *
     * @since 1.4
     */
	@Override
    public GraphSelectionListener[] getGraphSelectionListeners() {
        return (GraphSelectionListener[])listenerList.getListeners(
                GraphSelectionListener.class);
    }
    

    public <T extends EventListener> T[] getListeners (final Class<T> listenerType) { 
    	return listenerList.getListeners (listenerType); 
    }

    
    /**
     * Forwards the given notification event to all
     * <code>GraphSelectionListeners</code> that registered
     * themselves as listeners for this GraphSelectionModel model.
     *
     * @param gsEvent  the event to be forwarded
     *
     * @see #addGraphSelectionListener
     * @see GraphSelectionEvent
     * @see EventListenerList
     */
    public void fireGraphSelectionChanged (final GraphSelectionEvent gsEvent) {
		// Guaranteed to return a non-null array
    	final Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
		    if (listeners[i] == GraphSelectionListener.class) {
		    	((GraphSelectionListener)listeners[i+1]).valueChanged (gsEvent);
		    }
		}
    }

    
	@Override
	public boolean isValueAdjusting() {
		return isAdjusting;
	}

	
	@Override
	public void setValueIsAdjusting (final boolean newVal) {
		final boolean change = (isAdjusting != newVal);
		
		if (change) {
			isAdjusting = newVal;
			
			if (newVal) {
				preNewSelection.clear ();
				preNewSelection.addAll (selection);
			}
			
			if (!newVal && !preNewSelection.equals (selection)) {
				
				final Set<Object> deletedSelection = new HashSet<Object> (preNewSelection);
				final Set<Object> addedSelection = new HashSet<Object> (selection);
				deletedSelection.removeAll (selection);
				addedSelection.removeAll (preNewSelection);
				preNewSelection.clear ();
				
				if (!addedSelection.isEmpty()) {
					LOGGER.debug ("addedSelection: "+addedSelection);
					fireGraphSelectionAdded (addedSelection);
				} 
				if (!deletedSelection.isEmpty()) {
					fireGraphSelectionRemoved (deletedSelection);
				}
				
			}	
		}
	}
}
