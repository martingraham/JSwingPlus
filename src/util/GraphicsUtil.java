package util;

import io.DataPrep;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import sun.swing.SwingUtilities2;
import ui.UIConstants;
import util.colour.TintFilter;
import util.swing.AbstractMyHintsKey;



/**
 *  Description of the Class
 *
 *@author     marting
 *@created    07 October 2005
 */
public class GraphicsUtil extends Object {


	final static Logger LOGGER = Logger.getLogger (GraphicsUtil.class);
	
    public static final char ELLIPSIS = '\u2026';
    public static final String GRAPHICPROPS = "graphic";
    public static final Insets ZEROINSETS = new Insets (0, 0, 0, 0);
    public static final String IMAGEDIR = Messages.getString ("graphic", "ImageDir");
    public static final Color NULLCOLOUR = new Color (0, 0, 0, 0);
    public static final BasicStroke DEFAULTSTROKE = new BasicStroke ();
    private static final int FACTORIAL [] = {1, 1, 2, 6, 24, 120, 720};
    public static final TexturePaint DRAGSURFACE = GraphicsUtil.makeTexture ("DragPattern", 0.4f, 0f, 0f);
    public static final String SVG_GRAPHICS = Messages.getString ("BatikGraphicsClassID");
    
    public static final String DRAW_ALL_OFFSCREEN = "Draw_all_offscreen";
    public static final String DRAW_ONSCREEN = "Draw_onscreen";
    public static final AbstractMyHintsKey DRAWTOFILE = new AbstractMyHintsKey (55) {
		@Override
		public boolean isCompatibleValue (final Object val) {
			return (DRAW_ALL_OFFSCREEN == val) || (DRAW_ONSCREEN == val);
		}
    };

    public static final FileFilter JPG_FILENAME_FILTER = new JPGFilter ();
    public static final FileFilter GIF_FILENAME_FILTER = new GIFFilter ();
    
    /**
     *  Description of the Method
     */


    

	public static int stringCharsFitLength2 (int maxStrLength, final String str, final int strPixelLength,
	 										final FontMetrics fontMetrics, final boolean ellip) {
		int roughGuess = str.length();
		int maxLength = maxStrLength;
		
		if (maxLength < strPixelLength) {
			final int stringLength = str.length();
			if (ellip) {
				maxLength = Math.max (0, maxLength - fontMetrics.charWidth (ELLIPSIS));
			}
			
			roughGuess = (int)(((double) (maxLength) / (double) strPixelLength) * stringLength);
			int guessStringLength;
	
			if (roughGuess > 0) {
				final String guessString = (roughGuess >= stringLength ? str : str.substring (0, roughGuess));
				guessStringLength = fontMetrics.stringWidth (guessString);
			} else {
				guessStringLength = 0;
			}
	
			if (guessStringLength > maxLength - 2) {
				while (guessStringLength > maxLength - 2 && --roughGuess >= 0) {
					// do --roughGuess before charAt so we are taking away last character of current considered substring in charAt
					guessStringLength -= fontMetrics.charWidth (str.charAt (roughGuess));
				}
			} else {
				while (roughGuess < stringLength && guessStringLength < maxLength - 2) {
					guessStringLength += fontMetrics.charWidth (str.charAt (roughGuess));
					roughGuess++; // do after charAt so roughGuess, when used in charAt, inputStream the first character after the current considered substring
				}
				roughGuess--;
			}
		}

		return roughGuess;
	}


	// Calcs line between centres of two rectangles, but starting and finishing at rectangle boundaries
	// [   ]----[    ]

