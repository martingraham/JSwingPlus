package io.parcoord;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import model.parcoord.ColumnTypedTableModel;

import org.apache.log4j.Logger;

import util.StringUtils;
import data.OrdinalDatum;
import io.CSVReaderOrdinalLines;
import io.DataPrep;
import io.OrdinalDataSource;


public class MakeTableModel {

	protected ColumnTypedTableModel tableModel;
	protected List<Format> columnTextFormatters;
	protected boolean doubleRowHeaders;
	protected boolean makeAllStringType;
	protected List<Class<?>> enforcedColumnTypes;
	private static final Logger LOGGER = Logger.getLogger (MakeTableModel.class);

	public MakeTableModel () {
		tableModel = new ColumnTypedTableModel ();	
		columnTextFormatters = new ArrayList<Format> ();
		doubleRowHeaders = false;
		makeAllStringType = false;
		enforcedColumnTypes = null;
	}

	
	public DefaultTableModel buildDataModel (final String dataFileName) {
		InputStream iStream = null;
		try {
			iStream = DataPrep.getInstance().getInputStream (dataFileName);
		} catch (FileNotFoundException fnfe) {
			LOGGER.error (fnfe.toString(), fnfe);
			return null;
		}
		return buildDataModel (iStream);
	}
	
	public DefaultTableModel buildDataModel (final InputStream iStream) {

		final BufferedReader bufReader = DataPrep.getInstance().getBufferedReader (iStream);
		
		if (bufReader != null) {
			final CSVReaderOrdinalLines ordCSVReader = new CSVReaderOrdinalLines (bufReader, "\t,", "\"", true, true, doubleRowHeaders);
			//final CSVReaderOrdinalLines cr = new CSVReaderOrdinalLines (fr, "\t", "", true, true);
			final int colSize = ordCSVReader.getColumnsObj().getColumnList().size();
		   
		    final List<String> columnHeaderList = ordCSVReader.getColumnsObj().getColumnList();
		    LOGGER.debug (columnHeaderList);
		    tableModel.setColumnIdentifiers (new Vector<String> (columnHeaderList));

			List<String> dataRow;
		
		    while (!((dataRow = ordCSVReader.readDataRow()) == null)) {    	
		    	addRowToTable (dataRow, tableModel, ordCSVReader, colSize);
			}  
		
		    
			try {
				bufReader.close ();
			}
			catch (final java.io.IOException x) {
				LOGGER.error ("error in reading: "+Arrays.toString(x.getStackTrace()));
			}
		
			LOGGER.debug ("dtm: "+tableModel.getColumnCount()+", "+tableModel.getRowCount());
			
		}
		
		return tableModel;
	}
	
