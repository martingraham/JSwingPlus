package example.matrix.renderers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import swingPlus.matrix.AbstractEdgeRenderer;



public class ShapeRenderer extends AbstractEdgeRenderer {
	
   	/**
	 * 
	 */
	private static final long serialVersionUID = 3083925789431090043L;

	
    public ShapeRenderer () {
	    // Don't paint behind the component
	    setOpaque (true);
	    setBackground (Color.red); // garish colour just so we can be certain no background rendering inputStream happening
    }

    @Override
	public void paintComponent (final Graphics gContext) {
    	
    	if (edge != null) {
    		final Shape shape = (Shape)edge.getEdgeObject();
    		
    		if (shape != null) {
    			final Rectangle rect = shape.getBounds();
    			final double height = rect.getHeight();
    			final double width = rect.getWidth();
    			final double h2 = this.getHeight();
    			final double w2 = this.getWidth();
    			final double hratio = h2 / height;
    			final double wratio = w2 / width;	
	    		
    			final Graphics2D graphics2D = (Graphics2D)gContext;
	    	    graphics2D.scale (Math.min (hratio, wratio), Math.min (hratio, wratio));
	    	    graphics2D.fill (shape);
	    	    graphics2D.scale (1.0 / Math.min (hratio, wratio), 1.0 / Math.min (hratio, wratio));
	    	}
    	}
    }
    
    @Override
    public Dimension getPreferredSize () {
    	if (edge != null) {
    		final Shape shape = (Shape) edge.getEdgeObject();
    		final Rectangle rect = shape.getBounds();
    		final double height = rect.getHeight ();
    		final double width = rect.getWidth ();
			//double sh = Math.max (192, h / 20);
			//double sw = w / (h / sh);
			prefSize.setSize (height, width);
    	}
		return prefSize;
    }
}