	public static void gapLine (final Rectangle rect1, final Rectangle rect2, 
			final Point start, final Point end) {

		final Point point1 = new Point (rect1.x + (rect1.width >> 1), rect1.y + (rect1.height >> 1));
		final Point point2 = new Point (rect2.x + (rect2.width >> 1), rect2.y + (rect2.height >> 1));
		final Point pointDiff = new Point (point2.x - point1.x, point2.y - point1.y);


	    final double angle2 = angleToVertical (pointDiff.x, pointDiff.y);
	    double angle1 = angleToVertical (rect1.width, rect1.height);
	    boolean vertEdgeCutOff = (angle1 <= angle2);

	    if (vertEdgeCutOff) {
            final double ratio = (rect1.width >> 1) / Math.abs (pointDiff.getX());
	        start.setLocation (point1.x + ((point1.x > point2.x ? -rect1.width : rect1.width) >> 1), point1.y + (int)(ratio * pointDiff.getY()));
	    } else {
	    	final double ratio = (rect1.height >> 1) / Math.abs (pointDiff.getY());
	    	start.setLocation (point1.x + (int)(ratio * pointDiff.getX()), point1.y + ((point1.y > point2.y ? -rect1.height : rect1.height) >> 1));
	    } 


	    angle1 = angleToVertical (rect2.width, rect2.height);
		vertEdgeCutOff = (angle1 <= angle2);

		if (vertEdgeCutOff) {
            final double ratio = (rect2.width >> 1) / Math.abs (pointDiff.getX());
	        end.setLocation (point2.x + ((point2.x > point1.x ? -rect2.width : rect2.width) >> 1), point2.y - (int)(ratio * pointDiff.getY()));
	    } else {
	    	final double ratio = (rect2.height >> 1) / Math.abs (pointDiff.getY());
	    	end.setLocation (point2.x - (int)(ratio * pointDiff.getX()), point2.y + ((point2.y > point1.y ? -rect2.height : rect2.height) >> 1));
	    } 
	}


	private static double angleToVertical (final int x, final int y) {
		double ang = Math.atan2 (y, x);
		ang = Math.abs ((Math.PI / 2.0) - Math.abs (ang));
		return ang;
	}

	
	public static Point rotate (final Point point, final double theta) {
		final double x = point.x;
		final double y = point.y;
		point.y = (int)((Math.sin (theta) * x) + (Math.cos (theta) * y));
		point.x = (int)((-Math.sin (theta) * y) + (Math.cos (theta) * x));
		return point;
	}
	
	// From http://blog.persistent.info/2004/03/java-lineline-intersections.html
	public static Point2D getIntersection (Point2D.Double point, final double x1, final double y1, 
			final double x2, final double y2, 
			final double x3, final double y3, 
			final double x4, final double y4) {

		if (point == null) {
			point = new Point2D.Double();
		}
		
		point.x = det (det (x1, y1, x2, y2), x1 - x2,
		  det (x3, y3, x4, y4), x3 - x4) /
		det (x1 - x2, y1 - y2, x3 - x4, y3 - y4);
		point.y = det (det (x1, y1, x2, y2), y1 - y2,
		  det (x3, y3, x4, y4), y3 - y4) /
		det (x1 - x2, y1 - y2, x3 - x4, y3 - y4);
		
		return point;
	}

	private final static double det (final double a, final double b, 
			final double c, final double d) {
		return a * d - b * c;
	}
	
	
	public static Point2D orthogonal (final int x, final int y) {
		return new Point2D.Double (-y / 10.0, x / 10.0);
	}
	
	
	public static Point getParametric (final Line2D line, final double percentage) {

		final Point point = new Point ();		
        point.x = (int) (line.getX1() + (line.getX2() - line.getX1()) * percentage);
        point.y = (int) (line.getY1() + (line.getY2() - line.getY1()) * percentage);
        return point;
    }
	
	
	public static Point getParametric (final QuadCurve2D quadCurve, final double percentage) {

		final double tParam = percentage;
		final Point point = new Point ();
		
		// Construct blending terms for quadratic spline
		final double t1 = parPowerCalc (2, 0, tParam);
		final double t2 = parPowerCalc (2, 1, tParam);
		final double t3 = parPowerCalc (2, 2, tParam);
		
        point.x = (int) (t1*quadCurve.getX1() + t2*quadCurve.getCtrlX() + t3*quadCurve.getX2());
        point.y = (int) (t1*quadCurve.getY1() + t2*quadCurve.getCtrlY() + t3*quadCurve.getY2());
        return point;
    }
	
