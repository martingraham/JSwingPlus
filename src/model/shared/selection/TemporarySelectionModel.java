package model.shared.selection;

import java.util.BitSet;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;

import model.shared.SortedTableColumn;

import org.apache.log4j.Logger;

import ui.ParCoordUI.Combinator;


public class TemporarySelectionModel {

	private final static Logger LOGGER = Logger.getLogger (TemporarySelectionModel.class);
	BitSet storedState;
	
	public TemporarySelectionModel () {
		storedState = new BitSet ();
	}
	
	public void selectRange (final int from, final int to, final JTable table, 
			final SortedTableColumn stc, final Combinator select) {
		selectRange (from, to, table.getRowSorter(), table.getSelectionModel(), stc, select);
	}
	
	
	public void selectRange (final int from, final int to, final RowSorter<? extends TableModel> rowSorter, 
			final ListSelectionModel lsm, final SortedTableColumn stc, final Combinator select) {
		final List<Integer> columnOrder = stc.getFilteredOrderedList();
		final boolean adjusting = lsm.getValueIsAdjusting();
		//List<ListSelectionListener> listenerList = new ArrayList<ListSelectionListener> ();
		//for (ListSelectionListener listener : lsm.)
		
		if (!adjusting) {
			lsm.setValueIsAdjusting (true);
		}
		final boolean useRowSorter = (rowSorter != null && rowSorter.getModelRowCount() > 0);

		
    	for (int n = from; n <= to; n++) {
    		final int primaryModelIndex = columnOrder.get(n).intValue();
    		final int primaryViewIndex = (!useRowSorter ? primaryModelIndex : rowSorter.convertRowIndexToView (primaryModelIndex));		
    		final boolean inLastSelect = storedState.get (primaryViewIndex);
    		
    		if (select == Combinator.ADD || (select == Combinator.FILTER && inLastSelect)) {
    			lsm.addSelectionInterval (primaryViewIndex, primaryViewIndex);
    		} else {
       			lsm.removeSelectionInterval (primaryViewIndex, primaryViewIndex);
    		}
    	}
    	
		if (!adjusting) {
			lsm.setValueIsAdjusting (false);
		}
	}
	
	
	