	public void addRowToTable (final List<String> dataRow, final DefaultTableModel tModel, 
			final OrdinalDataSource ordDataSource, final int colSize) {
    	if (!dataRow.isEmpty()) {
	    	//LOGGER.debug (dataRow);
    		final Vector<Object> vector = new Vector<Object> (colSize);
	    	
	    	for (int colIndex = 0; colIndex < dataRow.size(); colIndex++) {
	    		String str = dataRow.get (colIndex);
	    		if ("NULL".equalsIgnoreCase(str)) {
	    			str = "";
	    		}
	    		Class<?> klass = tModel.getColumnClass (colIndex);
	    		
	    		if (klass == null || klass == Object.class) {
	    			if (makeAllStringType) {
	    				klass = String.class;
	    			} else if (checkEnforcedColumnTypeExists (colIndex)){
	    				klass = enforcedColumnTypes.get (colIndex);
	    			} else {
		    			//final String columnName = columnHeaderList.get (colIndex);
		    			final String columnName = tModel.getColumnName (colIndex);
		    			klass = basicTypeString (str, colIndex, ordDataSource.getOrdinalData (columnName), columnTextFormatters);
		    			LOGGER.debug ("col: "+colIndex+"\n"+ordDataSource.getOrdinalData (columnName));
	    			}
	    			
	    			if (klass == null) {
	    				str = null;
	    			} else {
	    				tableModel.setColumnClass (colIndex, klass);
	    				LOGGER.debug (klass);
	    			}
	    		}
	    		
	    		if (klass == Double.class) {
	    			try {
	    				vector.add (str.isEmpty() ? null : Double.valueOf (str));
	    			}
	    			catch (NumberFormatException nfe) {
	    				vector.add (null);
	    			}
	    			//v.add (Math.log1p (Double.valueOf (s)) / Math.log(2));
	    		}
	    		else if (klass == Integer.class) {
	    			try {
	    				vector.add (str.length() == 0 ? null : Integer.valueOf (str));
	    			}
	    			catch (NumberFormatException nfe) {
	    				try {
	    					vector.add (str.length() == 0 ? null : Integer.valueOf (str.substring (0, str.length() - 1)));
	    				}
	    				catch (NumberFormatException nfe2) {
		    				vector.add (null);
		    			}
	    			}
	    		}
	    		else if (klass == Date.class) {
	    			final Format format = columnTextFormatters.get (colIndex);
	    			
	    			try {
	    				//LOGGER.debug ("Initial: "+s);
	    				//LOGGER.debug ("format: "+f.parseObject(s));
	    				//LOGGER.debug ("dateformat: "+((DateFormat)f).parseObject(s));
						vector.add (str.length() == 0 ? null :
							(format instanceof DateFormat ? ((DateFormat)format).parse(str) : str)
						);
					} catch (ParseException parsee) {
						vector.add (null);
						//v.add (s.length() == 0 ? null : new Date ());
					}
	    		}
	    		else if (klass == OrdinalDatum.class) {
	    			final Map<String, OrdinalDatum> columnOrdinals = ordDataSource.getOrdinalData (tModel.getColumnName(colIndex));
		    		if (columnOrdinals == null) {
		    			vector.add (null);
		    		} else {
		    			final OrdinalDatum ordDatum = columnOrdinals.get (str);
		    			vector.add (ordDatum);
		    		}
	    		}
	    		else {
	    			vector.add (str == null || str.length() == 0 ? null : str);
	    		}
	    		
	    	}
	    	
	    	tModel.addRow (vector);
    	}
	}
	
	
	boolean checkEnforcedColumnTypeExists (final int columnIndex) {
		return (enforcedColumnTypes != null &&
				columnIndex >= 0 &&
				enforcedColumnTypes.size() > columnIndex &&
				enforcedColumnTypes.get(columnIndex) != null);
	}
	
	
	Class<?> basicTypeString (final String str, final int columnIndex,
			final Map<String, OrdinalDatum> columnOrdinals, final List<Format> formatters) {
		
		boolean typeFound = false;
		Class<?> klass = null;
		
		if (str != null && !str.isEmpty()) {
			
			if (columnOrdinals != null) {
				final OrdinalDatum ordDatum = columnOrdinals.get (str);
				if (ordDatum != null) {
					klass = OrdinalDatum.class;
					typeFound = true;
				}
			}
			
			if (!typeFound) {
	    		try {
	    			final Integer intObj = Integer.valueOf (str);	
	    			//if (i.intValue() != 0) {
	    				klass = Integer.class;
	    				typeFound = true;
	    			//}
	    		} catch (NumberFormatException nfe) {
	    			LOGGER.debug (nfe.toString());
	    		}
			}
			
			if (!typeFound) {
	    		try {
	    			final Double doubleObj = Double.valueOf (str);
	    			final int decPointIndex = str.indexOf ('.');
	    			final int fractionDigits = (decPointIndex == -1 ? 0 : (str.length() - decPointIndex) - 1);
	    			klass = Double.class;
	    			typeFound = true;
	    			final NumberFormat numFormat = NumberFormat.getNumberInstance();
	    			numFormat.setMaximumFractionDigits (fractionDigits);
	    			setFormatter (formatters, columnIndex, numFormat);
	    			//v.add (Math.log1p(d) / Math.log(2));
	    		} catch (NumberFormatException nfe) {
	    			LOGGER.debug (nfe);
	    		}
			}
			
			if (!typeFound) {
				final DateFormat dateFormat = StringUtils.getInstance().isStringDate (str);
				if (dateFormat != null) {
					typeFound = true;
					setFormatter (formatters, columnIndex, dateFormat);
					klass = Date.class;
				}
			}
		}
		
		if (!typeFound && str != null && str.length() > 0 && !"NULL".equalsIgnoreCase(str)) {
			klass = String.class;
		}	
		
		return klass;
	}
	
    public void setFormatter (final List<Format> formatters, final int columnIndex, final Format format) {
    	if (columnIndex >= formatters.size()) {
    		for (int n = formatters.size(); n <= columnIndex; n++) {
    			formatters.add (n, null);
    		}
    	}
    	formatters.set (columnIndex, format);
    	LOGGER.debug (columnIndex+" = "+format);
    }
    
    
	public final ColumnTypedTableModel getTableModel () {
		return tableModel;
	}
	
	public final List<Format> getColumnTextFormatters () {
		return columnTextFormatters;
	}


	public final void setDoubleRowHeaders (final boolean doubleRowHeaders) {
		this.doubleRowHeaders = doubleRowHeaders;
	}
	
	public final void setMakeAllStringType (final boolean makeAllStringType) {
		this.makeAllStringType = makeAllStringType;
	}
	
	public final void setEnforcedColumnTypes (final List<Class<?>> enforcedColumnTypes) {
		this.enforcedColumnTypes = enforcedColumnTypes;
	}
}