	public static Point getParametric (final CubicCurve2D cubicCurve, final double percentage) {

		final double tParam = percentage;
		final Point point = new Point ();
		
		// Construct blending terms for cubic spline
		final double t1 = parPowerCalc (3, 0, tParam);
		final double t2 = parPowerCalc (3, 1, tParam);
		final double t3 = parPowerCalc (3, 2, tParam);
		final double t4 = parPowerCalc (3, 3, tParam);
		
        point.x = (int) (t1*cubicCurve.getX1() + t2*cubicCurve.getCtrlX1() + t3*cubicCurve.getCtrlX2() + t4*cubicCurve.getX2());
        point.y = (int) (t1*cubicCurve.getY1() + t2*cubicCurve.getCtrlY1() + t3*cubicCurve.getCtrlY2() + t4*cubicCurve.getY2());
        return point;
    }
 
	// Calculates individual binomial coefficients (Bn) for a spline
	// i.e. for B2 in a cubic spline, power == 1, maxPower == 3, multiplier is 3! / (1! * (3 - 1)!) = 3! / 2! = 3
	// then function is 3 * ((tParam ^ 1) * ((1 - tParam) ^ 2))) 
    private static double parPowerCalc (final int maxPower, final int power, final double tParam) {
        final int term = FACTORIAL[maxPower] / (FACTORIAL[power] * FACTORIAL[maxPower - power]);
        return term * Math.pow (tParam, power) * Math.pow (1.0 - tParam, maxPower - power);
    }

    
	public static String makeFilename (final Class<?> classObj, final String descriptor) {
		final String propertyString = Messages.getString (GRAPHICPROPS, classObj, descriptor);
		return IMAGEDIR + "/" + propertyString;
    }
		
	public static String makeFilename (final String descriptor) {
		final String propertyString = Messages.getString (GRAPHICPROPS, descriptor);
		LOGGER.debug (IMAGEDIR + "/" + propertyString);
		return IMAGEDIR + "/" + propertyString;
    }
	
	/*
	public static String makeHTMLURLString (final Class<?> cl, final String descriptor) {
		 final String iconPath = GraphicsUtil.makeFilename (cl, descriptor);
		 return DataPrep.getInstance().getRelativeURL(iconPath).toString(); 
	}
	
	public static String makeHTMLURLString (final String descriptor) {
		 final String iconPath = GraphicsUtil.makeFilename (descriptor);
		 return DataPrep.getInstance().getRelativeURL(iconPath).toString(); 
	}
	*/
	public static BufferedImage loadBufferedImage (final String fileName) {
		
		BufferedImage bim = null;
		
		if (fileName != null) {
			try {
				final URL url = DataPrep.getInstance().getRelativeURL (fileName);
				if (url != null) {
					bim = ImageIO.read (DataPrep.getInstance().getRelativeURL (fileName));
				
					// From http://stackoverflow.com/questions/196890/java2d-performance-issues
			        final GraphicsConfiguration gConfig = getDefaultGraphicsConfig(); 
		 
					/* 
					 * if image is already compatible and optimized for current system  
					 * settings, simply return it 
					 */ 
					if (! bim.getColorModel().equals (gConfig.getColorModel())) { 
						// image is not optimized, so create a new image that is 
						LOGGER.info ("loadBufferedImage++++++++++");
						printBufferedImageDetails (bim);
						final BufferedImage newImage = copyImage (bim);
						bim = newImage;
					}
				}
			}
			catch (final IOException ioe) {
				LOGGER.error (ioe.toString(), ioe);
			}
		}
		
		return bim; 
	}
	
	

    
	
    public static BufferedImage initImage (final BufferedImage img, final Component fitMe, final int transparency) {
	    return initImage (img, fitMe, fitMe.getSize(), transparency);
	}
    
    public static BufferedImage initImage (BufferedImage img, final Component fitMe, final Dimension dim, final int transparency) {
	    return initImageWithinTolerance (img, fitMe, dim, transparency, 0);
	}
    
