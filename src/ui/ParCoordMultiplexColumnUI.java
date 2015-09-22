package ui;

import util.GraphicsUtil;
import util.ui.UIUtils;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import model.shared.SortedTableColumn;

import swingPlus.parcoord.JParCoord;

/**
 * Class that associates certain rendered swing components and selection models 
 * with ordered table columns depending on the data in that column
 * Currently a JSlider and BoundedRangeModel for continuous / large columns
 * and a JCheckBox set and a BitSet for highly discrete columns with few values
 * @author cs22
 *
 */
public class ParCoordMultiplexColumnUI extends ParCoordUI {

    private static final Class<ParCoordMultiplexColumnUI> CLASS_OBJ = ParCoordMultiplexColumnUI.class;
    private static final Logger LOGGER = Logger.getLogger (CLASS_OBJ);

    protected MouseWheelListener mouseWheelListener;
    protected List<AbstractColumnInteractor> axisInteractors;

    protected final static int INTERACTOR_MIN_SIZE = 16;
    
    // Extended Handler - replaces all apart from listening to keypresses
    // and property change events
    protected class Handler2 extends Handler implements MouseWheelListener {

    	Robot robot;
    	
    	Handler2 () {
    		try {
    			robot = new Robot ();
    		} catch (Exception ex) {
    			LOGGER.error ("Robot instantiation error", ex);
    		}
    	}
    	
    	
        @Override
        public void keyPressed (final KeyEvent kEvent) {
        	if (isTableActive ()) {
	        	super.keyPressed (kEvent);
	
	        	// catch keys that map to clearSelection and selectAll actions
	        	// and reset the boundedRangeModels if so
	        	final InputMap map = table.getInputMap (JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	        	final KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent (kEvent);
	        	final Object binding  = map.get(keyStroke);
	        	
	        	if (binding != null) {
	        		final ActionMap aMap = table.getActionMap();
	        		final Action action = (aMap == null) ? null : aMap.get(binding);
	            	if (action instanceof Actions2) {
	            		final String name = ((Actions2)action).getName();
	            		if ("clearSelection".equals(name) || "selectAll".equals(name)) {
	            			resetColumnInteractorSelections ();
	            		}
	            	}
	        	}
	        	
	        	if (activeViewColumnIndex != -1) {
		        	calibrateColumnInteractor (activeViewColumnIndex);
		        	final AbstractColumnInteractor cInter = getAxisInteractor (activeViewColumnIndex);
		        	cInter.cacheSelection ();
		        	final KeyListener[] keyListeners = cInter.getComponent().getKeyListeners();
		        	for (KeyListener kListener : keyListeners) {
		        		kListener.keyPressed (kEvent);
		        	}
	
		        	if (kEvent.isConsumed()) {
		        		deltaTableSelectionModel (activeViewColumnIndex);
		        		//setTableSelectionModel ();
			            ((JParCoord)table).setRedrawSelections (true);
			            table.repaint ();
		        	}
	        	}	
        	}
        }

        
        // PropertyChangeListener
        @Override
		public void propertyChange (final PropertyChangeEvent event) {
        	super.propertyChange (event);
        	// Reset interactor widgets when model is replaced in table
        	if (event.getPropertyName().equals ("model") && 
        			event.getNewValue() != event.getOldValue()) {
        		//System.err.println ("w00t");
        		setNewAxisInteractors ();
        	}
		}
		
		// Temp variables for mouse / slider interaction 
		transient int componentX = 0, componentY = 0;
		transient int activeViewColumnIndex = 0;
		transient List<Dimension> lastDraggedRanges = new ArrayList<Dimension> ();
		transient boolean tableAutoscrolls = false;
        transient boolean goneOutside = true;
		
	    // MouseInputListener       
		@Override
		public void mouseEntered (final MouseEvent mEvent) {
			table.requestFocusInWindow();
			if (isTableActive() && !isDragging) {
				ToolTipManager.sharedInstance().setDismissDelay (5000);
	    		ToolTipManager.sharedInstance().setInitialDelay (0);
	    		//ToolTipManager.sharedInstance().setReshowDelay (0);
	    		prepareInteractorComponents (INTERACTOR_MIN_SIZE);
				startBrushing ();	
			}
		}

		
		@Override
		public void mouseExited (final MouseEvent mEvent) {
			if (isTableActive() && !isDragging) {
				table.setToolTipText ("");
				ToolTipManager.sharedInstance().setInitialDelay (500);	
				stopBrushing ();
				goneOutside = true;
			}
		}

		
		@Override
		public void mouseReleased (final MouseEvent mEvent) {
			if (isTableActive()) {
				isDragging = false;
				table.setAutoscrolls (tableAutoscrolls);
				
				calibrateColumnInteractor (activeViewColumnIndex);	
				
				final AbstractColumnInteractor cInter = getAxisInteractor (activeViewColumnIndex);
				cInter.cacheSelection ();		    
			  	final MouseEvent me2 = GraphicsUtil.convertMouseEvent (mEvent, cInter.getComponent(), componentX, componentY);
				cInter.mouseReleased (me2);
				
	    		deltaTableSelectionModel (activeViewColumnIndex);
	    		
	            ((JParCoord)table).setRedrawSelections (true);
	            table.repaint ();
			}
		}
	
	
		
		@Override
		public void mouseDragged (final MouseEvent mEvent) {
			if (isTableActive()) {
				calibrateColumnInteractor (activeViewColumnIndex);

				final AbstractColumnInteractor cInter = getAxisInteractor (activeViewColumnIndex);
			  	cInter.cacheSelection ();
				final MouseEvent me2 = GraphicsUtil.convertMouseEvent (mEvent, table, componentX, componentY);
				cInter.mouseDragged (me2);
				
				final List<Dimension> ranges = sortedIndexRangeFromRangeModel (activeViewColumnIndex);
				//System.err.println ("ranges: "+ranges+"\ncached: "+lastDraggedRanges);
				if (!ranges.equals (lastDraggedRanges)) {
					lastDraggedRanges.clear ();
					lastDraggedRanges.addAll (ranges);
					
	        		deltaTableSelectionModel (activeViewColumnIndex);
					((JParCoord)table).setRedrawSelections (true);
				} else {
					if (changedToNoRangesSet (activeViewColumnIndex)) {
						((JParCoord)table).setRedrawSelections (true);
						table.getSelectionModel().clearSelection();
					} else if (changedFromNoRangesSet (activeViewColumnIndex)) {
						LOGGER.debug ("hello from changed from no selection");
						((JParCoord)table).setRedrawSelections (true);
						table.getSelectionModel().setSelectionInterval (0, table.getRowCount() - 1);
					} else {
						final int colX = findColumnX (activeViewColumnIndex);
						final int cWidth = table.getColumnModel().getColumn(activeViewColumnIndex).getWidth();
						final Rectangle columnRect = new Rectangle (colX, 0, cWidth, visibleRectNoInsets.height);
						paintLegendLayer (null, new Dimension (Math.max (0, activeViewColumnIndex - 1), activeViewColumnIndex), columnRect);
					}
				}
				
		        table.repaint ();
			}
		}
	
		
		@Override
		public void mousePressed (final MouseEvent mEvent) {
            if (isTableActive()) {   
	            tableAutoscrolls = table.getAutoscrolls();
	            table.setAutoscrolls (false);
	            isDragging = true;
	            lastDraggedRanges.clear ();
	            activeViewColumnIndex = table.columnAtPoint (mEvent.getPoint());
	            
	            final AbstractColumnInteractor cInter = getAxisInteractor (activeViewColumnIndex);
	
	            final int cmx = findColumnMidX (activeViewColumnIndex);
	            componentX = cmx - cInter.getComponentXOffset();
	            componentY = cInter.getComponent().getY(); //visibleRect.y - jrs.getArrowHeight();
	            
	            calibrateColumnInteractor (activeViewColumnIndex);
	            final MouseEvent me2 = GraphicsUtil.convertMouseEvent (mEvent, cInter.getComponent(), componentX, componentY);
	 
	            if (me2.getX() >= 0 && me2.getX() < cInter.getComponent().getWidth()) {
	            	cInter.cacheSelection ();
	            	cInter.mousePressed (me2);
		              
			        ((JParCoord)table).setRedrawSelections (true);
			        table.repaint ();
	            }
            }
        }
		

		@Override
        public void mouseMoved (final MouseEvent mEvent) {

			if (isTableActive()) {
				final Point mPoint = mEvent.getPoint ();
		        final int viewColumnIndex = table.columnAtPoint (mEvent.getPoint());
		        if (goneOutside) {
		        	goneOutside = false;
		        	prepareInteractorComponents (INTERACTOR_MIN_SIZE);
		        }
				calibrateColumnInteractor (viewColumnIndex);
				final AbstractColumnInteractor cInter = getAxisInteractor (viewColumnIndex);
				
				final boolean toolTipSet = cInter.setComponentSpecificToolTip (mEvent.getY(), viewColumnIndex, visibleRect, ParCoordMultiplexColumnUI.this);
				if (!toolTipSet) {
					doBrushing (mPoint);
				}
			}
		}
		
		
		
		
		public void mouseWheelMoved (final MouseWheelEvent mWheelEvent) {
			robot.keyPress (mWheelEvent.getWheelRotation() > 0 ? 
				KeyEvent.VK_LEFT : KeyEvent.VK_RIGHT);
		}
    }
    

//
//  Factory methods for the Listeners
//

