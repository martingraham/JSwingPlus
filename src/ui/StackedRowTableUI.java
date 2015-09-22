package ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;
import javax.swing.RepaintManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import util.anim.AnimTimer;
import util.anim.RangeTransformer;
import util.anim.SineTransform;

import org.apache.log4j.Logger;



public class StackedRowTableUI extends AnimatedTableUI {

	private final static Logger LOGGER = Logger.getLogger (StackedRowTableUI.class);
	
	
	protected CellColumnPositions cellColumnPositions;
	protected boolean relativeLayout;
	protected double[] rowValueTotals;
	PropertyChangeListener tablePropertyListener2;
	protected MouseAdapter headerDragListener;
	
	
	
	@Override
	protected void installDefaults () {
		super.installDefaults ();
		
		cellColumnPositions = new CellColumnPositions ();
		setRelativeLayout (false);
		
		columnSortAnimator = new VariableColumnSortAnim (0.02, new SineTransform ());
		columnSortAnimTimer = new AnimTimer (20, columnSortAnimator);
		
		rowSortAnimator = new VariableRowSortAnim (0.02, new SineTransform ());
		rowSortAnimTimer = new AnimTimer (20, rowSortAnimator);
		
		table.setShowVerticalLines (false);
		table.setAutoResizeMode (JTable.AUTO_RESIZE_LAST_COLUMN);
		
		resetHeaderSizes ();
	}

	
	
	class VariableColumnSortAnim extends ColumnSortAnim {
		
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1703982196191698551L;

		VariableColumnSortAnim (final double stepVal, final RangeTransformer newrt) {
    		super (stepVal, newrt);
    	}
        
        @Override
        public void animationFinished () {
        	super.animationFinished ();
        	resetHeaderSizes ();
        }
		
        public void animCellRect (final Rectangle cellRect, final int vRow, final int vCol) {
        	if (cellColumnPositions != null && cellColumnPositions.getCellFromXCoords() != null) {
	        	final int mRow = table.convertRowIndexToModel (vRow);
	        	final int mCol = table.convertColumnIndexToModel (vCol);
	        	if (cellColumnPositions.getCellFromXCoords().length > mRow
	        			&& cellColumnPositions.getCellFromXCoords()[0].length > mCol) {
		        	final double xCoord = cellColumnPositions.getCellFromXCoords() [mRow][mCol];
		        	final double width = cellColumnPositions.getCellFromWidths() [mRow][mCol];
		        	final double xAnim = xCoord + (((float)cellRect.x - xCoord) * per);
		        	final double widthAnim = width + (((float)cellRect.width - width) * per);
		        	cellRect.x = (int)Math.round (xAnim);
		        	cellRect.width = (int)Math.round (widthAnim);
		        	cellColumnPositions.getCellCurXCoords() [mRow][mCol] = cellRect.x;
		        	cellColumnPositions.getCellCurWidths() [mRow][mCol] = cellRect.width;
	        	}
        	}
        }
    }
	
	
	
	class VariableRowSortAnim extends RowSortAnim {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2676254617316042858L;

		VariableRowSortAnim (final double stepVal, final RangeTransformer newrt) {
    		super (stepVal, newrt);
    	}
        
        @Override
        public void animationFinished () {
        	super.animationFinished ();
        	resetHeaderSizes ();
        }
    }
	
	
	/**
	 * Extend startColumnAnim (which is called after a TableModel data change)
	 * to recalculate all the cell widths based on their data values.
	 */
	protected void startColumnAnim () {
		
		if (columnAnimationEnabled) {
			if (columnSortAnimator.isStartCalled() || rowSortAnimator.isStartCalled()) {
				// If animation already running, make current positions the old positions before restarting
				cellColumnPositions.pushCurrentCoords();
			}
		}
		
		super.startColumnAnim ();
		
		if (columnAnimationEnabled) {
			rowValueTotals = calcRowValueTotals ();
		}
	}
	
	
	/**
	 * Extend startRowAnim (which is called after a RowSorter event)
	 * to recalculate all the cell heights based on their data values.
	 */
	protected void startRowAnim () {
		
		if (rowAnimationEnabled) {
			if (columnSortAnimator.isStartCalled() || rowSortAnimator.isStartCalled()) {
				// If animation already running, make current positions the old positions before restarting
				cellColumnPositions.pushCurrentCoords();
			}
		}
		
		super.startRowAnim ();
		
		if (rowAnimationEnabled) {
			rowValueTotals = calcRowValueTotals ();
		}
	}
	
	
	
