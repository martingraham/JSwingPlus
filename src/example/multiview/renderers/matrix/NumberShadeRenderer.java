package example.multiview.renderers.matrix;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import swingPlus.matrix.AbstractEdgeRenderer;



public class NumberShadeRenderer extends AbstractEdgeRenderer {
	


	/**
	 * 
	 */
	private static final long serialVersionUID = -1448778513664444506L;
	private static final Font NFONT = Font.decode ("Gill-Sans-MT-plain-9");

	
    public NumberShadeRenderer () {
	    // Don't paint behind the component
	    setOpaque (true);
	    setBackground (Color.red); // garish colour just so we can be certain no background rendering inputStream happening
    }

    @Override
	public void paintComponent (final Graphics gContext) {
    	
    	if (edge != null) {
    		final Integer iObj= (Integer)edge.getEdgeObject();
    		if (iObj != null) {
    			

    			final int height = this.getHeight();
    			final int width = this.getWidth();
	    		
    			final int size = iObj.intValue();
    			final Graphics2D graphics2D = (Graphics2D)gContext;
    			final int sizeScaled = Math.min (size * 10, 255);
	    	    graphics2D.setColor (new Color (190 - (sizeScaled * 2 / 3), 190 - (sizeScaled * 2 / 3), 220));
	    	    graphics2D.fillRect (0, 0, width, height);
	    	    
	    	    if (height > 6) {
		    	    String str = Integer.toString (size);
	    			FontRenderContext frc = graphics2D.getFontRenderContext();
	    			Rectangle2D strBounds = NFONT.getStringBounds (str, frc);
		    	    
	    			Color c = graphics2D.getColor();
		    	    graphics2D.setColor(new Color ((c.getRed() + 128) % 256, (c.getGreen() + 128) % 256, (c.getBlue() + 128) % 256));
		    	    graphics2D.drawString (str, (width - (int)strBounds.getWidth()) / 2, (int)strBounds.getHeight() + (height - (int)strBounds.getHeight()) / 2);
	    	    }
	    	}
    	}
    }
}
