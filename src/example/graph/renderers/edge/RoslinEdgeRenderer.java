package example.graph.renderers.edge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import example.graph.roslin.Animal;

import model.graph.Edge;
import model.graph.EdgeDirection;

import swingPlus.graph.AbstractGraphEdgeRenderer;
import swingPlus.graph.JGraph;
import util.colour.ColorUtilities;

public class RoslinEdgeRenderer extends AbstractGraphEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3309916289376155650L;
	
	protected static final Stroke STROKE = new BasicStroke (2.0f);
	protected static final Color[] COLOURS = {Color.gray, Color.blue, Color.pink, Color.lightGray, 
 			ColorUtilities.mixColours (Color.blue, Color.white, 0.5f),
 			ColorUtilities.mixColours (Color.pink, Color.white, 0.5f)};
	
 	protected transient boolean selected;
 	
	public RoslinEdgeRenderer () {
		super ();
		setBackground (Color.blue.brighter());
		setForeground (Color.black);
		//setFont (Font.decode ("Arial-plain-11"));
	}
	
	
	@Override
	public Component getGraphEdgeRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus,
			final int x1, final int y1, final int x2, final int y2) {
		super.getGraphEdgeRendererComponent (graph, value, isSelected, hasFocus, x1, y1, x2, y2);
		selected = isSelected;
		return this;
	}


	
	@Override
	public void paintComponent (final Graphics graphics) {
		if (obj instanceof Edge) {
			translateToOrigin (graphics);
			final Edge edge = (Edge)obj;
			final Object parent = (edge.getEdgeObject() == EdgeDirection.FROM) ? edge.getNode1() : edge.getNode2();
			final int colourIndex = getColourIndex (parent);
			((Graphics2D)graphics).setStroke (STROKE);
			graphics.setColor (COLOURS [colourIndex]);
			final int diffx = toX - fromX;
			final int diffy = toY - fromY;
			graphics.drawLine (fromX, fromY, fromX + (diffx / 2), fromY + (diffy / 2));
			graphics.setColor (COLOURS [colourIndex + 3]);
			if (selected) {
				graphics.setColor (Color.yellow);
			}
			graphics.drawLine (fromX + (diffx / 2), fromY + (diffy / 2), toX, toY);
		}
	}
	
	protected int getColourIndex (final Object parentObj) {
		if (parentObj instanceof Animal) {
			return ((Animal)parentObj).isMale() ? 1 : 2;
		}
		return 0;
	}
}