	public void setRelativeLayout (final boolean newSetting) {
		if (relativeLayout != newSetting) {
			relativeLayout = newSetting;
			startColumnAnim ();
		}
	}
	
	public boolean isRelativeLayout () { return relativeLayout; }
	
    
    @Override
    protected void paintCells (final Graphics graphics, final int rMin, final int rMax, final int cMin, final int cMax) {
    	final JTableHeader header = table.getTableHeader();
    	final TableColumn draggedColumn = (header == null) ? null : header.getDraggedColumn();
	
    	final TableColumnModel tableColModel = table.getColumnModel();
    	final int columnMargin = tableColModel.getColumnMargin();
	

	    Rectangle cellRect;
		TableColumn aColumn;
		int columnWidth;
		
		final boolean ltr = table.getComponentOrientation().isLeftToRight();
		final int cFirst = ltr ? cMin : cMin + 1;
		
	    for (int row = rMin; row <= rMax; row++) {
    		
	    	cellRect = table.getCellRect (row, cMin, false);
	    	if (rowSortAnimator.isStartCalled()) {
	    		rowSortAnimator.animCellRect (cellRect, row);
	    	} 
	    	
	    	final double widthAvailable = getAvailableWidth (row);
	    	if (rowValueTotals == null || rowValueTotals.length == 0) {
	    		grabCellWidths ();
	    	}
	    	final double valToPixelRatio = widthAvailable / rowValueTotals [row];
	    	// DIFFERENCE /\/\/\
	    	
	    	if (!ltr) {
                aColumn = tableColModel.getColumn (cMin);
                if (aColumn != draggedColumn) {
                    //columnWidth = aColumn.getWidth();
                   // columnWidth = (int)getCellWidth (row, cMin, valToPixelRatio);	// DIFFERENCE
                    getCellBounds (cellRect, row, cMin);
	                if (columnSortAnimator.isStartCalled()) {
                    	columnSortAnimator.animCellRect (cellRect, row, cMin);
                    }
                    paintCell (graphics, cellRect, row, cMin);
                }
	    	}
	    	
	    	
	    	// unrounded double variables used as values can cause fractional widths.
	    	// if not accounted for, these fractions cause row widths to be less than the table width
	    	
	    	double unroundedX = cellRect.getX();
	    	
		    for (int column = cFirst; column <= cMax; column++) {
	          	aColumn = tableColModel.getColumn (column);
	          	//columnWidth = aColumn.getWidth();	// DIFFERENCE
	          	final double unroundedColumnWidth = getCellWidth (row, column, valToPixelRatio);	// DIFFERENCE
	          	columnWidth = (int)Math.ceil (unroundedColumnWidth);	// DIFFERENCE
	          	getCellBounds (cellRect, row, column); // we just use getCellBounds for the y coord
	
	          	if (!ltr) {
	          		unroundedX -= unroundedColumnWidth;
	          		cellRect.x = (int)Math.round (unroundedX);
	          	}
	          	
	          	cellRect.width = columnWidth - columnMargin; // DIFFERENCE
	            if (columnSortAnimator.isStartCalled()) {
	            	columnSortAnimator.animCellRect (cellRect, row, column);
	            }
	              
	            paintCell (graphics, cellRect, row, column);
	              
	            if (aColumn == draggedColumn) {
	                drawSheen ((Graphics2D)graphics, cellRect);
	            }
	
	            if (ltr) {	            
	            	unroundedX += unroundedColumnWidth;
	            	cellRect.x = (int)Math.round (unroundedX);
	            }
		    }
	    }
    }
    
    

    
    protected double getCellWidth (final int vRow, final int vCol, final double valToPixelRatio) {
        final Object value = table.getValueAt (vRow, vCol);
        double columnWidth = 0.0f;
        
        if (value instanceof Number) {
        	columnWidth = (valToPixelRatio * ((Number)value).doubleValue()) + table.getColumnModel().getColumnMargin();
        } else {
        	final TableColumn aColumn = table.getColumnModel().getColumn(vCol);
        	columnWidth = aColumn.getWidth();
        }
        return columnWidth;
    }
    

    
    @Override
    protected void grabXCoords () {
    	grabCellWidths ();
    }
    
    
    /*
     *	if timer isn't currently running, make a cache of current cell widths 
     */
    protected void grabCellWidths () {
    	
    	// Get current row value totals
    	rowValueTotals = calcRowValueTotals ();
    	resetHeaderSizes();
    		
		double[][] cellFromWidths = cellColumnPositions.getCellFromWidths();
		if (cellFromWidths == null || cellFromWidths.length < table.getModel().getRowCount()
				|| cellFromWidths[0].length < table.getModel().getColumnCount()) {
			cellColumnPositions.fitTo (table.getModel().getRowCount(), table.getModel().getColumnCount());
			cellFromWidths = cellColumnPositions.getCellFromWidths();
		}
		final double[][] cellFromXCoords = cellColumnPositions.getCellFromXCoords();
		final double[][] cellCurWidths = cellColumnPositions.getCellCurWidths();
		final double[][] cellCurXCoords = cellColumnPositions.getCellCurXCoords();
		
		final int viewRowCount = table.getRowCount();
		final int viewColCount = table.getColumnCount();
		final int colMargin = table.getColumnModel().getColumnMargin();
		
		for (int row = 0; row < viewRowCount; row++) {
			final int mRow = table.convertRowIndexToModel (row);
			float x = table.getColumnModel().getColumnMargin() / 2.0f;
			double valToPixelRatio = 0.0;
			
			final int widthAvailable = getAvailableWidth (row);
	    	valToPixelRatio = widthAvailable / rowValueTotals [row];
			
			for (int col = 0; col < viewColCount; col++) {
				final double width = getCellWidth (row, col, valToPixelRatio);
				final int mCol = table.convertColumnIndexToModel (col);
				cellFromWidths [mRow][mCol] = width - colMargin;
				cellFromXCoords [mRow][mCol] = x;
				cellCurWidths [mRow][mCol] = width - colMargin;
				cellCurXCoords [mRow][mCol] = x;
				x += width;
			}
		}
    }
    
    
    /**
     * Resets header widths to match the width of the items in the view's top row
     * 
     * Except...
     * Setting the header column preferred widths
     * affects the table column widths as the header TableColumnModel seems to be the
     * table's as well....
     */
    protected void resetHeaderSizes () {
    	final JTableHeader tHeader = table.getTableHeader();

    	if (table.getWidth() > 0) {
	    	final int widthAvailable = getAvailableWidth (0);
	    	double lastX = 0, curX = 0;
	    	
	    	for (int col = 0; col < table.getColumnCount(); col++) {	
	    		final double width = getCellWidth (0, col, widthAvailable / (rowValueTotals == null ? 1.0 : rowValueTotals[0]));
	    		
	    		final TableColumn tCol = tHeader.getColumnModel().getColumn(col);
	    		curX += width;
	    		int colWidth = (int)Math.round (curX - lastX);

	    		if (col == table.getColumnCount() - 1) {
	    			colWidth = (int)Math.round (table.getWidth() - lastX);
	    		}
	    		
	    		tCol.setPreferredWidth (colWidth);
	    		// Fix the column rigidly to stop JTable's doLayout() taking/assigning extra pixels here and there
	    		tCol.setMaxWidth (colWidth);
	    		tCol.setMinWidth (colWidth);
	    		//tCol.setWidth (colWidth);

	    		lastX = curX;
	    	}
    	}
    	
    	RepaintManager.currentManager(table).markCompletelyClean (table);
    }
    
    
    
