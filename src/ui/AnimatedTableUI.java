package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.RowSorter;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import swingPlus.shared.CellRendererPane2;
import util.anim.AnimActionBase;
import util.anim.AnimTimer;
import util.anim.AnimTimerQueue;
import util.anim.RangeTransformer;
import util.anim.SineTransform;


public class AnimatedTableUI extends BasicTableUI {

	private final static Logger LOGGER = Logger.getLogger (AnimatedTableUI.class);
	
	
	PropertyChangeListener tablePropertyListener;
	
	boolean rowAnimationEnabled, columnAnimationEnabled;
	boolean disableAAWhenAnimating;
	
	Timer rowSortAnimTimer;
	RowSortAnim rowSortAnimator;
	RowSorterListener rowSortAnimListener;
	
	Timer columnSortAnimTimer;
	ColumnSortAnim columnSortAnimator;
	TableModelListener tableChangeListener;
	TableColumnModelListener columnModelListener;
	
	
	AnimTimerQueue timerQueue;
	
	RowColumnPositions rowPositions = new RowColumnPositions (0);
	RowColumnPositions columnPositions = new RowColumnPositions (0);
	
	Color sheen1 = new Color (255, 255, 255, 128);
	Color sheen2 = new Color (0, 0, 0, 48);
	
	CellRendererPane2 fastRenderPane;
	
	
	public static ComponentUI createUI (final JComponent comp) {
		return new AnimatedTableUI();
	}
	
	
	@Override
	protected void installListeners () {
		super.installListeners ();
		
		tablePropertyListener = new TablePropertyListener ();
		rowSortAnimListener = makeRowSorterListener ();
		tableChangeListener = makeTableModelListener ();
		columnModelListener = makeTableColumnModelListener ();
		
		table.addPropertyChangeListener (tablePropertyListener);
		
		// Fire off some faked PropertyChangeEvent objects that register the JTable's 
		// various models with the listeners in this UI class
		final PropertyChangeEvent[] pcEvents = {
				new PropertyChangeEvent (this, "rowSorter", null, table.getRowSorter()),
				new PropertyChangeEvent (this, "columnModel", null, table.getColumnModel()),
				new PropertyChangeEvent (this, "model", null, table.getModel())
		};
		
		for (PropertyChangeEvent pcEvent : pcEvents) {
			tablePropertyListener.propertyChange (pcEvent);
		}
	}
	
	
	@Override
	protected void uninstallListeners () {
		super.uninstallListeners ();
		
		if (table != null) {
			// Fire off some faked PropertyChangeEvent objects that de-register the JTable's 
			// various models with the listeners in this UI class
			final PropertyChangeEvent[] pcEvents = {
					new PropertyChangeEvent (this, "rowSorter", table.getRowSorter(), null),
					new PropertyChangeEvent (this, "columnModel", table.getColumnModel(), null),
					new PropertyChangeEvent (this, "model", table.getModel(), null)
			};
			for (PropertyChangeEvent pcEvent : pcEvents) {
				tablePropertyListener.propertyChange (pcEvent);
			}
			
			table.removePropertyChangeListener (tablePropertyListener);
		}
	}
	
	
	@Override
	protected void installDefaults () {
		super.installDefaults ();
		
		rowSortAnimator = new RowSortAnim (0.02, new SineTransform ());
		rowSortAnimTimer = new AnimTimer (20, rowSortAnimator);
		
		columnSortAnimator = new ColumnSortAnim (0.02, new SineTransform ());
		columnSortAnimTimer = new AnimTimer (20, columnSortAnimator);
		
		rowAnimationEnabled = true;
		columnAnimationEnabled = true;
		
		setDisableAAWhenAnimating (true);

		table.getTableHeader().setResizingAllowed (false);
		
		fastRenderPane = new CellRendererPane2 ();
		table.add (fastRenderPane);
		
		timerQueue = new AnimTimerQueue ();
		//table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
	}
	
	@Override
	protected void uninstallDefaults () {
		super.uninstallDefaults ();
		rowSortAnimTimer.stop();
		columnSortAnimTimer.stop();
		
		table.remove (fastRenderPane);
		timerQueue.clear ();
	}
	
	
	
	
	
