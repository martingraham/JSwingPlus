package swingPlus.shared;


import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


public class JColumnHeader extends ScaledTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -956659657708093407L;

	JTable primaryTable;
	protected transient PropertyChangeListener tablePropertyListener;
	
	public JColumnHeader () {
		super ();
		this.setBackground (new Color (224, 224, 224));
		this.setSelectionBackground (new Color (255, 255, 128));
		tablePropertyListener = new TablePropertyListener ();
		this.addMouseListener (new RowSortMouseListener ());
		this.setColumnSelectionAllowed (true);
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
		    	this.setIntercellSpacing (jTable.getIntercellSpacing());
		    	this.setAutoResizeMode (jTable.getAutoResizeMode());
		    	setModel (jTable.getModel()); 	
		    	setColumnModel (jTable.getColumnModel ());
	    	}
	    	
	    	firePropertyChange ("table", oldTable, primaryTable); 
    	}
    }
    
    
    public JTable getTable () {
    	return primaryTable;	
    }
	
	
    @Override
    public void setModel (final TableModel tableModel) {
    	setAutoCreateColumnsFromModel (false);
		setColumnSelectionAllowed (true);
		
    	this.setAutoCreateRowSorter (false);
    	super.setModel (tableModel);

		final TableRowSorter<? extends TableModel> trs = new TableRowSorter<TableModel> (tableModel);
		trs.setRowFilter(
			new RowFilter<TableModel, Integer> () {
				@Override
				public boolean include (final Entry<? extends TableModel, ? extends Integer> entry) {
					final int rowIndex = ((Integer)entry.getIdentifier()).intValue();
					return rowIndex == 0;
				}
			}
		);
		this.setRowSorter (trs);
    }
    
    

    
    @Override
    public boolean isCellEditable (final int row, final int col) { return false; }
    // ^^^Stops double-clicking starting cell editing^^^
    
    //JTableHeader
   // BasicTableHeaderUI
    
    
    class TablePropertyListener implements PropertyChangeListener {
    	/**
    	 * Listens for new objects being plugged in to the primary JTable
    	 * to replace existing TableModel and tableColumnModels.
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
				
				else if ("columnModel".equals (propertyName)) {
					setColumnModel ((TableColumnModel)propEvent.getNewValue());
				}
			}
		}
    }
    
    
	/**
	 * Mouse listener to sort rows if a column is selected by double-clicking
	 * in the column header
	 * 
	 * Remember, sorting rows involves comparing elements in one column that
	 * cuts across all the rows for comparison.
	 * Sorting columns involves comparing elements in one row that cuts across
	 * all the columns for comparison.
	 * @author cs22
	 *
	 */
    class RowSortMouseListener extends MouseAdapter {
    	
		@Override
		public void mouseClicked (final MouseEvent mEvent) {
			
			if ((mEvent.getClickCount() % 3) == 2 && mEvent.getSource() instanceof JTable) {
				final JTable columnHeader = (JTable)mEvent.getSource();
				final RowSorter<? extends TableModel> sorter = getTable().getRowSorter ();
                
				if (sorter != null) {
                    final int columnIndex = columnHeader.columnAtPoint(mEvent.getPoint());
                    
                    if (columnIndex != -1) {
                    	// Inserted to clear row selection on row resort as row selection was losing track;
                    	// May need to be rescinded
                    	JColumnHeader.this.getTable().getSelectionModel().clearSelection();
                        final int columnIndexModel = columnHeader.convertColumnIndexToModel (columnIndex);
                        sorter.toggleSortOrder (columnIndexModel);
                        
                        /**
                         * This next bit flicks the table within its viewport to the top or
                         * bottom depending on whether the column is sorted ascending or descending.
                         */
                        final List<SortKey> keys = new ArrayList<SortKey> (sorter.getSortKeys());
                        int sortIndex;
                        for (sortIndex = keys.size() - 1; sortIndex >= 0; sortIndex--) {
                            if (keys.get(sortIndex).getColumn() == columnIndexModel) {
                                break;
                            }
                        }
                        
                    	final Component comp = getTable().getParent ();
    					if (comp instanceof JViewport && sortIndex != -1) {
                        	final SortOrder sOrder = keys.get(sortIndex).getSortOrder();
                        	final int viewRow = (sOrder == SortOrder.ASCENDING) ? 0 : getTable().getRowCount () - 1;
    						final JViewport jvp = (JViewport)comp;
    						final Rectangle rect = getTable().getCellRect (viewRow, columnIndex, true);
    					    // The location of the viewport relative to the table
    						final Point point = jvp.getViewPosition();
    					    rect.setLocation (rect.x - point.x, rect.y - point.y);
    					    jvp.scrollRectToVisible (rect);
    					} 
                    }
                }
			}	
		}
    }

}
