package example.multiview.renderers.matrix;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;

import example.matrix.renderers.StringHeaderRenderer;
import example.multiview.KeyedData;

import util.IconCache;

public class KeyedDataHeaderRenderer extends StringHeaderRenderer {

	private static final long serialVersionUID = 3116702798941453750L;
	/**
	 * 
	 */
	
	private static final Color BACKGROUND = new Color (246, 240, 224);
	private static final Icon ICON = IconCache.makeIcon ("PersonIcon");

	public KeyedDataHeaderRenderer () {
		this (true);
	}
	
	public KeyedDataHeaderRenderer (boolean showIcon) {
		super ();
		setIcon (showIcon ? ICON : null);
		setBackground (BACKGROUND);
		setForeground (Color.black);
	}
	
	@Override
	   public Component getTableCellRendererComponent (final JTable table, final Object value,
               final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
		//this.setEnabled (isSelected);
		if (value instanceof KeyedData) {
			KeyedData kNode = (KeyedData)value;
			if (kNode != null) {
				this.setText (kNode.toString());
			}
		}
		
		return this;
	}
}
