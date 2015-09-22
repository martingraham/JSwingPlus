package example.tablelist.renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ColourBarCellRenderer extends DefaultTableCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3396578738991433405L;
	Object value;
	Color colour;
	
	public ColourBarCellRenderer (final Color colour) {
		super ();
		this.colour = colour;
		setOpaque (false);
	}
	
	@Override
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		 this.value = value;
		 return super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
	 }
	 
	@Override
	 public void paintComponent (final Graphics graphics) {
		graphics.setColor (colour);
		graphics.fill3DRect (0, 0, this.getWidth(), this.getHeight(), true);
		
		super.paintComponent (graphics);
	 }
}
