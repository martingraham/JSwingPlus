package model.shared.selection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
//import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

/**
 * Class that links together different <code>ListSelectionModel</code> objects
 * 
 * Developed for sharing selections between <code>JTable</code> objects as the order
 * of a ListSelectionModel in a JTable depends on its view order. Two JTables can
 * share a <code>TableModel</code> but can't share a ListSelectionModel if you
 * want to enable them to order the data independently. 
 * @author cs22
 *
 */
public class LinkedTableRowSelectionModel implements ListSelectionListener, PropertyChangeListener {

	private final static Logger LOGGER = Logger.getLogger (LinkedTableRowSelectionModel.class);
	
	protected Set<JTable> tables;
	protected Map<ListSelectionModel, JTable> modelTableMap;
	
	private boolean isAdjusting;
	//BitSet modelIndex;

	public LinkedTableRowSelectionModel () {
		this (null);
	}
	
	public LinkedTableRowSelectionModel (final JTable table) {
		tables = new HashSet<JTable> ();
		modelTableMap = new HashMap<ListSelectionModel, JTable> ();
		setAdjusting (false);
		//modelIndex = new BitSet (32);
		addJTable (table);
	}
	
	/**
	 * Add a jtable
	 * @param table
	 */
	public void addJTable (final JTable table) {
		if (table != null && tables.add (table)) {
			table.addPropertyChangeListener ("selectionModel", this);
			addListSelectionListener (table, table.getSelectionModel());
			generateListSelectionModelToTableMap ();
		}
	}
	
	/**
	 * Remove a jtable
	 * @param table
	 */
	public void removeJTable (final JTable table) {
		if (table != null && tables.remove (table)) {
			table.removePropertyChangeListener ("selectionModel", this);	
			removeListSelectionListener (table, table.getSelectionModel());
			generateListSelectionModelToTableMap ();
		}
	}
	

	/**
	 * Defensive copy of table set
	 * @return Set of JTable instances
	 */
	public Set<JTable> getTables () { return new HashSet<JTable> (tables); }

	
	/**
	 * An event has occurred on one of the registered JTable's ListSelectionModels
	 */
	@Override
	public void valueChanged (final ListSelectionEvent lsEvent) {
		if (!isAdjusting()) {	// stops recursive calls of the following routine;
			setAdjusting (true);
			//LOGGER.debug ("Source adj: "+e.getValueIsAdjusting());
			
			if (!lsEvent.getValueIsAdjusting() && lsEvent.getSource() instanceof ListSelectionModel) {
				passSelectionStateToOtherTableModels (lsEvent);
				furtherCheckedEventHandling (lsEvent);
			}	
			setAdjusting (false);
		}
	}
	
