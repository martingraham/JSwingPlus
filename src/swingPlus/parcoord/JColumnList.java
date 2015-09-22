package swingPlus.parcoord;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultRowSorter;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import util.Messages;


public class JColumnList extends JTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6806409585372065270L;
	private final static Logger LOGGER = Logger.getLogger (JColumnList.class);

	JTable assocTable;
	Map<Object, Integer> colOrder = new HashMap<Object, Integer> ();
	Map<String, TableColumn> restoreColumnCache;
	SecondaryColumnListener scl = new SecondaryColumnListener ();
	
	static final String COLUMNHEADER = Messages.getString ("columnListColumnHeader");
	static final String INCLUDEDHEADER = Messages.getString ("columnListIncludedHeader");
	
	public JColumnList (final JTable linkedTable) {
		super (new DefaultTableModel (0, 2));
		
		assocTable = linkedTable;
		
		//this.getModel().add
		
		populateModel ();
		populateColumnCache (assocTable.getColumnModel());
		
		assocTable.getColumnModel().addColumnModelListener (new SecondaryColumnListener ());
		assocTable.addPropertyChangeListener (new SecondaryPropertyChangeListener ());
		
		((DefaultTableModel)getModel()).setColumnIdentifiers (new Object[] {COLUMNHEADER, INCLUDEDHEADER});	
		
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
		final BitSet columnVisible = columnVisibility (assocTable);

		for (int modelColumnIndex = 0; modelColumnIndex < assocTable.getModel().getColumnCount(); modelColumnIndex++) {
			final Vector<Object> columnState = new Vector<Object> ();
			columnState.add (assocTable.getModel().getColumnName (modelColumnIndex));
			columnState.add (columnVisible.get (modelColumnIndex) ? Boolean.TRUE : Boolean.FALSE);
			((DefaultTableModel)getModel()).addRow (columnState);
		}
	}
	
	/**
	 * Returns a BitSet that indicates whether a column at a given (bit) index
	 * in the TableModel appears in the visible TableColumnModel associated with a JTable
	 * @param table
	 * @return a BitSet indicating column membership in the JTable's TableColumnModel
	 */
	protected BitSet columnVisibility (final JTable table) {
		final BitSet colVisibility = new BitSet (table.getModel().getColumnCount());
		for (int visColumn = 0; visColumn < table.getColumnModel().getColumnCount(); visColumn++) {
			final TableColumn tableColumn = table.getColumnModel().getColumn (visColumn);
			colVisibility.set (tableColumn.getModelIndex());
		}
		
		return colVisibility;
	}
	
	
	
	
	/**
	 * Cache columns added to associated JTable so we can revive them again
	 * after deselection and reselection
	 * @param tcm
	 */
	protected void populateColumnCache (final TableColumnModel tcm) {
        restoreColumnCache = new HashMap <String, TableColumn> ();
		for (int colViewIndex = 0; colViewIndex < tcm.getColumnCount(); colViewIndex++) {
			addToColumnCache (tcm, colViewIndex);
		}
	}
	
	protected void addToColumnCache (final TableColumnModel tcm, final int viewIndex) {
		final TableColumn tableColumn = tcm.getColumn (viewIndex);
    	restoreColumnCache.put (((String)tableColumn.getHeaderValue()), tableColumn);
	}

	
	
	
	/**
	 *  Set auto-sorting so row order in the JColumnList follows the column order
	 *  in the associated JTable, and make sure the user can't fiddle with that
	 */
	protected void fixAutoOrdering () {
		populateOrderMap ();
		final TableRowSorter<TableModel> trs = new TableRowSorter<TableModel> (this.getModel());
		this.setRowSorter (trs);
		trs.setComparator (0, new JpcOrderComparator ());
		trs.setSortable (0, false);
		trs.setSortable (1, false);
		final SortKey sKey = new SortKey (0, SortOrder.ASCENDING);
		final List<SortKey> keys = new ArrayList<SortKey> ();
		keys.add (sKey);
		trs.setSortKeys (keys);
	}
	
	
	/**
	 * Cache to speed up column view index look ups
	 */
	protected void populateOrderMap () {
		final TableColumnModel columnModel = assocTable.getColumnModel ();
		colOrder.clear();
		for (int column = 0; column < columnModel.getColumnCount(); column++) {
			colOrder.put (assocTable.getColumnName (column), Integer.valueOf (column));
		}
	}
	
	
	
    @Override
    /**
     * Track changes to table
     */
    public void tableChanged (final TableModelEvent tmEvent) {
    	super.tableChanged (tmEvent);
    	//logger.debug ("e: "+e.getFirstRow()+"|"+e.getLastRow()+"|"+e.getColumn()+"|"+e.getType()+"|"+e.getSource());
    	
    	/**
    	 * This bit only gets called on UPDATEs, not INSERTs or DELETEs, so doesn't get
    	 * called when populateModel() does its work.
    	 */
    	if (tmEvent.getFirstRow() != TableModelEvent.HEADER_ROW 
    			&& tmEvent.getFirstRow() == tmEvent.getLastRow()
    			&& tmEvent.getColumn() >= 0
    			&& tmEvent.getType() == TableModelEvent.UPDATE) {
    		final String columnName = (String) getModel().getValueAt (tmEvent.getFirstRow(), 0);

    		final boolean select = ((Boolean) getModel().getValueAt (tmEvent.getFirstRow(), tmEvent.getColumn())).booleanValue();
    		if (select) {
    			LOGGER.debug ("cnadd: "+columnName);
    			final TableColumn aColumn = restoreColumnCache.get (columnName);
        		LOGGER.debug ("cnadd column: "+aColumn);
        		if (aColumn != null) {
        			assocTable.addColumn (aColumn);
        		}
    		} else {
    			try {
    				final TableColumnModel columnModel = assocTable.getColumnModel();
    				final int columnIndex = columnModel.getColumnIndex (columnName);
	        		LOGGER.debug ("cnrem: "+columnName+", insets: "+columnIndex);
	    			columnModel.removeColumn (columnModel.getColumn (columnIndex));
    			} catch (IllegalArgumentException iae) {
    				LOGGER.error (iae.getLocalizedMessage(), iae);
    			}
    		}
    		  
    		this.repaint();
    	}
    }
    
    void sortRows () {
    	populateOrderMap ();
    	((DefaultRowSorter<?, ?>)this.getRowSorter()).sort();
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

        	addToColumnCache ((TableColumnModel)colEvent.getSource(), colEvent.getToIndex ());
        	populateModel ();
        	sortRows ();
        }

        /** Tells listeners that a column was removed from the model. */
        public void columnRemoved (final TableColumnModelEvent colEvent) {
        	//logger.debug ("col removed e: "+e.getFromIndex()+"|"+e.getToIndex()+"|"+e.getSource());
        	populateModel ();
        	sortRows ();
        }

        /** Tells listeners that a column was repositioned. */
        public void columnMoved (final TableColumnModelEvent colEvent) {
        	sortRows ();
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
				sortRows ();
			}
			else if ("columnModel".equals (evt.getPropertyName())) {
				// New column model so clear old values and cache new TableColumn objects
				restoreColumnCache.clear ();
				
				if (evt.getOldValue() instanceof TableColumnModel) {
					((TableColumnModel)evt.getOldValue()).removeColumnModelListener (scl);
				}
				if (evt.getNewValue() instanceof TableColumnModel) {
					final TableColumnModel colModel = (TableColumnModel)evt.getNewValue();
					colModel.addColumnModelListener (scl);
					populateColumnCache (colModel);
				}
				
				populateModel ();
				sortRows ();
			}
		}
    	
    }
    
    
    /**
     * Class that sorts the rows in JColumnList based on the columns order of appearance
     * in the associated JTable
     * @author cs22
     *
     */
    class JpcOrderComparator implements Comparator<Object>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4109069903768140575L;

		@Override
		public int compare (final Object obj1, final Object obj2) {
			final Integer int1 = colOrder.get(obj1);
			final Integer int2 = colOrder.get(obj2);
			return (int1 != null ? int1.intValue() : assocTable.getColumnCount()) - (int2 != null ? int2.intValue() : assocTable.getColumnCount());
		}
    }
}
