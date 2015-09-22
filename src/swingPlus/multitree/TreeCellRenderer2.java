package swingPlus.multitree;

import java.awt.Graphics;
import java.awt.Shape;

import javax.swing.tree.TreeCellRenderer;

public interface TreeCellRenderer2 extends TreeCellRenderer {

	public void setShape (Shape newShape);
	
	// For drawing direct to a buffered image
	public void draw (Graphics graphics);
}
