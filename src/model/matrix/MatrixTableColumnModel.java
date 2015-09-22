package model.matrix;

import java.util.Collection;

import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;


public class MatrixTableColumnModel extends DefaultTableColumnModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3528501132541254812L;
	
	
    /**
     *  Deletes the <code>column</code> from the
     *  <code>tableColumns</code> array.  This method will do nothing if
     *  <code>column</code> inputStream not in the table's columns list.
     *  <code>tile</code> inputStream called
     *  to resize both the header and table views.
     *  This method also posts a <code>columnRemoved</code>
     *  event to its listeners.
     *
     * @param	column		the <code>TableColumn</code> to be removed
     * @see	#addColumn
     */
    @Override
	public void removeColumn (final TableColumn column) {
    	if (column == null && this.getColumnCount () > 0) {
    	    if (selectionModel != null) {
    			selectionModel.removeIndexInterval (0, this.getColumnCount() - 1);
    		}
    		
    		for (int n = 0; n < this.getColumnCount(); n++) {
    			final TableColumn col = this.getColumn (n);
    			col.removePropertyChangeListener (this);
    		}
    		
    		tableColumns.clear ();
    		totalColumnWidth = -1;
    		
    	    fireColumnRemoved (new TableColumnModelEvent(this, -1, -1));
    	} else {
    		super.removeColumn (column);
    	}
    }
    
    
    
    
    // Lets us set a new ordering for columns without millions of events firing,
    // vector shuffling etc that would happen with multiple moveColumn calls 
    public void setColumns (final Collection<TableColumn> columns) {
    	tableColumns.clear ();
    	tableColumns.addAll (columns);
    	this.fireColumnMoved (new TableColumnModelEvent (this, 0, this.getColumnCount() - 1));
    }

}
