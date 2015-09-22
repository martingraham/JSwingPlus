package ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SortOrder;

import model.shared.SortedTableColumn;


class JCheckBoxColumnInteractor extends AbstractColumnInteractor {

	static final JCheckBox JCB = new JCheckBox ();
	transient int[] yPositions;
	
	JCheckBoxColumnInteractor () {
		super ();
		selectionModel = new BitSet ();
		oldSelectionModel = new BitSet ();
		comp = JCB;
	}


	@Override
	public void setComponentWithModel() {
		// empty method
	}
	
	@Override
	public void resetSelection () {
		final BitSet bset = (BitSet)selectionModel;
   		bset.clear ();
	}
	
	@Override
	public boolean isEmptySelection () {
		final BitSet bset = (BitSet)selectionModel;
		return bset.isEmpty();
	}
	
	
	@Override
	public boolean firstSelectionAlwaysRemove () {
		return false;
	}
	
	@Override
	public void setDirection (final SortOrder sortOrder) {
		// empty method
	}

	
	@Override
	public void cacheSelection () {
		final BitSet other = (BitSet)oldSelectionModel;
		final BitSet bset = (BitSet)selectionModel;
		
		other.clear ();
		other.or (bset);
	}
	
	@Override
	public boolean compareCachedSelection () {
		final BitSet other = (BitSet)oldSelectionModel;
		final BitSet bset = (BitSet)selectionModel;
		
		return other.equals (bset);
	}
	
	


	@Override
	public boolean isEmptyCachedSelection () {
		final BitSet bset = (BitSet)oldSelectionModel;
		return bset.isEmpty();
	}
	
	
	
	@Override
	public int getComponentXOffset () {
		return JCB.getWidth();
	}
	
	@Override
	public void draw (final Graphics graphics, final int colMidX, final Rectangle visibleRect, final int columnViewIndex, final JTable table) {
		final BitSet discreteSelections = (BitSet)selectionModel;
		yPositions = getDiscreteYPos (columnViewIndex, visibleRect, table);
		
 		for (int discIndex = 0; discIndex < yPositions.length; discIndex++) {
    		JCB.setLocation (colMidX - getComponentXOffset(), yPositions[discIndex] - (JCB.getHeight() / 2));
     		JCB.setSelected (discreteSelections.get(discIndex));
    		final Graphics compGraphics = graphics.create (JCB.getX(), JCB.getY(), JCB.getWidth(), (int)JCB.getHeight());
    		JCB.paint (compGraphics);
    	}
 		JCB.setLocation (colMidX, 0);
	}
	
	@Override
	public List<Dimension> getSelectionRangeInY () {
		final BitSet bset = (BitSet)selectionModel;
		final List<Dimension> selList = new ArrayList<Dimension> ();
		for (int bit = bset.nextSetBit(0); bit >= 0; bit = bset.nextSetBit (bit + 1)) {
			selList.add (new Dimension (yPositions [bit] - 1, yPositions [bit] + 1));
		}
		return selList;
	}
	
	@Override
	public void prepareComponent (final JTable table, final Rectangle area, final int minScalar) {
		//boolean empty = isEmptySelection ();
   		JCB.setSize (new Dimension (Math.max (16, minScalar), Math.max (16, minScalar)));
		JCB.setOpaque (false);
		JCB.setForeground (table.getSelectionForeground());
	}
	
	@Override
	public boolean setComponentSpecificToolTip (int y, int viewColumnIndex, Rectangle visibleRect, ParCoordUI pcui) {
		//
		return false;
	}
	
	public int[] getDiscreteYPos (final int columnViewIndex, final Rectangle visibleRect, final JTable table) {
		final SortedTableColumn stc = (SortedTableColumn) table.getColumnModel().getColumn(columnViewIndex);
		
		final int discreteSize = stc.getDiscreteRange(); 
       	if (discreteSize > 0 && discreteSize <= 10) {
	       	final int yPos[] = new int [discreteSize];
	       	final boolean numeric = stc.isA (Integer.class);
	 		for (int discIndex = 0; discIndex < discreteSize; discIndex++) {
	 			Object obj = null;
	 			if (numeric) {
	 				obj = Integer.valueOf (discIndex + ((Integer)stc.getMin()).intValue());
	 			} else {
	 				final int modelRowIndex = ((List<Integer>)stc.getDiscreteList()).get(discIndex);
	 	 			obj = table.getModel().getValueAt (modelRowIndex, stc.getModelIndex());
	 			}
	 			
	 			yPos[discIndex] = ((ParCoordUI)table.getUI()).getY (obj, columnViewIndex, visibleRect);
	 		}
	    	return yPos;
       	}
       	return new int[0];
    }
	
	@Override
	public List<DeltaRange> getOldToNewChanges () {
		final BitSet other = (BitSet)oldSelectionModel;
		final BitSet bset = (BitSet)selectionModel;
		final BitSet eor = new BitSet ();
		eor.or (other);
		eor.xor (bset);
		
		final List<DeltaRange> deltaRanges = new ArrayList<DeltaRange> ();
		for (int bit = eor.nextSetBit(0); bit >= 0; bit = eor.nextSetBit (bit + 1)) {
			final DeltaRange dRange = new DeltaRange (yPositions [bit] - 1, yPositions [bit] + 1,
								true, true, other.get (bit));
			deltaRanges.add (dRange);
		}
		
		if (isEmptyCachedSelection ()) {
			final int bit = eor.nextSetBit (0);
			if (bit >= 0) {
				if (bit > 0) {
					final DeltaRange dRange = new DeltaRange (yPositions [0] - 1, yPositions [bit],
							true, false, true);
					deltaRanges.add (dRange);
				}
				
				if (bit < yPositions.length - 1) {
					final DeltaRange dRange = new DeltaRange (yPositions [bit], yPositions [yPositions.length - 1],
							false, true, true);
					deltaRanges.add (dRange);
				}
			}
		}
		
		else if (isEmptySelection ()) {
			final DeltaRange dRange = new DeltaRange (yPositions [0], yPositions [yPositions.length - 1],
					true, true, false);
			deltaRanges.clear ();
			deltaRanges.add (dRange);
		}
		
		//System.err.println (deltaRanges);
		
		return deltaRanges.isEmpty() ? (List<DeltaRange>)Collections.EMPTY_LIST : deltaRanges;
	}
	
	@Override
	public void mouseReleased (final MouseEvent mEvent) {
		
		int yPosIndex = -1, minDiff = Integer.MAX_VALUE;
		for (int index = 0; index < yPositions.length; index++) {
			final int yDiff = Math.abs (mEvent.getY() - yPositions[index]);
			if (yDiff < minDiff) {
				minDiff = yDiff;
				yPosIndex = index;
			}
		}
		
		if (yPosIndex >= 0 && minDiff <= comp.getHeight()) {
			((BitSet)selectionModel).flip(yPosIndex);
		}
	}
}
