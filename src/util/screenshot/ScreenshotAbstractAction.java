package util.screenshot;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import util.PropertyPrefixBasedAction;



public abstract class ScreenshotAbstractAction extends PropertyPrefixBasedAction {
    	
	/**
	 * 
	 */
	private static final long serialVersionUID = -328352540250286570L;
	private static final Logger LOGGER = Logger.getLogger (ScreenshotAbstractAction.class);
	private static final String PROPERTY_FILE_NAME = "screenshot";
	String dateTime;
	
	public ScreenshotAbstractAction (final int keyCode, final String actionPrefix) {
		super (keyCode, actionPrefix);
	}
	
	protected void setup (final int keyCode, final String actionPrefix) {
		setTextPropertyFile (this.getClass().getPackage().getName()+"."+PROPERTY_FILE_NAME);
		super.setup (keyCode, actionPrefix);
	}
	
	public void actionPerformed (final ActionEvent evt) {
		//final Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();	 
        final Window[] windows = Window.getWindows();
        setup ();
        
		final SimpleDateFormat sdf = new SimpleDateFormat ("yyMMdd_HHmmss");
		dateTime = sdf.format (new Date ());
        
		for (final Window w : windows) {	
			if (w.isShowing()) {
		        final int width = w.getWidth();
		        final int height = w.getHeight();
		        
		        if (width * height > 0 && width > 50 && height > 50) {	
					
		        	LOGGER.info ("Width: "+width+", height: "+height);
					SwingUtilities.invokeLater (
						new Runnable () {
							
							@Override
							public void run() {
						        // create screen shot
								captureAndOutputWindow (w);     
							}
						}
					);
		        }
			}
		}
	}
	
	abstract protected void setup ();
	
	abstract protected boolean captureAndOutputWindow (Window window);
	
	public String makeFilename (final Window window, final String suffix) {
		return "Screenshot"+window.getName()+"_"+dateTime+"."+suffix;
	}
	
	public File getScreenshotFileLocation (final Window window, final String suffix, final boolean useUserHomeDir) {
		LOGGER.debug ("Generating screenshot file location");
		
		final File current = (useUserHomeDir ? new File (System.getProperty("user.home")): new File (""));
		LOGGER.debug ("current: "+(current == null ? "null" : current.toString()));
		
		final File currentAbs = current.getAbsoluteFile();
		LOGGER.debug ("current absolute: "+(currentAbs == null ? "null" : currentAbs.toString()));
		
		final File folder = new File (currentAbs, "screenshot"+suffix.toUpperCase());
		LOGGER.debug ("current absolute + screenshot folder: "+(folder == null ? "null" : folder.toString()));
		if (! folder.isDirectory()) {
			folder.mkdir();
		}
		
		final File screenshotFile = new File (folder, makeFilename (window, suffix)); 
		LOGGER.debug ("current absolute + screenshot folder + filename: "+
				(screenshotFile == null ? "null" : screenshotFile.toString()));
		
		return screenshotFile;
	}
	
	
	public File getScreenshotFileLocation (final Window window, final String suffix) {
		return getScreenshotFileLocation (window, suffix, true);
	}
}
