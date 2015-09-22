package model.multitree;

import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public interface CrossTreeSelectionModel extends TreeSelectionModel {

	public boolean addSelection (DefaultMutableTreeNode tNode, boolean recurseUp, boolean recurseDown);
	public boolean addSelection (MutableTreeNode2 tNode, boolean recurseUp, boolean recurseDown);

	public boolean removeSelection (DefaultMutableTreeNode tNode, boolean recurseUp, boolean recurseDown);
	public boolean removeSelection (MutableTreeNode2 tNode, boolean recurseUp, boolean recurseDown);

	public boolean isSelected (DefaultMutableTreeNode tNode);
	public boolean isSelected (MutableTreeNode2 tNode);
	
	public Set<Object> allSelectedUserObjects ();
}
