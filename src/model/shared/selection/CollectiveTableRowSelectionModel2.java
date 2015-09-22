package model.shared.selection;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;


import org.apache.log4j.Logger;

import swingPlus.shared.CrossFilteredTable;
import util.threads.LatchRunnable;
import util.threads.ParallelCollectionProcess;

/**
 * Class that listens to a number of JTables (+extensions of) 
 * and maintains a selection model that is the logical AND of
 * all those tables' selection models
 * 
 * @author cs22
 */
public class CollectiveTableRowSelectionModel2 extends LinkedTableRowSelectionModel {

	
	protected ListSelectionModel uberSelectionModel;	// Shared SelectionModel
	boolean updateOnFinishAdjusting;		// Update only when incoming ListSelectionEvent's SelectionModel has finished adjusting?
	private static final Logger LOGGER = Logger.getLogger (CollectiveTableRowSelectionModel2.class);	
	
	
	public CollectiveTableRowSelectionModel2 () {
		super (null);
		
		uberSelectionModel = new DefaultListSelectionModel ();
		uberSelectionModel.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		updateOnFinishAdjusting = true;
	}
	
	@Override
	public void addJTable (final JTable table) {
		super.addJTable (table);
		if (table instanceof CrossFilteredTable) {
			((CrossFilteredTable)table).setUberSelection (getUberSelectionModel());
		}
	}
	
	@Override
	public void removeJTable (final JTable table) {
		super.removeJTable (table);
		if (table instanceof CrossFilteredTable) {
			((CrossFilteredTable)table).setUberSelection (null);
		}
	}
	
	/**
	 * Instead of translating changes to other ListSelectionModels, in this class we
	 * calculate a model-indexed based 'uberselection'model that logically ANDs all
	 * the currently registered JTable's ListSelectionModels.
	 * Each of these JTable's is then messaged that the uberselection has changed (if it has)
	 */
	@Override
	protected void passSelectionStateToOtherTableModels (final ListSelectionEvent lsEvent) {
		final ListSelectionModel rlsModel = (ListSelectionModel) lsEvent.getSource();
		//System.err.println ("e: "+e);
		
		// if the updateOnFinishAdjusting variable is true, only update when the calling selectionModel has finished updating 
		if (!updateOnFinishAdjusting || !rlsModel.getValueIsAdjusting()) {
			
			final boolean uberIsEmptyBefore = uberSelectionModel.isSelectionEmpty ();
			final JTable jTable = modelTableMap.get (rlsModel);
			
			//System.err.println ("rls model: "+rlsModel);
			
			// Make a list of non-empty selection models from the associated tables
			final List<ListSelectionModel> activeSelections = new ArrayList<ListSelectionModel> ();
			for (ListSelectionModel selModel : modelTableMap.keySet()) {
				if (! selModel.isSelectionEmpty ()) {
					activeSelections.add (selModel);
				}
			}
			
			if (jTable != null) {
				// clear shared selection Model
				uberSelectionModel.setValueIsAdjusting (true);
				uberSelectionModel.clearSelection ();
				
				if (!activeSelections.isEmpty()) {
					final BitSet bSet = new BitSet ();	
					
					/**
					 * Do 
					 */
					final long nano = System.nanoTime();
					final ListSelectionTranslatingProcess lstp = new ListSelectionTranslatingProcess ();
					lstp.doParallel (activeSelections, bSet);			
					LOGGER.debug ("Parallel CTRSelModel2 : "+((System.nanoTime() - nano)/1E6)+" ms.");
					
					// Copy the BitSet state to the shared selection model
					for (int index = bSet.nextSetBit (0); index >= 0; index = bSet.nextSetBit (index + 1)) {
						final int nextClearBit = bSet.nextClearBit (index);
						uberSelectionModel.addSelectionInterval (index, nextClearBit - 1);
						index = nextClearBit;
					} 
				}
				
				uberSelectionModel.setValueIsAdjusting (false);
			}
			
			final boolean uberIsEmptyAfter = uberSelectionModel.isSelectionEmpty ();
			
			//System.err.println ("uber set to: "+uberSelectionModel);
			
			// Apart from the case in which the shared selection model was empty before and after
			// we then alert the associated tables' selectionModels, which should get them to redraw
			if (!uberIsEmptyBefore || !uberIsEmptyAfter) {
				final ListSelectionEvent uberProvoke = new ListSelectionEvent (uberSelectionModel, 0, 0, false);
				for (JTable jtable : tables) {
					jtable.valueChanged (uberProvoke);
				}
			}
		}
	}

	public final boolean isUpdateOnFinishAdjusting () {
		return updateOnFinishAdjusting;
	}

	public final void setUpdateOnFinishAdjusting (final boolean updateOnFinishAdjusting) {
		this.updateOnFinishAdjusting = updateOnFinishAdjusting;
	}

	public final ListSelectionModel getUberSelectionModel () {
		return uberSelectionModel;
	}
	
	
	
	
	class ListSelectionTranslatingProcess extends ParallelCollectionProcess {

		boolean firstSelectionModel;
		
		public ListSelectionTranslatingProcess () {
			firstSelectionModel = true;
		}
		
		@Override
		public LatchRunnable makeSubListProcess (final List<?> subList, final CountDownLatch cLatch) {
			return new ProcessListSelectionModels (subList, cLatch);
		}	

		@Override
		public void addPartialResult (final Object mergedResult, final Object partialResult) {
			final BitSet bSet = (BitSet)mergedResult;
        	if (firstSelectionModel) {
        		bSet.or ((BitSet)partialResult);
        		firstSelectionModel = false;
        	} else {
        		bSet.and ((BitSet)partialResult);
        	}
		}	
	}
	
	
	class ProcessListSelectionModels extends LatchRunnable {

		List<ListSelectionModel> selectionModels;
		BitSet subBitSet;	
		
		ProcessListSelectionModels (final List<?> lsms, final CountDownLatch latch) {
			super (latch);
			selectionModels = (List<ListSelectionModel>)lsms;
			subBitSet = new BitSet ();	
		}
		
		@Override
		public void run() {
			
			boolean firstSelectionModel = true;
			final BitSet modelSet = new BitSet ();
			
			// Make a BitSet where 1 is a shared 'AND' selection between the currently non-empty SelectionModels
			// The BitSet and shared selection model are model-indexed, table selection models are view-indexed
			// to that particular table.
			LOGGER.debug ("in processlistselectionModels...");
			for (ListSelectionModel selModel : selectionModels) {
				final JTable otherTable = modelTableMap.get (selModel);
				//System.err.println ("sel model: "+selModel);
				
				modelSet.clear ();
				// Make sure selection indices aren't beyond the actual range of rows in a table, otherwise convertRowIndexToModel throws an error
				final int minRange = Math.max (selModel.getMinSelectionIndex(), 0);
				final int maxRange = Math.min (selModel.getMaxSelectionIndex(), otherTable.getRowCount() - 1);
				for (int viewIndex = minRange; viewIndex <= maxRange; viewIndex++) {
				
					if (selModel.isSelectedIndex (viewIndex)) {
						final int modelIndex = otherTable.convertRowIndexToModel (viewIndex);
						modelSet.set (modelIndex);
					}
				}
				
				if (firstSelectionModel) {
					subBitSet.or (modelSet);
					firstSelectionModel = false;
				} else {
					subBitSet.and (modelSet);
				}
			}
			
	        super.run ();
	        LOGGER.debug ("exiting processlistselectionModels...");
		}
		
		
		public Object getResult () { return subBitSet; }
	}
}