    /**
     * Find the total values of all numeric data in any row
     * @return double maxValueTotal
     */
    protected double[] calcRowValueTotals () {
		final int viewRowCount = table.getRowCount();
		final int viewColCount = table.getColumnCount();
    	final double[] valueTotals = new double [viewRowCount];
		double max = 0.0;
		
		for (int row = 0; row < viewRowCount; row++) {
			final int mRow = table.convertRowIndexToModel (row);
			double valTotal = 0.0;
			
			for (int col = 0; col < viewColCount; col++) {
				final int mCol = table.convertColumnIndexToModel (col);
				final Object value = table.getModel().getValueAt (mRow, mCol);
   			 	if (value instanceof Number) {
   			 		final double doub = ((Number)value).doubleValue();
   			 		valTotal += doub;
   			 	} 
			}
			
			valueTotals [row] = valTotal;
			max = Math.max (max, valTotal);
		}
		
		if (isRelativeLayout()) {
			for (int n = 0; n < valueTotals.length; n++) {
				valueTotals [n] = max;
			}
		}
		
		return valueTotals;
    }
    
    
    /**
     * Returns the width of a row available to be shared proportionally between number values
     * @param viewRow - index of row in view
     * @return width of available row space
     */
    protected int getAvailableWidth (final int viewRow) {
    	int widthAvailable = table.getWidth();
    	final TableColumnModel tableColModel = table.getColumnModel();
    	int marginCount = 0;
    	
    	for (int column = 0; column < table.getColumnCount(); column++) {
			 final Object value = table.getValueAt (viewRow, column);
			 if (! (value instanceof Number)) {
				 final TableColumn aColumn = tableColModel.getColumn(column);
				 widthAvailable -= aColumn.getWidth ();
			 } else {
				 marginCount++;
			 }
		}
    	widthAvailable -= (marginCount * tableColModel.getColumnMargin());	// Column margins don't count in the share out
		return widthAvailable;
    }
    
    
    