	/**
	 * Subclasses can override to produce a different action to receiving row sorting events
	 * @return RowSorterListener object
	 */
	protected RowSorterListener makeRowSorterListener () {
		return new RowSortAnimListener ();
	}
	
	
	/**
	 * Subclasses can override to produce a different action to receiving table model events
	 * @return TableModelListener object
	 */
	protected TableModelListener makeTableModelListener () {
		return new ColumnSortAnimListener ();
	}
	
	
	/**
	 * Subclasses can override to produce a different action to receiving table column model events
	 * @return TableColumnModelListener object
	 */
	protected TableColumnModelListener makeTableColumnModelListener () {
		return new ColumnModelListener ();
	}
	
	
	
	
	
	public void setRowAnimationEnabled (final boolean enable) {
		rowAnimationEnabled = enable;
	}
	
	public void setColumnAnimationEnabled (final boolean enable) {
		columnAnimationEnabled = enable;
	}
	
	
	public boolean isRowAnimationEnabled () {
		return rowAnimationEnabled;
	}
	
	public boolean isColumnAnimationEnabled () {
		return columnAnimationEnabled;
	}
	
	
	
	public void setDisableAAWhenAnimating (final boolean enable) {
		disableAAWhenAnimating = enable;
	}
	
	public boolean isDisableAAWhenAnimating () {
		return disableAAWhenAnimating;
	}
	
	
	
	protected class TablePropertyListener implements PropertyChangeListener {
		@Override
		public void propertyChange (final PropertyChangeEvent propEvent) {
			
			// The "de-register/registering the JTable" chicanery is
			// needed so the UI's Listeners are called after the 
			// JTable's corresponding Listeners (TableModel/ColumnModel etc
			// call listeners in reverse registered order).
			// In the UI's Listeners
			// we can then mark the JTable as 'clean', thus squashing
			// any repaint events called in the JTable's Listeners
			// which would flicker the final layout in before the
			// animation process starts. Good eh?
			
			if (propEvent != null) {
				final String propertyName = propEvent.getPropertyName();

				if ("rowSorter".equals (propertyName)) {
					final RowSorter<? extends TableModel> oldModel = (RowSorter<? extends TableModel>)propEvent.getOldValue();
					if (oldModel != null) {
						oldModel.removeRowSorterListener (rowSortAnimListener);
					}
					final RowSorter<? extends TableModel> newModel = (RowSorter<? extends TableModel>)propEvent.getNewValue();
					if (newModel != null) {
						newModel.removeRowSorterListener (table);
						newModel.addRowSorterListener (rowSortAnimListener);
						newModel.addRowSorterListener (table);
					}
				}	
				
				else if ("columnModel".equals (propertyName)) {
					final TableColumnModel oldModel = (TableColumnModel)propEvent.getOldValue();
					if (oldModel != null) {
						oldModel.removeColumnModelListener (columnModelListener);
					}
					final TableColumnModel newModel = (TableColumnModel)propEvent.getNewValue();
					if (newModel != null) {
						newModel.removeColumnModelListener (table);
						newModel.addColumnModelListener (columnModelListener);
						newModel.addColumnModelListener (table);
					}
				}
				
				else if ("model".equals (propertyName)) {
					final TableModel oldModel = (TableModel)propEvent.getOldValue();
					if (oldModel != null) {
						oldModel.removeTableModelListener (tableChangeListener);
					}
					final TableModel newModel = (TableModel)propEvent.getNewValue();
					if (newModel != null) {
						newModel.removeTableModelListener (table);
						newModel.addTableModelListener (tableChangeListener);
						newModel.addTableModelListener (table);
					}
				}
			}
		}
	}
	
	
	
	class RowSortAnim extends AnimActionBase {

    	/**
		 * 
		 */
		private static final long serialVersionUID = 5718087044458098875L;
		double per;
		
    	RowSortAnim (final double stepVal, final RangeTransformer newrt) {
    		super (stepVal, newrt);
    	}

        @Override
		public void doWork (final ActionEvent evt) {
        	per = this.getPercentTransition (MOVE) / 100.0;
        	table.repaint ();
        }
        
        @Override
        public void animationFinished () {
        	grabYCoords ();
        }
        
        @Override
        public void cleanUp () {
        	super.cleanUp ();
        	// do final repaint after startCalled is set to false in super.cleanUp ()
        	// this means this repaint doesn't have AA automatically turned off
        	table.repaint (); 
        }
        
