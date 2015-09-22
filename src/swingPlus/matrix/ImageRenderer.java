package swingPlus.matrix;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;




public class ImageRenderer extends AbstractEdgeRenderer {
	
   	/**
	 * 
	 */
	private static final long serialVersionUID = 3083925789431090043L;

	
    public ImageRenderer () {
	    // Don't paint behind the component
	    setOpaque (true);
	    setBackground (Color.red); // garish colour just so we can be certain no background rendering inputStream happening
    }
    
    
    @Override
	public void paintComponent (final Graphics gContext) {
    	
    	if (edge != null) {
    		final Image img = (Image) edge.getEdgeObject();
    		if (img != null) {
    			final double imgHeight = img.getHeight(this);
	    		final double imgWidth = img.getWidth(this);
	    		final double renderHeight = this.getHeight();
	    		final double renderWidth = this.getWidth();
	    		double sh = 0.0, sw = 0.0;
	    		final double hratio = renderHeight / imgHeight;
	    		final double wratio = renderWidth / imgWidth;
	    		if (hratio > wratio) {
	    			sw = renderWidth;
	    			sh = imgHeight * wratio;
	    		} else {
	    			sw = imgWidth * hratio;
	    			sh = renderHeight;
	    		}
	    		
	    		final Graphics2D graphics2D = (Graphics2D)gContext;
	    	    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	    	                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    	    graphics2D.drawImage (img, 0, 0, (int)sw, (int)sh, null);
	    	}
    	}
    }
    
    @Override
    public Dimension getPreferredSize () {
    	if (edge != null) {
    		final Image img = (Image) edge.getEdgeObject();
    		final double imgHeight = img.getHeight (this);
    		final double imgWidth = img.getWidth (this);
    		final double adjHeight = Math.max (192, imgHeight / 20);
    		final double adjWidth = imgWidth / (imgHeight / adjHeight);
			prefSize.setSize (adjWidth, adjHeight);
    	}
		return prefSize;
    }
}
