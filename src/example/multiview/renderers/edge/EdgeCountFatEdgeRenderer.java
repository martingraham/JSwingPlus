package example.multiview.renderers.edge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import model.graph.Edge;

import swingPlus.graph.AbstractGraphEdgeRenderer;
import swingPlus.graph.JGraph;

public class EdgeCountFatEdgeRenderer extends AbstractGraphEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3309916289376155650L;
 	
 	boolean selected;
 	Color colour;
 	Color selectedColour;
 	
 	Integer edgeVal;
 	
	public EdgeCountFatEdgeRenderer () {
		super ();
		//setBorder (null);
		setBackground (Color.blue.brighter());
		setForeground (Color.black);
		//setFont (Font.decode ("Arial-plain-11"));
		colour = new Color (128, 128, 164);
		selectedColour = new Color (64, 64, 128);
	}

	
	@Override
	public Component getGraphEdgeRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus,
			final int x1, final int y1, final int x2, final int y2) {
		super.getGraphEdgeRendererComponent (graph, value, isSelected, hasFocus, x1, y1, x2, y2);
		final Edge edge = (Edge)value;
		final Object edgeValue = edge.getEdgeObject();
		if (edgeValue instanceof Integer) {
			edgeVal = (Integer)edge.getEdgeObject();
		}
		setForeground (isSelected ? selectedColour : colour);
		selected = isSelected;
		return this;
	}
	
	@Override
	public void paintComponent (final Graphics graphics) {
		if (obj != null) {
			translateToOrigin (graphics);
			((Graphics2D)graphics).setStroke (new BasicStroke ((float)Math.sqrt (edgeVal.floatValue())));
			graphics.drawLine (fromX, fromY, toX, toY);
			translateBack (graphics);
		}
	}
}
