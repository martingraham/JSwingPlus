package example.multiview.renderers.matrix;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;

import org.codehaus.jackson.JsonNode;

import example.matrix.renderers.StringHeaderRenderer;

import util.IconCache;

public class JSONObjHeaderRenderer extends StringHeaderRenderer {

	private static final long serialVersionUID = 3116702798941453750L;
	/**
	 * 
	 */
	
	private static final Color BACKGROUND = new Color (246, 240, 224);
	private static final Icon ICON = IconCache.makeIcon ("PersonIcon");

	public JSONObjHeaderRenderer () {
		this (true);
	}
	
	public JSONObjHeaderRenderer (boolean showIcon) {
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
		if (value instanceof JsonNode) {
			JsonNode jNode = (JsonNode)value;
			JsonNode labelNode = jNode.get("label");
			if (labelNode != null) {
				this.setText (labelNode.getTextValue());
			}
		}
		
		return this;
	}
}