	public void selectRange2 (final int from, final int to, final RowSorter<? extends TableModel> rowSorter, 
			final ListSelectionModel lsm, final SortedTableColumn stc, final Combinator select) {
		final List<Integer> columnOrder = stc.getFilteredOrderedList();
		final boolean adjusting = lsm.getValueIsAdjusting();
		if (!adjusting) {
			lsm.setValueIsAdjusting (true);
		}
		
		if (select == Combinator.FILTER) { 
			for (int row = 0; row < from; row++) {
				final int primaryModelIndex = columnOrder.get(row).intValue();
				final int primaryViewIndex = (rowSorter == null ? primaryModelIndex : rowSorter.convertRowIndexToView (primaryModelIndex));
				if (lsm.isSelectedIndex (primaryViewIndex)) {
					lsm.removeSelectionInterval (primaryViewIndex, primaryViewIndex);
				}
			}
		}

		if (select == Combinator.ADD) { 
	    	for (int n = from; n <= to; n++) {
	    		final int primaryModelIndex = columnOrder.get(n).intValue();
	    		final int primaryViewIndex = (rowSorter == null ? primaryModelIndex : rowSorter.convertRowIndexToView (primaryModelIndex));
	    		if (!lsm.isSelectedIndex (primaryViewIndex)) {
	    			lsm.addSelectionInterval (primaryViewIndex, primaryViewIndex);
	    		}
	    	}
		}
    	
		if (select == Combinator.FILTER) { 
			final int last = stc.getLastIndexOf (stc.getMax());
			for (int row = to + 1; row < last; row++) {
				final int primaryModelIndex = columnOrder.get(row).intValue();
				final int primaryViewIndex = (rowSorter == null ? primaryModelIndex : rowSorter.convertRowIndexToView (primaryModelIndex));
				if (lsm.isSelectedIndex (primaryViewIndex)) {
					lsm.removeSelectionInterval (primaryViewIndex, primaryViewIndex);
				}
			}
		}
    	
		if (!adjusting) {
			lsm.setValueIsAdjusting (false);
		}
	}

	
	public void selectRange2 (final int from, final int to, final BitSet bs, 
			final RowSorter<? extends TableModel> rowSorter, final SortedTableColumn stc, final Combinator select) {
		final List<Integer> columnOrder = stc.getFilteredOrderedList();
		final boolean useRowSorter = (rowSorter != null && rowSorter.getModelRowCount() > 0);
		
		if (select == Combinator.FILTER) { 
			for (int row = 0; row < from; row++) {
				final int modelIndex = columnOrder.get(row).intValue();
				final boolean modelIndexInView = (!useRowSorter ? true : rowSorter.convertRowIndexToView (modelIndex) != -1);
				if (modelIndexInView && bs.get (modelIndex)) {
					bs.clear (modelIndex);
				}
			}
		}

		if (select == Combinator.ADD) { 
	    	for (int row = from; row <= to; row++) {
	    		final int modelIndex = columnOrder.get(row).intValue();
				if (modelIndex < 0 ) {
					LOGGER.debug ("model Index: "+modelIndex);
				}
				if (modelIndex >= columnOrder.size() ) {
					LOGGER.debug ("model Index: "+modelIndex);
				}
				if (modelIndex >= rowSorter.getModelRowCount()) {
					LOGGER.debug ("model Index: "+modelIndex+", mrc: "+rowSorter.getModelRowCount()+", vrc: "+rowSorter.getViewRowCount());
				}
				final boolean modelIndexInView = (!useRowSorter ? true : rowSorter.convertRowIndexToView (modelIndex) != -1);
	    		if (modelIndexInView && !bs.get (modelIndex)) {
	    			bs.set (modelIndex);
	    		}
	    	}
		}
    	
		if (select == Combinator.FILTER) { 
			final int last = stc.getLastIndexOf (stc.getMax());
			for (int row = to + 1; row < last; row++) {
				final int modelIndex = columnOrder.get(row).intValue();
				final boolean modelIndexInView = (!useRowSorter ? true : rowSorter.convertRowIndexToView (modelIndex) != -1);
				if (modelIndexInView && bs.get (modelIndex)) {
					bs.clear (modelIndex);
				}
			}
		}
	}
	
	
	public void modify (final ListSelectionModel target, final BitSet bitSet, 
			final RowSorter<? extends TableModel> rowSorter, final SortedTableColumn stc, final Combinator select) {
		//final List<Integer> columnOrder = stc.getFilteredOrderedList();
		final boolean adjusting = target.getValueIsAdjusting();
		if (!adjusting) {
			target.setValueIsAdjusting (true);
		}
		final boolean useRowSorter = (rowSorter != null && rowSorter.getModelRowCount() > 0);
		
		if (select == Combinator.FILTER) { 
			for (int row = 0; row < bitSet.nextSetBit(0); row++) {
				final int primaryModelIndex = row; //columnOrder.get(row).intValue();
				final int primaryViewIndex = (!useRowSorter ? primaryModelIndex : rowSorter.convertRowIndexToView (primaryModelIndex));
				if (target.isSelectedIndex (primaryViewIndex)) {
					target.removeSelectionInterval (primaryViewIndex, primaryViewIndex);
				}
			}
		}
		
		//if (select == Combinator.ADD) {
		if (!bitSet.isEmpty()) {
			for (int row = bitSet.nextSetBit(0); row < bitSet.length(); row++) {
				final int primaryModelIndex = row; //columnOrder.get(row).intValue();
				final int primaryViewIndex = (!useRowSorter ? primaryModelIndex : rowSorter.convertRowIndexToView (primaryModelIndex));
				final boolean targetSelected = target.isSelectedIndex (primaryViewIndex);
				final boolean modifierSelected = bitSet.get (primaryModelIndex);
				if (primaryViewIndex != -1 && !targetSelected && modifierSelected && select == Combinator.ADD) {
					target.addSelectionInterval (primaryViewIndex, primaryViewIndex);
				}
				else if (targetSelected && !modifierSelected && select == Combinator.FILTER) {
					target.removeSelectionInterval (primaryViewIndex, primaryViewIndex);
				}
			}
		}
		//}
		
		if (select == Combinator.FILTER) { 
			final int last = stc.getLastIndexOf (stc.getMax());
			for (int row = bitSet.length(); row < last; row++) {
				final int primaryModelIndex = row; //columnOrder.get(row).intValue();
				final int primaryViewIndex = (!useRowSorter ? primaryModelIndex : rowSorter.convertRowIndexToView (primaryModelIndex));
				if (target.isSelectedIndex (primaryViewIndex)) {
					target.removeSelectionInterval (primaryViewIndex, primaryViewIndex);
				}
			}
		}
    	
		if (!adjusting) {
			target.setValueIsAdjusting (false);
		}
	}
	
	
	
	public void copy (final JTable table) {
		if (table != null && table.getSelectionModel() != null) {
			copy (table.getSelectionModel());
		}
	}
	
	public void copy (final ListSelectionModel lsm) {
        for (int n = lsm.getMinSelectionIndex(); n < lsm.getMaxSelectionIndex(); n++) {
        	storedState.set (n, lsm.isSelectedIndex (n));
        }
	}
	
	public void clear () {
		storedState.clear ();
	}
}
