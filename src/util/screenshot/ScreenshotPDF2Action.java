package util.screenshot;

import java.awt.Desktop;
import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.log4j.Logger;

import util.XMLConstants2;




public class ScreenshotPDF2Action extends ScreenshotSVGAction {
    	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6363748064775893082L;
	
	static final Logger LOGGER = Logger.getLogger (ScreenshotPDF2Action.class);
	static final String PDF = "pdf";
	
	
	public ScreenshotPDF2Action (final int keyCode) {
		super (keyCode, "CameraPDF");
	}

	private final transient Object lock = new Object ();

	
	@Override
	protected boolean captureAndOutputWindow (final Window window) {
		
		writeSVG = false;
		
		if (!super.captureAndOutputWindow (window)) {
			return false;
		}
		
		try {
			final File screenshotFile = getScreenshotFileLocation (window, PDF); 
			final PipedOutputStream pos = new PipedOutputStream ();
			final BufferedOutputStream bos = new BufferedOutputStream (pos);
			final Writer out = new OutputStreamWriter (bos, XMLConstants2.UTF8);
			
			final PDFTranscoder transcoder = new PDFTranscoder();
			transcoder.addTranscodingHint (PDFTranscoder.KEY_WIDTH, (float)window.getWidth());
			transcoder.addTranscodingHint (PDFTranscoder.KEY_HEIGHT, (float)window.getHeight());
			
			final PipedInputStream pis = new PipedInputStream ();
			final BufferedInputStream bis = new BufferedInputStream (pis);
			final TranscoderInput input = new TranscoderInput (bis);
			
			final OutputStream ostream = new FileOutputStream (screenshotFile);
		    final TranscoderOutput output = new TranscoderOutput (ostream);
		    
		    		    
		    final Thread thr = new Thread (
		    	new Runnable() {
					@Override
					public void run() {

						try {
						    LOGGER.debug ("in pdf write thread");
							transcoder.transcode (input, output);
							LOGGER.debug ("gibber");
						    ostream.flush ();
						    ostream.close ();
						    bis.close ();
						    bos.close ();
						    LOGGER.debug ("exiting pdf write thread");
						    lock.notifyAll ();
						    if (Desktop.isDesktopSupported()) {
								Desktop.getDesktop().open (screenshotFile);
						    }
						} catch (TranscoderException e) {
							LOGGER.error (e.toString(), e);
						} catch (IOException e) {
							LOGGER.error (e.toString(), e);
						}
					} 		
		    	}
		    );
		    
		    final Thread throut = new Thread (
		    	new Runnable() {
					@Override
					public void run() {

						try {
							LOGGER.debug ("in svg write thread");
						    svgGenerator.stream (out, USE_CSS);
						    //Thread.currentThread().wait ();
						    synchronized (lock) {
							    if (thr.isAlive()) {
							    	LOGGER.debug ("thr inputStream alive");
							    	lock.wait ();
							    	//Thread.currentThread().sleep(100);
							    }
						    }
						    LOGGER.debug ("exiting svg write thread");
						} catch (IOException e) {
							LOGGER.error (e.toString(), e);
						} catch (InterruptedException e) {
							LOGGER.error (e.toString(), e);
						}
					} 		
		    	}
		    );

		    LOGGER.debug ("hello1");
		    pos.connect (pis);
		    LOGGER.debug ("hello2");
			throut.start ();
			LOGGER.debug ("hello3");
		    thr.start ();
		    LOGGER.debug ("hello4");

		} catch (FileNotFoundException e) {
			LOGGER.error (e.toString(), e);
		} catch (IOException e) {
			LOGGER.error (e.toString(), e);
		} 

		return true;
	}
}
