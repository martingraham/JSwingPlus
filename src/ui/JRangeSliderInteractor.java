package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;

import model.shared.SortedTableColumn;

import prefuse.util.ui.JRangeSlider2;
import swingPlus.shared.border.MatteBorder2;
import util.GraphicsUtil;
import util.Messages;
import util.colour.ColorUtilities;


class JRangeSliderInteractor extends AbstractColumnInteractor {

	static final JRangeSlider2 JRS = new JRangeSlider2 (0, 100, 20, 50, JRangeSlider2.VERTICAL);
	static final Dimension prefDim = new Dimension ();
	final static Border INACTIVE_BORDER = BorderFactory.createEmptyBorder ();
	final static Border ACTIVE_BORDER = new MatteBorder2 (0, 1, 0, 1, null, new Color (255, 255, 255, 64), null, new Color (0, 0, 0, 64));

	private static final Class<?> KLASS = JRangeSliderInteractor.class;
	static final String UPPER_TEXT = Messages.getString (KLASS, "upperText");
	static final String LOWER_TEXT = Messages.getString (KLASS, "lowerText");
	static final String RANGE_TEXT = Messages.getString (KLASS, "thumbText");

	
	JRangeSliderInteractor () {
		super ();
		selectionModel = new DefaultBoundedRangeModel (0, 500, 0, 500);
		oldSelectionModel = new DefaultBoundedRangeModel (0, 500, 0, 500);
		comp = JRS;
	}


	@Override
	public void setComponentWithModel() {
		((JRangeSlider2)comp).setModel((BoundedRangeModel)selectionModel);
	}
	
	@Override
	public void resetSelection () {
		final BoundedRangeModel brModel = (BoundedRangeModel)selectionModel;
   		brModel.setRangeProperties (brModel.getMinimum(), brModel.getMaximum() - brModel.getMinimum(), 
				brModel.getMinimum(), brModel.getMaximum(), false);
	}
	
	@Override
	public boolean isEmptySelection () {
		final BoundedRangeModel brm = (BoundedRangeModel)selectionModel;
		return brm.getValue() == brm.getMinimum() && brm.getValue() + brm.getExtent() == brm.getMaximum();
	}
	
	@Override
	public boolean firstSelectionAlwaysRemove () {
		return true;
	}
	
	@Override
	public void setDirection (final SortOrder sortOrder) {
		JRS.setDirection (sortOrder == SortOrder.ASCENDING ? JRangeSlider2.LEFTRIGHT_TOPBOTTOM : JRangeSlider2.RIGHTLEFT_BOTTOMTOP);
	}


	
	@Override
	public void cacheSelection () {
		final BoundedRangeModel brm = (BoundedRangeModel)selectionModel;
		final BoundedRangeModel other = (BoundedRangeModel)oldSelectionModel;
		
		other.setRangeProperties (brm.getValue(), brm.getExtent(), brm.getMinimum(), brm.getMaximum(), false);
	}
	
	@Override
	public boolean compareCachedSelection () {
		final BoundedRangeModel other = (BoundedRangeModel)oldSelectionModel;
		final BoundedRangeModel brm = (BoundedRangeModel)selectionModel;
		
		return (brm.getMinimum() == other.getMinimum() && brm.getMaximum() == other.getMaximum())
			&& brm.getValue() == other.getValue() && brm.getExtent() == other.getExtent();
	}
	
	@Override
	public boolean isEmptyCachedSelection () {
		final BoundedRangeModel other = (BoundedRangeModel)oldSelectionModel;
		return other.getValue() == other.getMinimum() && other.getValue() + other.getExtent() == other.getMaximum();
	}
	
	
	
	@Override
	public int getComponentXOffset () {
		return JRS.getWidth() / 2;
	}
	
