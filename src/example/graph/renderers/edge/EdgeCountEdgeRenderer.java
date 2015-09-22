package example.graph.renderers.edge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import model.graph.Edge;

import swingPlus.graph.AbstractGraphEdgeRenderer;
import swingPlus.graph.JGraph;
import util.colour.ColorUtilities;

public class EdgeCountEdgeRenderer extends AbstractGraphEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3309916289376155650L;
	
 	static final private Stroke STROKE = new BasicStroke (1.0f);
 	static final private Stroke SELECTED_STROKE = new BasicStroke (2.0f);
 	
 	static final int COLOUR_RANGE = 256;
 	
 	boolean selected;
 	Color[] colours;
 	Color[] selectedColours;
 	
	public EdgeCountEdgeRenderer () {
		super ();
		//setBorder (null);
		setBackground (Color.blue.brighter());
		setForeground (Color.black);
		//setFont (Font.decode ("Arial-plain-11"));
		colours = new Color [COLOUR_RANGE];
		selectedColours = new Color [COLOUR_RANGE];
		final Color colour1 = new Color (240, 240, 240);
		final Color colour2 = Color.black;
		final Color colour3 = new Color (128, 128, 255);
		final Color colour4 = Color.orange;
		for (int col = 0; col < colours.length; col++) {
			colours [col] = ColorUtilities.mixColoursAndAlpha (colour1, colour2, ((COLOUR_RANGE - 1) - col) / (float)(COLOUR_RANGE - 1));
			selectedColours [col] = ColorUtilities.mixColoursAndAlpha (colour3, colour4, ((COLOUR_RANGE - 1) - col) / (float)(COLOUR_RANGE - 1));
		}
	}

	
	@Override
	public Component getGraphEdgeRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus,
			final int x1, final int y1, final int x2, final int y2) {
		super.getGraphEdgeRendererComponent (graph, value, isSelected, hasFocus, x1, y1, x2, y2);
		final Edge edge = (Edge)value;
		final Object obj1 = edge.getNode1 ();
		final Object obj2 = edge.getNode2 ();
		final int sumedges = graph.getModel().getEdges(obj1).size() + graph.getModel().getEdges(obj2).size();
		setForeground (isSelected ? selectedColours [Math.min (COLOUR_RANGE - 1, sumedges)] : colours [Math.min (COLOUR_RANGE - 1, sumedges)]);
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