        public void animCellRect (final Rectangle cellRect, final int row) {
        	final int[] rowYFromCoords = rowPositions.getFromCoords();
        	int[] rowYCurrentCoords = rowPositions.getCurrentCoords();
        	if (rowYFromCoords != null && rowYFromCoords.length > 0) {
	        	final int modelRow = table.convertRowIndexToModel (row);
	        	final int yFrom = modelRow >= rowYFromCoords.length ? 0 : rowYFromCoords [modelRow];
	        	final int yAnim = yFrom + (int)((double)(cellRect.getY() - yFrom) * per);
	        	cellRect.y = yAnim;
	        	if (modelRow < rowYCurrentCoords.length) {
	        		rowYCurrentCoords [modelRow] = yAnim;
	        	}
        	}
        }
    }

	protected class RowSortAnimListener implements RowSorterListener {

		@Override
		public void sorterChanged (final RowSorterEvent rse) {
			LOGGER.debug ("Event: row sorter");
			startRowAnim ();
		}
	}
	
	
	
	
	
	class ColumnSortAnim extends AnimActionBase {

    	/**
		 * 
		 */
		private static final long serialVersionUID = 5718087044458098875L;
		double per;
		
    	ColumnSortAnim (final double stepVal, final RangeTransformer newrt) {
    		super (stepVal, newrt);
    	}

        @Override
		public void doWork (final ActionEvent evt) {
        	per = this.getPercentTransition (MOVE) / 100.0;
        	table.repaint ();
        }     
        
        @Override
        public void animationFinished () {
        	grabXCoords ();
        }
        
        @Override
        public void cleanUp () {
        	super.cleanUp ();
        	// do final repaint after startCalled is set to false in super.cleanUp ()
        	// this means this repaint doesn't have AA automatically turned off
        	table.repaint (); 
        }
        
        public void animCellRect (final Rectangle cellRect, final int vRow, final int vCol) {     
        	final int[] columnXFromCoords = columnPositions.getFromCoords();
        	int[] columnXCurrentCoords = columnPositions.getCurrentCoords();
        	if (columnXFromCoords != null) {
        		final int modelCol = table.convertColumnIndexToModel (vCol);
        		if (columnXFromCoords.length > modelCol) {
		        	final int xFrom = columnXFromCoords [modelCol];
		        	final int xAnim = xFrom + (int)((double)(cellRect.x - xFrom) * per);
		        	cellRect.x = xAnim;
		        	columnXCurrentCoords [modelCol] = xAnim;
        		}
        	}
        }
    }
	

	
	protected class ColumnSortAnimListener implements TableModelListener {

		@Override
		public void tableChanged (final TableModelEvent tme) {
			LOGGER.debug ("Event: table changed");
			startColumnAnim ();
		}
	}
	
	
	protected class ColumnModelListener implements TableColumnModelListener {

		@Override
		public void columnAdded (final TableColumnModelEvent tcmEvent) {
			LOGGER.debug ("Event: column added");
			startColumnAnim ();
		}

		@Override
		public void columnRemoved (final TableColumnModelEvent tcmEvent) {
			LOGGER.debug ("Event: column removed");
			startColumnAnim ();
		}

		@Override
		public void columnMoved (final TableColumnModelEvent tcmEvent) {
			//pushCoords();
			// EMPTY	
		}

		@Override
		public void columnMarginChanged (final ChangeEvent cEvent) {
			//System.err.println ("-- column margin event -- "+cEvent.toString());
			if (columnAnimationEnabled) {
				RepaintManager.currentManager(table).markCompletelyClean (table);
			}
		}
	

		@Override
		public void columnSelectionChanged (final ListSelectionEvent lsEvent) {
			// EMPTY
		}	
	}
	
	
	
