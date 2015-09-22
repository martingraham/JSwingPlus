package model.shared.selection;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableModel;

import model.matrix.MatrixTableModel;

import org.apache.log4j.Logger;

/**
 * Class that links together different <code>ListSelectionModel</code> objects
 * 
 * Developed for sharing selections between <code>JTable</code> objects as the order
 * of a ListSelectionModel in a JTable depends on its view order. Two JTables can
 * share a <code>TableModel</code> but can't share a ListSelectionModel if you
 * want to enable them to order the data independently. 
 * 
 * This version is for JMatrix's which don't coordinate on shared model indices, but
 * through shared objects we can access via a MatrixTableModel's methods
 * @author cs22
 *
 */
public class LinkedMatrixRowSelectionModel extends LinkedTableRowSelectionModel {

	final static Logger LOGGER = Logger.getLogger (LinkedMatrixRowSelectionModel.class);

	public LinkedMatrixRowSelectionModel () {
		this (null);
	}
	
	public LinkedMatrixRowSelectionModel (final JTable table) {
		super (table);
	}
	
	
	/**
	 * Pass changes in one ListSelectionModel to the correct indices in other ListSelectionModels
	 * For 2 JMatrix's we need to go viewIndex1 --> modelIndex1 --> object --> modelIndex2 --> viewIndex2 
	 * to get the indexing correct
	 * @param lsEvent
	 */
	protected void passSelectionStateToOtherTableModels (final ListSelectionEvent lsEvent) {
		final ListSelectionModel rlsModel = (ListSelectionModel) lsEvent.getSource();
		final JTable table = modelTableMap.get (rlsModel);

		if (table != null) {
			final TableModel tableModel = table.getModel();
			
			if (tableModel instanceof MatrixTableModel) {
				final MatrixTableModel matrixModel = (MatrixTableModel)tableModel;
	
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
								final TableModel otherTableModel = otherTable.getModel();
								
								if (otherTableModel instanceof MatrixTableModel) {
									final MatrixTableModel otherMatrixModel = (MatrixTableModel)otherTableModel;
									// Sometimes the event will have a higher lastIndex than there are
									// rows in a filtered JTable. This can cause errors, so take the min.
									final int endLimit = Math.min (lsEvent.getLastIndex (), table.getRowCount());
			
									for (int viewIndex = lsEvent.getFirstIndex(); viewIndex <= endLimit; viewIndex++) {
										final boolean state = rlsModel.isSelectedIndex (viewIndex);
										final int mIndex = table.convertRowIndexToModel (viewIndex);
										//modelIndex.set (mIndex, state);
										
										if (mIndex >= 0) {
											final Object rowObject = matrixModel.getRowObject (mIndex);
											
											if (rowObject != null) {
												final int otherMIndex = otherMatrixModel.getRowIndex (rowObject);
												
												if (otherMIndex != -1) {
													final int otherViewIndex = otherTable.convertRowIndexToView (otherMIndex);
					
													if (state) {
														otherModel.addSelectionInterval (otherViewIndex, otherViewIndex);
													} else {
														otherModel.removeSelectionInterval (otherViewIndex, otherViewIndex);
													}
												}
											}
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
	}
	
}
