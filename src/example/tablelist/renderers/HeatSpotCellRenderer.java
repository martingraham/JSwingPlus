package example.tablelist.renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;

import util.colour.ColourArray;
import util.colour.RGBNonLinear;

public class HeatSpotCellRenderer extends DefaultTableCellRenderer implements PropertyChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3396578738991433405L;

	boolean empty;
	ColourArray heatMapScale = new ColourArray (new RGBNonLinear ());
	NumberFormat formatter = NumberFormat.getInstance();
	NumberFormat tipFormatter = NumberFormat.getInstance();
	double min = 0.0, max = 10.0;
	boolean selected;
	MatteBorder mBorder = BorderFactory.createMatteBorder (2, 2, 0, 0, new Color (255, 255, 255, 128));
	Color sheen1 = new Color (255, 255, 255, 128);
	Color sheen2 = new Color (0, 0, 0, 48);
	
	public HeatSpotCellRenderer () {
		super ();
		setOpaque (false);
		formatter.setMaximumFractionDigits (3);
		tipFormatter.setMaximumFractionDigits (6);
	}
	
	@Override
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		 
		final Component comp = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
		 
		 if (value instanceof Number) {
			 final double valueInt = ((Number)value).doubleValue();
			 empty = (valueInt == 0.0);
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
	 
	@Override
	 public void paintComponent (final Graphics graphics) {
		if (empty) {
			//graphics.fillRect (0, 0, this.getWidth(), this.getHeight());
		} else {
			graphics.fill3DRect (0, 0, this.getWidth(), this.getHeight(), true);
			this.setForeground (Color.black);
			super.paintComponent (graphics);
			if (selected) {
				drawSheen ((Graphics2D)graphics, new Rectangle (0, 0, this.getWidth(), this.getHeight()));
			}
		}
	 }

	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if (evt != null && "minmax".equals (evt.getPropertyName())) {
			final Point2D minmax = (Point2D)evt.getNewValue ();
			min = minmax.getX();
			max = minmax.getY();
		}
	}
	
    protected void drawSheen (final Graphics2D graphics2D, final Rectangle cellRect) {
	   	 final GradientPaint gradient = new GradientPaint (
	   			 cellRect.x, cellRect.y, sheen1, 
	   			 (int)cellRect.getMaxX(), (int)cellRect.getMaxY(), sheen2,
	                false); 
	   	 final Paint oldPaint = graphics2D.getPaint();
	   	 graphics2D.setPaint (gradient);
	   	 graphics2D.fill (cellRect);
	   	 graphics2D.setPaint (oldPaint);
   }
    
    public String toString () {
    	return "Heat Map Renderer";
    }
}
