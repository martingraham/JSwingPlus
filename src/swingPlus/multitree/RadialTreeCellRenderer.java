package swingPlus.multitree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JTree;
import javax.swing.UIManager;

public class RadialTreeCellRenderer extends AbstractTreeCellRenderer2 {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8473654526208091946L;
	
	
	Color selBackground;
	
	@Override
	public Component getTreeCellRendererComponent (final JTree tree, final Object value,
			final boolean selected, final boolean expanded, final boolean leaf, final int row,
			final boolean hasFocus) {
		
    	selBackground = UIManager.getDefaults().getColor ("Tree.selectionBackground");
		super.getTreeCellRendererComponent (tree, value, selected, expanded, leaf, row, hasFocus);

		return this;
	}
	
	@Override
	public void paint (final Graphics graphics) {
		draw (graphics);
	}
	
	@Override
	public void draw (final Graphics graphics) {
    	final Object userObject = node.getUserObject();
    	//if (tree.getSelectionModel().)
        graphics.setColor (selected ? Color.red : selBackground);
        ((Graphics2D)graphics).fill (shape);
    	graphics.setColor(Color.gray);
        
        ((Graphics2D)graphics).draw (shape);
	}
}