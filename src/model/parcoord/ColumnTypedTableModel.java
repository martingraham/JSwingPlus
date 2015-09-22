package model.parcoord;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

public class ColumnTypedTableModel extends DefaultTableModel  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9051364764099277597L;
	private final static Logger LOGGER = Logger.getLogger (ColumnTypedTableModel.class);
	
	protected List<Class<?>> columnTypes = new ArrayList<Class<?>> ();
	
    @Override
	public Class<?> getColumnClass (final int columnIndex) {
    	if (columnIndex >= columnTypes.size()) {
    		return Object.class;
    	}
    	final Class<?> klass = columnTypes.get (columnIndex);
    	return klass == null ? Object.class : columnTypes.get (columnIndex);
    }
    
    public void setColumnClass (final int columnIndex, final Class<?> klass) {
    	if (columnIndex >= columnTypes.size()) {
    		for (int n = columnTypes.size(); n <= columnIndex; n++) {
    			columnTypes.add (n, null);
    		}
    	}
    	columnTypes.set (columnIndex, klass);
    	LOGGER.debug (columnIndex+" = "+klass);
    }
}