    // tolerance - if image is less than 'tolerance' pixels wider and taller
    // than the area we want a new image for, just keep the same image
    public static BufferedImage initImageWithinTolerance (BufferedImage img, final Component fitMe, 
    		final Dimension dim, final int transparency, final int tolerance) { 	
	    if (img == null 
	    		|| img.getHeight (fitMe) < dim.height || img.getHeight (fitMe) > dim.height + tolerance 
	    		|| img.getWidth (fitMe) < dim.width || img.getWidth (fitMe) > dim.width + tolerance) {
	     	LOGGER.debug ("init buffer: "+img+", comp: "+(fitMe == null ? null : fitMe.getSize())+", d: "+dim.getSize());
	        final GraphicsConfiguration gConfig = getDefaultGraphicsConfig();
	        //GraphicsConfiguration gc2 = fitMe.getGraphicsConfiguration();
  	
	       	//img = gc.createCompatibleImage
            //      (Math.max (1, fitMe.getWidth()), Math.max (1, fitMe.getHeight()), Transparency.BITMASK);
	        //int width =
		    img = gConfig.createCompatibleImage
		       		(Math.max (1, dim.width), Math.max (1, dim.height), transparency);
		    img.setAccelerationPriority (1.0f);
		    printBufferedImageDetails (img);
	    }
	    return img;
	}
    
    
    public static final GraphicsConfiguration getDefaultGraphicsConfig () {
        final GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice gDevice = gEnv.getDefaultScreenDevice();
        final GraphicsConfiguration gConfig = gDevice.getDefaultConfiguration();
        return gConfig;
    }
    

	public static void clearImage (final Image img) {
	    if (img != null) {
	       img.getGraphics().dispose();
	       img.flush();
	       //img = null;
	    }
	}
	
	public static Graphics primeImageGraphics (final Graphics graphics, final Rectangle clip, 
			final Image img, final Color color, final int layerIndex) {
    	if (isSVGGraphics (graphics)) {
    		return graphics;
    	}
		final Graphics2D g2D = (Graphics2D)img.getGraphics();
 		if (layerIndex != UIConstants.ALL) {
 			g2D.setComposite (AlphaComposite.SrcOver);
 		}
		//g2D.setComposite (AlphaComposite.Src);
		g2D.setBackground (layerIndex == UIConstants.ALL ? color : NULLCOLOUR);
		g2D.clearRect (clip.x, clip.y, clip.width, clip.height);
		return g2D;
	}
	
