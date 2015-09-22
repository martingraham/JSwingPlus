package io;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.basic.BasicFileChooserUI;

import org.apache.log4j.Logger;


public class LaunchSaveFileDialogBasic extends JFileChooser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7362145517497218262L;
	final static Logger LOGGER = Logger.getLogger (LaunchSaveFileDialogBasic.class);
	
	
	static Map<FileSuffixFilter, TableModelWriter> writerMap = new HashMap<FileSuffixFilter, TableModelWriter> ();	
	static {
		final Collection<TableModelWriter> writers = TableModelWriter.getAvailableWriters();
		for (TableModelWriter writer : writers) {
			final String suffix = writer.getTableSegment ("FileType");
			final String descriptor = writer.getTableSegment ("FileDescriptor");
			final FileSuffixFilter ffilter = new FileSuffixFilter (suffix, descriptor);
			writerMap.put (ffilter, writer);
		}
	}
	
	public LaunchSaveFileDialogBasic (String[] suffixes) {
		super (".");
		
		setMultiSelectionEnabled (false);
		
		final Collection<FileSuffixFilter> filters = writerMap.keySet();
		final List<FileSuffixFilter> filterList = new ArrayList<FileSuffixFilter> (filters);
		Collections.sort (filterList);
		for (FileSuffixFilter filter : filterList) {
			for (String suffix : suffixes) {
				if (filter.getSuffix().equalsIgnoreCase (suffix)) {
					addChoosableFileFilter (filter);
					break;
				}
			}
		}
		setAcceptAllFileFilterUsed (false);
		
		addPropertyChangeListener (
			new PropertyChangeListener () {
				
				@Override
				public void propertyChange (final PropertyChangeEvent evt) {
					
					if (FILE_FILTER_CHANGED_PROPERTY.equals (evt.getPropertyName())
							|| SELECTED_FILE_CHANGED_PROPERTY.equals (evt.getPropertyName())) {
						final FileFilter filter = getFileFilter ();
						final FileChooserUI fcui = getUI();
						
						if (filter instanceof FileSuffixFilter && fcui instanceof BasicFileChooserUI) {
							final FileSuffixFilter suffixFilter = (FileSuffixFilter)filter;
							final String suffix = suffixFilter.getSuffix();
							final BasicFileChooserUI bfcui = (BasicFileChooserUI)fcui;
							final String curFileName = bfcui.getFileName();
							
							if (curFileName != null && curFileName.length() > suffix.length() && !curFileName.endsWith (suffix)) {		
								final int suffixIndex = curFileName.lastIndexOf ('.');
								final String newFileName = curFileName.substring (0, suffixIndex == -1 ? curFileName.length() : suffixIndex) + suffix;
								setSelectedFile (new File (newFileName));
							}
						}
					}
				}	
			}
		);
		
		final SimpleDateFormat sdf = new SimpleDateFormat ("yyMMdd_HHmmss");
		final String dateTime = sdf.format (new Date ());
		setSelectedFile (new File (dateTime+"subset"));
		setFileFilter (getChoosableFileFilters() [0]);
	}
}