	protected void startRowAnim () {
		if (rowAnimationEnabled) {
			if (rowSortAnimator.isStartCalled() || columnSortAnimator.isStartCalled()) {
				// If animation already running, make current positions the old positions before restarting
				rowPositions.pushCurrentCoords();
			} else if (rowPositions.getFromCoords() == null) {
				grabYCoords ();
				grabXCoords ();
			}
			
			restartAnimTimer (rowSortAnimTimer);
		}
	}
	
	
	protected void startColumnAnim () {
		if (columnAnimationEnabled) {
			if (columnSortAnimator.isStartCalled() || rowSortAnimator.isStartCalled()) {
				// If animation already running, make current positions the old positions before restarting
				LOGGER.debug ("Already animating");
				columnPositions.pushCurrentCoords();
			} else if (columnPositions.getFromCoords() == null) {
				grabYCoords ();
				grabXCoords ();
			}
			
			restartAnimTimer (columnSortAnimTimer);
		}
	}
	
	
	protected void restartAnimTimer (final Timer animTimer) {
		//timerQueue.offer ((AnimTimer)animTimer);
		//System.err.println ("is start called: "+rowSortAnimator.getStartCalled());
		animTimer.restart ();
		RepaintManager.currentManager(table).markCompletelyClean (table);
	}
	

	
	
	 /** Paint a representation of the <code>table</code> instance
     * that was set in installUI().
     */
    public void paint (final Graphics graphics, final JComponent comp) {
    	
    	//System.err.println ("\n*** paint *** @ "+System.currentTimeMillis()+"\n");
    	final Rectangle clip = graphics.getClipBounds();
    	final Rectangle bounds = table.getBounds();
        // account for the fact that the graphics has already been translated
        // into the table's bounds
        bounds.x = bounds.y = 0;

		if (table.getRowCount() <= 0 || table.getColumnCount() <= 0 ||
	                // this check prevents us from painting the entire table
	                // when the clip doesn't intersect our bounds at all
	                !bounds.intersects(clip)) {
	
	           // paintDropLines(g);
		    return;
		}

		
		final boolean ltr = table.getComponentOrientation().isLeftToRight();

		Point upperLeft = clip.getLocation();
        if (!ltr) {
            upperLeft.x++;
        }

        final Point lowerRight = new Point(clip.x + clip.width - (ltr ? 1 : 0),
                                     clip.y + clip.height);

        int rMin = table.rowAtPoint (upperLeft);
        int rMax = table.rowAtPoint (lowerRight);
        // This should never happen (as long as our bounds intersect the clip,
        // which is why we bail above if that is the case).
        if (rMin == -1) {
        	rMin = 0;
        }
        // If the table does not have enough rows to fill the view we'll get -1.
        // (We could also get -1 if our bounds don't intersect the clip,
        // which is why we bail above if that is the case).
        // Replace this with the index of the last row.
        if (rMax == -1) {
        	rMax = table.getRowCount() - 1;
        }

        int cMin = 0; //table.columnAtPoint(ltr ? upperLeft : lowerRight); 
        int cMax = table.getColumnModel().getColumnCount() - 1; //table.columnAtPoint(ltr ? lowerRight : upperLeft);        
        // This should never happen.
        if (cMin == -1) {
        	cMin = 0;
        }
        // If the table does not have enough columns to fill the view we'll get -1.
        // Replace this with the index of the last column.
        if (cMax == -1) {
        	cMax = table.getColumnCount() - 1;
        }

        final Graphics2D g2d = (Graphics2D)graphics;
        final boolean isAnimating = rowSortAnimator.isStartCalled() || columnSortAnimator.isStartCalled ();
	    final Object staticAARenderingHint = g2d.getRenderingHint (RenderingHints.KEY_ANTIALIASING);
	    final Object staticTextAARenderingHint = g2d.getRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING);

	    if (isDisableAAWhenAnimating () && isAnimating) {
		    g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, 
		    		RenderingHints.VALUE_ANTIALIAS_OFF);
		    g2d.setRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING, 
		    		RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
        
	    if (LOGGER.isDebugEnabled()) {
	    	LOGGER.debug ("repaint: isAnim: "+isAnimating+", disWhenAA: "+isDisableAAWhenAnimating ()+", rhint: "+g2d.getRenderingHint (RenderingHints.KEY_ANTIALIASING));
	    }
	    // Paint the grid.
        paintGrid (graphics, rMin, rMax, cMin, cMax);

        // Paint the cells.
        paintCells (graphics, rMin, rMax, cMin, cMax);
        
        if (!rowSortAnimator.isStartCalled()) {
        	grabYCoords ();
        }
        
        if (!columnSortAnimator.isStartCalled()) {
        	grabXCoords ();
        }
        