	public static Graphics primeImageGraphics (final Graphics graphics, final Rectangle clip, 
			final Image[] img, final Color color, final int layerIndex) {
    	return primeImageGraphics (graphics, clip, img[layerIndex], color, layerIndex);
	}
	
	
	public final static void printBufferedImageDetails (final BufferedImage img) {
        final GraphicsConfiguration gConfig = getDefaultGraphicsConfig();
		final ImageCapabilities iCap = img.getCapabilities (gConfig);
		if (LOGGER.isDebugEnabled()) {
			final StringBuilder strBuild = new StringBuilder ();
			strBuild.append ("\nImage: "+img);
			strBuild.append ("\nImage Type: "+img.getType());
			strBuild.append ("\nVolatile: "+iCap.isTrueVolatile()+"\tAcceleration: "+iCap.isAccelerated());
			strBuild.append ("\nColor Model: "+img.getColorModel());
			strBuild.append ("\nAcceleration Priority: "+img.getAccelerationPriority());
			strBuild.append ("\nTransparency: "+img.getTransparency());
			strBuild.append ("\n--------------------");
			LOGGER.debug(strBuild.toString());
		}
	}
	
	
	public static TexturePaint makeTexture (final String propertyNameString, final float alpha, final float xgap, final float ygap) {

        TexturePaint texPaint = null;
        final String fileName = makeFilename (propertyNameString);
        BufferedImage img = loadBufferedImage (fileName);

        if (img != null) {
           	if (alpha > 0.0f) {
        	   	final BufferedImage bimg = new BufferedImage (img.getWidth () + (int)(xgap * 2), img.getHeight () + (int)(ygap * 2), BufferedImage.TYPE_INT_ARGB);
           		final Graphics2D gTexture = bimg.createGraphics();
           		gTexture.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER, alpha));  // set transparency to * 0.7;
           		gTexture.drawImage (img, (int)xgap, (int)ygap, null);
           		img = bimg;
           	}
           	texPaint = new TexturePaint (img, new Rectangle (0, 0, img.getWidth (), img.getHeight ()));
        }

	    return texPaint;
	}
	
	
	public static Rectangle makeRect (final Point point1, final Point point2) {
		final int x = Math.min (point1.x, point2.x);
		final int y = Math.min (point1.y, point2.y);
		final int width = Math.abs (point1.x - point2.x);
		final int height = Math.abs (point1.y - point2.y);
		return new Rectangle (x, y, width, height);
	}
	
	/**
	 * 
	 * @param rect1 - Covering rectangle
	 * @param rect2 - Rectangle being overlapped
	 * @return the proportion of the area (between 0.0 and 1.0) of r2 overlapped by r1
	 */
	public static double overlap (final Rectangle rect1, final Rectangle rect2) {
		
		final int y1 = Math.max (rect1.y, rect2.y);
		final int y2 = Math.min (rect1.y + rect1.height, rect2.y + rect2.height);
		if (y1 >= y2) {
			return 0.0;
		}
		
		final int x1 = Math.max (rect1.x, rect2.x);
		final int x2 = Math.min (rect1.x + rect1.width, rect2.x + rect2.width);
		if (x1 >= x2) {
			return 0.0;
		}
		
		return ((double)(x2 - x1) / rect2.width) * ((double)(y2 - y1) / rect2.height);
	}
	
	
	/**
	 * 
	 * @param rect - The Rectangle to be shrunk
	 * @param insets - the Insets object whose dimensions we shrink the Rectangle by
	 */
	public static Rectangle adjustRectangle (final Rectangle rect, final Insets insets) {
		rect.x += insets.left;
		rect.y += insets.top;
		rect.width -= (insets.left + insets.right);
		rect.height -= (insets.top + insets.bottom);
		return rect;
	}
	
	/**
	 * 
	 * @param dim - The Dimension to be shrunk
	 * @param insets - the Insets object whose dimensions we shrink the Rectangle by
	 */
	public static Dimension adjustDimension (final Dimension dim, final Insets insets) {
		dim.width -= (insets.left + insets.right);
		dim.height -= (insets.top + insets.bottom);
		return dim;
	}
	

	
	
	/*
	public static void greyImage (final BufferedImage bi) {

		for (int x = bi.getWidth(); --x >= 0;) {
			int fib = 0, fia = 0;
			for (int y = bi.getHeight(); --y >= 0;) {
				fib = bi.getRGB (x, y);
				fia = GrayFilter2.DEFAULT_FILTER.filterRGB (x, y, fib);
				bi.setRGB(x, y, fia);
			}
			//LOGGER.debug ("fi g: "+colorString(fib)+" --> "+colorString(fia));
		}
	}
	*/
	
	
	public final static void removeComponents (final Container container, final Class<?> classObj) {

		final Component[] floatComps = container.getComponents ();
		for (int n = floatComps.length; --n >= 0;) {
			if (floatComps[n].getClass() == classObj) {
				container.remove (floatComps[n]);
			}
		}
	}
	
	
	public final static MouseEvent scaleMouseEvent (final MouseEvent mEvent, final Point2D scaling) {
		return scaleMouseEvent(mEvent, scaling.getX(), scaling.getY());
	}
	
	public final static MouseEvent scaleMouseEvent (MouseEvent mEvent, final double scalex, final double scaley) {
		final Point mousePoint = mEvent.getPoint ();

		//LOGGER.debug ("!!!!!!!!!!!!!!!!\nScaling Mouse Event\n!!!!!!!!!!");
		if (scalex != 1.0 || scaley != 1.0) {
			final double x = (double)mousePoint.x / scalex;
			final double y = (double)mousePoint.y / scaley;
			//e = new MouseEvent (e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)x, (int)y, 
			//					e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
			mEvent = new MouseEvent (mEvent.getComponent(), mEvent.getID(), mEvent.getWhen(), mEvent.getModifiers(), (int)x, (int)y, 
					mEvent.getClickCount(), mEvent.isPopupTrigger());
		}
		return mEvent;
	}
	
	public final static Point scalePoint (final Point point, final double scalex, final double scaley) {
		point.x = (int)((double)point.x * scalex);
		point.y = (int)((double)point.y * scaley);
		return point;
	}
	
	
	public final static MouseEvent convertMouseEvent (final MouseEvent mEvent, final JComponent comp, final int left, final int top) {
		final MouseEvent me2 = new MouseEvent (comp, mEvent.getID() + 1, System.nanoTime(),
        		mEvent.getModifiers(), mEvent.getX() - left, mEvent.getY() - top, 
        		mEvent.getXOnScreen(), mEvent.getYOnScreen(),
        		mEvent.getClickCount(), mEvent.isPopupTrigger(), mEvent.getButton());
        //System.err.println ("me2: "+me2.toString());
        return me2;
	}
	
	public final static boolean isSVGGraphics (final Graphics graphics) {
     	return graphics == null ? false : SVG_GRAPHICS.equals (graphics.getClass().getCanonicalName());
	}
	
	public final static boolean isNonScreenImage (final Graphics2D g2d) {
		return g2d.getRenderingHint (DRAWTOFILE) == DRAW_ALL_OFFSCREEN;
	}
	
	
	public final static BufferedImage copyImage (final BufferedImage origImage) {
		return copyImage (origImage, origImage.getTransparency());
	}
	
	/**
	 * Proper copy image, not just a twice-removed reference to the original image like subImage gives you
	 * @param origImage - the original image
	 * @return a copy of the original image
	 */
	public final static BufferedImage copyImage (final Image origImage, final int transparency) {
		LOGGER.info ("copyImage++++++++++");
		final BufferedImage bim = initImage (null, null, 
				new Dimension (origImage.getWidth (null), origImage.getHeight (null)), 
				transparency);
		final Graphics graphics = bim.getGraphics ();
		graphics.drawImage (origImage, 0, 0, null);
		return bim;
	}
	
	
	public final static BufferedImage makeTintedImage (final Color tint, final BufferedImage origImage) {

		final BufferedImage tintedImage = copyImage (origImage);
		final TintFilter tFilter = new TintFilter (tint);
		
		//tFilter.filterRGBPixels (0, 0, tintedImage.getWidth(), tintedImage.getHeight(), 
		//		tintedImage.getData().getPixels(x, y, w, h, iArray), 0, tintedImage.getWidth());
		
		for (int x = tintedImage.getWidth(); --x >= 0;) {
			for (int y = tintedImage.getHeight(); --y >= 0;) {
				final int fib = tintedImage.getRGB (x, y);
				final int fia = tFilter.filterRGB (x, y, fib);
				tintedImage.setRGB (x, y, fia);
			}
			//LOGGER.debug ("fi g: "+colorString(fib)+" --> "+colorString(fia));
		}
		
		return tintedImage;
	}
	
	
	public static List<JComponent> getComponentsBeneath (final JComponent root, final Class<?> classType, final boolean matchSubclasses) {
		final List<JComponent> matchingComps = new ArrayList<JComponent> ();
		if (classType != null) {
			gcbRecurse (root, matchingComps, classType, matchSubclasses);
		}
		
		return matchingComps;
	}
	
	static void gcbRecurse (final JComponent parent, final List<JComponent> matchingComps, final Class<?> classType, final boolean matchSubclasses) {
		if (classType.equals (parent.getClass()) || (matchSubclasses && classType.isAssignableFrom (parent.getClass()))) {
			matchingComps.add (parent);
		} else { 
			final Component[] children = parent.getComponents();
			for (int child = 0; child < children.length; child++) {
				final Component childComp = children [child];
				if (childComp instanceof JComponent) {
					gcbRecurse ((JComponent)childComp, matchingComps, classType, matchSubclasses);
				}
			}
		}
	}
	
	
	public static void makeThumbnailFileCache (final File directory, final File imageCache, final Dimension maxDims, final FileFilter filter) {
		if (imageCache.mkdir()) {
			LOGGER.info ("Thumbnail directory: "+directory);
			makeThumbnailCacheRecurse (directory, imageCache, maxDims, filter);
		}
	}
	
	
	/**
	 * Routine to calculate a rounded box shape, i.e. a box with semi-circular ends
	 * @param path	Path2D object that rounded box shape is returned in
	 * @param arc	Arc2D object used for calculating rounded edge
	 * @param arc2	Arc2D object used for calculating rounded edge
	 * @param height	height of rounded box, rounded edges are on the greater of height or width
	 * @param width	width of rounded box, rounded edges are on the greater of height or width
	 */
	public static void makeRoundedBox (final Path2D path, final Arc2D arc,
			final int height, final int width) {
		
		path.reset ();
		
		if (height > width) {		
			arc.setArc (0, 0, width - 1, width - 1, 180, -180, Arc2D.OPEN);
			path.append (arc, false);
			arc.setArc (0, height - width, width - 1, width - 1, 0, -180, Arc2D.OPEN);
		} else {
			arc.setArc (0, 0, height - 1, height - 1, 270, -180, Arc2D.OPEN);
			path.append (arc, false);
			arc.setArc (width - height, 0, height - 1, height - 1, 90, -180, Arc2D.OPEN);
		}
		
		path.append (arc, true);
		path.closePath();
	}
	
	
	public static Icon mergeIcons (final Icon icon1, final Icon icon2, final int hgap, final Component observer) {
		final Dimension mergedIconDim = new Dimension (
				icon1.getIconWidth() + hgap + icon2.getIconWidth(),
				Math.max (icon1.getIconHeight(), icon2.getIconHeight()));
		final BufferedImage img = GraphicsUtil.initImage (null, null, mergedIconDim, Transparency.TRANSLUCENT);
		final Graphics2D g2d = img.createGraphics();
		icon1.paintIcon (observer, g2d, 0, (img.getHeight() - icon1.getIconHeight()) / 2);
		icon2.paintIcon (observer, g2d, icon2.getIconWidth() + hgap,
				(img.getHeight() - icon2.getIconHeight()) / 2);
		return new ImageIcon (img);
	}
	
	
	static void makeThumbnailCacheRecurse (final File directory, final File cache, final Dimension maxDims, final FileFilter filter) {	
		
		final File[] files = directory.listFiles (filter);
		LOGGER.debug ("From directory: "+directory.getAbsolutePath());
		
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					 makeThumbnailCacheRecurse (file, cache, maxDims, filter);
				}
				else {
					try {
						final BufferedImage bim = ImageIO.read (file);
						final double shrinkRatio = Math.min (maxDims.getWidth() / bim.getWidth(), maxDims.getHeight() / bim.getHeight());
						final int sWidth = (int)Math.round (bim.getWidth() * shrinkRatio);
						final int sHeight = (int)Math.round (bim.getHeight() * shrinkRatio);
						final int type = (bim.getTransparency() == Transparency.OPAQUE) ?
								BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
						final BufferedImage newBim = new BufferedImage (sWidth, sHeight, type);
						final Graphics2D g2d = (Graphics2D)newBim.getGraphics();
						g2d.setRenderingHint (RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
						newBim.getGraphics().drawImage (bim, 0, 0, sWidth, sHeight, null);
						final File thumbnailFile = new File (cache.getPath() + File.separatorChar + "THUMB" + file.getName());
						ImageIO.write (newBim, "jpg", thumbnailFile);
						g2d.dispose ();
						bim.flush();
						newBim.flush();
						LOGGER.info ("Writing thumbnail "+thumbnailFile.getPath());
					} catch (IOException ioe) {
						LOGGER.error ("error reading image file", ioe);
					}
				}
			}
		}
	}
	
	
	
	public final static void setJDK7WindowProperties (final Window window, final boolean opaque, final float translucency, final Shape windowShape) {
		try {
			final Class<?> windowClass = Class.forName ("java.awt.Window");
			final Method mSetWindowTranslucency = windowClass.getMethod ("setOpacity", float.class);
			mSetWindowTranslucency.invoke (window, translucency);
			if (windowShape != null) {
				final Method mSetWindowShape = windowClass.getMethod ("setShape", Shape.class);
				mSetWindowShape.invoke (window, windowShape);
			}
		} catch (NoSuchMethodException ex) {
			LOGGER.error ("NoSuchMethodException Error", ex);
		} catch (SecurityException ex) {
			LOGGER.error ("SecurityException Error", ex);
		} catch (ClassNotFoundException ex) {
			LOGGER.error ("ClassNotFoundException Error", ex);
		} catch (IllegalAccessException ex) {
			LOGGER.error ("IllegalAccessException Error", ex);
		} catch (IllegalArgumentException ex) {
			LOGGER.error ("IllegalArgumentException Error", ex);
		} catch (InvocationTargetException ex) {
			LOGGER.error ("InvocationTargetException Error", ex);
		}
	}
	
	public final static void setJDK6WindowProperties (final Window window, final boolean opaque, final Shape windowShape) {
		try {
			final Class<?> awtUtilitiesClass = Class.forName ("com.sun.awt.AWTUtilities");
			final Method mSetWindowOpaque = awtUtilitiesClass.getMethod ("setWindowOpaque", Window.class, boolean.class);
			mSetWindowOpaque.invoke (null, window, opaque);
			if (windowShape != null) {
				final Method mSetWindowShape = awtUtilitiesClass.getMethod ("setWindowShape", Window.class, Shape.class);
				mSetWindowShape.invoke (null, window, windowShape);
			}
		} catch (NoSuchMethodException ex) {
			LOGGER.error ("NoSuchMethodException Error", ex);
		} catch (SecurityException ex) {
			LOGGER.error ("SecurityException Error", ex);
		} catch (ClassNotFoundException ex) {
			LOGGER.error ("ClassNotFoundException Error", ex);
		} catch (IllegalAccessException ex) {
			LOGGER.error ("IllegalAccessException Error", ex);
		} catch (IllegalArgumentException ex) {
			LOGGER.error ("IllegalArgumentException Error", ex);
		} catch (InvocationTargetException ex) {
			LOGGER.error ("InvocationTargetException Error", ex);
		}
	}
	
	
	public final static void setJDK6AATextProperties (final JComponent jcomp) {
		// set anti-alias hint for JEditorPane via reflection	
		try {
			final Class<?> swingUtilities2AAInfo = Class.forName ("sun.swing.SwingUtilities2$AATextInfo");
			final Constructor<?> cons = swingUtilities2AAInfo.getConstructor (Object.class, Integer.class);
			final Object consInstance = cons.newInstance (RenderingHints.VALUE_TEXT_ANTIALIAS_ON, Integer.valueOf (160));
			jcomp.putClientProperty (SwingUtilities2.AA_TEXT_PROPERTY_KEY, consInstance);		
		} catch (NoSuchMethodException ex) {
			LOGGER.error ("NoSuchMethodException Error", ex);
		} catch (SecurityException ex) {
			LOGGER.error ("SecurityException Error", ex);
		} catch (ClassNotFoundException ex) {
			LOGGER.error ("ClassNotFoundException Error", ex);
		} catch (IllegalAccessException ex) {
			LOGGER.error ("IllegalAccessException Error", ex);
		} catch (IllegalArgumentException ex) {
			LOGGER.error ("IllegalArgumentException Error", ex);
		} catch (InvocationTargetException ex) {
			LOGGER.error ("InvocationTargetException Error", ex);
		} catch (InstantiationException ex) {
			LOGGER.error ("InstantiationException Error", ex);
		}
	}
	
	
	
	public final static String[] getInstalledFontNames () {
		final GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		return gEnv.getAvailableFontFamilyNames();
	}

	
	static class JPGFilter implements FileFilter {
		
		@Override
		public boolean accept (final File file) {
			return (file.exists() && (file.isDirectory() || file.getName().endsWith (".jpg")));
		}

	    public String getDescription() {
	    	return "JPG Files";
	    }
	}
	
	
	static class GIFFilter implements FileFilter {
		
		@Override
		public boolean accept (final File f) {
			return (f.exists() && (f.isDirectory() || f.getName().endsWith (".gif")));
		}

	    public String getDescription() {
	    	return "GIF Files";
	    }
	}
}