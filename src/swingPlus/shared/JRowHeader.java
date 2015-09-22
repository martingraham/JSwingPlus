package swingPlus.shared;


import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;



public class JRowHeader extends ScaledTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4713633281951713790L;
	
	JTable primaryTable; // table this is a row header for
	transient protected PropertyChangeListener tablePropertyListener;
	
	public JRowHeader () {
		super ();
		this.setBackground (new Color (224, 224, 224));
		this.setSelectionBackground (new Color (255, 255, 128));
		tablePropertyListener = new TablePropertyListener ();
		this.addMouseListener (new ColumnSortMouseListener ());
	}

	
	
    @Override
    public void setModel (final TableModel tableModel) {
    	setAutoCreateColumnsFromModel (false);
    	
    	super.setModel (tableModel);
    	
    	final TableColumnModel columnModel = createDefaultColumnModel ();
    	setColumnModel (columnModel);
    	final TableColumn tColumn = new TableColumn (0);
    	addColumn (tColumn);
		setColumnSelectionAllowed (false);
		getColumnModel().getColumn(0).setResizable (false);
    }
    
    
    public void setTable (final JTable jTable) {
    	
    	if (primaryTable != jTable) {
        	final JTable oldTable = primaryTable;	
        	primaryTable = jTable;
    	
	    	if (oldTable != null) {
	    		oldTable.removePropertyChangeListener (tablePropertyListener);
	    		oldTable.removePropertyChangeListener ("masterScale", scalePropertyListener);
	    	}
	    	
	    	if (jTable != null) {
	    		primaryTable.addPropertyChangeListener (tablePropertyListener);
	    		primaryTable.addPropertyChangeListener ("masterScale", scalePropertyListener);
		    	setModel (jTable.getModel()); 	
				setRowSorter (jTable.getRowSorter ());
				setSelectionModel (jTable.getSelectionModel ());
	    	}
	    	
	    	firePropertyChange ("table", oldTable, primaryTable); 
    	}
    }
    
    public JTable getTable () {
    	return primaryTable;	
    }
    
    
    @Override
    public boolean isCellEditable (final int row, final int col) { return false; }
    // ^^^Stops double-clicking starting cell editing^^^
    
    
    
    class TablePropertyListener implements PropertyChangeListener {
    	/**
    	 * Listens for new objects being plugged in to the primary JTable
    	 * to replace existing TableModel, RowSorter and ListSelectionModels.
    	 * This is different to internal changes to existing models which are
    	 * picked up by the relevant "model-type"-listener methods.
    	 */
    	
		@Override
		public void propertyChange (final PropertyChangeEvent propEvent) {
			if (propEvent != null) {
				final String propertyName = propEvent.getPropertyName();
				
				if ("model".equals (propertyName) ) {
					setModel ((TableModel)propEvent.getNewValue());
				}
				
				else if ("rowSorter".equals (propertyName)) {
					setRowSorter ((RowSorter<? extends TableModel>)propEvent.getNewValue());
				}
				
				else if ("selectionModel".equals (propertyName)) {
					setSelectionModel ((ListSelectionModel)propEvent.getNewValue());
				}
				
				else if ("rowHeight".equals (propertyName)) {
					setRowHeight (((Integer)propEvent.getNewValue()).intValue());
				}
			}
		}
    }
    

    class ColumnSortMouseListener extends MouseAdapter {
    	/**
    	 * Mouse listener to sort columns if a row is selected by double-clicking
    	 * in the row header
    	 * 
    	 * Remember, sorting columns involves comparing elements in one row that
    	 * cuts across all the columns for comparison.
    	 * Sorting rows involves comparing elements in one column that cuts across
    	 * all the rows for comparison.
    	 * @author cs22
    	 *
    	 */
    	
		@Override
		public void mouseClicked (final MouseEvent mEvent) {
			if ((mEvent.getClickCount() % 3) == 2 && mEvent.getSource() instanceof JTable) {
				final JTable rowHeader = (JTable)mEvent.getSource();	// The rowHeader table
				final int viewRow = rowHeader.rowAtPoint (mEvent.getPoint());
				
				if (getTable() instanceof ColumnSortableTable) {
					((ColumnSortableTable)getTable()).getColumnsSorter().sort (viewRow);
					
					/**
					 * Flick table, if in a viewport, to the far left so sorted items
					 * are visible.
					 */
					final Component comp = getTable().getParent ();
					
					if (comp instanceof JViewport) {
						final JViewport jvp = (JViewport)comp;
						final Rectangle rect = getTable().getCellRect (viewRow, 0, true);
					    // The location of the viewport relative to the table
						final Point point = jvp.getViewPosition();
					    rect.setLocation (rect.x - point.x, rect.y - point.y);
					    jvp.scrollRectToVisible(rect);
					}    
				}
			}	
		}
    } 
}
