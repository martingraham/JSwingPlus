package util;

import io.DataPrep;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;


public class ExternalFileLaunchAction extends PropertyPrefixBasedAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7368272268886374700L;
	static final private Logger LOGGER = Logger.getLogger (ExternalFileLaunchAction.class);
	
	
	URL url;
	
	public ExternalFileLaunchAction (final int keyCode, final String actionPrefix, final URL url) {
		super (keyCode, actionPrefix);
		this.url = url;
	}
	
	@Override
	public void actionPerformed (final ActionEvent event) {
		
		final Runnable launchRunnable = new Runnable () {

			@Override
			public void run() {
				try {
					final URI uri = url.toURI();
					final String path = uri.getPath();
					File tempFile = null;
					
					if (path == null || path.contains ("jar!")) {
						final String urlPath = url.getPath();
						final String suffix = urlPath.substring (urlPath.lastIndexOf('.'));
						final String prefix = urlPath.substring (urlPath.lastIndexOf ('/') + 1, urlPath.lastIndexOf('.'));
						LOGGER.debug ("temp file: "+prefix+suffix);
		
						tempFile = File.createTempFile (prefix, suffix);
						//if (!tempFile.exists()) { // Idiot boy. Will always be true.
							final InputStream ins = DataPrep.getInstance().getInputStream (url);
							final OutputStream outs = new BufferedOutputStream (new FileOutputStream (tempFile));
							DataPrep.getInstance().copyInputStream (ins, outs);
							outs.flush();
							outs.close();
							tempFile.deleteOnExit();
						//}
					} else {
						tempFile = new File (uri);
					}
					
					Desktop.getDesktop().open (tempFile);
				} catch (final IOException ioe) {
					LOGGER.error ("Could not open file based on url "+url+" with external program", ioe);
				} catch (final URISyntaxException usee) {
					LOGGER.error ("Illegal syntax for URI conversion in "+url, usee);
				} catch (final IllegalArgumentException iae) {
					LOGGER.error ("Cannot find file at "+url, iae);
				}		
			}
		};
		
		if (Desktop.isDesktopSupported()) {
			final Thread launchRunnableThread = new Thread (launchRunnable);
			launchRunnableThread.start();
		}
	}
}
