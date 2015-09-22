package util.screenshot;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import util.GraphicsUtil;

public class ScreenshotComponent {

	
	final static private Logger LOGGER = Logger.getLogger (ScreenshotComponent.class);
	
	
	public BufferedImage captureComponentImage (final Component comp) {
		return captureComponentImage (comp, 1.0f);
	}
	
	public BufferedImage captureComponentImage (final Component comp, final float scale) {

		BufferedImage image = null;
		
		if (comp != null && scale > 0.0f) {
			final Dimension dims = new Dimension ((int)(comp.getWidth() * scale), (int)(comp.getHeight() * scale));
			image = GraphicsUtil.initImage (null, comp, dims, Transparency.TRANSLUCENT);
	        
			if (image != null) {
	        	final Graphics2D g2d = image.createGraphics();
	        	g2d.setRenderingHint (GraphicsUtil.DRAWTOFILE, GraphicsUtil.DRAW_ALL_OFFSCREEN);
	        	g2d.setRenderingHint (RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	        	g2d.scale (scale, scale);
	        	comp.paint (g2d);
	        } 
		}
		return image;
	}
	
	
	public void captureAndOutputComponent (final Component comp, final OutputStream out) {

		final BufferedImage image = captureComponentImage (comp);
       
        if (image != null) {
        	try {
				ImageIO.write (image, ScreenshotAction.PNG, out);
			} catch (final IOException ioe) {
				LOGGER.error (ioe.toString(), ioe);
			}
			
	        image.flush();
        } 
	}
	
	
	public File captureAndOutputComponent (final Component comp, final String dateTime, final File folder) {

		final BufferedImage image = captureComponentImage (comp);
		final String filename = "Screenshot"+comp.getName()+"_"+dateTime+"."+ScreenshotAction.PNG;
        final File screenshotFile = new File (folder, filename); 
       
        if (image != null) {
        	try {
				ImageIO.write (image, "png", screenshotFile);
			} catch (final IOException ioe) {
				LOGGER.error (ioe.toString(), ioe);
				return null;
			}
			
	        image.flush();
        } 
		return screenshotFile;
	}
}
