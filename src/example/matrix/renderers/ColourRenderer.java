package example.matrix.renderers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import swingPlus.matrix.AbstractEdgeRenderer;



public class ColourRenderer extends AbstractEdgeRenderer {
	
   	/**
	 * 
	 */
	private static final long serialVersionUID = 3083925789431090043L;

	
    public ColourRenderer () {
	    // Don't paint behind the component
	    setOpaque (true);
	    setBackground (Color.red); // garish colour just so we can be certain no background rendering inputStream happening
    }

    @Override
	public void paintComponent (final Graphics gContext) {
    	
    	if (edge != null) {
    		final Color color = (Color)edge.getEdgeObject();
    		if (color != null) {
    			final int height = this.getHeight();
    			final int width = this.getWidth();
	    		
    			final Graphics2D graphics2D = (Graphics2D)gContext;
	    	    graphics2D.setColor (color);
	    	    graphics2D.fillRect (0, 0, width, height);
	    	}
    	}
    }
}
