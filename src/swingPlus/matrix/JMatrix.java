package swingPlus.matrix;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.plaf.TableUI;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import model.graph.Edge;
import model.matrix.AbstractMatrixTableModel;
import model.matrix.MatrixTableColumnModel;
import model.matrix.DefaultMatrixTableModel;
import model.matrix.NodeMetrics;
import model.shared.EdgeComparator;

import swingPlus.shared.AbstractColumnsSorter;
import swingPlus.shared.JBasicMatrix;
import swingPlus.shared.ColumnSortableTable;
import swingPlus.shared.SortWidget;
import swingPlus.shared.border.OrthogonalTitlesBorder;
import swingPlus.shared.tooltip.AbstractRendererToolTip;

import ui.MatrixUI;

import util.GraphicsUtil;
import util.Messages;



public class JMatrix extends JBasicMatrix implements ColumnSortableTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4095432469174098433L;
	
	private final static Logger LOGGER = Logger.getLogger (JMatrix.class);
	private final static String UI_CLASS_ID = "MatrixUI";
	static {
		UIManager.put (UI_CLASS_ID, "ui.MatrixUI");
	}
	
	static public final int ROWS = 0, COLUMNS = 1;
	private final static Font CELL_FONT = Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "MatrixUI.cellFont"));
	private final Border lineBorder = BorderFactory.createLineBorder (Color.black, 3);
	//private final Border titleBorder = BorderFactory.createTitledBorder (lineBorder, "ToolTip");
	private final Border titleBorder = new OrthogonalTitlesBorder (lineBorder, "ToolTip");

	
	NullRenderer nullRenderer = new NullRenderer ();
	NullEdgeObjectRenderer nullEdgeObjRenderer = new NullEdgeObjectRenderer ();
	
	Map<String, TableColumn> restoreColumnCache;
	ListSelectionModel brushSelection;
	
	SortWidget sortRowsButton, sortColumnsButton;
	NodeMetrics rowMetrics, columnMetrics;
	
	ColumnsSorter colSort;
	ColumnModelToViewIndex colModelViewIndex;// = new ColumnModelToViewIndex ();
	
	
	
	public JMatrix () {
		this (null);
	}
	
	public JMatrix (final TableModel tableModel) {
		super (tableModel);
		brushSelection = new DefaultListSelectionModel ();
		setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
		setFillsViewportHeight (true);
		this.setCellSelectionEnabled (true);
		this.setAutoCreateRowSorter (true);
				
		setRowHeader (new JMatrixRowHeader ());
		setColumnHeader (new JMatrixColumnHeader ());
		
		colSort = new ColumnsSorter (this);
		
		sortRowsButton = new SortWidget ();
		sortRowsButton.setToolTipText (Messages.getString ("rowSortButtonTooltipText"));
    	sortRowsButton.addListSelectionListener (new DefaultRowsSelectionListener ());
    	sortColumnsButton = new SortWidget ();
		sortColumnsButton.setToolTipText (Messages.getString ("columnSortButtonTooltipText"));
    	sortColumnsButton.addListSelectionListener (new DefaultColumnsSelectionListener ());
		
    	this.setFont (CELL_FONT);
    	initModelDependentSettings ();

    	final AbstractRendererToolTip niceToolTip = new MatrixRendererToolTip (this);
    	setRendererToolTip (niceToolTip);
    	niceToolTip.setBorder (titleBorder);
	}
	
	

	
	@Override
    public void updateUI() {
    	setUI((TableUI)UIManager.getUI(this));
    }

    @Override
	public MatrixUI getUI() {
        return (MatrixUI)ui;
    }

    @Override
    public String getUIClassID() {
        return UI_CLASS_ID;
    }
    
    
    
    
    
    protected void initModelDependentSettings () {
    	if (getModel() instanceof DefaultMatrixTableModel) {
    		final DefaultMatrixTableModel mtm = (DefaultMatrixTableModel)getModel();
	   	
	    	sortRowsButton.setSortListModel (mtm.getRowMetrics().getComparatorList()); 	
	    	sortColumnsButton.setSortListModel (mtm.getColumnMetrics().getComparatorList());
    	
	    	((ColumnsSorter)this.getColumnsSorter()).sort (mtm.getColumnMetrics());

			if (this.getRowSorter() instanceof TableRowSorter) {
				final TableRowSorter<? extends TableModel> trs = (TableRowSorter<? extends TableModel>)this.getRowSorter();
				// removes 1st row (i.e. row of column header names)
				trs.setRowFilter (mtm.getRowFilter());
				
				trs.setSortable (0, true);
				trs.setComparator (0, mtm.getRowMetrics());
				final List <RowSorter.SortKey> sortKeys 
			    	= new ArrayList<RowSorter.SortKey>();
				sortKeys.add (new RowSorter.SortKey (0, SortOrder.ASCENDING));
				trs.setSortKeys (sortKeys);
					
				attachComparatorsToColumns ();
			}
    	}
    }

    
    void attachComparatorsToColumns () {
		if (this.getRowSorter() instanceof TableRowSorter) {
			final TableRowSorter<? extends TableModel> trs = (TableRowSorter<? extends TableModel>)this.getRowSorter();
			final EdgeComparator oComp = EdgeComparator.getInstance();
			for (int n = this.getColumnCount(); --n > 0;) {
				trs.setComparator (n, oComp);
			}
			trs.setComparator (0, ((DefaultMatrixTableModel)getModel()).getRowMetrics());
		}
    }
	

    /**
     * Returns the default table header object, which is
     * a <code>JTableHeader</code>.  A subclass can override this
     * method to return a different table header object.
     *
     * @return the default table header object
     * @see javax.swing.table.JTableHeader
     */
	//@Override
    //protected JTableHeader createDefaultTableHeader() {
    //    return new JScaledTableHeader (this.getColumnModel());
    //}
    
    @Override
    public void setModel (final TableModel dataModel) {
    	LOGGER.debug ("model set");
    	super.setModel (dataModel);	
	
		if (getComponentScaler() != null) {
			// update model dependent settings of other models/components if
			// they have been initialised. If not, we are in a constructor which
			// calls this routine at the end of the constructor. Due to super (TableModel tm)
			// not allowing variables to be set up first
			initModelDependentSettings ();
		}
    }
     
  
    public SortWidget getSortButton (final int rowsOrColumns) {
    	if (rowsOrColumns == ROWS) {
    		return sortRowsButton;
    	}
    	else if (rowsOrColumns == COLUMNS) {
    		return sortColumnsButton;
    	}
    	return null;
    }
    
  
    
    @Override
    public void setRowHeight (final int row, final int rowHeight) {
    	super.setRowHeight (row, rowHeight);
    	if (getRowHeader() != null) {
    		getRowHeader().setRowHeight (row, rowHeight);
    	}
    }
    
     
    public AbstractColumnsSorter getColumnsSorter () { return colSort; }
    
    /**
     * Creates default columns for the table from
     * the data model using the <code>getColumnCount</code> method
     * defined in the <code>TableModel</code> interface.
     * <BARLEY_PATTERN>
     * Clears any existing columns before creating the
     * new columns based on information from the model.
     *
     * @see     #getAutoCreateColumnsFromModel
     */
    @Override
	public void createDefaultColumnsFromModel() {
    
    	final TableModel tableModel = getModel();
        if (tableModel != null) {

            // Remove any current columns
        	final TableColumnModel columnModel = getColumnModel();
            columnModel.removeColumn (null);	// If using MatrixTableColumnModel, this will remove every column at once
            while (columnModel.getColumnCount() > 0) {
                columnModel.removeColumn (columnModel.getColumn(0));
            }
            
            // Create new columns from the data model info
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
            	final TableColumn newColumn = new TableColumn (col);
                addColumn (newColumn);
            }

        }
        
     // Hide the first column, which is the row list
		this.getColumnModel().removeColumn (this.getColumnModel().getColumn(0)); 
    }
    
    
    @Override
	protected TableColumnModel createDefaultColumnModel() {
        return new MatrixTableColumnModel ();
    }
    


    
    @Override
	protected void configureEnclosingScrollPane() {
    	
    	super.configureEnclosingScrollPane();
    	final Container parent = getParent();
 
    	if (parent instanceof JViewport) {
        	final Container grandParent = parent.getParent();
            
        	if (grandParent instanceof JScrollPane) {
        		final JScrollPane scrollPane = (JScrollPane)grandParent;
                scrollPane.setCorner (ScrollPaneConstants.LOWER_LEFT_CORNER, sortRowsButton);
                scrollPane.setCorner (ScrollPaneConstants.UPPER_RIGHT_CORNER, sortColumnsButton);
                scrollPane.setCorner (ScrollPaneConstants.UPPER_LEFT_CORNER, new MatrixSwapAxesButton (this));
            }
        }   
    }
    
  

    
      
    @Override
    public TableCellRenderer getCellRenderer (final int row, final int column) {
    	final Object obj = getValueAt (row, column);
    	return getCellRenderer (obj, row, column);
    }

    
    
    public TableCellRenderer getCellRenderer (final Object value, final int row, final int column) {
    	TableCellRenderer tcr = null;

    	if (value instanceof Edge) {
    		final Edge edge = (Edge)value;
    		final Object edgeObj = edge.getEdgeObject();
    		if (edgeObj != null) {
    			final Class<?> klass = edgeObj.getClass();
    			tcr = getDefaultRenderer (klass);
    		} else {
    			tcr = nullEdgeObjRenderer;
    		}
    	} 
    	else if (value instanceof Collection) {
    		if (((Collection<?>)value).isEmpty()) {
    			tcr = nullRenderer;
    		} else {
    			tcr = getDefaultRenderer (value.getClass());
    		}
    	}
    	
    	if (tcr == null) {
    		tcr = super.getCellRenderer (row, column);
    	}

    	return tcr;
    }
    
    
    
    @Override
    public Component prepareRenderer (final TableCellRenderer renderer, final int row, final int column) {
    	final Object value = getValueAt (row, column);
        return prepareRenderer (renderer, value, row, column);
    }
    
   
    
    public Component prepareRenderer (final TableCellRenderer renderer, final Object value,
    		final int row, final int column) {

        boolean isSelected = false;
        boolean hasFocus = false;

        // Only indicate the selection and focused cell if not printing
        if (!isPaintingForPrint()) {
            isSelected = isCellSelected(row, column);
            final boolean rowIsLead = (selectionModel.getLeadSelectionIndex() == row);
            final boolean colIsLead = (columnModel.getSelectionModel().getLeadSelectionIndex() == column);
            hasFocus = (rowIsLead && colIsLead) && isFocusOwner();
        }

        return renderer.getTableCellRendererComponent(this, value,
	                                              isSelected, hasFocus,
	                                              row, column);
    }
    
    
    /**
     * Method that combines getCellRenderer and prepareRenderer so that
     * only one call to getValueAt (row, column) is needed. This is beneficial
     * in JMatrix as the MatrixTableModel is a graph and getting edges between 
     * two nodes is often not a single operation.
     * @param row       the row of the cell to render, where 0 is the first row
     * @param column    the column of the cell to render, where 0 is the first column
     * @return          the <code>Component</code> under the event location
     */
    public Component getAndPrepareCellRenderer (final int row, final int column) {
    	final Object value = getValueAt (row, column);
    	final TableCellRenderer tcr = getCellRenderer (value, row, column);
    	return prepareRenderer (tcr, value, row, column);
    }
    
    
    /**
     * Overrides <code>JComponent</code>'s <code>getToolTipText</code>
     * method in order to allow the renderer's tips to be used
     * if it has text set.
     * 
     * <bold>Note:</bold> For <code>JTable</code> to properly display
     * tooltips of its renderers
     * <code>JTable</code> must be a registered component with the
     * <code>ToolTipManager</code>.
     * This is done automatically in <code>initializeLocalVars</code>,
     * but if at a later point <code>JTable</code> is told
     * <code>setToolTipText(null)</code> it will unregister the table
     * component, and no tips from renderers will display anymore.
     *
     * @see JComponent#getToolTipText
     */ 
    @Override
    public String getToolTipText (final MouseEvent event) {
        return "tip";
    }
    
    
    
    void sortRows () {
    	final TableRowSorter<?> trs = (TableRowSorter<?>)JMatrix.this.getRowSorter ();
    	final List<SortKey> keys = new ArrayList<SortKey> (trs.getSortKeys());
		trs.setSortKeys (Collections.EMPTY_LIST); // Get round the bit of code in TableRowSorter which checks for sameness of SortKey list
		int sortIndex;
        for (sortIndex = keys.size() - 1; sortIndex >= 0; sortIndex--) {
            if (keys.get(sortIndex).getColumn() == 0) {
                break;
            }
        }
       
        if (sortIndex == -1) {
            // Key doesn't exist
        	final SortKey sortKey = new SortKey (0, SortOrder.ASCENDING);
            keys.add (0, sortKey);
        }
        //else if (sortIndex == 0) {
        //    // It's the primary sorting key, toggle it
        //    keys.set(0, toggle(keys.get(0)));
       //}
        else {
            // It's not the first, but was sorted on, remove old
            // entry, insert as first with ascending.
            keys.remove (sortIndex);
            keys.add (0, new SortKey (0, SortOrder.ASCENDING));
        }
		trs.setSortKeys (keys);
    }
    
    
    
    public void flipColumnModelWithRows () {
    	createDefaultColumnsFromModel ();
    	attachComparatorsToColumns ();
    	
    	if (this.getModel() instanceof DefaultMatrixTableModel) {
    		// Swap the ordering lists for rows and columns
    		final DefaultMatrixTableModel mtm = (DefaultMatrixTableModel)this.getModel();
    		final List<Comparator<Object>> colComp = mtm.getColumnMetrics().getComparatorList();
    		final List<Comparator<Object>> rowComp = mtm.getRowMetrics().getComparatorList();
    		final List<Comparator<Object>> tempComp = new ArrayList<Comparator<Object>> (colComp);
    		
    		colComp.clear();
    		colComp.addAll (rowComp);
    		
    		rowComp.clear ();
    		rowComp.addAll (tempComp);
    		
    		// Sort rows and columns
    		((ColumnsSorter)getColumnsSorter()).sort (mtm.getColumnMetrics());
    		sortRows ();
    	}
    }
 
    
    
    // Places where column model or it's ordering can change
    // Intercept methods and set the <code>ColumnModelToViewIndex</code>
    // model to be dirty.
    
    
    /**
     * Maps the index of the column in the table model at
     * <code>modelColumnIndex</code> to the index of the column
     * in the view.  Returns the index of the
     * corresponding column in the view; returns -1 if this column is not
     * being displayed.  If <code>modelColumnIndex</code> is less than zero,
     * returns <code>modelColumnIndex</code>.
     *
     * @param   modelColumnIndex     the index of the column in the model
     * @return   the index of the corresponding column in the view
     *
     * @see #convertColumnIndexToModel
     */
    @Override
	public int convertColumnIndexToView (final int modelColumnIndex) {
        if (modelColumnIndex < 0) {
            return modelColumnIndex;
        }
        return colModelViewIndex.convertColumnIndexToView (modelColumnIndex);
    }
    
    @Override
	public void setColumnModel (final TableColumnModel columnModel) {
    	LOGGER.debug ("colModelViewIndex: "+colModelViewIndex);
    	colModelViewIndex = new ColumnModelToViewIndex ();
    	colModelViewIndex.setDirty (true);

    	super.setColumnModel (columnModel);
    }
    
	@Override
	public void columnAdded (final TableColumnModelEvent tcmEvent) {
		colModelViewIndex.setDirty (true);
		super.columnAdded (tcmEvent);
	}

	@Override
	public void columnMoved (final TableColumnModelEvent tcmEvent) {
		colModelViewIndex.setDirty (true);
		//super.columnMoved (e);
		// instead do below
		
        if (isEditing()) {
            removeEditor();
        }
        
        // Calculate clipped repainting 
        //
        // As moving a column shouldn't involve redrawing the entire JTable
        final int colFromIndex = tcmEvent.getFromIndex();
        final int colToIndex = tcmEvent.getToIndex ();
  		int cc1 = Math.min (colFromIndex, colToIndex);
		int cc2 = Math.max (colFromIndex, colToIndex);
		cc1 = Math.max (0, cc1 - 1);
		cc2 = Math.min (this.getColumnModel().getColumnCount() - 1, cc2 + 1);

		LOGGER.debug ("c1: "+colFromIndex+", c2: "+colToIndex);
        
		final Rectangle rect1 = this.getTableHeader().getHeaderRect (cc1);
		final Rectangle rect2 = this.getTableHeader().getHeaderRect (cc2);
		final int x1 = Math.min (rect1.x, rect2.x);
		//x1 += (x1 == r1.x ? r1.width / 2 : r2.width / 2);
		final int x2 = Math.max ((int)rect1.getMaxX(), (int)rect2.getMaxX());
		repaint (x1, 0, x2 - x1, this.getHeight());
	}

	@Override
	public void columnRemoved (final TableColumnModelEvent tcmEvent) {
		colModelViewIndex.setDirty (true);
		super.columnRemoved (tcmEvent);
	}
    
	
	
    public void setScale (final Point2D newScale) {	
    	final Point2D oldScale = getComponentScaler().getScale();
    	getComponentScaler().setScale (newScale);
    	this.firePropertyChange ("masterScale", oldScale, newScale);
    }
    
    
    
    static class NullRenderer extends AbstractEdgeRenderer {
    	/**
		 * 
		 */
		private static final long serialVersionUID = -8899155560427685595L; 

        @Override
		public Component getTableCellRendererComponent (final JTable table, final Object value,
				final boolean isSelected, final boolean hasFocus, final int row, final int column) {  	
    		return this;
        }
        
        @Override
    	public void paintComponent (final Graphics gContext) {
        	// EMPTY
        }
   }

    
    static class NullEdgeObjectRenderer extends AbstractEdgeRenderer {
        /**
		 * 
		 */
		private static final long serialVersionUID = -3859695747918617531L;

		@Override
		public Component getTableCellRendererComponent (final JTable table, final Object value,
				final boolean isSelected, final boolean hasFocus, final int row, final int column) {  	
    		return this;
        }
        
        @Override
    	public void paintComponent (final Graphics gContext) {
        	gContext.setColor (Color.blue);
        	gContext.fillRect (0, 0, this.getWidth(), this.getHeight());
        }
   }


    /**
     * Class that sorts columns according to either values in a particular row
     * that cuts across all columns (via sort (int viewRow)) or by a set of
     * comparators (MultiComparator) that compare properties of the objects that
     * represent each column overall (via sort (Comparator<Object> objComp))
     * @author cs22
     *
     */
	class ColumnsSorter extends AbstractColumnsSorter {
	
		private final ColumnsSorter2 cs2 = new ColumnsSorter2 ();
		
		public ColumnsSorter (final JTable jTable) {
			super (jTable);
		}
	    
	    protected void rearrangeColumns (final TableColumnModel columnModel, final List<TableColumn> columnOrder) {
	    	if (columnModel instanceof MatrixTableColumnModel) {
				((MatrixTableColumnModel)columnModel).setColumns (viewToModel);
			} else {
				super.rearrangeColumns (columnModel, columnOrder);
			}
	    }
	    
	    public void sort (final Comparator<Object> objComp) {
	    	cs2.setComparator (objComp);
	    	sort (-1, cs2);
	    }
	    

		@Override
		public int compare (final TableColumn tc1, final TableColumn tc2) {
			final Object obj1 = jTable.getModel().getValueAt (modelRow, tc1.getModelIndex());
			final Object obj2 = jTable.getModel().getValueAt (modelRow, tc2.getModelIndex());
			return EdgeComparator.getInstance().compare (obj1, obj2);
		}
		
		/**
		* Wrapper class for a MultiComparator for objects
		* Converts references to TableColumn to the matching objects in the model
		*/
		class ColumnsSorter2 implements Comparator<TableColumn> {

			Comparator<Object> comp;
			
			public void setComparator (final Comparator<Object> comp) {
				this.comp = comp;
			}

			@Override
			public int compare (final TableColumn tc1, final TableColumn tc2) {
				final AbstractMatrixTableModel amtm = (AbstractMatrixTableModel)jTable.getModel();
				final Object obj1 = amtm.getColumnObject (tc1.getModelIndex());
				final Object obj2 = amtm.getColumnObject (tc2.getModelIndex());
				return comp.compare (obj1, obj2);
			}
		}
	}
	
	

	
	
	/**
	 * 
	 * @author cs22
	 * Listener to pass to the column sort button widget.
	 * Button launches a list, and moving items in this list
	 * also moves items in the getColumnMetrics() MultiComparator object and
	 * we resort the columns according to this comparator.
	 */
	class DefaultColumnsSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged (final ListSelectionEvent lsEvent) {
			final JList source = (JList) lsEvent.getSource ();
            if (lsEvent.getFirstIndex() == source.getModel().getSize() && lsEvent.getLastIndex() == source.getModel().getSize()
                      && !lsEvent.getValueIsAdjusting()) {
            	((ColumnsSorter)getColumnsSorter()).sort (((DefaultMatrixTableModel)getModel()).getColumnMetrics());
            }
		}			
	}
	
	
	/**
	 * 
	 * @author cs22
	 * Listener to pass to the row sort button widget.
	 * Button launches a list, and moving items in this list
	 * also moves items in the getRowMetrics() MultiComparator object.
	 * 
	 * Since rows in a table already have a sorter, we set the sort key to be 
	 * based on column zero which is associated with the getRowMetrics() 
	 * MultiComparator and we resort the rows according to this comparator.
	 * 
	 * The extra guff down below is because RowSorter only auto resorts if you
	 * change a column's comparator or the order of column comparison for sorting
	 * the rows.
	 * Since we are just reordering within one MultiComparator object associated
	 * with the first column, we have to do the tinkering sequence below to get
	 * it to re-sort.
	 */
	class DefaultRowsSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged (final ListSelectionEvent lsEvent) {
			final JList source = (JList) lsEvent.getSource ();
            if (lsEvent.getFirstIndex() == source.getModel().getSize() && lsEvent.getLastIndex() == source.getModel().getSize()
                      && !lsEvent.getValueIsAdjusting()) {
            	sortRows ();
            }
		}
	}
	
	
	/**
	 * @author cs22
	 * 
	 * Class that holds a model to view index for the columns in a
	 * <code>TableColumnModel</code> to use in the convertColumnToIndexView
	 * method as the default implementation in <code>JTable</code> is
	 * extremely slow (linear time, not indexed).
	 * 
	 * Down side is we must remember to recalculate index if it has been
	 * affected by changes to the TableColumnModel.
	 */
	class ColumnModelToViewIndex {
		int[] model2ViewIndex;
		boolean dirty;
		
		ColumnModelToViewIndex () {
			setDirty (true);
		}
		
	    public void buildColumnIndexTable () {
	    	model2ViewIndex = new int [getModel().getColumnCount()];
	        for (int column = 0; column < getColumnCount(); column++) {
	        	model2ViewIndex [getColumnModel().getColumn(column).getModelIndex()] = column;
	        }
	    }
	    
	    public int convertColumnIndexToView (final int modelColumnIndex) {
	    	if (isDirty()) {
	    		buildColumnIndexTable ();
	    		setDirty (false);
	    	}
	    	if (modelColumnIndex >= model2ViewIndex.length) {
	    		return -1;
	    	}
	    	return model2ViewIndex [modelColumnIndex];
	    }
	    
	    public int[] getIndices () {
	    	return model2ViewIndex;
	    }

		public final boolean isDirty() {
			return dirty;
		}

		// If true, need to recalculate the index before we can use it again
		// Caused by moving / adding / removing columns in <code>TableColumnModel</code>
		public final void setDirty (final boolean dirty) {
			this.dirty = dirty;
		}
	}
}