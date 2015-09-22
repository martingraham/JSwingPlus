package util.colour;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
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
public class GrayFilter2 extends RGBImageFilter {
    private final boolean brighter;
    private final int percent;
    
    public static final GrayFilter2 DEFAULT_FILTER = new GrayFilter2 (false, 0);
    /**
     * Creates a disabled image
     */
    public static Image createDisabledImage (final Image img) {
    	final GrayFilter2 filter = new GrayFilter2 (true, 50);
    	final ImageProducer prod = new FilteredImageSource(img.getSource(), filter);
    	return Toolkit.getDefaultToolkit().createImage(prod);
    }
    
    /**
     * Constructs a GrayFilter object that filters a color image to a 
     * grayscale image. Used by buttons to create disabled ("grayed out")
     * button images.
     *
     * @param makeBrighter  a boolean -- true if the pixels should be brightened
     * @param per  an int in the range 0..100 that determines the percentage
     *           of gray, where 100 is the darkest gray, and 0 is the lightest
     */
    public GrayFilter2 (final boolean makeBrighter, final int per) {
    	super ();
        brighter = makeBrighter;
        percent = per;

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
    	int gray = (int)Math.round ((0.30 * ((rgb >> 16) & 0xff) + 
                0.59 * ((rgb >> 8) & 0xff) + 
                0.11 * (rgb & 0xff)));


        if (brighter) {
            gray = (255 - ((255 - gray) * (100 - percent) / 100));
        } else {
            gray = (gray * (100 - percent) / 100);
        }
	
        if (gray < 0) {
        	gray = 0;
        } else if (gray > 255) {
        	gray = 255;
        }
        return (rgb & 0xff000000) | (gray << 16) | (gray << 8) | (gray << 0);
    }
}

