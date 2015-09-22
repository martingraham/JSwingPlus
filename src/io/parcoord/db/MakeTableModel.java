package io.parcoord.db;



import io.db.Connect;
import io.db.ConnectFactory;
import io.db.FormatResultSet;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import model.parcoord.ColumnTypedTableModel;

import org.apache.log4j.Logger;

import util.Messages;
import util.StringUtils;


public class MakeTableModel {

	ColumnTypedTableModel tableModel;
	List<Format> columnTextFormatters;
	final static Logger LOGGER = Logger.getLogger (MakeTableModel.class);

	public MakeTableModel () {
		tableModel = new ColumnTypedTableModel ();	
		columnTextFormatters = new ArrayList<Format> ();
	}

	public DefaultTableModel makeTable (final ResultSet resultSet) throws SQLException {
		resultSet.beforeFirst(); // Move to start of result set

		final ResultSetMetaData rsmd = resultSet.getMetaData ();
		final List<String> columnNames = new ArrayList<String> ();
		for (int col = 1; col <= rsmd.getColumnCount(); col++) {
			columnNames.add (rsmd.getColumnLabel (col));
		}

		final int colSize = columnNames.size();
		
		LOGGER.debug (columnNames);
		tableModel.setColumnIdentifiers (new Vector<String> (columnNames));

		//rsmd.
		while (resultSet.next()) {
			final Vector<Object> vector = new Vector<Object> (colSize);
			
			for (int colIndex = 1; colIndex <= rsmd.getColumnCount(); colIndex++) {
				Class<?> klass = tableModel.getColumnClass (colIndex - 1);
	    		
	    		if (klass == null) {
	    			final String item = resultSet.getString (colIndex);
	    			final String columnClassName = rsmd.getColumnClassName (colIndex);
	    			LOGGER.debug ("Column: "+rsmd.getColumnLabel(colIndex)+": "+columnClassName);
	    			
	    			try {
						klass = Class.forName (columnClassName);
					} catch (ClassNotFoundException cnfe) {
						LOGGER.debug ("error", cnfe);
					}
					
	    			if (klass != null && item != null) {
		    			if (Date.class.isAssignableFrom (klass)) {
		    				final DateFormat dateFormat = StringUtils.getInstance().isStringDate (item);
		    				if (dateFormat != null) {
		    					setFormatter (columnTextFormatters, colIndex - 1, dateFormat);
		    					//cl = Date.class;
		    				}
		    			}
		    			else if (Double.class.isAssignableFrom (klass)) {
		    	    		try {
		    	    			//final Double doub = Double.valueOf (item);
		    	    			final int decPointIndex = item.indexOf ('.');
		    	    			final int fractionDigits = (decPointIndex == -1 ? 0 : (item.length() - decPointIndex) - 1);
		    	    			final NumberFormat numberFormat = NumberFormat.getNumberInstance();
		    	    			numberFormat.setMaximumFractionDigits (fractionDigits);
		    	    			setFormatter (columnTextFormatters, colIndex - 1, numberFormat);
		    	    			//v.add (Math.log1p(d) / Math.log(2));
		    	    		} catch (NumberFormatException nfe) {
		    	    			LOGGER.error (nfe);
		    	    		}
		    			}
		    			tableModel.setColumnClass (colIndex - 1, klass);
	    			}
	    		}
	    		final Object obj = resultSet.getObject (colIndex);
	    		if (obj != null) {
	    			//LOGGER.debug ("object: "+o.getClass());
	    		}
	    		vector.add (obj); 
			}
			
			tableModel.addRow (vector);
		}
		
		for (int colIndex = 1; colIndex <= rsmd.getColumnCount(); colIndex++) {
			final Class<?> klass = tableModel.getColumnClass (colIndex - 1);
			if (klass == null) {
				tableModel.setColumnClass (colIndex - 1, String.class);
			}
		}
		
		return tableModel;
	}
	
	
	public DefaultTableModel buildDataModel (final String SQLConnectionProperties) {
		final Connect connect = ConnectFactory.getConnect (Messages.makeProperties (SQLConnectionProperties));
		final String SQL = Messages.getString ("io.parcoord.db.queryExamples", "query");

		DefaultTableModel dtm = null;
		try {
			final Statement stmt = connect.getConnection().createStatement();
			final ResultSet resultSet = stmt.executeQuery (SQL);
			dtm = this.makeTable (resultSet);
			final DatabaseMetaData dmd = connect.getConnection().getMetaData();
			final ResultSet resultSet2 = dmd.getProcedures (connect.getConnection().getCatalog(), null, "%");
			FormatResultSet.getInstance().printResultSet (resultSet2);
			
			resultSet.close();
			resultSet2.close();
			stmt.close();
		} catch (final SQLException sqle) {
			LOGGER.error ("error", sqle);
			sqle.printStackTrace();
		}
		connect.close ();
		return dtm;
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
}
