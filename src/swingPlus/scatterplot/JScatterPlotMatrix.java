package swingPlus.scatterplot;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.plaf.TableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import model.parcoord.ParCoordRowSorter;

import org.apache.log4j.Logger;

import sun.swing.table.DefaultTableCellHeaderRenderer;
import swingPlus.parcoord.JParCoord;
import ui.ScatterPlotMatrixUI;

public class JScatterPlotMatrix extends JParCoord {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3502022512346716868L;
	private final static Logger LOGGER = Logger.getLogger (JScatterPlotMatrix.class);

	private final static String UICLASSID = "ScatterPlotMatrixUI";
	static {
		UIManager.put (UICLASSID, "ui.ScatterPlotMatrixUI");
	}
	
	
	
	public JScatterPlotMatrix () {
		this (null, null);
	}
	
	public JScatterPlotMatrix (final TableModel tModel) {
		this (tModel, null);
	}
	
	public JScatterPlotMatrix (final TableModel tModel, final TableColumnModel cModel) {
		super (tModel, cModel);
		this.setDoubleBuffered (true);
	}
	
	@Override
    public void updateUI() {
		setUI((TableUI)UIManager.getUI(this));
    }

	@Override
	public ScatterPlotMatrixUI getUI() {
        return (ScatterPlotMatrixUI)ui;
    }

    @Override
    public String getUIClassID() {
        return UICLASSID;
    }
    
    @Override
    public void setTableHeader (final JTableHeader tableHeader) {
    	super.setTableHeader (tableHeader);
    	// Stops arrows being drawn. Superclass JParCoord uses DefaultParCoordCellHeaderRenderer 
    	// that interrogates SortedColumn objects for whether to draw arrows or not.
    	// We don't do sorting by column in the scatterplot matrix but we still have SortedColumn objects.
    	getTableHeader().setDefaultRenderer (new DefaultTableCellHeaderRenderer ());
    	// Adds a listener for when a column resize operation has finished
    	getTableHeader().addMouseListener (new ColumnDragOpFinishedListener ());
    }
    
    
    
    @Override
    public void setAutoCreateRowSorter (final boolean autoCreateRowSorter) {
    	final boolean oldValue = this.getAutoCreateRowSorter ();
    	super.setAutoCreateRowSorter (autoCreateRowSorter);
        
        if (autoCreateRowSorter) {
            setRowSorter (new UnsortableSorter (getModel(), this.getColumnModel()));
        }
        firePropertyChange("autoCreateRowSorter", oldValue,
                           autoCreateRowSorter);
    }
    
    
    
    public Dimension getPlotUnderPoint (final Point point) {
    	return getPlotUnderPoint (point.x, point.y);
    }
    
    public Dimension getPlotUnderPoint (final int x, final int y) {
    	return getUI().getPlotUnderPoint (x, y);
    }
    
    @Override
    /**
     * Track changes to table cell selections
     */
    public void valueChanged (final ListSelectionEvent event) {
    	super.valueChanged (event);
    	final boolean isAdjusting = event.getValueIsAdjusting();
        if (!isAdjusting && getRowCount() > 0 && getColumnCount() > 0) {
        	setRedrawSelections (true);
        	this.getUI().resetIncrementalDrawing ();
        	repaint();
        }
    }
    
    
    
    /**
     * Routines that try to limit the amount of redrawing during and after a column drag,
     * for either a column move or a column resize.
     * Firstly, we don't update the scatterplot matrix as we are dragging, by not calling
     * repaints in the columnMoved and columnMarginChanged listener methods.
     * Secondly, at the finish we need to draw everything to the right of and including
     * the leftmost column affected by the drag event but not the columns to the left.
     * This is calculated and a repaint called by the ColumnDragOpFinishedListener class.
     * @author Martin
     *
     */
    
    
    /**
     * Invoked when a column is repositioned. If a cell is being
     * edited, then editing is stopped and the cell is redrawn.
     * Application code will not use these methods explicitly, they
     * are used internally by JTable.
     *
     * @param event   the event received
     * @see TableColumnModelListener
     */
    @Override
	public void columnMoved (final TableColumnModelEvent event) {
        // If I'm currently editing, then I should stop editing
        if (isEditing()) {
            removeEditor();
        }
        
        /*
        if (event.getFromIndex() != event.getToIndex()) {
        	int minCol = Math.min (event.getFromIndex(), event.getToIndex());
        	int clipx = calcColX (minCol);
        	setRedrawAll (true);
        	repaint (clipx, 0, getSize().width - clipx, getSize().height);
        }
        */
    }
    
    @Override
	public void columnMarginChanged (final ChangeEvent event) {
    	if (isEditing()) {
            removeEditor();
        }
    	final TableColumn resizingColumn = tableHeader == null ? null : tableHeader.getResizingColumn();
		// Need to do this here, before the parent's
		// layout manager calls getPreferredSize().
		if (resizingColumn != null && autoResizeMode == AUTO_RESIZE_OFF) {
		    resizingColumn.setPreferredWidth(resizingColumn.getWidth());
		}
		revalidate();
    }
    
    
    int calcColX (final int columnViewIndex) {
    	final TableColumnModel tcm = getColumnModel();
    	int x = 0;
		for (int col = 0; col < columnViewIndex; col++) {
			final int colWidth = tcm.getColumn(col).getWidth();
			x += colWidth;
		}
		return x;
    }
    

    class ColumnDragOpFinishedListener extends MouseAdapter {
    	
    	int startMinx = 0;
    	int withinColumnXOffset = 0;
    	

    	@Override
    	public void mousePressed (final MouseEvent event) {
    		startMinx = calcMinColXforLocation (event.getX());
    		withinColumnXOffset = event.getX() - startMinx;
    	}
    	
    	
    	@Override
		public void mouseReleased (final MouseEvent event) {
    		final Dimension size = JScatterPlotMatrix.this.getSize();
    		final int endMinx = calcMinColXforLocation (event.getX() - withinColumnXOffset);
    		final int clipx = Math.max (0, Math.min (startMinx, endMinx));
    		setRedrawAll (true);
    		repaint (clipx, 0, size.width - clipx, size.height);
    	}
    	
    	
    	int calcMinColXforLocation (final int eventX) {
    		final TableColumnModel tcm = JScatterPlotMatrix.this.getColumnModel();

    		int minx = 0;
    		if (eventX >= 0) {
	    		for (int col = 0; col < tcm.getColumnCount(); col++) {
	    			final int colWidth = tcm.getColumn(col).getWidth();
	    			if (eventX >= minx && eventX < minx + colWidth) {
	    				break;
	    			}
	    			minx += colWidth;
	    		}
    		}
    		return minx;
    	}
    }
    
    // Class that stops unnecessary sorting in the scatterplot matrix
    // i.e. stops sorting on clicking a header
    static class UnsortableSorter<M extends TableModel> extends ParCoordRowSorter<M> {
    	
    	public UnsortableSorter (final M model, final TableColumnModel columnModel) {
    		super (model, columnModel);
    	}
    	
    	@Override
    	public void toggleSortOrder (final int column) {
    		// EMPTY
    	}
    	
    	@Override
    	public boolean isSortable (final int column) {
    		return false;
    	}
    }
}
