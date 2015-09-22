package example.graph.renderers.edge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import swingPlus.graph.AbstractGraphEdgeRenderer;
import swingPlus.graph.JGraph;

public class SimpleEdgeRenderer extends AbstractGraphEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3309916289376155650L;
	
 	static final private Stroke STROKE = new BasicStroke (1.0f);
 	static final private Stroke SELECTED_STROKE = new BasicStroke (2.0f);
 	
 	static final private Color COLOUR = Color.lightGray;
 	static final private Color SELECTED_COLOUR = new Color (128, 128, 224);
 	
 	boolean selected;
 	
	public SimpleEdgeRenderer () {
		super ();
		//setBorder (null);
		setBackground (Color.blue.brighter());
		setForeground (Color.black);
		//setFont (Font.decode ("Arial-plain-11"));
	}

	
	@Override
	public Component getGraphEdgeRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus,
			final int x1, final int y1, final int x2, final int y2) {
		super.getGraphEdgeRendererComponent (graph, value, isSelected, hasFocus, x1, y1, x2, y2);
		setForeground (isSelected ? SELECTED_COLOUR : COLOUR);
		selected = isSelected;
		return this;
	}
	
	@Override
	public void paintComponent (final Graphics graphics) {
		if (obj != null) {
			translateToOrigin (graphics);
			((Graphics2D)graphics).setStroke (selected ? SELECTED_STROKE : STROKE);
			graphics.drawLine (fromX, fromY, toX, toY);
			translateBack (graphics);
		}
	}
}
