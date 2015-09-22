package io;

import io.parcoord.MakeTableModel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import util.Messages;
import util.XMLConstants2;

public class TableModelWriter {

	static final Map<String, TableModelWriter> WRITERS_BY_PREFIX = new HashMap <String, TableModelWriter> ();
	public static final Logger LOGGER = Logger.getLogger (TableModelWriter.class);
	public static final String PROPERTIES = "io.tablewriter";
	
	TableModel data;
	ListSelectionModel lsm;	// Model-indexed list selection model
	ListSelectionModel csm;	// Model-indexed column selection model
	String fileName, encoding, nullEntry;
	final Map<String, String> segmentMap;
	
	static {
		final String onlyOneEntryPerType = "FileType";
		final List<String> availableTypes = Messages.getKeysWithRegex (PROPERTIES, ".*"+onlyOneEntryPerType+".*");
		for (String availableType: availableTypes) {
			final String typePrefix = availableType.substring (0, availableType.indexOf (onlyOneEntryPerType));
			LOGGER.debug ("availableType: "+availableType+", prefix: "+typePrefix);
			final TableModelWriter tModelWriter = new TableModelWriter (XMLConstants2.UTF8, "");
			populateSegmentMap (tModelWriter, typePrefix);
			WRITERS_BY_PREFIX.put (typePrefix, tModelWriter);
		}
	}

	
	public static void main (final String[] args) {
		final String fileName = (args.length > 0 ? args[0] : Messages.getString ("ExampleDataFile"));
		final MakeTableModel mtm = new MakeTableModel ();
		final DefaultTableModel dtm = mtm.buildDataModel (fileName);
		
		LOGGER.debug ("dtm: "+dtm.getRowCount());
		
		final TableModelWriter writer = getTableModelWriter ("ODF");
		writer.setFileName ("conv.xml");
		writer.setTableStructure (dtm, null, null);
		writer.write();
	}
	
	
	public TableModelWriter (final String encoding, final String nullEntry) {		
		this ("", "", encoding, nullEntry);
	}
	
	public TableModelWriter (final String delimiter, final String encoding, final String nullEntry) {		
		this (delimiter, delimiter, encoding, nullEntry);
	}
	
	public TableModelWriter (final String delimiter, final String headerDelimiter, final String encoding, final String nullEntry) {		
		segmentMap = new HashMap<String, String> ();
		segmentMap.put ("Delimiter", delimiter);
		segmentMap.put ("HeaderDelimiter", headerDelimiter);
		this.encoding = encoding;
		this.nullEntry = nullEntry;
	}
	
	
	public void setTableStructure (final TableModel tModel, final ListSelectionModel lsm, final ListSelectionModel csm) {
		data = tModel;
		this.lsm = lsm;
		this.csm = csm;
	}
	
	
	public void setFileName (final String fileName) {
		this.fileName = fileName;
	}


	
	public Map<String, String> getSegmentMap () { return segmentMap; }
	
	
	public String getTableSegment (final String key) {
		final String segment = getSegmentMap().get(key);
		return segment == null ? "" : segment;
	}
	


	public boolean isDataSet () {
		return data != null;
	}

	public void write () {
		if (isDataSet() && fileName != null) {
			try {
				final File file = new File (fileName);
				final PrintWriter pWriter = DataPrep.getInstance().makeBufferedPrintWriter (file, encoding, false);
				pWriter.write (getTableSegment ("FileStart"));
				writeEmbedded (pWriter);
				pWriter.write (getTableSegment ("FileEnd"));	
				pWriter.close();
			} catch (IOException e) {
				LOGGER.error (e.toString(), e);
			}
		}
	}
	
	public void writeEmbedded (final PrintWriter pWriter) {
		if (isDataSet() && pWriter != null) {
			pWriter.write (getTableSegment ("TableStart"));
			
			final String columnsDecl = getTableSegment ("ColumnsDecl");
			if (columnsDecl != null && columnsDecl.length() > 0) {
				final Format pattern = new MessageFormat (columnsDecl);
				final StringBuilder sBuilder = new StringBuilder ();
				final String singleColumnDecl = getTableSegment ("SingleColumnDecl");
				for (int col = 0; col < data.getColumnCount(); col++) {
					if (csm == null || csm.isSelectedIndex (col)) {
						sBuilder.append (singleColumnDecl);
					}
				}
				final Object[] holders = {sBuilder.toString()};
				final String columnDeclarations = pattern.format (holders);
				pWriter.write (columnDeclarations);
			}
			
			
			int lastCol = 0;
			for (int col = 0; col < data.getColumnCount(); col++) {
				if (csm == null || csm.isSelectedIndex (col)) {
					lastCol = col;
				}
			}
				
			final String headerDelimiter = getTableSegment ("HeaderDelimiter");
			pWriter.write (getTableSegment ("HeaderRowStart"));
			for (int col = 0; col < data.getColumnCount(); col++) {
				if (csm == null || csm.isSelectedIndex (col)) {
					String columnName = data.getColumnName (col);
					if (columnName != null && columnName.indexOf (headerDelimiter) != -1) {
						columnName = '\"' + columnName + '\"';
					}
					pWriter.write (columnName);
					if (col == lastCol) {
						pWriter.println ();
					} else {
						pWriter.write (headerDelimiter);
					}
				}
			}
			pWriter.write (getTableSegment ("HeaderRowEnd"));
			
			
			final String rowStart = getTableSegment ("RowStart");
			final String rowEnd = getTableSegment ("RowEnd");
			final String delimiter = getTableSegment ("Delimiter");
			
			
			for (int row = 0; row < data.getRowCount(); row++) {
				
				if (lsm == null || lsm.isSelectedIndex (row)) {
					pWriter.write (rowStart);
					
					for (int col = 0; col < data.getColumnCount(); col++) {
						
						if (csm == null || csm.isSelectedIndex (col)) {
							final Object cellObject =  data.getValueAt (row, col);
							String cellString = (cellObject == null ? null : cellObject.toString());
							if (cellString != null && cellString.indexOf (delimiter) != -1) {
								cellString = '\"' + cellString + '\"';
							}
							pWriter.write (cellString == null ? nullEntry : cellString);
							if (col == lastCol) {
								pWriter.println ();
							} else {
								pWriter.write (delimiter);
							}
						}
					}
					
					pWriter.write (rowEnd);
				}
			}
			
			pWriter.write (getTableSegment ("TableEnd"));
		}
	}
	
	
	
	static void populateSegmentMap (final TableModelWriter writerInstance, final String propertyKeyPrefix) {
		final List<String> tableSegmentKeys = Messages.getKeysWithPrefix (PROPERTIES, propertyKeyPrefix);
		for (String tableSegmentKey : tableSegmentKeys) {
			writerInstance.getSegmentMap().put (tableSegmentKey.substring (propertyKeyPrefix.length()), 
					Messages.getString (PROPERTIES, tableSegmentKey));
		}
	}
	
	static public TableModelWriter getTableModelWriter (final String type) {
		return WRITERS_BY_PREFIX.get (type);
	}
	
	static public Collection<TableModelWriter> getAvailableWriters () {
		return WRITERS_BY_PREFIX.values();
	}
}
