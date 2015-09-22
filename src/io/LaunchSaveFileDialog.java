package io;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.swing.table.TableColumnModel;

import model.shared.TableUtils;

import org.apache.log4j.Logger;

import swingPlus.parcoord.JParCoord;


public class LaunchSaveFileDialog {

	
	final static Logger LOGGER = Logger.getLogger (LaunchSaveFileDialog.class);
	
	
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
	
	
	public LaunchSaveFileDialog (final Collection<JParCoord> parCoords) {
		
		SwingUtilities.invokeLater (
				
			new Runnable () {
				
				@Override
				public void run() {
					File curLoc = null;
					try {
						curLoc = new File (new File(".").getCanonicalPath());
					} catch (final IOException ioe) {
						LOGGER.error ("Current directory error", ioe);
					}
				    
					LOGGER.debug ("Current file dialog directory: "+curLoc.toString());
				
					final JFileChooser jfc = new JFileChooser (curLoc);
					jfc.setMultiSelectionEnabled (false);
					
					final Collection<FileSuffixFilter> filters = writerMap.keySet();
					final List<FileSuffixFilter> filterList = new ArrayList<FileSuffixFilter> (filters);
					Collections.sort (filterList);
					for (FileFilter filter : filters) {
						jfc.addChoosableFileFilter (filter);
					}
					jfc.setAcceptAllFileFilterUsed (false);
					
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
					
					final SimpleDateFormat sdf = new SimpleDateFormat ("yyMMdd_HHmmss");
					final String dateTime = sdf.format (new Date ());
					jfc.setSelectedFile (new File (dateTime+"subset"));
					jfc.setFileFilter (jfc.getChoosableFileFilters() [0]);

					final int returnVal = jfc.showSaveDialog (null);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
		    			new Thread() {
		    				@Override
							public void run() {	

		    					final FileFilter filter = jfc.getFileFilter();
								String suffix = "";
								if (filter instanceof FileSuffixFilter) {
									final FileSuffixFilter suffixFilter = (FileSuffixFilter)filter;
									suffix = suffixFilter.getSuffix();
								}
								
								String fileName = jfc.getSelectedFile().getPath();
								if (! fileName.endsWith (suffix)) {
									fileName = fileName.concat (suffix);
								}
								
								final TableModelWriter tmWriter = writerMap.get (filter);
								if (tmWriter != null) {
									tmWriter.setFileName (fileName);
									final Collection<TableColumnModel> columnModels = new ArrayList<TableColumnModel> ();
									for (JTable table : parCoords) {
										columnModels.add (table.getColumnModel());
									}
									final Iterator<JParCoord> compIterator = parCoords.iterator();
									final JParCoord jpc = compIterator.next();
									tmWriter.setTableStructure (jpc.getModel(), jpc.getUberSelection(), TableUtils.combineColumnModels (columnModels));
									tmWriter.write ();
								}
		    				}
						}.start();
					}
				}
			}
		);
	}
}