	@Override
	public void draw (final Graphics graphics, final int colMidX, final Rectangle visibleRect, final int columnViewIndex, final JTable table) {
    	JRS.setLocation (colMidX - getComponentXOffset(), visibleRect.y - JRS.getArrowHeight());
    	final boolean fullRangeSelected = (JRS.getHighValue() == JRS.getMaximum() && JRS.getLowValue() == JRS.getMinimum());	
    	JRS.setThumbColor (fullRangeSelected ? GraphicsUtil.NULLCOLOUR : ColorUtilities.addAlpha (table.getSelectionForeground(), 32));
		JRS.setForeground (fullRangeSelected ? Color.lightGray : ColorUtilities.addAlpha (table.getSelectionForeground(), 192));
    	JRS.setThumbBorder (fullRangeSelected ? INACTIVE_BORDER : ACTIVE_BORDER);
		final Graphics compGraphics = graphics.create (JRS.getX(), JRS.getY(), JRS.getWidth(), JRS.getHeight());
    	JRS.paint (compGraphics);
	}
	
	@Override
	public List<Dimension> getSelectionRangeInY () {
		final BoundedRangeModel brm = (BoundedRangeModel)selectionModel;
		final int lowerY = JRS.toScreen (brm.getValue()) + JRS.getY();
		final int upperY = JRS.toScreen (brm.getValue() + brm.getExtent()) + JRS.getY();
		final List<Dimension> selList = new ArrayList<Dimension> ();
		selList.add (new Dimension (lowerY, upperY));
		return selList;
	}
	
	@Override
	public void prepareComponent (final JTable table, final Rectangle area, final int minScalar) {
   		prefDim.setSize (Math.max (minScalar, JRangeSlider2.PREFERRED_BREADTH), Math.max (minScalar, area.height + (JRS.getArrowHeight() * 2)));
   		if (prefDim.height != JRS.getHeight() || prefDim.width != JRS.getWidth()) {
	   		JRS.setArrowHeight (Math.max (minScalar, JRangeSlider2.PREFERRED_BREADTH));
	   		JRS.setSize (prefDim);
	   		JRS.setPreferredSize (prefDim);
			JRS.setOpaque (false);
			JRS.setThumbColor (ColorUtilities.addAlpha (table.getSelectionForeground(), 64));
   		}
	}
	
