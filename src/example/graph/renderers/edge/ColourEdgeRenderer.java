package example.graph.renderers.edge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import model.graph.Edge;

import swingPlus.graph.AbstractGraphEdgeRenderer;

public class ColourEdgeRenderer extends AbstractGraphEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3309916289376155650L;
	
 	static final private Stroke STROKE = new BasicStroke (2.0f);
 	
	public ColourEdgeRenderer () {
		setBorder (null);
		setBackground (Color.blue.brighter());
		setForeground (Color.black);
		//setFont (Font.decode ("Arial-plain-11"));
	}

	
	@Override
	public void paintComponent (final Graphics graphics) {
		if (obj != null) {
			((Graphics2D)graphics).setStroke (STROKE);
			final Color col = (Color)((Edge)obj).getEdgeObject();
			//g.setColor (Color.black);
			graphics.setColor (col);
			
			graphics.drawLine (fromX - this.getX(), fromY - this.getY(), toX - this.getX(), toY - this.getY());
		}
	}
}
