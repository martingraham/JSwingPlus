package ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SortOrder;


/**
 * In ParCoordMultiplexColumnUI, the interactions with the columns is made through a 
 * number of different interactive elements i.e. a range slider for continuous ranges, or
 * a set of checkboxes for small, discrete sets of choices
 * This interface aims to make a common interface to the differing styles of interaction so
 * the ParCoordMultiplexColumnUI class can draw them, interrogate them and pass on MouseEvents
 * without caring what particular swing component is being used as a rubberstamp or what model
 * is being used to store that component's selection state for a column
 * @author cs22
 *
 */
interface ColumnInteractorInterface {
	// Set the shared swing component for the class to use the info in this instance
	void setComponentWithModel ();
	
	// Selection based methods.
	// Selections in this interface/derived classes are in terms of the swing component
	// that is used to render/interact with the column i.e. the selection model will be
	// a BoundedRangeModel for a JRangeSlider2, a BitSet for a set of checkboxes.
	// These are translated to y-coords by other methods in this interface, and the y-coords
	// are converted to row indices by the ParCoordMultiplexColumnUI class and related table
	// and parcoord column classes.
	void resetSelection ();
	boolean isEmptySelection ();
	void cacheSelection ();
	boolean compareCachedSelection ();
	boolean isEmptyCachedSelection ();
	boolean firstSelectionAlwaysRemove ();
	
	// for components where directionality matters
	void setDirection (SortOrder sortOrder);
	
	// Get the swing component, and methods to prepare for and draw the component to screen
	JComponent getComponent ();
	int getComponentXOffset ();
	void prepareComponent (JTable table, Rectangle area, int minScalar);
	void draw (Graphics graphics, int columnMidX, Rectangle visibleRect, int columnViewIndex, JTable table);
	
	// set a tooltip for the shared component
	boolean setComponentSpecificToolTip (int y, int viewColumnIndex, Rectangle visibleRect, ParCoordUI pcui);

	// get the y-ranges that cover current selections made in the column
	List<Dimension> getSelectionRangeInY ();

	/**
	 *  get the y-ranges that cover the difference in selections made between the cached
	 *  selection state and the current selection states
	 * @return
	 */
	List<DeltaRange> getOldToNewChanges ();
}
