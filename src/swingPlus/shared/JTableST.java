package swingPlus.shared;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

public class JTableST extends JTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7195597260983864949L;
	private final static Logger LOGGER = Logger.getLogger (JTableST.class);
	
	
	public JTableST () {
		super ();
	}
	
	
	public JTableST (final TableModel tableModel) {
		super (tableModel);
	}
	
	
	public JTableST (final TableModel tableModel, final TableColumnModel columnModel) {
		super (tableModel, columnModel);
	}

	
	public JTableST (final TableModel tableModel, final TableColumnModel columnModel, final ListSelectionModel selectionModel) {
		super (tableModel, columnModel, selectionModel);
	}

    /**
     * Invoked when a column is repositioned. If a cell is being
     * edited, then editing is stopped and the cell is redrawn.
     * Application code will not use these methods explicitly, they
     * are used internally by JTable.
     *
     * @param tcmEvent   the event received
     * @see TableColumnModelListener
     */
    @Override
	public void columnMoved (final TableColumnModelEvent tcmEvent) {
        // If I'm currently editing, then I should stop editing
        if (isEditing()) {
            removeEditor();
        }
        
        // Calculate clipped repainting 
        //
        // As moving a column shouldn't involve redrawing the entire JTable
        final int col1 = tcmEvent.getFromIndex();
        final int col2 = tcmEvent.getToIndex ();
  		int minCol = Math.min (col1, col2);
		int maxCol = Math.max (col1, col2);
		minCol = Math.max (0, minCol - 1);
		maxCol = Math.min (this.getColumnModel().getColumnCount() - 1, maxCol + 1);

        LOGGER.debug ("col 1: "+col1+", col 2: "+col2);
        
		final Rectangle rect1 = this.getTableHeader().getHeaderRect (minCol);
		final Rectangle rect2 = this.getTableHeader().getHeaderRect (maxCol);
		final int x1 = Math.min (rect1.x, rect2.x);
		//x1 += (x1 == r1.x ? r1.width / 2 : r2.width / 2);
		final int x2 = Math.max ((int)rect1.getMaxX(), (int)rect2.getMaxX());
		repaint (x1, 0, x2 - x1, this.getHeight());
    }
    
    
    /**
     * make basic tooltips for header
     */
    @Override
    public JTableHeader createDefaultTableHeader () {
    	return new JTableHeader (columnModel) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText (final MouseEvent mEvent) {
                String tip = null;
                final java.awt.Point mPoint = mEvent.getPoint();
                final int index = columnModel.getColumnIndexAtX(mPoint.x);
                if (index >= 0 && index < columnModel.getColumnCount()) {
                	final Object headerVal = columnModel.getColumn(index).getHeaderValue();
                	if (headerVal != null) {
                		tip = headerVal.toString()+" Column";
                	}
                }
                return tip;
            }
        };
    }
}