	/**
	 * When extending the class whack any other ListSelectionEvent processing in here
	 * @param lsEvent ListSelectionEvent
	 */
	public void furtherCheckedEventHandling (final ListSelectionEvent lsEvent) {
		// EMPTY
	}

	
	/**
	 * Pass changes in one ListSelectionModel to the correct indices in other ListSelectionModels
	 * This requires that the Jtable's involved are all based on the same TableModel to do the 
	 * model<-->view indexing correctly
	 * @param lsEvent
	 */
	protected void passSelectionStateToOtherTableModels (final ListSelectionEvent lsEvent) {
		final ListSelectionModel rlsModel = (ListSelectionModel) lsEvent.getSource();
		final JTable table = modelTableMap.get (rlsModel);

		if (table != null) {
			
			for (ListSelectionModel otherModel : modelTableMap.keySet()) {
				
				if (otherModel != rlsModel) {
					otherModel.setValueIsAdjusting (true);
					final JTable otherTable = modelTableMap.get (otherModel);
					
					if (otherTable != null) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug ("Other Model Listener: "+otherTable);
							//LOGGER.debug ("Same? "+(jt.getSelectionModel().equals (rlsModel)));
							LOGGER.debug ("jt: "+table+"\njt sm: "+table.getSelectionModel()+"\nhm sm: "+rlsModel);
							LOGGER.debug ("lsEvent: "+lsEvent.toString());
							LOGGER.debug ("jt: "+table.getRowCount());
						}

						
						if (lsEvent.getFirstIndex() != -1 && lsEvent.getLastIndex() != -1) {
							// Sometimes the event will have a higher lastIndex than there are
							// rows in a filtered JTable. This can cause errors, so take the min.
							final int endLimit = Math.min (lsEvent.getLastIndex (), table.getRowCount());
							
							for (int m = lsEvent.getFirstIndex(); m <= endLimit; m++) {
								final boolean state = rlsModel.isSelectedIndex (m);
							
								final int mIndex = table.convertRowIndexToModel (m);
								//modelIndex.set (mIndex, state);
								if (mIndex >= 0) {
									System.err.print ("mindex: "+mIndex);
									final int otherViewIndex = otherTable.convertRowIndexToView (mIndex);
									System.err.println ("\tvindex: "+otherViewIndex);
									if (state) {
										otherModel.addSelectionInterval (otherViewIndex, otherViewIndex);
									} else {
										otherModel.removeSelectionInterval (otherViewIndex, otherViewIndex);
									}
								}
							}	
						}
						else if (lsEvent.getFirstIndex () == -1 && rlsModel.isSelectionEmpty()) {
							otherModel.clearSelection();
						}
					}
					
					otherModel.setValueIsAdjusting (false);
				}
			}
		}
	}
	
	
	/**
	 * When a ListSelectionModel is switched in/out from one of the registered JTable's
	 * then we must remove the listener to the old selectionModel and add a listener
	 * to the new selectionModel
	 */
	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if (evt.getSource() instanceof JTable) {
			final JTable table = (JTable)evt.getSource ();
			//if (evt.getPropertyName().equals ("selectionModel")) {
				removeListSelectionListener (table, (ListSelectionModel)evt.getOldValue());		
				addListSelectionListener (table, (ListSelectionModel)evt.getNewValue());
				generateListSelectionModelToTableMap ();
				//((DefaultTableModel)(table.getModel())).fireTableStructureChanged();
				//((DefaultTableModel)(table.getModel())).fireTableDataChanged();
			//}
		}
	}
	
	
	/**
	 * Add the LinkedTableRowSelectionModel to the jtables' ListSelectionModel listeners
	 * @param table
	 * @param lsm
	 */
	protected void addListSelectionListener (final JTable table, final ListSelectionModel lsm) {
		if (lsm != null && !isModelUsedByOtherTables (table, lsm)) {
			lsm.addListSelectionListener (this);
		}
	}
		
	/**
	 * Remove the LinkedTableRowSelectionModel from the jtables' ListSelectionModel listeners
	 * @param table
	 * @param lsm
	 */
	protected void removeListSelectionListener (final JTable table, final ListSelectionModel lsm) {
		if (lsm != null && !isModelUsedByOtherTables (table, lsm)) {
			lsm.removeListSelectionListener (this);
		}
	}
	
	
	/**
	 * Test if a JTable's current ListSelectionModel is also used by any other JTable's currently
	 * registered with the LinkedTableRowSelectionModel
	 * @param table
	 * @param lsm
	 * @return true if lsm is already the ListSelectionListener for any of the registered JTable's
	 */
	protected boolean isModelUsedByOtherTables (final JTable table, final ListSelectionModel lsm) {
		final Iterator<JTable> tableIterator = tables.iterator();
		while (tableIterator.hasNext()) {
			final JTable otherTable = tableIterator.next ();
			if (otherTable != table) {
				final ListSelectionModel otherSelectionModel = otherTable.getSelectionModel();
				if (lsm == otherSelectionModel) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Generate a map of ListSelectionModel instances to JTable instances
	 * @return
	 */
	protected Map<ListSelectionModel, JTable> generateListSelectionModelToTableMap () {
		modelTableMap.clear ();
		for (JTable table : tables) {
			final ListSelectionModel model = table.getSelectionModel();
			if (model != null) {
				modelTableMap.put (model, table);
			}
		}
		return modelTableMap;
	}
	
	public Map<ListSelectionModel, JTable> getSelectionModelToTableMap () {
		return modelTableMap;
	}


	public final boolean isAdjusting() {
		return isAdjusting;
	}

	public final void setAdjusting (final boolean isAdjusting) {
		this.isAdjusting = isAdjusting;
	}
}