    @Override
    protected Handler getHandler() {
        if (handler == null) {
            handler = new Handler2();
        }
        return handler;
    }
    

    
    
    /**
     * Convert a BoundedRangeModel's range into an index range on the appropriate SortedTableColumn's
     * filtered and sorted index
     * @param viewColumnIndex
     * @return a Dimension object that holds the range in terms of lower and upper indices for the associated SortedTableColumn
     */
    List<Dimension> sortedIndexRangeFromRangeModel (final int viewColumnIndex) {
    	final AbstractColumnInteractor cInter = getAxisInteractor (viewColumnIndex);
    	final List<Dimension> yRanges = cInter.getSelectionRangeInY ();
    	//System.err.println ("yranges: "+yRanges);
    	final List<Dimension> sortedIndexRanges = new ArrayList<Dimension> ();
    	
    	for (int range = 0; range < yRanges.size(); range++) {
			//final int storeDirection = jrs.getDirection ();
			//jrs.setDirection (JRangeSlider2.LEFTRIGHT_TOPBOTTOM);
    		final Dimension yRange = yRanges.get (range);
			final Object lowVal = this.getValue (yRange.width, viewColumnIndex, visibleRect); 
			final Object upperVal = this.getValue (yRange.height, viewColumnIndex, visibleRect); 
			//jrs.setDirection (storeDirection);
			
			final Dimension sortedIndexRange = new Dimension ();
			final SortedTableColumn stc = (SortedTableColumn<?>)table.getColumnModel().getColumn (viewColumnIndex);
			stc.getRange (sortedIndexRange, (Comparable)lowVal, (Comparable)lowVal, (Comparable)upperVal, (Comparable)upperVal);
    	
			sortedIndexRanges.add (sortedIndexRange);
    	}
    	//System.err.println ("sir: "+sortedIndexRanges);
		//System.err.println ("column: "+viewColumnIndex+", sortedIndexRange: "+sortedIndexRange);
		return sortedIndexRanges;
    }
    
    
    /**
     * Change the JTable's row ListSelectionModel according to a change in a column interactor
     * @param viewColumnIndex	index in JTable's TableColumnModel of column the interactor was associated with
     */
    void deltaTableSelectionModel (final int viewColumnIndex) {
    	
    	final AbstractColumnInteractor cInter = getAxisInteractor (viewColumnIndex);
    	//resetListSelectionModelFromScratch ();
    	
    	//return;
    	
    	long nano = System.nanoTime ();	
		//LOGGER.debug ("oldRange: "+oldRange+", currentRange: "+currentRange);

		final ListSelectionModel rsm = table.getSelectionModel();
		
		// If everything reset to off then clear selection and return
		if (changedToNoRangesSet (-1)) {
			rsm.clearSelection();
			return;
		}
		
		rsm.setValueIsAdjusting (true);
		if (cInter.firstSelectionAlwaysRemove() && changedFromNoRangesSet (viewColumnIndex)) {
			rsm.setSelectionInterval (0, table.getRowCount() - 1);
		} 		

		final int logSize = 31 - Integer.numberOfLeadingZeros (table.getRowCount());
		final SortedTableColumn stc = (SortedTableColumn<?>)table.getColumnModel().getColumn (viewColumnIndex);	    	
		final Dimension changeRange = new Dimension ();
		final List<List<Dimension>> rangeSortedIndices = getColumnSortedIndexRanges (viewColumnIndex);
		final int rangeTotal = getRangeTotal (rangeSortedIndices, viewColumnIndex);
		final BitSet rangeColumns = getRangeSelectedColumns (viewColumnIndex);
		
		List<DeltaRange> deltaRanges = cInter.getOldToNewChanges();
		//System.err.println ("delta: "+deltaRanges);
		
		for (int rangeIndex = 0; rangeIndex < deltaRanges.size(); rangeIndex++) {
			final DeltaRange range = deltaRanges.get (rangeIndex);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug ("Delta range "+rangeIndex+" : "+range.toString());
			}
			
			if (range.isRemove()) {
	    		removeRangeFromSelectionModel (viewColumnIndex, range);
			} else {
	    		final Object lowValObj = this.getValue (range.getLowVal(), viewColumnIndex, visibleRect); 
	    		final Object highValObj = this.getValue (range.getHighVal(), viewColumnIndex, visibleRect);
	    		stc.getRange (changeRange, (Comparable)lowValObj, (Comparable)highValObj, range.isIncludeLowVal(), range.isIncludeHighVal());
	    		final int affectedRowSize = changeRange.height - changeRange.width + 1;
	    		
	    		if (LOGGER.isDebugEnabled()) {
		    		LOGGER.debug ("for log all rows: "+logSize+", range all columns: "+rangeTotal+", size affected rows: "+affectedRowSize);
		    		LOGGER.debug ("ops for binary search: "+(affectedRowSize * rangeColumns.cardinality() * logSize));
		    		LOGGER.debug ("ops for bitset range: "+(rangeTotal + (rangeTotal == 0 ? 0 : affectedRowSize)));
	    		}
	      	  	
	    		//if (logSize < )	
	      		final BitSet modelSelectionSet = getCurrentSelection (rangeSortedIndices, rangeColumns, viewColumnIndex);
	    		mergeBitSetWithSelectionModel (modelSelectionSet, changeRange, viewColumnIndex);
			}
		}
		