    /**
     * Routine that works out column at a point
     * This is now dependent on which row the point also intersects
     * @param point - 2D coordinate of interest
     * @return the view column index of the column under the point
     */
    public int getColumnAtPoint (final Point point) {
    	final int viewRow = table.rowAtPoint (point);
    	final int modelRow = table.convertRowIndexToModel (viewRow);
    	double xTotal = 0;
    	int viewCol = 0;
    	
    	for (; viewCol < table.getColumnCount() && xTotal <= point.getX(); viewCol++) {
    		final int modelCol = table.convertColumnIndexToModel (viewCol);
    		xTotal += cellColumnPositions.getCellCurWidths() [modelRow][modelCol];
    	}
    	LOGGER.debug ("View col: "+viewCol);
    	
    	return viewCol - 1;
    }
    
    
    static class CellColumnPositions {
		double[][] cellFromWidths;
    	double[][] cellFromXCoords;
		double[][] cellCurWidths;
    	double[][] cellCurXCoords;
    	
    	public void fitTo (final int rowTotal, final int colTotal) {
    		//cellFromWidths = new double [rowTotal][colTotal];
    		//cellFromXCoords = new double [rowTotal][colTotal];
    		//cellCurWidths = new double [rowTotal][colTotal];
    		//cellCurXCoords = new double [rowTotal][colTotal];
    		
    		cellFromWidths = fit (cellFromWidths, rowTotal, colTotal);
    		cellFromXCoords = fit (cellFromXCoords, rowTotal, colTotal);
    		cellCurWidths = fit (cellCurWidths, rowTotal, colTotal);
    		cellCurXCoords = fit (cellCurXCoords, rowTotal, colTotal);
    	}
    	
    	protected double[][] fit (final double[][] array, final int rowTotal, final int colTotal) {
    		final double[][] newArray = new double [rowTotal][colTotal];
    		if (array != null) {
	    		for (int a = 0; a < array.length; a++) {
	    			System.arraycopy (array[a], 0, newArray[a], 0, array[a].length);
	    		}
    		}
    		return newArray;
    	}
    	
    	
    	public double[][] getCellFromWidths() {
			return cellFromWidths;
		}
    
		public double[][] getCellFromXCoords() {
			return cellFromXCoords;
		}
		
	   	public double[][] getCellCurWidths() {
			return cellCurWidths;
		}
    
		public double[][] getCellCurXCoords() {
			return cellCurXCoords;
		}
		
		
    	/**
    	 * Make the current coordinates the 'new' from coordinates
    	 */
    	public void pushCurrentCoords () {
    		System.arraycopy (cellCurXCoords, 0, cellFromXCoords, 0, cellFromXCoords.length);
    		System.arraycopy (cellCurWidths, 0, cellFromWidths, 0, cellFromWidths.length);
    	}
    }
}