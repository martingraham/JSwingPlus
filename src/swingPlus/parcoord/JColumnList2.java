package swingPlus.parcoord;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import swingPlus.shared.JTableST;
import util.Messages;


public class JColumnList2 extends JTableST {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6806409585372065270L;
	private final static Logger LOGGER = Logger.getLogger (JColumnList2.class);

	Set<JTable> assocTable;
	Map<String, Map <JTable, List<TableColumn>>> restoreColumnCache;
	TableColumnModelListener scl = new SecondaryColumnListener ();
	PropertyChangeListener spcl = new SecondaryPropertyChangeListener ();
	
	static final String COLUMNHEADER = Messages.getString ("columnListColumnHeader");
	static final String INCLUDEDHEADER = Messages.getString ("columnListIncludedHeader");
	
	public JColumnList2 () {
		super (new DefaultTableModel (0, 2));
		((DefaultTableModel)getModel()).setColumnIdentifiers (new Object[] {COLUMNHEADER, INCLUDEDHEADER});		
		((DefaultTableModel)getModel()).setRowCount (0);
		this.setAutoCreateRowSorter (true);
		restoreColumnCache = new HashMap <String, Map<JTable, List<TableColumn>>> ();
		assocTable = new HashSet<JTable> ();
	}
	
	public void addTable (final JTable jTable) {
		if (!assocTable.contains (jTable)) {
			assocTable.add (jTable);
			jTable.getColumnModel().addColumnModelListener (scl);
			jTable.addPropertyChangeListener (spcl);
			
			addToModel (jTable);
			addToColumnCache (jTable);
		}
	}
	
	
	public void removeTable (final JTable jTable) {
		if (assocTable.contains (jTable)) {
			assocTable.remove (jTable);
			jTable.getColumnModel().removeColumnModelListener (scl);
			jTable.removePropertyChangeListener (spcl);
			
			addToModel (jTable);
			addToColumnCache (jTable);
		}
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
	 * Fill the columnlist's TableModel with data from the associated JTable's column info
	 */
	protected void addToModel (final JTable jTable) {
		for (int modelColumnIndex = 0; modelColumnIndex < jTable.getModel().getColumnCount(); modelColumnIndex++) {
			final String columnName = jTable.getModel().getColumnName (modelColumnIndex);
			addToModel (jTable, columnName, modelColumnIndex);
		}
	}
	
	
	protected void addToModel (final JTable jTable, final String columnName, final int modelColumnIndex) {
		if (restoreColumnCache.get (columnName) == null) {
			final Vector<Object> columnState = new Vector<Object> ();
			columnState.add (columnName);
			LOGGER.debug ("added column name "+columnName+" in "+jTable.getName()+" to column list");
			final int vis = jTable.convertColumnIndexToView (modelColumnIndex);
			columnState.add (vis >= 0 ? Boolean.TRUE : Boolean.FALSE);
			((DefaultTableModel)getModel()).addRow (columnState);
		}
	}

	

	
	protected void removeFromModel (final JTable jTable) {	
		for (String colName : restoreColumnCache.keySet()) {
			removeFromModel (jTable, colName);
		}
	}
	
	protected void removeFromModel (final JTable jTable, final String colName) {	
		final Map<JTable, List<TableColumn>> tableColMap = restoreColumnCache.get (colName);
		if (tableColMap != null) {
			if (tableColMap.isEmpty() || (tableColMap.size() == 1 && tableColMap.containsKey (jTable))) {
				for (int row = 0; row < getModel().getRowCount(); row++) {
					if (getModel().getValueAt(row, 0).equals (colName)) {
						((DefaultTableModel)getModel()).removeRow (row);
						break;
					}
				}
			}
		}
	}
	
	

	
	/**
	 * Cache columns added to associated JTable so we can revive them again
	 * after deselection and reselection
	 * @param tcm
	 */
	protected void addToColumnCache (final JTable jTable) {
		final TableColumnModel tcm = jTable.getColumnModel ();
		for (int colViewIndex = 0; colViewIndex < tcm.getColumnCount(); colViewIndex++) {
			addToColumnCache (jTable, tcm, colViewIndex);
		}
	}
	
	protected void addToColumnCache (final JTable jTable, final TableColumnModel tcm, final int viewIndex) {
		final TableColumn tableCol = tcm.getColumn (viewIndex);
		final String colName = jTable.getColumnName (viewIndex);
       	
       	LOGGER.debug ("cache add for "+jTable.getName()+": "+tableCol.getHeaderValue().toString());
       	
       	Map<JTable, List<TableColumn>> colsForName = restoreColumnCache.get (colName);
       	if (colsForName == null) {
       		colsForName = new HashMap<JTable, List<TableColumn>> ();
       		restoreColumnCache.put (colName, colsForName);
       	}
       	
       	List<TableColumn> colsForTableAndName = colsForName.get (jTable);
       	if (colsForTableAndName == null) {
       		colsForTableAndName = new ArrayList<TableColumn> ();
       		colsForName.put (jTable, colsForTableAndName);
       	}
       	
       	if (!colsForTableAndName.contains (tableCol)) {
       		colsForTableAndName.add (tableCol);
       	}
	}
	
	
	
	protected void removeFromColumnCache (final JTable jTable) {
		final Iterator<String> colNameIter = restoreColumnCache.keySet().iterator();
		
		while (colNameIter.hasNext()) {
			removeFromColumnCache (jTable, colNameIter.next(), colNameIter);
       	}
	}

	
	protected void removeFromColumnCache (final JTable jTable, final String columnName, final Iterator<String> colNameIter) {
		final Map<JTable, List<TableColumn>> colsForName = restoreColumnCache.get (columnName);
		
		if (colsForName != null) {
			final Iterator<JTable> colIter = colsForName.keySet().iterator();
	   		
			while (colIter.hasNext()) {
	   			final JTable table = colIter.next();
	   			
	   			if (table.equals (jTable)) {
	   				colIter.remove();
	   			}
	   		}
		}
		
		if (colNameIter != null && (colsForName == null || colsForName.isEmpty ())) {
			colNameIter.remove();
		}
		else if (colsForName.isEmpty ()) {
			restoreColumnCache.remove (columnName);
		}
	}
	
	
	@Override
	public String toString () {
		
		final StringBuilder sBuild = new StringBuilder ();
		for (String columnName : restoreColumnCache.keySet()) {
			sBuild.append(columnName).append(":\n");
			
			final Map<JTable, List<TableColumn>> colsForName = restoreColumnCache.get (columnName);
			for (Entry<JTable, List<TableColumn>> entry : colsForName.entrySet()) {
				final JTable table = entry.getKey();
				final List<TableColumn> columns = entry.getValue();
				sBuild.append('\t').append(table.getName()).append(":\n");

				for (TableColumn column : columns) {
					sBuild.append ("\t\t").append(column.getHeaderValue().toString()).append("\n");
				}
			}
			
		}
		
		return sBuild.toString();
	}

	
    @Override
    /**
     * Track changes to table
     */
    public void tableChanged (final TableModelEvent tmEvent) {
    	super.tableChanged (tmEvent);
    	LOGGER.debug ("e: "+tmEvent.getFirstRow()+"|"+tmEvent.getLastRow()+"|"+tmEvent.getColumn()+"|"+tmEvent.getType()+"|"+tmEvent.getSource());
    	
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
        		final Map<JTable, List<TableColumn>> colTableMap = restoreColumnCache.get (columnName);
        		for (Entry<JTable, List<TableColumn>> entry : colTableMap.entrySet()) {
        			final JTable table = entry.getKey();		
        			final List<TableColumn> columns = colTableMap.get (table);
        			
        			for (TableColumn tc : columns) {
        				table.addColumn (tc);
                		LOGGER.debug ("cnadd column: "+tc);
        			}
        		}
    		} else {
    			try {
    				final Map<JTable, List<TableColumn>> colTableMap = restoreColumnCache.get (columnName);
    	       		for (Entry<JTable, List<TableColumn>> entry : colTableMap.entrySet()) {
    	       			final JTable table = entry.getKey();		
            			final List<TableColumn> columns = colTableMap.get (table);
            			
            			for (TableColumn tableCol : columns) {
            				final TableColumnModel tcm = table.getColumnModel();
            				final int columnIndex = tcm.getColumnIndex (columnName);
            				tcm.removeColumn (tcm.getColumn (columnIndex));
            				LOGGER.debug ("cnrem: "+columnName+", insets: "+columnIndex);
            			}
            		}
    			} catch (IllegalArgumentException iae) {
    				LOGGER.error (iae.getLocalizedMessage(), iae);
    			}
    		}
    		  
    		this.repaint();
    	}
    }
    

    
    /**
     * Class that listens to changes in the associated JTables' TableColumnModels
     * and updates JColumnList appropriately (i.e. reorder and redo selection states)
     * @author cs22
     *
     */
    class SecondaryColumnListener implements TableColumnModelListener {
        /** Tells listeners that a column was added to the model. */
        public void columnAdded (final TableColumnModelEvent tcmEvent) {
        	// When the associated JTable adds a column object, cache it so
        	// we can bring it back again when necessary.
        	LOGGER.debug ("col added e: "+tcmEvent.getFromIndex()+"|"+tcmEvent.getToIndex()+"|"+tcmEvent.getSource());
        	final TableColumnModel tcm = (TableColumnModel)tcmEvent.getSource();
        	
        	for (JTable jt : assocTable) {
        		if (jt.getColumnModel().equals(tcm)) {
        			final String colName = jt.getColumnName (tcmEvent.getToIndex ());
        			LOGGER.debug ("Add column: "+colName+" for "+jt.getName());
        			final int modelIndex = jt.convertColumnIndexToModel (tcmEvent.getToIndex ());
                	
                	addToModel (jt, colName, modelIndex);
                	addToColumnCache (jt, tcm, tcmEvent.getToIndex ());
        		}
        	}

        }

        /** Tells listeners that a column was removed from the model. */
        public void columnRemoved (final TableColumnModelEvent tcmEvent) {
        	final TableColumnModel tcm = (TableColumnModel)tcmEvent.getSource();
        	for (JTable jTable : assocTable) {
        		if (jTable.getColumnModel().equals (tcm)) {
        			//String colName = jt.getModel().getColumnName (jt.convertColumnIndexToModel (e.getToIndex ()));
        			//removeFromModel (jt, colName);
                	//removeFromColumnCache (jt, colName);
        		}
        	}
        }

        /** Tells listeners that a column was repositioned. */
        public void columnMoved (final TableColumnModelEvent tcmEvent) {
        	// EMPTY
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
        	// EMPTY
        }
    }
    
    /**
     * Class that picks up TableModel and TableColumnModel changes from the JTable this
     * JColumnList is representing
     * @author cs22
     *
     */
    class SecondaryPropertyChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange (final PropertyChangeEvent evt) {
			if ("model".equals (evt.getPropertyName())) {
				if (evt.getSource() instanceof JTable) {

					final JTable jTable = (JTable)evt.getSource();
					// New Model for a table.
					// remove every stored column associated with that table
					// and then add in the new column data
					if (jTable.getAutoCreateColumnsFromModel()) {
						//System.err.println (jt.getName());
						//System.err.println ("1\n"+JColumnList2.this.toString());
						removeFromModel (jTable);
						//System.err.println ("2\n"+JColumnList2.this.toString());
						removeFromColumnCache (jTable);
						//System.err.println ("3\n"+JColumnList2.this.toString());
						//System.err.println ("hiiiii");
						addToModel (jTable);
						//System.err.println ("4\n"+JColumnList2.this.toString());
						addToColumnCache (jTable);
			            //System.err.println ("5\n"+JColumnList2.this.toString());
					}
				}
			}
			else if ("columnModel".equals (evt.getPropertyName())) {
				// New column model so clear old values and cache new TableColumn objects
				
				final JTable jTable = (JTable)evt.getSource();
	
				if (evt.getOldValue() instanceof TableColumnModel && 
					evt.getSource() instanceof JTable) {
					((TableColumnModel)evt.getOldValue()).removeColumnModelListener (scl);
					//System.err.println ("1\n"+JColumnList2.this.toString());
					removeFromModel (jTable);
					//System.err.println ("2\n"+JColumnList2.this.toString());
					removeFromColumnCache (jTable);
					//System.err.println ("3\n"+JColumnList2.this.toString());
				}
				if (evt.getNewValue() instanceof TableColumnModel && 
					evt.getSource() instanceof JTable) {
					final TableColumnModel tcm = (TableColumnModel)evt.getNewValue();
	        		tcm.addColumnModelListener (scl);
	                addToColumnCache (jTable);
	                //System.err.println ("4\n"+JColumnList2.this.toString());
	                addToModel (jTable);
	                //System.err.println ("5\n"+JColumnList2.this.toString());
				}
			}
		}
    	
    }
}