		//resetListSelectionModelFromScratch();
    	
		rsm.setValueIsAdjusting (false);
    	nano = System.nanoTime() - nano;
    	LOGGER.debug ("Recalc selection model: "+(nano/1E6)+" ms.");
    	
    }
    
    
    
    /**
     * Method that takes in a change in a interactor selection and converts it to a
     * removal operation on the JTable's row {@link ListSelectionModel}
     * 
     * @param activeColumnViewIndex	view-based index of column under selection
     * @param List<DeltaRange> list of changes from previous interactor selection state
     */
    void removeRangeFromSelectionModel (final int activeColumnViewIndex, final DeltaRange removeRange) {

    	final ListSelectionModel rsm = table.getSelectionModel ();
		final boolean alreadyAdjusting = rsm.getValueIsAdjusting();
		if (!alreadyAdjusting) {
			rsm.setValueIsAdjusting (true);
		}

		final Object lowValObj = this.getValue (removeRange.getLowVal(), activeColumnViewIndex, visibleRect); 
		final Object highValObj = this.getValue (removeRange.getHighVal(), activeColumnViewIndex, visibleRect);
		
		final Dimension changeRange = new Dimension ();
		final SortedTableColumn stc = (SortedTableColumn<?>)table.getColumnModel().getColumn (activeColumnViewIndex);	    	
		stc.getRange (changeRange, (Comparable)lowValObj, (Comparable)highValObj, removeRange.isIncludeLowVal(), removeRange.isIncludeHighVal());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("lowy: "+removeRange.getLowVal()+"\tlowValObj: "+lowValObj+"\tsindex :"+changeRange.width);
			LOGGER.debug ("highy: "+removeRange.getHighVal()+"\thighValObj: "+highValObj+"\tsindex :"+changeRange.height);
		}

