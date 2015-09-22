package swingPlus.tablelist;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import swingPlus.shared.JTableST;




public class ColumnSortControl extends JTableST {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7171028541653966966L;
	private final static Logger LOGGER = Logger.getLogger (ColumnSortControl.class);
	
	static final String COLUMNHEADER = "Sort By";
	
	
	protected JTable assocTable;
	
	
	public ColumnSortControl (final JTable table)  {
		super (new DefaultTableModel (0, 1));
		assocTable = table;
		
		populateModel ();
		
		assocTable.getColumnModel().addColumnModelListener (new SecondaryColumnListener ());
		assocTable.addPropertyChangeListener (new SecondaryPropertyChangeListener ());
		
		getSelectionModel().addListSelectionListener (new SelectionListener ());
		((DefaultTableModel)getModel()).setColumnIdentifiers (new Object[] {COLUMNHEADER});	
		
		fixAutoOrdering();
	}
	


	@Override
	public boolean isCellEditable (final int row, final int column) {
		return this.convertColumnIndexToModel (column) != 0;
	}
	
	@Override
	public Class<?> getColumnClass (final int column) {
		final int modelColumnIndex = this.convertColumnIndexToModel (column);
		final Object obj = this.getModel().getValueAt (0, modelColumnIndex);
		return obj.getClass();
	}
	
	
	/**
	 * Fill this table's model with data from the associated JTable's column info
	 */
	protected void populateModel () {
		((DefaultTableModel)getModel()).setRowCount (0);

		for (int modelColumnIndex = 0; modelColumnIndex < assocTable.getModel().getColumnCount(); modelColumnIndex++) {
			final Vector<Object> columnState = new Vector<Object> ();
			columnState.add (assocTable.getModel().getColumnName (modelColumnIndex));
			((DefaultTableModel)getModel()).addRow (columnState);
		}
	}
	
	
	/**
	 *  Set auto-sorting so row order in the JColumnList follows the column order
	 *  in the associated JTable, and make sure the user can't fiddle with that
	 */
	protected void fixAutoOrdering () {
		//populateOrderMap ();
		final TableRowSorter<TableModel> trs = new TableRowSorter<TableModel> (this.getModel());
		this.setRowSorter (trs);
		trs.setComparator (0, new ColumnOrderComparator ());
		trs.setSortable (0, false);
		final SortKey sKey = new SortKey (0, SortOrder.ASCENDING);
		final List<SortKey> keys = new ArrayList<SortKey> ();
		keys.add (sKey);
		trs.setSortKeys (keys);
	}
	
    
    
	
    class SelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged (ListSelectionEvent tmEvent) {	
			if (!tmEvent.getValueIsAdjusting ()) {
				//System.err.println ("sel: "+getSelectionModel().isSelectionEmpty());
				for (int index = tmEvent.getFirstIndex(); index <= tmEvent.getLastIndex(); index++) {
					if (ColumnSortControl.this.getSelectionModel().isSelectedIndex (index)) {
			    		final String columnName = (String) getValueAt (index, 0);
			    		final int columnIndex = assocTable.getColumnModel().getColumnIndex (columnName);
			    		//System.err.println ("col: "+columnName+", ind: "+columnIndex);
			    		final int modColumnIndex = assocTable.convertColumnIndexToModel (columnIndex);
			    		assocTable.getRowSorter().toggleSortOrder(modColumnIndex);
			    		clearSelection();
					}
				}
    		  
				ColumnSortControl.this.repaint();
			}
		}
    	
    }
    
    /**
     * Class that listens to changes in the associated JTable's TableColumnModel
     * and updates JColumnList appropriately (insets.e. reorder and redo selectiion states)
     * @author cs22
     *
     */
    class SecondaryColumnListener implements TableColumnModelListener {
        /** Tells listeners that a column was added to the model. */
        public void columnAdded (final TableColumnModelEvent colEvent) {
        	// When the associated JTable adds a column object, cache it so
        	// we can bring it back again when necessary.
        	LOGGER.debug ("col added e: "+colEvent.getFromIndex()+"|"+colEvent.getToIndex()+"|"+colEvent.getSource());
        	populateModel ();
        	//sortRows ();
        }

        /** Tells listeners that a column was removed from the model. */
        public void columnRemoved (final TableColumnModelEvent colEvent) {
        	//logger.debug ("col removed e: "+e.getFromIndex()+"|"+e.getToIndex()+"|"+e.getSource());
        	populateModel ();
        	//sortRows ();
        }

        /** Tells listeners that a column was repositioned. */
        public void columnMoved (final TableColumnModelEvent colEvent) {
        	//sortRows ();
        }

        /** Tells listeners that a column was moved due to a margin change. */
        public void columnMarginChanged (final ChangeEvent cEvent) {	
        	// EMPTY
        }

        /**
         * Tells listeners that the selection model of the
         * TableColumnModel changed.
         */
        public void columnSelectionChanged (final ListSelectionEvent lsEvent) {
        	//EMPTY
        }
    }
    
    /**
     * Class that picks up TableModel and TableColumnModel changes from the JTable this
     * JColumnList inputStream representing
     * @author cs22
     *
     */
    class SecondaryPropertyChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange (final PropertyChangeEvent evt) {
			if ("model".equals (evt.getPropertyName())) {
				populateModel ();
				//sortRows ();
			}
			else if ("columnModel".equals (evt.getPropertyName())) {
				populateModel ();
				//sortRows ();
			}
		}
    	
    }
    
    
    /**
     * Class that sorts the rows in JColumnList based on the columns order of appearance
     * in the associated JTable
     * @author cs22
     *
     */
    class ColumnOrderComparator implements Comparator<Object>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4109069903768140575L;

		@Override
		public int compare (final Object obj1, final Object obj2) {
			return (obj1.toString().compareTo(obj2.toString()));
		}
    }
}
