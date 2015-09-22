package swingPlus.tablelist;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import swingPlus.shared.JTableST;
import ui.StackedRowTableUI;

public class JEditableVarColTable extends JTable {

	
	  /**
	 * 
	 */
	private static final long serialVersionUID = -4083965772886824414L;

	
	
    public JEditableVarColTable () {
        this (null, null, null);
    }

    public JEditableVarColTable (final TableModel tableModel) {
        this (tableModel, null, null);
    }

    public JEditableVarColTable (final TableModel tableModel, final TableColumnModel colModel) {
        this (tableModel, colModel, null);
    }
    
    public JEditableVarColTable (final TableModel tableModel, final TableColumnModel colModel,
    		final ListSelectionModel selectionModel) {
    	super (tableModel, colModel, selectionModel);
    }
    
    
	@Override
    public int columnAtPoint (final Point point) {
        int xCoord = point.x;
        if( !getComponentOrientation().isLeftToRight() ) {
            xCoord = getWidth() - xCoord;
        }
        
        if (this.getUI() instanceof StackedRowTableUI) {
        	final Point newP = new Point (point);
        	final StackedRowTableUI srtui = (StackedRowTableUI) this.getUI();
        	return srtui.getColumnAtPoint (newP);
        }

        return getColumnModel().getColumnIndexAtX(xCoord);
    }
	
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