		//System.err.println ("lowVal: "+lowVal+"\tlowy: "+lowY+"\tlowValObj: "+lowValObj+"\tsindex :"+changeRange.width);
		//System.err.println ("highVal: "+highVal+"\thighy: "+highY+"\thighValObj: "+highValObj+"\tsindex :"+changeRange.height);

		final List<Integer> filteredSortedIndexList = stc.getFilteredOrderedList();
		for (int sortedIndex = changeRange.width; sortedIndex <= changeRange.height; sortedIndex++) {
			final int modelIndex = filteredSortedIndexList.get (sortedIndex);
			final int viewIndex = table.convertRowIndexToView (modelIndex);
			if (viewIndex != -1) {
				rsm.removeSelectionInterval (viewIndex, viewIndex);
			}
		}
		
		if (!alreadyAdjusting) {
			rsm.setValueIsAdjusting (false);
		}
    }

    
    /**
     * Method that returns two arrays corresponding to the low and high indices of the
     * current slider settings in terms of the associated SortedTableColumn set 
     * @param activeColumnViewIndex
     * @return
     */
    List<List<Dimension>> getColumnSortedIndexRanges (final int activeColumnViewIndex) {
    	final int columnCount = table.getColumnModel().getColumnCount();
    	final List<List<Dimension>> sortedIndices = new ArrayList<List<Dimension>> ();
    	
    	for (int viewColumnIndex = 0; viewColumnIndex < columnCount; viewColumnIndex++) {
    		calibrateColumnInteractor (viewColumnIndex);
    		final List<Dimension> sortedIndexRanges = sortedIndexRangeFromRangeModel (viewColumnIndex);
	    	sortedIndices.add (sortedIndexRanges);
    	}
    	
    	if (activeColumnViewIndex >= 0 && activeColumnViewIndex < columnCount) {
    		calibrateColumnInteractor (activeColumnViewIndex);
    	}

    	return sortedIndices;
    }
    
    
    /**
     * Counts the total range given by the sortedIndexRanges variable, excluding the
     * column referenced by the excludeViewColumn variable
     * @param sortedIndexRanges
     * @param excludeViewColumn
     * @return
     */
    int getRangeTotal (final List<List<Dimension>> allSortedIndexRanges, final int excludeViewColumn) {
    	int total = 0;
    	final int columnCount = table.getColumnModel().getColumnCount();
    	final List<AbstractColumnInteractor> cInters = getAxisInteractors ();
    	
    	for (int viewColumnIndex = 0; viewColumnIndex < columnCount; viewColumnIndex++) {
    		
    		if (excludeViewColumn != viewColumnIndex) {
    	   		final AbstractColumnInteractor cInter = cInters.get (table.convertColumnIndexToModel (viewColumnIndex));
        		
    	   		if (!cInter.isEmptySelection()) {
    	   			final List<Dimension> sortedIndexRanges = allSortedIndexRanges.get (viewColumnIndex);
        			for (int rangeIndex = 0; rangeIndex < sortedIndexRanges.size(); rangeIndex++) {
        				final Dimension range = sortedIndexRanges.get (rangeIndex);
            			total += range.height - range.width;
        			}
        		}
    		}
    	}
    	return total;
    }
    
    
    /**
     * Returns a BitSet containing the view-indexed positions of columns that currently have
     * active ranges selected on them. The column indexed by the excludeViewColumn variable is disregarded.
     * @param sortedIndexRanges
     * @param excludeViewColumn
     * @return
     */
    BitSet getRangeSelectedColumns (final int excludeViewColumn) {
    	final BitSet viewColumnsWithActiveRanges = new BitSet ();
    	final int columnCount = table.getColumnModel().getColumnCount();
    	
    	final List<AbstractColumnInteractor> cInters = getAxisInteractors ();
    	for (int viewColumnIndex = 0; viewColumnIndex < columnCount; viewColumnIndex++) {
    		//if (excludeViewColumn != viewColumnIndex && (jrs.getMinimum() != low || jrs.getMaximum() != high)) {
    	   	if (excludeViewColumn != viewColumnIndex) {	 
    	   		final AbstractColumnInteractor cInter = cInters.get (table.convertColumnIndexToModel (viewColumnIndex));
        		if (!cInter.isEmptySelection()) {
        			viewColumnsWithActiveRanges.set (viewColumnIndex);
        		}
    		}
    	}
    	//System.err.println ("activeRanges: "+viewColumnsWithActiveRanges);
    	return viewColumnsWithActiveRanges;
    }
    
    
    void addRangeViaBinaryTest (final int viewColumnIndex, final int lowVal,
    		final int highVal, final boolean select, final boolean incLowVal, final boolean incHighVal) {
    	// Empty Method
    }
    
    
    /**
     * Are the ranges empty? i.e. the selection is over the whole range of each column
     * The column indexed by exceptThisViewIndex is disregarded.
     * @param exceptThisViewIndex
     * @return true if all the column selected ranges are non-active
     */
    boolean areRangesEmpty (final int exceptThisViewIndex) {
    	boolean noRangesSet = true;
    	final List<AbstractColumnInteractor> cInters = getAxisInteractors ();
    	for (int viewColumnIndex = 0; viewColumnIndex < table.getColumnModel().getColumnCount() && noRangesSet; viewColumnIndex++) {
    		if (viewColumnIndex != exceptThisViewIndex) {
    			final AbstractColumnInteractor cInter = cInters.get (table.convertColumnIndexToModel (viewColumnIndex));
	    		if (!cInter.isEmptySelection()) {
		    		noRangesSet = false;
	    		}
    		}
    	}
    	return noRangesSet;
    }
    
    
    /**
     * Compare current selected range of an interactor with
     * the cached selection value. If the cached selection indicates no selection and the
     * current selection indicates an active selection then true is returned.
     * @param activeColumnViewIndex
     * @return true if the current range has changed from a previous non-active state
     */
    boolean changedFromNoRangesSet (final int activeColumnViewIndex) {
    	boolean rangesEmpty = areRangesEmpty (activeColumnViewIndex);
    	final List<AbstractColumnInteractor> axisInteractors = getAxisInteractors ();
    	if (activeColumnViewIndex >= 0 && activeColumnViewIndex < table.getColumnModel().getColumnCount()) {
    		final AbstractColumnInteractor cInter = axisInteractors.get (table.convertColumnIndexToModel (activeColumnViewIndex));
     		rangesEmpty &= (cInter.isEmptyCachedSelection() && !cInter.isEmptySelection());
    	}
    	return rangesEmpty;
    }

    
    /**
     * Compare current selected range of an interactor with
     * the cached selection value. If the cached selection indicates an active selection and the
     * current selection indicates no selection then true is returned
     * @param activeColumnViewIndex
     * @param oldRange
     * @return true if the current range has changed to a non-active state
     */
    boolean changedToNoRangesSet (final int activeColumnViewIndex) {
    	boolean rangesEmpty = areRangesEmpty (activeColumnViewIndex);
    	final List<AbstractColumnInteractor> axisInteractors = getAxisInteractors ();
    	if (activeColumnViewIndex >= 0 && activeColumnViewIndex < table.getColumnModel().getColumnCount()) {
    		final AbstractColumnInteractor cInter = axisInteractors.get (table.convertColumnIndexToModel (activeColumnViewIndex));
      		rangesEmpty &= (cInter.isEmptySelection() && !cInter.compareCachedSelection ());
    	}
    	return rangesEmpty;
    }

    
    
    /**
     * Returns a BitSet with the conjunctive selection of all the model indices covered by the columns
     * containing active selection ranges
     * The column indexed by exceptThisViewIndex is disregarded.
     * @param sortedIndexRanges
     * @param rangeColumns
     * @param exceptThisColumn
     * @return BitSet model indices selected in all active columns
     */
    BitSet getCurrentSelection (final List<List<Dimension>> allSortedIndexRanges, final BitSet rangeColumns, final int exceptThisColumn) { 	
    	
    	final BitSet modelSelection = new BitSet ();
    	final boolean noRangesSet = rangeColumns.isEmpty();
    	LOGGER.debug ("noRangesSet: "+noRangesSet);
    	if (noRangesSet) {
    		return null;
    		
    	} else {
    		modelSelection.set (0, table.getModel().getRowCount());
    	
    		final BitSet tempModelBS = new BitSet ();
	    	for (int viewColumnIndex = 0; viewColumnIndex < table.getColumnModel().getColumnCount() && !modelSelection.isEmpty(); viewColumnIndex++) {
	    		if (rangeColumns.get (viewColumnIndex) && viewColumnIndex != exceptThisColumn) {
	    			tempModelBS.clear ();
	    			final List<Dimension> sortedIndexRanges = allSortedIndexRanges.get (viewColumnIndex);
		    		final SortedTableColumn stc = (SortedTableColumn<?>)table.getColumnModel().getColumn (viewColumnIndex);	    	
		    		for (int range = 0; range < sortedIndexRanges.size(); range++) {
		    			fillBitSet (tempModelBS, stc, sortedIndexRanges.get (range), true);
		    		}
		    		modelSelection.and (tempModelBS);
	    		}
	    	}

	    	return modelSelection;
    	}
   	}
   
   
    /**
     * Calculates the model indices currently selected in a given column (given by the stc and
     * sortedIndexRange variables) and combines them with the supplied BitSet. The combination
     * (and / or) is given by the boolean or variable (true == or, false == and)
     * @param modelSelection
     * @param stc
     * @param sortedIndexRange
     * @param or
     */
   	void fillBitSet (final BitSet modelSelection, final SortedTableColumn stc, 
   			final Dimension sortedIndexRange, final boolean or) {
	   	final List<Integer> filteredList = stc.getFilteredOrderedList();
	   	final BitSet tempBitSet = new BitSet ();
	   	for (int sortedIndex = sortedIndexRange.width; sortedIndex <= sortedIndexRange.height; sortedIndex++) {
	   		final int modelIndex = filteredList.get (sortedIndex);
	   		tempBitSet.set (modelIndex);
	   	}
	   	if (or) {
	   		modelSelection.or (tempBitSet);
	   	} else {
	   		modelSelection.and (tempBitSet);
	   	}
   	}
   
   
   	/**
   	 * Takes a model indexed BitSet with the conjunctive selection from a number of columns
   	 * It then combines this selection with a selected range from another column and adds this
   	 * conjunctive range to the table's ListSelectionModel
   	 * @param bs
   	 * @param changeRange
   	 * @param activeColumnViewIndex
   	 */
    void mergeBitSetWithSelectionModel (final BitSet bs, final Dimension changeRange, final int activeColumnViewIndex) {
    	final ListSelectionModel rsm = table.getSelectionModel();
		final SortedTableColumn stc = (SortedTableColumn<?>)table.getColumnModel().getColumn (activeColumnViewIndex);	    	
		final List<Integer> filteredSortedIndex = stc.getFilteredOrderedList();
    	
		final boolean alreadyAdjusting = rsm.getValueIsAdjusting();
		if (!alreadyAdjusting) {
			rsm.setValueIsAdjusting (true);
		}

    	for (int sortedRowIndex = changeRange.width; sortedRowIndex <= changeRange.height; sortedRowIndex++) {
    		final int modelIndex = filteredSortedIndex.get (sortedRowIndex);
    		if (bs == null || bs.get (modelIndex)) {
    			final int viewIndex = table.convertRowIndexToView (modelIndex);
    			rsm.addSelectionInterval (viewIndex, viewIndex);
    		}
    	}
    	
		if (!alreadyAdjusting) {
			rsm.setValueIsAdjusting (false);
		}
    }
    
	/**
	 * 
     * This recalculates the selection model from scratch according to all the BoundedRangeModels
     * currently active given the RangeSliders on screen.
     */
    void resetListSelectionModelFromScratch () { 	
    	
    	final List<List<Dimension>> sortedIndexRanges = getColumnSortedIndexRanges (-1);
    	final BitSet rangeColumns = getRangeSelectedColumns (-1);
    	final BitSet currentSelection = getCurrentSelection (sortedIndexRanges, rangeColumns, -1);
    	LOGGER.info("Current Selection Size: "+(currentSelection == null ? "null" : currentSelection.cardinality()));
    	
    	final ListSelectionModel rsm = table.getSelectionModel();
    	rsm.setValueIsAdjusting (true);
    	rsm.clearSelection ();
		// Copy the BitSet state to the shared selection model
    	copyBitSetToSelectionModel (currentSelection, rsm);
    	rsm.setValueIsAdjusting (false);
    }
    
    


    /**
     * Copies a model indexed BitSet to a view indexed ListSelectionModel
     * @param modelSelection
     * @param rsm
     */
    void copyBitSetToSelectionModel (final BitSet modelSelection, final ListSelectionModel rsm) {
    	// BitSet is model indexed, SelectionModel is view indexed
    	//LOGGER.info ("nsb: "+modelSelection.nextSetBit (0));
    	if (modelSelection != null) {
			for (int i = modelSelection.nextSetBit (0); i >= 0; i = modelSelection.nextSetBit (i + 1)) {
				final int viewIndex = table.convertRowIndexToView (i);
				if (viewIndex != -1) {
					rsm.addSelectionInterval (viewIndex, viewIndex);
				}
			} 
    	}
    	//LOGGER.info("rsm: "+rsm.getMinSelectionIndex()+" count: "+table.getSelectedRowCount());
    }
    