        if (isDisableAAWhenAnimating () && isAnimating) {
        	g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, staticAARenderingHint); 
        	g2d.setRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING, staticTextAARenderingHint); 
        }
    }
    
    /*
     * Paints the grid lines within <I>aRect</I>, using the grid
     * color set with <I>setGridColor</I>. Paints vertical lines
     * if <code>getShowVerticalLines()</code> returns true and paints
     * horizontal lines if <code>getShowHorizontalLines()</code>
     * returns true.
     */
    private void paintGrid (final Graphics graphics, final int rMin, final int rMax, final int cMin, final int cMax) {
        graphics.setColor(table.getGridColor());

        final Rectangle minCell = table.getCellRect(rMin, cMin, true);
        final Rectangle maxCell = table.getCellRect(rMax, cMax, true);
        final Rectangle damagedArea = minCell.union (maxCell);
        final Rectangle cellRect = new Rectangle ();
        
        if (table.getShowHorizontalLines()) {
	        final int tableWidth = damagedArea.x + damagedArea.width;
		    int y = damagedArea.y;
	    
		    for (int row = rMin; row <= rMax; row++) {
		    	cellRect.y = y;
		    	if (rowSortAnimator.isStartCalled()) {
		    		rowSortAnimator.animCellRect (cellRect, row);
		    	} 
	    		y = (int)cellRect.y;
				y += table.getRowHeight(row);
				graphics.drawLine(damagedArea.x, y - 1, tableWidth - 1, y - 1);
		    }
		}
        if (table.getShowVerticalLines()) {
	        final TableColumnModel tableColModel = table.getColumnModel();
	        final int tableHeight = damagedArea.y + damagedArea.height;
		    int x;
		    final boolean ltr = table.getComponentOrientation().isLeftToRight();
		    final int cFirst = ltr ? cMin : cMax;
		    final int cLast = ltr ? cMax + 1 : cMin - 1;
		    final int cInc = ltr ? 1 : -1;
		    
			x = damagedArea.x;
			for (int column = cFirst; column != cLast; column += cInc) {
				cellRect.x = x;
				if (columnSortAnimator.isStartCalled()) {
		    		columnSortAnimator.animCellRect (cellRect, 0, column);
		    	} 
				x = (int)cellRect.x;
				
				final int colWidth = tableColModel.getColumn(column).getWidth();
			    x += colWidth;
			    graphics.drawLine(x - 1, 0, x - 1, tableHeight - 1);
			}
		}
    }
    
    

    protected void paintCells (final Graphics graphics, final int rMin, final int rMax, final int cMin, final int cMax) {
    	final JTableHeader header = table.getTableHeader();
    	final TableColumn draggedColumn = (header == null) ? null : header.getDraggedColumn();
	
    	final TableColumnModel tableColModel = table.getColumnModel();
    	//final int columnMargin = tableColModel.getColumnMargin();
	
    	//LOGGER.debug ("cmin: "+cMin+", cmax: "+cMax);
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
	    	
	    	if (!ltr) {
                aColumn = tableColModel.getColumn (cMin);
                if (aColumn != draggedColumn) {
                    columnWidth = aColumn.getWidth();
                    getCellBounds (cellRect, row, cMin);
	                if (columnSortAnimator.isStartCalled()) {
                    	columnSortAnimator.animCellRect (cellRect, row, cMin);
                    }
                    paintCell(graphics, cellRect, row, cMin);
                }
	    	}
	    
		    for (int column = cFirst; column <= cMax; column++) {
	          	aColumn = tableColModel.getColumn (column);
	          	columnWidth = aColumn.getWidth();
	          	getCellBounds (cellRect, row, column);
	
	          	if (!ltr) {
	          		cellRect.x -= columnWidth;
	          	}
	          	
	            if (columnSortAnimator.isStartCalled()) {
	            	columnSortAnimator.animCellRect (cellRect, row, column);
	            }
	              
	            paintCell (graphics, cellRect, row, column);
	              
	            if (aColumn == draggedColumn) {
	                drawSheen ((Graphics2D)graphics, cellRect);
	            }
	
	            if (ltr) {	            
	            	cellRect.x += columnWidth;
	            }
		    }
	    }
	    
	    fastRenderPane.removeAll ();
    }
    
    
    protected void paintCell (final Graphics graphics, final Rectangle cellRect, final int row, final int column) {
        if (table.isEditing() && table.getEditingRow( )== row &&
                                 table.getEditingColumn() == column) {
        	final Component component = table.getEditorComponent();
            component.setBounds(cellRect);
            component.validate();
        }
        else {
        	if (row < table.getRowCount() && column < table.getColumnCount()) {
	        	final TableCellRenderer renderer = table.getCellRenderer(row, column);
	        	final Component component = table.prepareRenderer(renderer, row, column);
	            //rendererPane.paintComponent(graphics, component, table, cellRect.x, cellRect.y,
                //        cellRect.width, cellRect.height, true);
	        	fastRenderPane.paintComponent(graphics, component, table, cellRect.x, cellRect.y,
	                                        cellRect.width, cellRect.height, false);
        	}
        }
    }
    
    protected void drawSheen (final Graphics2D graphics2D, final Rectangle cellRect) {
    	 final GradientPaint gradient = new GradientPaint (
    			 cellRect.x, cellRect.y, sheen1, 
    			 (int)cellRect.getMaxX(), (int)cellRect.getMaxY(), sheen2,
                 false); 
    	 final Paint oldPaint = graphics2D.getPaint();
    	 graphics2D.setPaint (gradient);
    	 graphics2D.fill (cellRect);
    	 graphics2D.setPaint (oldPaint);
    }
    
    
    protected void grabYCoords () {
		// if row anim timer isn't current running, make a cache of current row y positions
    	
		rowPositions.resize (table.getModel().getRowCount());
		final int viewRowCount = table.getRowCount();
		final int[] rowCoords = rowPositions.getFromCoords();
		final int[] rowCurCoords = rowPositions.getCurrentCoords();
		
		for (int row = 0, y = table.getRowMargin() / 2; row < viewRowCount; row++) {
			final int modelIndex = table.convertRowIndexToModel (row);
			rowCoords [modelIndex] = y;
			rowCurCoords [modelIndex] = y;
			y += table.getRowHeight (row);
		}
    }
    
    
    protected void grabXCoords () {
		// if column anim timer isn't current running, make a cache of current column x positions
		
    	columnPositions.resize (table.getModel().getColumnCount());
		final int viewColumnCount = table.getColumnCount();
		final int[] columnCoords = columnPositions.getFromCoords();
		final int[] columnCurCoords = columnPositions.getCurrentCoords();
		//System.err.println ("mcount: "+table.getModel().getColumnCount()+", vcount: "+viewColumnCount+", ccl: "+columnCoords.length);
		
		for (int col = 0, x = table.getColumnModel().getColumnMargin() / 2; col < viewColumnCount; col++) {
			final int modelIndex = table.convertColumnIndexToModel (col);
			//System.err.println ("colIndex: "+col+", modelIndex: "+modelIndex);
			if (modelIndex < columnCoords.length) {
				columnCoords [modelIndex] = x;
				columnCurCoords [modelIndex] = x;
				x += table.getColumnModel().getColumn(col).getWidth();
			}
		}
    }
    
    
    protected void getCellBounds (final Rectangle cellRect, final int vRow, final int vCol) {
    	 final TableColumn aColumn = table.getColumnModel().getColumn(vCol);
         final int columnWidth = aColumn.getWidth() - table.getColumnModel().getColumnMargin();
         cellRect.width = columnWidth;
    }
    
    
    
    
    static class RowColumnPositions {
    	int[] fromCoords;
    	int[] curCoords;
    	
    	public RowColumnPositions (final int size) {
    		setSize (size);
    	}
    	
    	public void resize (final int newSize) {
    		if (fromCoords == null || newSize > fromCoords.length) {
    			setSize (newSize);
    		}
    	}
    	
    	public void setSize (final int size) {
    		if (fromCoords == null) {
    			fromCoords = new int [size];
    		} else {
    			fromCoords = Arrays.copyOf (fromCoords, size);
    		}
    		
    		if (curCoords == null) {
    			curCoords = new int [size];
    		} else {
    			curCoords = Arrays.copyOf (curCoords, size);
    		}
    	}
    	
    	public int[] getFromCoords () {
    		return fromCoords;
    	}
    	
    	public int[] getCurrentCoords () {
    		return curCoords;
    	}
    	
    	/**
    	 * Make the current coordinates the 'new' from coordinates
    	 */
    	public void pushCurrentCoords () {
    		System.arraycopy (curCoords, 0, fromCoords, 0, fromCoords.length);
    	}
    }
}