	@Override
	public boolean setComponentSpecificToolTip (final int y, final int viewColumnIndex, final Rectangle visibleRect, final ParCoordUI pcui) {
		return setComponentSpecificToolTip (y, viewColumnIndex, visibleRect, pcui, false);
	}
	
	
	protected boolean setComponentSpecificToolTip (final int y, final int viewColumnIndex, 
			final Rectangle visibleRect, final ParCoordUI pcui, final boolean draggedThumb) {

		final int handle = JRS.pickHandle (y - visibleRect.y + JRS.getArrowHeight());
		if (handle == JRangeSlider2.PICK_LEFT_OR_TOP || handle == JRangeSlider2.PICK_RIGHT_OR_BOTTOM
					|| (handle == JRangeSlider2.PICK_THUMB && draggedThumb)) {
			
			final Dimension range = new Dimension (0, 1);
			final int lowy = JRS.toScreen (JRS.getLowValue());
			final int highy = JRS.toScreen (JRS.getHighValue());
			//System.err.println ("low y: "+lowy);
			//System.err.println ("high y: "+highy);
			final Object hval = pcui.getValue (highy + getComponent().getY(), viewColumnIndex, visibleRect); 
			final Object lval = pcui.getValue (lowy + getComponent().getY(), viewColumnIndex, visibleRect); 	
			
			if (handle == JRangeSlider2.PICK_LEFT_OR_TOP) {
				pcui.setTooltip (LOWER_TEXT, lval, lval, viewColumnIndex, range);
			} else if (handle == JRangeSlider2.PICK_RIGHT_OR_BOTTOM) {
				pcui.setTooltip (UPPER_TEXT, hval, hval, viewColumnIndex, range);
			} else if (handle == JRangeSlider2.PICK_THUMB) {
	         	final SortedTableColumn stc = (SortedTableColumn) pcui.table.getColumnModel().getColumn (viewColumnIndex);
	         	if (!stc.isEmpty ()) {
		         	stc.getRange (range, (Comparable)lval, (Comparable)lval, 
		         			(Comparable)hval, (Comparable)hval); 
	         	}
				pcui.setTooltip (RANGE_TEXT, lval, hval, viewColumnIndex, range);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public List<DeltaRange> getOldToNewChanges () {
		final BoundedRangeModel other = (BoundedRangeModel)oldSelectionModel;
		final BoundedRangeModel brm = (BoundedRangeModel)selectionModel;
		final List<DeltaRange> deltaRanges = new ArrayList<DeltaRange> ();
		
		final int oldValMinY = JRS.toScreen (other.getValue()) + JRS.getY();
		final int newValMinY = JRS.toScreen (brm.getValue()) + JRS.getY();
		final int oldValMaxY = JRS.toScreen (other.getValue() + other.getExtent()) + JRS.getY();
		final int newValMaxY = JRS.toScreen (brm.getValue() + brm.getExtent()) + JRS.getY();
		
		if (overlap (other, brm)) {
		
			if (other.getValue() != brm.getValue()) {
				if (oldValMinY != newValMinY) {
					final boolean rangeTopEndReduced = oldValMinY < newValMinY;
					deltaRanges.add (new DeltaRange (Math.min (oldValMinY, newValMinY),
							Math.max (oldValMinY, newValMinY), 
							true,
							false,
							rangeTopEndReduced));
				}
			}
			if (other.getValue() + other.getExtent() != brm.getValue() + brm.getExtent()) {
				if (oldValMaxY != newValMaxY) {
					final boolean rangeBottomEndReduced = oldValMaxY > newValMaxY;
					deltaRanges.add (new DeltaRange (Math.min (oldValMaxY, newValMaxY),
							Math.max (oldValMaxY, newValMaxY), 
							false,
							true,
							rangeBottomEndReduced));
				}
			}
		}
		
		else {
			//System.err.println ("no overlap between current and cached - fast mouse drag");
			deltaRanges.add (new DeltaRange (oldValMinY, oldValMaxY, true, true, true));
			deltaRanges.add (new DeltaRange (newValMinY, newValMaxY, true, true, false));
		}
		
		return deltaRanges.isEmpty() ? (List<DeltaRange>)Collections.EMPTY_LIST : deltaRanges;
	}
	
	
	boolean overlap (final BoundedRangeModel oldModel, final BoundedRangeModel newModel) {
		final BoundedRangeModel minModel = (oldModel.getValue() < newModel.getValue() ? oldModel : newModel);
		final BoundedRangeModel maxModel = (oldModel == minModel ? newModel : oldModel);
		return (maxModel.getValue() >= minModel.getValue() && maxModel.getValue() <= minModel.getValue() + minModel.getExtent());
	}
	
	@Override
	public void mouseReleased (final MouseEvent mEvent) {
		final MouseListener[] mListeners = comp.getMouseListeners ();
		for (MouseListener mListener : mListeners) {
			mListener.mouseReleased (mEvent);
		}
	}
	
	@Override
	public void mouseDragged (final MouseEvent mEvent) {
		final MouseMotionListener[] mmListeners = comp.getMouseMotionListeners ();
		for (MouseMotionListener mmListener : mmListeners) {
			mmListener.mouseDragged (mEvent);
		}

		final JTable table = (JTable)mEvent.getComponent();
		final ParCoordUI tableUI = (ParCoordUI)table.getUI();
		final Point absPoint = mEvent.getLocationOnScreen();
		SwingUtilities.convertPointFromScreen (absPoint, table);
		final int viewColumnIndex = table.columnAtPoint (absPoint);
		final boolean toolTipSet = this.setComponentSpecificToolTip (mEvent.getY() + getComponent().getY(), 
				viewColumnIndex, tableUI.visibleRect, tableUI, true);
		if (toolTipSet) {
			ToolTipManager.sharedInstance().mouseMoved (
				GraphicsUtil.convertMouseEvent (mEvent, table, -absPoint.x + mEvent.getX(), 0)
			);
		}
	}
	
	@Override
	public void mousePressed (final MouseEvent mEvent) {
		final MouseListener[] mListeners = comp.getMouseListeners ();
		for (MouseListener mListener : mListeners) {
			mListener.mousePressed (mEvent);
		}	
	}
}
