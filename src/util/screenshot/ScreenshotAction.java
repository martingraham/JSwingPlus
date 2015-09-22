package util.screenshot;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import util.GraphicsUtil;


public class ScreenshotAction extends ScreenshotAbstractAction {
    	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7003161377576785784L;
	
	static final Logger LOGGER = Logger.getLogger (ScreenshotAction.class);
	static final String PNG = "png";


	public ScreenshotAction (final int keyCode) {
		super (keyCode, "Camera");
	}
	

	@Override
	protected boolean captureAndOutputWindow (final Window window) {

		final BufferedImage image = GraphicsUtil.initImage (null, window, Transparency.OPAQUE);
        
        if (image != null) {
        	final Graphics2D g2d = image.createGraphics();
        	g2d.setRenderingHint (GraphicsUtil.DRAWTOFILE, GraphicsUtil.DRAW_ALL_OFFSCREEN);
        	window.paint (g2d);
        	try {
				final File screenshotFile = getScreenshotFileLocation (window, PNG); 
				ImageIO.write (image, PNG, screenshotFile);
			} catch (final IOException ioe) {
				LOGGER.error (ioe.toString(), ioe);
				return false;
			}
			
	        image.flush();
        } 
		return true;
	}

	@Override
	protected void setup() {	
		// EMPTY
	}
}
