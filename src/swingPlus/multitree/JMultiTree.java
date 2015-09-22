package swingPlus.multitree;

import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.tree.TreeModel;

import javax.swing.JTree;

public class JMultiTree extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5115421161599564218L;

	public JMultiTree () {
		super ();
	}
	
	public JMultiTree (final TreeModel[] treeModels) {
		super ();
	
		for (TreeModel treeModel : treeModels) {
			final JTree jTree = new JTree (treeModel);
			this.add (jTree);
		}
	}
	
	
	public JMultiTree (final Collection<TreeModel> treeModels) {
		this (treeModels.toArray (new TreeModel [treeModels.size()]));
	}
}
