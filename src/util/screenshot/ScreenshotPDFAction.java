package util.screenshot;

import java.awt.Desktop;
import java.awt.Window;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.log4j.Logger;

import util.XMLConstants2;


public class ScreenshotPDFAction extends ScreenshotSVGAction {
    	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6363748064775893082L;
	
	final static Logger LOGGER = Logger.getLogger (ScreenshotPDFAction.class);
	static final String PDF = "pdf";
	

	public ScreenshotPDFAction (final int keyCode) {
		super (keyCode, "CameraPDF");
	}


	
	@Override
	protected boolean captureAndOutputWindow (final Window window) {
		
		writeSVG = false;
		
		if (!super.captureAndOutputWindow (window)) {
			return false;
		}
		
		try {
			final File screenshotFile = getScreenshotFileLocation (window, PDF); 
	        //final boolean useCSS = true; // we want to use CSS style attributes
	        final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
	        final Writer out = new OutputStreamWriter (bos, XMLConstants2.UTF8);
			svgGenerator.stream (out, USE_CSS);
					
			final PDFTranscoder transcoder = new PDFTranscoder();
			transcoder.addTranscodingHint (PDFTranscoder.KEY_WIDTH, (float)window.getWidth());
			transcoder.addTranscodingHint (PDFTranscoder.KEY_HEIGHT, (float)window.getHeight());
			
			final ByteArrayInputStream bin = new ByteArrayInputStream (bos.toByteArray());
			final TranscoderInput input = new TranscoderInput (bin);
			
			final OutputStream ostream = new FileOutputStream (screenshotFile);
		    final TranscoderOutput output = new TranscoderOutput (ostream);
		    
			transcoder.transcode (input, output);
			
		    ostream.flush ();
		    ostream.close ();
		    bin.close ();
		    bos.close ();

		    if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open (screenshotFile);
		    }
		} catch (FileNotFoundException e) {
			LOGGER.error (e.toString(), e);
		} catch (IOException e) {
			LOGGER.error (e.toString(), e);
		} catch (TranscoderException e) {
			LOGGER.error (e.toString(), e);
		}

		return true;
	}
}
