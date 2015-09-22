package example.tablelist.renderers;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;


public class HeatSpotCellRenderer2 extends HeatSpotCellRenderer {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8691610714590328665L;

	
	@Override
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		 
		final Component comp = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);

		
		 if (value instanceof Number) {
			 final double valueInt = ((Number)value).doubleValue();
			 empty = (valueInt <= 0.0);
			 final double diff = max - min;
			 final int colScaleMin = 43;
			 final int colScaleDiff = 128;
			 final double scale = (double)(colScaleDiff - 1.0) / diff;
			 final int scaleValue = Math.min (colScaleMin + colScaleDiff, 
					 Math.max (colScaleMin, colScaleMin + (colScaleDiff - (int)((valueInt - min) * scale))));
			 final Color heat = (empty ? Color.black : heatMapScale.getColour (scaleValue));
			 this.setToolTipText (tipFormatter.format (value));
			 this.setText (formatter.format (value));
			 comp.setForeground (heat);
			 selected = isSelected;
		 }
		 return comp;
	}
	
	
    public String toString () {
    	return "Heat Map Renderer, +ve values only";
    }
}
