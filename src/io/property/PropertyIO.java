package io.property;

import io.FileSuffixFilter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.basic.BasicFileChooserUI;

import org.apache.log4j.Logger;

import util.Messages;

public final class PropertyIO {

	
	final static Logger LOGGER = Logger.getLogger (PropertyIO.class);
	private final static FileFilter PROPERTIES_FILE_FILTER = new FileSuffixFilter (".properties", Messages.getString ("PropertiesFileDescriptor")); 

	private final static PropertyIO INSTANCE = new PropertyIO ();
	
	public static PropertyIO getInstance() { return INSTANCE; }
	
	
	private PropertyIO () {}
	
	
	JFileChooser setUpPropertiesFileChooser () {
		File curLoc = null;
		try {
			curLoc = new File (new File(".").getCanonicalPath());
		} catch (IOException ioe) {
			LOGGER.error ("Current directory error", ioe);
		}
	    
		LOGGER.debug ("Current file dialog directory: "+curLoc.toString());
		final JFileChooser jfc = new JFileChooser (curLoc);
		jfc.setMultiSelectionEnabled (false);
		jfc.addChoosableFileFilter (PROPERTIES_FILE_FILTER);
		jfc.setAcceptAllFileFilterUsed (false);
		return jfc;
	}
	
	
	
	public Properties loadPropertiesWithFileChooser () {
		Properties properties = new Properties ();
		final JFileChooser jfc = setUpPropertiesFileChooser ();
		final int returnVal = jfc.showOpenDialog (null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File file = jfc.getSelectedFile();
			try {
				final InputStream iStream = new BufferedInputStream (new FileInputStream (file));
				properties = new Properties ();
				properties.load (iStream);
				iStream.close();
			} catch (FileNotFoundException fnfe) {
				LOGGER.error ("Properties file not found error", fnfe);
			} catch (IOException ioe) {
				LOGGER.error ("Current directory error", ioe);
			}
		}
		
		return properties;
	}
	
	public void savePropertiesWithFileChooser (final Properties properties) {
		final JFileChooser jfc = setUpPropertiesFileChooser ();
		
		jfc.addPropertyChangeListener (
			new PropertyChangeListener () {
				
				@Override
				public void propertyChange (final PropertyChangeEvent evt) {
					
					if (JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals (evt.getPropertyName())) {
						final FileFilter filter = jfc.getFileFilter ();
						final FileChooserUI fcui = jfc.getUI();
						
						if (filter instanceof FileSuffixFilter && fcui instanceof BasicFileChooserUI) {
							final FileSuffixFilter suffixFilter = (FileSuffixFilter)filter;
							final String suffix = suffixFilter.getSuffix();
							final BasicFileChooserUI bfcui = (BasicFileChooserUI)fcui;
							final String curFileName = bfcui.getFileName();
							
							if (curFileName != null && curFileName.length() > suffix.length() && !curFileName.endsWith (suffix)) {		
								final int suffixIndex = curFileName.lastIndexOf ('.');
								final String newFileName = curFileName.substring (0, suffixIndex == -1 ? curFileName.length() : suffixIndex) + suffix;
								jfc.setSelectedFile (new File (newFileName));
							}
						}
					}
				}	
			}
		);

		final int returnVal = jfc.showSaveDialog (null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			saveProperties (properties, jfc.getSelectedFile());
		}
	}
	
	public void saveProperties (final Properties properties, final File file) {
		try {
			final File file2 = file.getName().endsWith(".properties") ? 
					file : new File (file.getPath()+".properties");
			final OutputStream out = new BufferedOutputStream (new FileOutputStream (file2));
			properties.store (out, Messages.getString ("ReportDialog.propertyComment"));
			out.close ();
		} catch (FileNotFoundException fnfe) {
			LOGGER.error ("Properties file not found error", fnfe);
		} catch (IOException ioe) {
			LOGGER.error ("Current directory error", ioe);
		}
	}
}
