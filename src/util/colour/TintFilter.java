package util.colour;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

/**
 * An image filter that "disables" an image by turning
 * it into a grayscale image, and brightening the pixels
 * in the image. Used by buttons to create an image for
 * a disabled button.
 *
 * @author      Jeff Dinkins
 * @author      Tom Ball
 * @author      Jim Graham
 * @version     1.16 11/17/05
 */
public class TintFilter extends RGBImageFilter {
    private final Color tintColour;
    

    public TintFilter (final Color tintColour) {
    	super ();
        this.tintColour = tintColour;

        // canFilterIndexColorModel indicates whether or not it is acceptable
        // to apply the color filtering of the filterRGB method to the color
        // table entries of an IndexColorModel object in lieu of pixel by pixel
     	// filtering.
        canFilterIndexColorModel = true;
    }
    
    /**
     * Overrides <code>RGBImageFilter.filterRGB</code>.
     */
    @Override
	public int filterRGB (final int x, final int y, final int rgb) {
        // Use NTSC conversion formula.
    	int tint;
    	if ((rgb >> 24) < 0xff) {
	    	int gray = (int)Math.round ((0.30 * ((rgb >> 16) & 0xff) + 
	                0.59 * ((rgb >> 8) & 0xff) + 
	                0.11 * (rgb & 0xff)));
	    	gray = gray & 0xff;
	    	gray = gray + (gray << 8) + (gray << 16);
	    	gray = (gray & 0x00fefefe);
	    	gray >>= 1;
	
	    	final int tintRGB = (tintColour.getRGB() & 0x00fefefe) >> 1;	
	    	final int mix = gray + tintRGB;
	    	tint = mix | (rgb & 0xff000000);
    	} else {
    		tint = rgb;
    	}
	
        return tint;
    }
}
