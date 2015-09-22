package model.multitree.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import model.multitree.MutableTreeNode2;


public class DefaultCrossTreeSelectionModel extends AbstractCrossTreeSelectionModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8302396742498091621L;
	Set<Object> selectedObjects;
	
	public DefaultCrossTreeSelectionModel () {
		super ();
		selectedObjects = new HashSet<Object> ();
	}
	
	
	
	@Override
	public boolean addSelection (final DefaultMutableTreeNode tNode, final boolean recurseUp, 
			final boolean recurseDown) {
		return changeSelection (tNode, recurseUp, recurseDown, true);
	}
	
	@Override
	public boolean addSelection (final MutableTreeNode2 tNode, final boolean recurseUp, 
			final boolean recurseDown) {
		return changeSelection (tNode, recurseUp, recurseDown, true);
	}
	
	@Override
	public boolean removeSelection (final DefaultMutableTreeNode tNode, final boolean recurseUp,
			final boolean recurseDown) {
		return changeSelection (tNode, recurseUp, recurseDown, false);
	}
	
	@Override
	public boolean removeSelection (final MutableTreeNode2 tNode, final boolean recurseUp, 
			final boolean recurseDown) {
		return changeSelection (tNode, recurseUp, recurseDown, false);
	}
	
	
	
	boolean changeSelection (final DefaultMutableTreeNode tNode, 
			final boolean recurseUp, final boolean recurseDown, final boolean addRemove) {
		final TreePath treePath = makeTreePath (tNode);
		
		boolean success = firelessChange (tNode.getUserObject(), addRemove);

		if (recurseDown) {
			success |= recurseChildren (tNode, addRemove);
		}
		if (recurseUp) {
			DefaultMutableTreeNode parentNode = tNode;
			while (parentNode.getParent() != null) {
				parentNode = (DefaultMutableTreeNode)parentNode.getParent ();
				success |= firelessChange (parentNode.getUserObject(), addRemove);
			}
		}
		
		notifySelectionChange (treePath);
		return success;
	}
	
	boolean recurseChildren (final DefaultMutableTreeNode tNode, final boolean addRemove)  {
		boolean success = false;
		if (tNode.getChildCount() > 0) {
			final Enumeration enu = tNode.children();
			while (enu.hasMoreElements()) {
				final DefaultMutableTreeNode child = (DefaultMutableTreeNode) enu.nextElement();
				success |= firelessChange (child.getUserObject(), addRemove);
				success |= recurseChildren (child, addRemove);
			}
		}
		return success;
	}
	
	boolean changeSelection (final MutableTreeNode2 tNode, 
			final boolean recurseUp, final boolean recurseDown, final boolean addRemove) {
		final TreePath treePath = makeTreePath (tNode);
		
		boolean success = firelessChange (tNode.getUserObject(), addRemove);
		if (recurseDown) {
			success |= recurseChildren (tNode, addRemove);
		}
		if (recurseUp) {
			MutableTreeNode2 parentNode = tNode;
			while (parentNode.getParent() != null) {
				parentNode = (MutableTreeNode2)parentNode.getParent ();
				success |= firelessChange (parentNode.getUserObject(), addRemove);
			}
		}
		
		notifySelectionChange (treePath);
		return success;
	}
	
	boolean recurseChildren (final MutableTreeNode2 tNode, final boolean addRemove)  {
		boolean success = false;
		if (tNode.getChildCount() > 0) {
			final Enumeration enu = tNode.children();
			while (enu.hasMoreElements()) {
				final MutableTreeNode2 child = (MutableTreeNode2) enu.nextElement();
				success |= firelessChange (child.getUserObject(), addRemove);
				success |= recurseChildren (child, addRemove);
			}
		}
		return success;
	}
	

	protected boolean firelessChange (final Object obj, final boolean addRemove) {
		if (addRemove) {
			return selectedObjects.add (obj);
		} else {
			return selectedObjects.remove (obj);
		}
	}
	

	
	@Override
	public Set<Object> allSelectedUserObjects() {
		return selectedObjects;
	}

	@Override
	public boolean isSelected (final DefaultMutableTreeNode tNode) {
		return selectedObjects.contains (tNode.getUserObject());
	}
	
	@Override
	public boolean isSelected (final MutableTreeNode2 tNode) {
		return selectedObjects.contains (tNode.getUserObject());
	}


	
	protected TreePath makeTreePath (final TreeNode tNode) {
		final List<TreeNode> nodeList = new ArrayList<TreeNode> ();
		if (tNode != null) {
			nodeList.add (tNode);
			TreeNode parentNode = tNode;
			while (parentNode.getParent() != null) {
				parentNode = parentNode.getParent ();
				nodeList.add (parentNode);
			}
		}
		Collections.reverse (nodeList);	// root first, node last
		return new TreePath (nodeList.toArray());
	}
	
	protected void notifySelectionChange (final TreePath newPath) {
		final TreeSelectionEvent event = new TreeSelectionEvent
			(this, newPath, true, null, newPath);
		
		fireValueChanged(event);
	}
}
