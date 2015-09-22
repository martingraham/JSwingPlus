package model.matrix;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractMatrixTableModel extends AbstractTableModel implements MatrixTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1636876301257831310L;

	protected Map<Object, Integer> invColumns;
	protected Map<Object, Integer> invRows;
	

	public AbstractMatrixTableModel () {
		super ();
    	invColumns = new HashMap <Object, Integer> ();
    	invRows = new HashMap <Object, Integer> ();
	}
	
    /**
     * 
     * @param column	index of the model column
     * @return	the object that is represented by that column index
     */
	@Override
    abstract public Object getColumnObject (int column);
    
    /**
     * 
     * @param row	index of the model row
     * @return	the object that is represented by that row index
     */
    @Override
    abstract public Object getRowObject (int row);
    
    
    /**
     * 
     * @param colObj		The object to get the model column index for
     * @return	The model column index of the object, -1 if not found
     */
    @Override
    public int getColumnIndex (final Object colObj) {
    	final Integer intObj = invColumns.get (colObj);
    	return (intObj == null ? -1 : intObj.intValue());
    }
    
    /**
     * 
     * @param rowObj		The object to get the model row index for
     * @return	The model row index of the object, -1 if not found
     */
    @Override
    public int getRowIndex (final Object rowObj) {
    	final Integer intObj = invRows.get (rowObj);
    	return (intObj == null ? -1 : intObj.intValue());
    }
}
