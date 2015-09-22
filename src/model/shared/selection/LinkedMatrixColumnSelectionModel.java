package model.shared.selection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumnModel;
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
 * @author cs22
 *
 */
public class LinkedMatrixColumnSelectionModel extends LinkedTableColumnSelectionModel {

	final static Logger LOGGER = Logger.getLogger (LinkedMatrixColumnSelectionModel.class);

	public LinkedMatrixColumnSelectionModel () {
		this (null);
	}
	
	public LinkedMatrixColumnSelectionModel (final JTable table) {
		super (table);
	}
	
	
	void passColumnSelectionStateToOtherTableModels (final ListSelectionEvent lsEvent) {	
	
		final ListSelectionModel colSelectionModel = (ListSelectionModel) lsEvent.getSource();
		TableColumnModel columnModel = null;
		final Collection<TableColumnModel> columnModels = columnModelTableMap.keySet();
		final Iterator<TableColumnModel> columnModelIterator = columnModels.iterator();
		
		while (columnModelIterator.hasNext () && columnModel == null) {
			final TableColumnModel nextColumnModel = columnModelIterator.next ();
			if (nextColumnModel.getSelectionModel() == colSelectionModel) {
				columnModel = nextColumnModel;
			}
		}
		
		if (columnModel != null) {
			final JTable table = columnModelTableMap.get (columnModel);
	
			if (table != null) {	
				final TableModel tableModel = table.getModel ();
				
				if (tableModel instanceof MatrixTableModel) {
					final MatrixTableModel matrixModel = (MatrixTableModel)tableModel;
	
					// Moving a column in a table calls the removeIndexInterval method
					// in the TableColumnModel's associated ListSelectionModel.
					// ListSelectionModel.removeIndexInterval makes a ListSelectionEvent
					// which is passed to valueChanged() for its ListSelectionListeners.
					// DefaultTableColumnModel's valueChanged routine just calls fireColumnSelectionChanged
					// which calls columnSelectionChanged in this class. 
					// When this happens e.getLastIndex() will be the index of the removed column
					// and if this is/was the last column it will throw an error when convertColumnIndexToModel
					// is called. Hence the maxIndex.
					//
					// In short anything that affects the TableColumnModel's ListSelectionModel
					// will end up calling columnSelectionChanged here... aargh
					final int maxIndex = Math.min (lsEvent.getLastIndex(), table.getColumnCount() - 1);
	
					for (TableColumnModel otherColumnModel : columnModels) {		
						final ListSelectionModel otherModel = otherColumnModel.getSelectionModel();
						
						if (otherModel != colSelectionModel) {
							otherModel.setValueIsAdjusting (true);
							final JTable otherTable = columnModelTableMap.get (otherColumnModel);
							
							if (otherTable != null) {
								LOGGER.debug ("Other Table: "+otherTable);
								final int[] otherModelToViewIndex = modelToViewOrderMap.get (otherColumnModel);
								final TableModel otherTableModel = otherTable.getModel ();
								
								if (otherTableModel instanceof MatrixTableModel) {
									final MatrixTableModel otherMatrixModel = (MatrixTableModel)otherTableModel;
								
									for (int viewIndex = lsEvent.getFirstIndex(); viewIndex <= maxIndex; viewIndex++) {
										final boolean state = colSelectionModel.isSelectedIndex (viewIndex);			
										final int mIndex = table.convertColumnIndexToModel (viewIndex);
										//modelIndex.set (mIndex, state);
										
										if (mIndex >= 0 && mIndex < otherModelToViewIndex.length) {
											final Object columnObject = matrixModel.getColumnObject (mIndex);
											
											if (columnObject != null) {
												final int otherModelIndex = otherMatrixModel.getColumnIndex (columnObject);
												
												if (otherModelIndex != -1) {
													final int otherViewIndex = otherModelToViewIndex [otherModelIndex];
													//int otherViewIndex = otherTable.convertColumnIndexToView (mIndex);
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
							otherModel.setValueIsAdjusting (false);
						}
					}
				}
			}
		}
	}
	
}