package example.graph.renderers.edge;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import model.graph.Edge;
import model.graph.EdgeDirection;


public class RoslinEdgeRenderer2 extends RoslinEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3309916289376155650L;
	
 	
	public RoslinEdgeRenderer2 () {
		super ();
		setBorder (null);
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
			if (selected) {
				graphics.setColor (Color.yellow);
			}
			graphics.drawLine (fromX, fromY, toX, toY);
		}
	}
}
