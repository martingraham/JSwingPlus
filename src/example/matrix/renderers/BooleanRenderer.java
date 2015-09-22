package example.matrix.renderers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import swingPlus.matrix.AbstractEdgeRenderer;



public class BooleanRenderer extends AbstractEdgeRenderer {
	
   	/**
	 * 
	 */
	private static final long serialVersionUID = 3083925789431090043L;

	
    public BooleanRenderer () {
	    // Don't paint behind the component
	    setOpaque (true);
	    setBackground (Color.red); // garish colour just so we can be certain no background rendering inputStream happening
    }

    @Override
	public void paintComponent (final Graphics gContext) {
    	
    	if (edge != null) {
    		final Boolean bool = (Boolean)edge.getEdgeObject();
    		if (bool == true) {
    			final int height = this.getHeight();
    			final int width = this.getWidth();
	    		
    			final Graphics2D graphics2D = (Graphics2D)gContext;
	    	    graphics2D.setColor (Color.green);
	    	    graphics2D.fillRect (0, 0, width, height);
	    	}
    	}
    }
}
