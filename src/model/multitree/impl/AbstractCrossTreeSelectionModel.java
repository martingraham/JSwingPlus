package model.multitree.impl;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.EventListener;
import java.util.Set;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import model.multitree.CrossTreeSelectionModel;
import model.multitree.MutableTreeNode2;

public class AbstractCrossTreeSelectionModel implements CrossTreeSelectionModel, Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3896243683141292061L;
	private final static Logger LOGGER = Logger.getLogger (AbstractCrossTreeSelectionModel.class);
	
	
	Set<Object> selectedObjects;
	/** List of listeners */
    protected EventListenerList listenerList = new EventListenerList ();
	
	
	@Override
	public void addPropertyChangeListener (final PropertyChangeListener listener) {
		// TODO Auto-generated method stub

	}
	
	//DefaultTreeSelectionModel

	@Override
	public void addSelectionPath (final TreePath path) {
		final Object obj = path.getLastPathComponent ();
		if (obj instanceof DefaultMutableTreeNode) {
			addSelection ((DefaultMutableTreeNode)obj, true, false);
		}
	}

	@Override
	public void addSelectionPaths (final TreePath[] paths) {
		for (int n = paths.length; --n >= 0;) {
			addSelectionPath (paths [n]);
		}
	}

    /**
     * Adds x to the list of listeners that are notified each time the
     * set of selected TreePaths changes.
     *
     * @param tsl the new listener to be added
     */
	public void addTreeSelectionListener (final TreeSelectionListener tsl) {
	   listenerList.add (TreeSelectionListener.class, tsl);
	}

   /**
     * Removes x from the list of listeners that are notified each time
     * the set of selected TreePaths changes.
     *
     * @param tsl the listener to remove
     */
	public void removeTreeSelectionListener (final TreeSelectionListener tsl) {
	   listenerList.remove (TreeSelectionListener.class, tsl);
	}

	@Override
	public void clearSelection() {
		selectedObjects.clear ();
	}

	@Override
	public TreePath getLeadSelectionPath() {
		//LOGGER.debug ("getLeadSelectionPath");
		return null;
	}

	@Override
	public int getLeadSelectionRow() {
		return 0;
	}

	@Override
	public int getMaxSelectionRow() {
		return 0;
	}

	@Override
	public int getMinSelectionRow() {
		return 0;
	}

	@Override
	public RowMapper getRowMapper() {
		return null;
	}

	@Override
	public int getSelectionCount() {
		return 0;
	}

	@Override
	public int getSelectionMode() {
		return 0;
	}

	@Override
	public TreePath getSelectionPath() {
		return null;
	}

	@Override
	public TreePath[] getSelectionPaths() {
		return null;
	}
	

	@Override
	public int[] getSelectionRows() {
		return null;
	}

	@Override
	public boolean isPathSelected (final TreePath path) {
		//LOGGER.debug ("isPathSelected");
		final Object obj = path.getLastPathComponent();
		if (obj instanceof DefaultMutableTreeNode) {
			return isSelected ((DefaultMutableTreeNode)obj);
		}
		if (obj instanceof MutableTreeNode2) {
			return isSelected ((MutableTreeNode2)obj);
		}
		return false;
	}

	@Override
	public boolean isRowSelected (final int row) {
		// TODO Auto-generated method stub
		//LOGGER.debug ("isRowSelected");
		return false;
	}

	@Override
	public boolean isSelectionEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removePropertyChangeListener (final PropertyChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeSelectionPath (final TreePath path) {
		final Object obj = path.getLastPathComponent ();
		if (obj instanceof DefaultMutableTreeNode) {
			removeSelection ((DefaultMutableTreeNode)obj, true, false);
		}
	}

	@Override
	public void removeSelectionPaths (final TreePath[] paths) {
		for (int n = paths.length; --n >= 0;) {
			removeSelectionPath (paths [n]);
		}
	}


	@Override
	public void resetRowSelection() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRowMapper (final RowMapper newMapper) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSelectionMode (final int mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSelectionPath (final TreePath path) {
		addSelectionPath (path);

	}

	@Override
	public void setSelectionPaths (final TreePath[] paths) {
		for (int n = paths.length; --n >= 0;) {
			setSelectionPath (paths[n]);
		}
	}

	
    /**
     * Notifies all listeners that are registered for
     * tree selection events on this object.  
     * @see #addTreeSelectionListener
     * @see EventListenerList
     */
    protected void fireValueChanged (final TreeSelectionEvent tse) {
		// Guaranteed to return a non-null array
    	final Object[] listeners = listenerList.getListenerList();
		// TreeSelectionEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
		    if (listeners[i]==TreeSelectionListener.class) {
				// Lazily create the event:
				// if (e == null)
				// e = new ListSelectionEvent(this, firstIndex, lastIndex);
				((TreeSelectionListener)listeners[i+1]).valueChanged(tse);
		    }	       
		}
    }
	
	

	@Override
	public boolean addSelection (final DefaultMutableTreeNode tNode,
			final boolean recurseUp, final boolean recurseDown) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addSelection (final MutableTreeNode2 tNode, final boolean recurseUp,
			final boolean recurseDown) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<Object> allSelectedUserObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSelected (final DefaultMutableTreeNode tNode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSelected (final MutableTreeNode2 tNode) {
		// TODO Auto-generated method stub
		return false;
	}

    
    public <T extends EventListener> T[] getListeners (final Class<T> listenerType) { 
    	return listenerList.getListeners (listenerType); 
    }

	@Override
	public boolean removeSelection (final DefaultMutableTreeNode tNode,
			final boolean recurseUp, final boolean recurseDown) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeSelection (final MutableTreeNode2 tNode, final boolean recurseUp,
			final boolean recurseDown) {
		// TODO Auto-generated method stub
		return false;
	}
}