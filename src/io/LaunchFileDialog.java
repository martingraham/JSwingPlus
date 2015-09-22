package io;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;


public class LaunchFileDialog {

	
	final static Logger LOGGER = Logger.getLogger (LaunchFileDialog.class);
	
	
	public LaunchFileDialog (final Class<?> entryClass) {
		
		SwingUtilities.invokeLater (
				
			new Runnable () {
				
				@Override
				public void run() {
					File curLoc = null;
					try {
						curLoc = new File (new File(".").getCanonicalPath());
					} catch (IOException e1) {
						LOGGER.error ("Current directory error", e1);
					}
				    
					LOGGER.debug ("Current file dialog directory: "+curLoc.toString());
				
					final JFileChooser jfc = new JFileChooser (curLoc);
					final int returnVal = jfc.showOpenDialog (null);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						final File file2 = jfc.getSelectedFile();
		    			final String fileName = file2.getAbsolutePath();
		    			new Thread() {
							@Override
							public void run() {		
								try {
									final Constructor<?> cons = entryClass.getConstructor (new Class[] {String.class});
									cons.newInstance (fileName);
								} catch (SecurityException e) {
									LOGGER.error ("Security error", e);
								} catch (NoSuchMethodException e) {
									LOGGER.error ("No such constructor method", e);
								} catch (IllegalArgumentException e) {
									LOGGER.error ("Illegal arguments to constructor", e);
								} catch (InstantiationException e) {
									LOGGER.error ("Cannot instantiate constructor", e);
								} catch (IllegalAccessException e) {
									LOGGER.error ("Check access privileges (public/protected etc)", e);
								} catch (InvocationTargetException e) {
									LOGGER.error ("Exception thrown by invoked constructor", e);
								}
		            		}
						}.start();
					}
				}
			}
		);
	}
}
