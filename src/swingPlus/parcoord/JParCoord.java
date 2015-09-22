package swingPlus.parcoord;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.plaf.TableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import model.parcoord.ParCoordRowSorter;
import model.shared.SortedTableColumn;

import swingPlus.parcoord.renderers.CurveRowRenderer;
import swingPlus.parcoord.renderers.RowRenderer;
import swingPlus.shared.CrossFilteredTable;
import ui.ParCoordUI;



public class JParCoord extends CrossFilteredTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4095432469174098433L;
	public static enum PolylineType {CURVES, STRAIGHTS, DOTS};
	public static Stroke defaultStroke = new BasicStroke (1.0f);
	private final static Logger LOGGER = Logger.getLogger (JParCoord.class);

	Set<Set<Integer>> synchroSets = new HashSet<Set<Integer>> ();
	ListSelectionModel brushSelection;
	RowRenderer renderer;
	Stroke stroke = defaultStroke;
	Stroke selectedStroke = defaultStroke;
	Color brushForegroundColour = Color.black, brushSelectionColour = Color.red;
	boolean redrawAll = true;
	boolean redrawSelections = true;
	boolean brushing = true;
	boolean drawUnselectedItems = true;
	int suggestedLabelSep = 650;
	
	TableColumnModel preSortedColumnSource;

	private final static String UI_CLASS_ID = "ParCoordUI";
	static {
		UIManager.put (UI_CLASS_ID, "ui.ParCoordMultiplexColumnUI");
		UIManager.put ("ParCoord.brushForeground", Color.black);
		UIManager.put ("ParCoord.brushSelection", Color.red);
	}
	
	public JParCoord () {
		this (null);
	}
	

	public JParCoord (final TableModel tModel) {
		this (tModel, null);	
	}
	
	
	public JParCoord (final TableModel tModel, final TableColumnModel tColModel) {
		super (tModel, tColModel);
		//this.setDoubleBuffered (true);
		brushSelection = new DefaultListSelectionModel ();
    	ToolTipManager.sharedInstance().registerComponent (this);	
	}
	
	
    /**
     * Notification from the <code>UIManager</code> that the L&F has changed.
     * Replaces the current UI object with the latest version from the
     * <code>UIManager</code>.
     *
     * @see JComponent#updateUI
     */
	@Override
    public void updateUI() {
    	setUI((TableUI)UIManager.getUI(this));
    }

    @Override
	public ParCoordUI getUI() {
        return (ParCoordUI)ui;
    }

    @Override
    public String getUIClassID() {
        return UI_CLASS_ID;
    }
    
  

    
    @Override
	public String getToolTipText (final MouseEvent event) {
    	//super.getToolTipText (event);
        return getToolTipText();
    }
    
    /**
     * Sets the data model for this table to <code>newModel</code> and registers
     * with it for listener notifications from the new data model.
     *
     * @param   dataModel        the new data source for this table
     * @exception IllegalArgumentException      if <code>newModel</code> is <code>null</code>
     * @see     #getModel
     * @beaninfo
     *  bound: true
     *  description: The model that is the source of the data for this view.
     */
    @Override
	public void setModel (final TableModel dataModel) {
    	final boolean change = (this.dataModel != dataModel);
    	super.setModel (dataModel);
        if (change && getAutoCreateRowSorter()) {
            setRowSorter (new ParCoordRowSorter (dataModel, this.getColumnModel()));
        }
    }
    
    /**
     * Creates default columns for the table from
     * the data model using the <code>getColumnCount</code> method
     * defined in the <code>TableModel</code> interface.
     * 
     * Clears any existing columns before creating the
     * new columns based on information from the model.
     *
     * @see     #getAutoCreateColumnsFromModel
     */
    @Override
	public void createDefaultColumnsFromModel() {
    	final TableModel tModel = getModel();
        if (tModel != null) {
            // Remove any current columns
        	final TableColumnModel tColModel = getColumnModel();
            while (tColModel.getColumnCount() > 0) {
                tColModel.removeColumn (tColModel.getColumn(0));
            }

            // Create new columns from the data model info
            for (int i = 0; i < tModel.getColumnCount(); i++) {
                final SortedTableColumn<?> newColumn = new SortedTableColumn (getModel(), i);
                //if (!newColumn.getOrderedList().isEmpty()) {
                	addColumn (newColumn);
                    LOGGER.debug ("column: "+newColumn.getHeaderValue()+", min: "+newColumn.getMin()+", max: "+newColumn.getMax());
                //}
            }
        }
    }

    
    
    /*
    @Override
    public JTableHeader createDefaultTableHeader () {
    	return new JTableHeader (columnModel) {

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
    */
    

    @Override
    public void setAutoCreateRowSorter (final boolean autoCreateRowSorter) {
    	final boolean oldValue = this.getAutoCreateRowSorter ();
    	super.setAutoCreateRowSorter (autoCreateRowSorter);
        
        if (autoCreateRowSorter) {
            setRowSorter (new ParCoordRowSorter (getModel(), this.getColumnModel()));
        }
        firePropertyChange("autoCreateRowSorter", oldValue,
                           autoCreateRowSorter);
    }
    
    
    @Override
    protected void createDefaultRenderers() {
    	super.createDefaultRenderers();
    	setDefaultRenderer (new CurveRowRenderer ());
    }
    
    
    /**
     * Equivalent to <code>revalidate</code> followed by <code>repaint</code>.
     */
    @Override
	protected void resizeAndRepaint() {
    	setRedrawAll (true);
        super.resizeAndRepaint ();
    }
    
    @Override
    public void setTableHeader (final JTableHeader tableHeader) {
        super.setTableHeader (tableHeader);
        if (tableHeader != null) {
        	tableHeader.setDefaultRenderer (new DefaultParCoordCellHeaderRenderer ());
        }
    }
    
    

    public void setDefaultRenderer (final RowRenderer newRenderer) {
    	if (renderer != newRenderer && newRenderer != null) {
    		renderer = newRenderer;
    		repaintIfNecessary ();
    	}
    }
    
    public RowRenderer getDefaultRenderer () {
    	return renderer;
    }
    
    
    
    @Override
	public void tableChanged (final TableModelEvent tmEvent) {

    	boolean repaintNec = false;
    	// Make sure the sorted column models are given details of new data to index
    	if (tmEvent.getType() == TableModelEvent.INSERT || tmEvent.getType() == TableModelEvent.DELETE
    			|| tmEvent.getLastRow() == Integer.MAX_VALUE) {
    		for (int n = 0; n < this.getColumnModel().getColumnCount(); n++) {
    			final SortedTableColumn<?> stc = (SortedTableColumn<?>)this.getColumnModel().getColumn (n);
    	   		
    			final BitSet filterSet = (this.getRowSorter() instanceof ParCoordRowSorter) ?
    					((ParCoordRowSorter)this.getRowSorter()).makeBitSetFromFilter() : null;
    			stc.setFilterSet (filterSet);
    	   		if (tmEvent.getType() == TableModelEvent.INSERT) {
        			stc.addData (tmEvent.getFirstRow(), tmEvent.getLastRow());
        		} else if (tmEvent.getType() == TableModelEvent.DELETE) {
        			stc.removeData (tmEvent.getFirstRow(), tmEvent.getLastRow());
        		} else {
        			stc.populateAndSort ((TableModel)tmEvent.getSource());
        		}
    		}
    		repaintNec = true;
    	}
    	
    	super.tableChanged (tmEvent);
    	
    	LOGGER.debug ("e: "+tmEvent.getFirstRow()+"|"+tmEvent.getLastRow()+"|"+tmEvent.getColumn()+"|"+tmEvent.getType());
    	// If an update of a single cell reindex the data concerned in the appropriate sorted column model
    	
    	final int viewColumn = convertColumnIndexToView (tmEvent.getColumn());
    	if (tmEvent.getFirstRow() != TableModelEvent.HEADER_ROW 
    			&& tmEvent.getFirstRow() == tmEvent.getLastRow()
    			&& viewColumn >= 0
    			&& tmEvent.getType() == TableModelEvent.UPDATE) {
    		final SortedTableColumn<?> stc = (SortedTableColumn<?>)this.getColumnModel().getColumn (viewColumn);
    		LOGGER.debug ("before reindex: min: "+stc.getMinBound()+", max: "+stc.getMaxBound());
			final BitSet filterSet = (this.getRowSorter() instanceof ParCoordRowSorter) ?
					((ParCoordRowSorter)this.getRowSorter()).makeBitSetFromFilter() : null;
			stc.setFilterSet (filterSet);
    		stc.reindexDatum (tmEvent.getFirstRow());

    		LOGGER.debug ("after reindex: min: "+stc.getMinBound()+", max: "+stc.getMaxBound());
    		repaintNec = true;
    	}
    	
    	if (repaintNec) {
	    	runSynchroSets ();
	    	resizeAndRepaint ();
    	}
    }
    
    
    @Override
    /**
     * Track changes to table cell selections
     */
    public void valueChanged (final ListSelectionEvent lsEvent) {
    	
    	/*	The call to super is needed to poke the three commented-out lines of code beneath it
    		into action. There's no access to the sortManager object outside JTable and we need
    		to set sortManager's ListSelectionModel field - 'modelSelection' - to null.
    		If it isn't nulled, the sortManager doesn't cache selection states after a filter
    		change (when prompted via a RowSorterListener interface) as it presumes the cache is
    		already built (it isn't) and selections aren't mapped over between filter changes.
    		
    		This took a whole day to figure out.
    		
    		//	if (sortManager != null) {
        	//    sortManager.viewSelectionChanged(e);
        	//	}
    	*/
  	
    	final boolean isAdjusting = lsEvent.getValueIsAdjusting();
        if (!isAdjusting && getRowCount() > 0 && getColumnCount() > 0) {
 
        	if (lsEvent.getSource().equals(this.getSelectionModel())) {
        		super.valueChanged (lsEvent);
        	}
        	setRedrawSelections (true);
        	if (this.isVisible()) {
        		final Rectangle rect = this.getVisibleRect ();
        		if (rect.height > 0 && rect.width > 0) {
        			repaint ();
        		}
        	}
        }
    }
    
    
    
    public ListSelectionModel getBrushModel () {
    	return brushSelection;
    }

    
	public final Color getBrushForegroundColour () {
		return brushForegroundColour;
	}

	public final void setBrushForegroundColour (final Color brushForegroundColour) {
		if (this.brushForegroundColour == null || 
				! this.brushForegroundColour.equals (brushForegroundColour)) {
			final Color old = this.brushForegroundColour;
			this.brushForegroundColour = brushForegroundColour;
			this.firePropertyChange ("brushForeground", old, brushForegroundColour);
		}
	}


	public final Color getBrushSelectionColour () {
		return brushSelectionColour;
	}

	public final void setBrushSelectionColour (final Color brushSelectionColour) {
		if (this.brushSelectionColour == null || 
				! this.brushSelectionColour.equals (brushSelectionColour)) {
			final Color old = this.brushSelectionColour;
			this.brushSelectionColour = brushSelectionColour;
			this.firePropertyChange ("brushSelection", old, brushSelectionColour);
		}
	}



	public final Stroke getStroke() {
		return stroke;
	}

	public final void setStroke (final Stroke stroke) {
		if (stroke != null && !stroke.equals (this.stroke)) {
			this.stroke = stroke;
	    	repaintIfNecessary ();
		}
	}
	
    
	public final Stroke getSelectedStroke() {
		return selectedStroke;
	}

	public final void setSelectedStroke (final Stroke selectedStroke) {
		if (selectedStroke != null && !selectedStroke.equals (this.selectedStroke)) {
			this.selectedStroke = selectedStroke;
			repaintIfNecessary ();
		}
	}
    
    void repaintIfNecessary () {
    	if (this.isVisible()) {
    		final Rectangle rect = this.getVisibleRect ();
    		if (rect.height > 0 && rect.width > 0) {
    			resizeAndRepaint ();
    		}
    	}
    }
    
    /*
    @Override
	public void repaint () {
    	
    	try {
			throw new Exception ();
		} catch (Exception e) {
			LOGGER.error (e.toString(), e);
		}
    	
    	super.repaint ();
    }
   
    
    @Override
	public void repaint (int x, int y, int w, int h) {
    	
    	LOGGER.debug ("x: "+x+", w: "+w+", y: "+y+", h: "+h);
		
    	
    	super.repaint (x, y, w, h);
    }
    
    
    @Override
	public void repaint (long tm, int x, int y, int w, int h) {
    	
    	LOGGER.debug ("tm: "+tm+", x: "+x+", w: "+w+", y: "+y+", h: "+h);
		
    	if (y > 0) {
    		try {
    			throw new Exception ();
    		} catch (Exception e) {
    			LOGGER.error (e.toString(), e);
    		}
    	}
    	
    	super.repaint (tm, x, y, w, h);
    }
    
    
    @Override
	public void repaint (long tm) {
    	
    	LOGGER.debug ("tm: "+tm);
		
    	
    	super.repaint (tm);
    }
    */
    
    /**
     * Invoked when a column is repositioned. If a cell is being
     * edited, then editing is stopped and the cell is redrawn.
     * <BARLEY_PATTERN>
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
        // Since columns are independently ordered, moving a column shouldn't involve
        // redrawing the entire parcoord (and shouldn't in a standard JTable either),
        // and re-ordering a column shouldn't involve redrawing the parallel coordinates
        // entirely (but it will in the 'one order for all' columns in a standard JTable)
        // The amount of redrawing depends on the current PolylineType as so...
        // DOTS = columns affected only
        // STRAIGHTS = columns affected + one column either side
        // CURVES = columns affected + two columns either side
        final int col1 = tcmEvent.getFromIndex();
        final int col2 = tcmEvent.getToIndex ();
        LOGGER.debug ("col1: "+col1+", col2: "+col2);
        setRedrawAll (true);
        final RowRenderer renderer = getDefaultRenderer ();
        
        final int columnSpread = renderer.getColumnSpread();
		int cc1 = Math.min (col1, col2);
		int cc2 = Math.max (col1, col2);
		cc1 = Math.max (0, cc1 - columnSpread);
		cc2 = Math.min (this.getColumnModel().getColumnCount() - 1, cc2 + Math.max (1, columnSpread));
   		final Rectangle rect1 = this.getTableHeader().getHeaderRect (cc1);
   		final Rectangle rect2 = this.getTableHeader().getHeaderRect (cc2); 	

        final int x1 = Math.min ((int)rect1.getCenterX(), (int)rect2.getCenterX()) - renderer.getPixelSpread();
        final int x2 = (cc2 < this.getColumnModel().getColumnCount() - 1)
         	? Math.max ((int)rect1.getCenterX(), (int)rect2.getCenterX())
         	: Math.max ((int)rect1.getMaxX(), (int)rect2.getMaxX())
         	+ renderer.getPixelSpread();

        repaint (x1, 0, x2 - x1, this.getHeight());
    }
    
    
    
    @Override
    public void columnAdded (final TableColumnModelEvent tcmEvent) {
    	super.columnAdded (tcmEvent);
    	final TableColumn tColumn = this.getColumnModel().getColumn (tcmEvent.getToIndex ());

    	if (tColumn instanceof SortedTableColumn<?>) {
    		LOGGER.debug ("Column Added: "+tColumn+", "+tColumn.getHeaderValue());
    		final SortedTableColumn<?> stc = (SortedTableColumn<?>)tColumn;
			final BitSet filterSet = (this.getRowSorter() instanceof ParCoordRowSorter) ?
					((ParCoordRowSorter)this.getRowSorter()).makeBitSetFromFilter() : null;
			stc.setFilterSet (filterSet);
			if (preSortedColumnSource == null) {
    			stc.populateAndSort (this.getModel());
    		} else {
				final TableColumn sourceColumn = preSortedColumnSource.getColumn (tcmEvent.getToIndex());
				if (sourceColumn instanceof SortedTableColumn) {
	    			stc.copyFromSource (this.getModel(), (SortedTableColumn)sourceColumn);
				} else {
					stc.populateAndSort (this.getModel());
				}
    		} 
    	}
    }
    
    /**
     * Invoked when the selection model of the <code>TableColumnModel</code>
     * is changed.
     * 
     * Application code will not use these methods explicitly, they
     * are used internally by JTable.
     *
     * @param  lsEvent  the event received
     * @see TableColumnModelListener
     */
    @Override
	public void columnSelectionChanged (final ListSelectionEvent lsEvent) { /* EMPTY */ }

	
	public final boolean isRedrawAll () {
		return redrawAll;
	}

	public final void setRedrawAll (final boolean redrawAll) {
		this.redrawAll = redrawAll;
	}

	
	public final boolean isRedrawSelections () {
		return redrawSelections;
	}

	public final void setRedrawSelections (final boolean redrawSelections) {
		this.redrawSelections = redrawSelections;
	}

	public final boolean isBrushing () {
		return brushing;
	}

	public final void setBrushing (final boolean brushing) {
		if (this.brushing != brushing) {
			this.brushing = brushing;
			this.firePropertyChange ("brushing", brushing, !brushing);
		}
	}
	
	
	/*
	@Override
	public void sorterChanged (RowSorterEvent e) {
		System.err.println ("get ussos: "+this.getUpdateSelectionOnSort());
		System.err.println ("p sel before: "+this.getSelectionModel());
		ListSelectionEvent event = new ListSelectionEvent (this, 0, 0, false);
		//super.valueChanged (event);
		super.sorterChanged (e);
        Throwable thr = new Throwable (); 
        thr.printStackTrace();
        System.err.println ("p sel after: "+this.getSelectionModel());
	}
	*/
	
	public final boolean isDrawUnselectedItems() {
		return drawUnselectedItems;
	}

	public final void setDrawUnselectedItems (final boolean drawUnselectedItems) {
		this.drawUnselectedItems = drawUnselectedItems;
	}
	
	
	public final int getSuggestedLabelSeparation () {
		return suggestedLabelSep;
	}
	
	public final void setSuggestedLabelSeparation (final int newVal) {
		final int oldVal = suggestedLabelSep;
		suggestedLabelSep = newVal;
		this.firePropertyChange ("labelSeparation", oldVal, newVal);	
	}

	
	public void addSynchroSet (final String[] modelColumnNames) {
		final Set<String> nameSet = new HashSet<String> (Arrays.asList (modelColumnNames));
		int[] modelColumnIndices = new int [this.getModel().getColumnCount()];
		int counter = 0;
		
		for (int column = 0; column < this.getModel().getColumnCount(); column++) {
			final String colName = this.getModel().getColumnName (column);
			if (nameSet.contains (colName)) {
				modelColumnIndices [counter] = column;
				counter++;
			}
		}
		
		addSynchroSet (Arrays.copyOf (modelColumnIndices, counter));
	}
	
	
	public void addSynchroSet (final int[] modelColumnIndices) {
		final Set<Integer> newSynchroSet = new HashSet<Integer> ();
		for (int n = modelColumnIndices.length; --n >= 0;) {
			final int index = modelColumnIndices [n];
			final int viewIndex = this.convertColumnIndexToView (index);
			final SortedTableColumn<?> stc = (SortedTableColumn<?>) this.getColumnModel().getColumn (viewIndex);
			if (stc.isA (Number.class)) {
				newSynchroSet.add (Integer.valueOf (index));
			}
		}
		if (newSynchroSet.size() > 1) {
			synchroSets.add (newSynchroSet);
			runSynchroSets ();
		}
	}
	

	
	protected void runSynchroSets () {
		for (Set<Integer> colSet : synchroSets) {
			Comparable min = null;
			Comparable max = null;
			
			for (Integer index : colSet) {
				final int viewIndex = this.convertColumnIndexToView (index.intValue());
				
				if (viewIndex != -1) {
					final SortedTableColumn<?> stc = (SortedTableColumn<?>) this.getColumnModel().getColumn (viewIndex);
					
					if (stc.isA (Number.class)) {
						final Comparable nin = stc.getMin();
						final Comparable nax = stc.getMax();
						if (min == null || nin.compareTo(min) < 0) {
							min = nin;
						}
						if (max == null || nax.compareTo(max) > 0) {
							max = nax;
						}
					}
				}
			}
			
			for (Integer index : colSet) {
				final int viewIndex = this.convertColumnIndexToView (index.intValue());
				if (viewIndex != -1) {
					final SortedTableColumn<?> stc = (SortedTableColumn<?>) this.getColumnModel().getColumn (viewIndex);
					if (stc.isA (Number.class)) {
						stc.setMinBound (min);
						stc.setMaxBound (max);
					}
				}
			}
		}
	}



	public final void createColumnsFromPreSortedSource (final TableColumnModel preSortedColumnSource) {
		this.preSortedColumnSource = preSortedColumnSource;
		if (preSortedColumnSource != null) {
			this.createDefaultColumnsFromModel();
		}
		this.preSortedColumnSource = null;		
	}
}
