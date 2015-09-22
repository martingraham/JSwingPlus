package util.screenshot;

import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.swing.RepaintManager;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import util.XMLConstants2;



public class ScreenshotSVGAction extends ScreenshotAbstractAction {
    	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4731385510914737955L;
	
	private static final Logger LOGGER = Logger.getLogger (ScreenshotSVGAction.class);
	protected static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
	protected static final String TABSTRING = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";
	static final String SVG = "svg";
	protected static final boolean USE_CSS = true; // we want to use CSS style attributes

	protected DOMImplementation domImpl;
	protected SVGGraphics2D svgGenerator = null;
	protected boolean writeSVG = true;
	


	public ScreenshotSVGAction (final int keyCode) {
		super (keyCode, "CameraSVG");
	}
	
	public ScreenshotSVGAction (final int keyCode, final String actionPrefix) {
		super (keyCode, actionPrefix);
	}

	
	@Override
	protected void setup() {
		domImpl = GenericDOMImplementation.getDOMImplementation();
	}
	

	@Override
	protected boolean captureAndOutputWindow (final Window window)  {
        // Ask the test to render into the SVG Graphics2D implementation.
        final Document document = domImpl.createDocument (SVG_NAMESPACE, SVG, null);
        // Create an instance of the SVG Generator.
        svgGenerator = new SVGGraphics2D (document);
        svgGenerator.getGeneratorContext().setPrecision(6);

        final RepaintManager currentManager = RepaintManager.currentManager (window);
        final boolean dBufferOn = currentManager.isDoubleBufferingEnabled ();
        currentManager.setDoubleBufferingEnabled(false);

        window.paint (svgGenerator);
        currentManager.setDoubleBufferingEnabled (dBufferOn);

        if (writeSVG) {
	        // Finally, stream out SVG to the standard output using
	        // UTF-8 encoding.
	        Writer out = null;
			try {
				final File screenshotFile = getScreenshotFileLocation (window, SVG); 
				final FileOutputStream fos = new FileOutputStream (screenshotFile);
				out = new OutputStreamWriter (fos, XMLConstants2.UTF8);
				svgGenerator.stream (out, USE_CSS);
			} catch (UnsupportedEncodingException uee) {
				LOGGER.error (uee.toString(), uee);
			} catch (SVGGraphics2DIOException svg2de) {
				LOGGER.error (svg2de.toString(), svg2de);
			} catch (FileNotFoundException fnfe) {
				LOGGER.error (fnfe.toString(), fnfe);
			} 
			finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						LOGGER.error (ioe.toString(), ioe);
					}
				}
			}
        }
		
		return true;
	}
}
