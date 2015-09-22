package ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;


public class MultiCellSpanTableUI extends AnimatedTableUI {

	final static Logger LOGGER = Logger.getLogger (MultiCellSpanTableUI.class);
	
	

	public static ComponentUI createUI (final JComponent comp) {
		return new MultiCellSpanTableUI();
	}
	
	
	protected void paintCells (final Graphics graphics, final int rMin, final int rMax, final int cMin, final int cMax) {
    	final JTableHeader header = table.getTableHeader();
    	final TableColumn draggedColumn = (header == null) ? null : header.getDraggedColumn();
	
    	final TableColumnModel tableColModel = table.getColumnModel();
    	//final int columnMargin = tableColModel.getColumnMargin();
	
	    Rectangle cellRect;
		TableColumn aColumn;
		int columnWidth;
		
		final boolean ltr = table.getComponentOrientation().isLeftToRight();
		final int cFirst = ltr ? cMin : cMin + 1;
		
	    for (int row = rMin; row <= rMax; row++) {
	    	LOGGER.debug ("row: "+row);
	    	preRowCalculation (row);
	    	
	    	cellRect = table.getCellRect (row, cMin, false);
	    	if (rowSortAnimator.isStartCalled()) {
	    		rowSortAnimator.animCellRect (cellRect, row);
	    	} 
	    	
	    	if (!ltr) {
                aColumn = tableColModel.getColumn (cMin);
                if (aColumn != draggedColumn) {
                    columnWidth = aColumn.getWidth();
                    getCellBounds (cellRect, row, cMin);
	                if (columnSortAnimator.isStartCalled()) {
                    	columnSortAnimator.animCellRect (cellRect, row, cMin);
                    }
                    paintCell (graphics, cellRect, row, cMin);
                }
	    	}
	    
	    	
		    for (int column = cFirst; column <= cMax; column++) {
	          	aColumn = tableColModel.getColumn (column);
	          	columnWidth = aColumn.getWidth();
	          	getCellBounds (cellRect, row, column);
	
	          	if (!ltr) {
	          		cellRect.x -= columnWidth;
	          	}
	          	
	          	final Object curValue = table.getValueAt (row, column);
	          	int cSpanCol = column;
	          	int spanWidth = cellRect.width;
	          	while (++cSpanCol <= cMax && (table.getValueAt (row, cSpanCol) == curValue)) {
	          		spanWidth += tableColModel.getColumn(cSpanCol).getWidth();
	          		//spanWidth += table.getColumnModel().getColumnMargin();
	          		if (LOGGER.isDebugEnabled()) {
	          			LOGGER.debug ("row: "+row+", csc: "+cSpanCol+", "+table.getValueAt (row, cSpanCol));
	          		}
	          	}
	          	if (LOGGER.isDebugEnabled()) {
	          		LOGGER.debug ("cspanCol: "+cSpanCol+", cmax: "+cMax+", curValue: "+curValue);
	          	}
	          	cellRect.width = spanWidth;
        	
	          	
	            if (columnSortAnimator.isStartCalled()) {
	            	columnSortAnimator.animCellRect (cellRect, row, column);
	            }
	              
	            paintCell (graphics, cellRect, row, column);
	              
	          	if (cSpanCol > column + 1) { // && cSpanCol < cMax) {
	          		column = cSpanCol - 1;
	          		if (LOGGER.isDebugEnabled()) {
	          			LOGGER.debug ("col back to: "+column);
	          		}
	          	}
	          	
	            if (aColumn == draggedColumn) {
	                drawSheen ((Graphics2D)graphics, cellRect);
	            }
	
	            if (ltr) {	            
	            	cellRect.x += cellRect.width + table.getColumnModel().getColumnMargin();
	            }
		    }
	    }
	    
	    fastRenderPane.removeAll ();
    }
	
	
	/**
	 * Method called before each row is painted in the paintCells method
	 * @param vrow
	 */
	protected void preRowCalculation (final int vrow) {
		/* EMPTY METHOD */
	}
}