//
//  The installation/uninstall procedures and support
//
    /**
     * Creates the mouse listener for the JTable.
     */
    protected MouseWheelListener createMouseWheelListener() {
        return (Handler2)getHandler();
    }
    
    

    public static ComponentUI createUI (final JComponent comp) {
    	LOGGER.debug ("new UI instance created");
        return new ParCoordMultiplexColumnUI();
    }

    
//  Installation

    @Override
    protected void installDefaults() {
    	super.installDefaults ();
    	setNewAxisInteractors ();
    }

    /**
     * Attaches listeners to the JTable.
     */
    
    @Override
    protected void installListeners () {
    	super.installListeners ();
    	
    	mouseWheelListener = createMouseWheelListener ();
    	table.addMouseWheelListener (mouseWheelListener);
    }
    
    
    @Override
    protected void installKeyboardActions() {
        LazyActionMap.installLazyActionMap ((JParCoord)table, ParCoordMultiplexColumnUI.class, "JParCoordRS.actionMap");

        final InputMap inputMap = getInputMap (JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
		if (LOGGER.isDebugEnabled()) {
			final KeyStroke keys[] = UIUtils.sortKeyStrokesByInputActionName (inputMap);
	    	for (KeyStroke k : keys) {
	    		LOGGER.debug ("sorted k: "+k+" == "+inputMap.get(k));
	    	}
		}
		
		SwingUtilities.replaceUIInputMap ((JParCoord)table,
					JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
					inputMap);
	}

    
    static void loadActionMap (final LazyActionMap map) {
    	map.put(new Actions2("selectFirstColumn", false, false, false, true));
        map.put(new Actions2("selectLastColumn", false, true, false, true));
	
		map.put(new Actions2("selectAll"));
	    map.put(new Actions2("clearSelection"));

        map.put(new Actions2("focusHeader"));
    }
    
    



//  Uninstallation

    @Override
    protected void uninstallListeners () {
    	super.uninstallListeners ();
    	
    	table.removeMouseWheelListener (mouseWheelListener);
    }
    
    
    /*
    @Override
    protected void uninstallDefaults() {
    	if (table.getTransferHandler() instanceof UIResource) {
    		table.setTransferHandler(null);
    	}
    }
	*/

    void setNewAxisInteractors () {
    	axisInteractors = new ArrayList<AbstractColumnInteractor> ();
    	for (int column = 0; column < table.getModel().getColumnCount(); column++) {
    		addNewAxisInteractor (column);
    	}
    }
    
    // Column Interactors
    void addNewAxisInteractor (final int columnModelIndex) {
    	final SortedTableColumn<?> stc = (SortedTableColumn<?>) table.getColumnModel().getColumn (table.convertColumnIndexToView (columnModelIndex));
    	final int discreteSize = stc.getDiscreteRange();
    	final AbstractColumnInteractor cInter = (discreteSize == 0 || discreteSize > 10) 
    		? new JRangeSliderInteractor ()
    		: new JCheckBoxColumnInteractor ();
    	axisInteractors.add (columnModelIndex, cInter);
    }
    
    
    void resetColumnInteractorSelections () {
    	final List<AbstractColumnInteractor> cInters = getAxisInteractors ();
    	for (AbstractColumnInteractor cInter : cInters) {
    		cInter.resetSelection();
    	}
    }
    
    List<AbstractColumnInteractor> getAxisInteractors () { return axisInteractors; }
    
    AbstractColumnInteractor getAxisInteractor (final int columnViewIndex) {
    	final List<AbstractColumnInteractor> axisInteractors = getAxisInteractors ();
    	final int columnModelIndex = table.convertColumnIndexToModel (columnViewIndex);
    	return (columnModelIndex >= 0 && columnModelIndex < axisInteractors.size() 
    			? axisInteractors.get (columnModelIndex) : null);
    }
    
    
    // New paint methods
    @Override
	void paintLegend (final Graphics gLegend, final int cMin, final int cMax) {
    	if (gLegend != null) {
    		super.paintLegend (gLegend, cMin, cMax);
    		prepareInteractorComponents (INTERACTOR_MIN_SIZE);
        	paintPretendComponents (gLegend, cMin, cMax);      	
    	}
    }
    
    
    void prepareInteractorComponents (final int minInteractorSize) {
    	final List<AbstractColumnInteractor> cInters = getAxisInteractors ();
    	final Set <JComponent> compSet = new HashSet <JComponent> ();
    	
    	for (AbstractColumnInteractor cInter : cInters) {
    		if (compSet.add (cInter.getComponent())) {
    			cInter.prepareComponent (table, visibleRect, minInteractorSize);
    		}
    	}
    }
    
    
    void paintPretendComponents (final Graphics graphics, final int cMin, final int cMax) {
    	final TableColumnModel columnModel = table.getColumnModel();
    	final int columns = columnModel.getColumnCount();
	    int x = 0;
	    //Rectangle clipRect = g.getClipBounds();
	    
	    final boolean lToR = table.getComponentOrientation().isLeftToRight();
	    final int columnStart = lToR ? 0 : columns;
	    final int columnEnd = lToR ? columns : -1;
	    final int columnIncrement = lToR ? 1 : -1;
	    
		for (int column = columnStart; column != columnEnd; column += columnIncrement) {
			final SortedTableColumn stc = (SortedTableColumn)columnModel.getColumn(column);
			final int width = stc.getWidth();
		    if (column >= cMin && column <= cMax && table.getEditingColumn() != column) {
	    		calibrateColumnInteractor (column);
	        	final AbstractColumnInteractor cInter = getAxisInteractor (column);
	        	cInter.draw (graphics, x - 1 + (width / 2), visibleRect, column, table);
		    }
		    x += width;
		}
    }
    
    void calibrateColumnInteractor (final int viewColumnIndex) {
    	final AbstractColumnInteractor cInter = getAxisInteractor (viewColumnIndex);
    	cInter.prepareComponent (table, visibleRect, 16);
		cInter.setComponentWithModel();
    	final SortedTableColumn<?> stc = (SortedTableColumn<?>) table.getColumnModel().getColumn (viewColumnIndex);
		cInter.setDirection (stc.getCurrentOrder());
    }
    
    
    int findColumnX (final int viewColumnIndex) {
    	final TableColumnModel columnModel = table.getColumnModel();
    	final int columns = columnModel.getColumnCount();
    	final boolean lToR = table.getComponentOrientation().isLeftToRight();
    	final int columnStart = lToR ? 0 : columns;
    	final int columnEnd = viewColumnIndex;
    	final int columnIncrement = lToR ? 1 : -1;
    	int x = 0;
	    
	    for (int column = columnStart; column != columnEnd; column += columnIncrement) {
	    	final int width = columnModel.getColumn(column).getWidth();
		    x += width;
		}
	    
	    return x;
    }
    
    int findColumnMidX (final int viewColumnIndex) {
    	final TableColumnModel columnModel = table.getColumnModel();
    	final int columns = columnModel.getColumnCount();
    	final boolean lToR = table.getComponentOrientation().isLeftToRight();
    	final int columnStart = lToR ? 0 : columns;
    	final int columnEnd = lToR ? viewColumnIndex + 1 : viewColumnIndex - 1;
    	final int columnIncrement = lToR ? 1 : -1;
    	int x = 0, midx = 0;
	    
	    for (int column = columnStart; column != columnEnd; column += columnIncrement) {
	    	final int width = columnModel.getColumn(column).getWidth();    
		    midx = x + (width / 2);
		    x += width;
		}
	    
	    return midx;
    }
}
    
  
    
abstract class AbstractColumnInteractor extends MouseAdapter implements ColumnInteractorInterface  {
	JComponent comp;
	Object selectionModel;
	Object oldSelectionModel;
	
	public JComponent getComponent () { return comp; }
}
