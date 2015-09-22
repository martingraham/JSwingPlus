package example.matrix.renderers;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;

import swingPlus.matrix.JHeaderRenderer;
import util.IconCache;

public class StringHeaderRenderer extends JHeaderRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8077146043053955659L;
	
	private static final Color BACKGROUND = new Color (246, 240, 224);
	private static final Icon ICON = IconCache.makeIcon ("PersonIcon");

	public StringHeaderRenderer () {
		super ();
		setIcon (ICON);
		setBackground (BACKGROUND);
		setForeground (Color.black);
	}
	
	@Override
	   public Component getTableCellRendererComponent (final JTable table, final Object value,
               final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
		//this.setEnabled (isSelected);
		this.setText (value == null ? "null" : value.toString());
		return this;
	}
}
