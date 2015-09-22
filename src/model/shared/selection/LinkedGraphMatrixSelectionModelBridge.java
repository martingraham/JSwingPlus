package model.shared.selection;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import model.graph.GraphSelectionEvent;
import model.graph.GraphSelectionModel;
import model.matrix.MatrixTableModel;

import swingPlus.graph.JGraph;

/**
 * Links together selections made on a common <code>Graph</code> model
 * that underlies a set of <code>JMatrix</code> and <code>JGraph</code> objects.
 * 
 * Effectively translates node selections in a <code>GraphSelectionModel</code>
 * to <code>ListSelectionModel</code> indices used for row selection
 * in <code>JTable</code> and vice versa.
 * 
 * @author cs22
 *
 */
public class LinkedGraphMatrixSelectionModelBridge {

	private final static Logger LOGGER = Logger.getLogger (LinkedGraphMatrixSelectionModelBridge.class);
	
	LinkedTableRowSelectionModel lrm;
	LinkedTableColumnSelectionModel lcm;
	LinkedGraphSelectionModel lgm;
	
	public LinkedGraphMatrixSelectionModelBridge () {
		
		lrm = new LinkedMatrixRowSelectionModel () {
			
			@Override
			public void furtherCheckedEventHandling (final ListSelectionEvent lsEvent) {
				bridgeListSelectionStateToGraphModels (lsEvent);
			}
			
			public void bridgeListSelectionStateToGraphModels (final ListSelectionEvent lsEvent) {			
				if (!lsEvent.getValueIsAdjusting() && lsEvent.getSource() instanceof DefaultListSelectionModel) {
					final DefaultListSelectionModel rlsModel = (DefaultListSelectionModel) lsEvent.getSource();
					final JTable jtable = modelTableMap.get (rlsModel);
					lgm.setAdjusting (true);
					
					if (jtable.getModel() instanceof MatrixTableModel) {
						final MatrixTableModel matrixModel = (MatrixTableModel) jtable.getModel();
						
						for (GraphSelectionModel otherGsm : lgm.getSelectionModelToGraphMap().keySet()) {
							final boolean otherAdjusting = otherGsm.isValueAdjusting();
							otherGsm.setValueIsAdjusting (true);
							
							//if (lsEvent.g)
							
							for (int m = lsEvent.getFirstIndex(); m <= lsEvent.getLastIndex(); m++) {
								final int modelRow = jtable.convertRowIndexToModel (m);
								final boolean selected = rlsModel.isSelectedIndex (m);
								
								if (modelRow != -1) {
									final Object obj = matrixModel.getRowObject (modelRow);
									if (obj != null) {
										otherGsm.setSelected (obj, selected);
									}
								}
							}
							otherGsm.setValueIsAdjusting (otherAdjusting);
						}
					}
					lgm.setAdjusting (false);
				}	
			}
		};
		
		
		lcm = new LinkedMatrixColumnSelectionModel () {
			@Override
			public void furtherCheckedEventHandling (final ListSelectionEvent lsEvent) {
				bridgeListSelectionStateToGraphModels (lsEvent);
			}	
			
			
			public void bridgeListSelectionStateToGraphModels (final ListSelectionEvent lsEvent) {			
				if (!lsEvent.getValueIsAdjusting() && lsEvent.getSource() instanceof DefaultListSelectionModel) {
					final DefaultListSelectionModel colSelectionModel = (DefaultListSelectionModel) lsEvent.getSource();
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
						final JTable jtable = columnModelTableMap.get (columnModel);
						lgm.setAdjusting (true);
						
						if (jtable.getModel() instanceof MatrixTableModel) {
							final MatrixTableModel matrixModel = (MatrixTableModel) jtable.getModel();
							
							for (GraphSelectionModel otherGsm : lgm.getSelectionModelToGraphMap().keySet()) {
								final boolean otherAdjusting = otherGsm.isValueAdjusting();
								otherGsm.setValueIsAdjusting (true);
								
								for (int m = lsEvent.getFirstIndex(); m <= lsEvent.getLastIndex(); m++) {
									final int modelRow = jtable.convertColumnIndexToModel (m);
									final boolean selected = colSelectionModel.isSelectedIndex (m);
									
									if (modelRow != -1) {
										final Object obj = matrixModel.getColumnObject (modelRow);
										if (obj != null) {
											otherGsm.setSelected (obj, selected);
										}
									}
								}
								otherGsm.setValueIsAdjusting (otherAdjusting);
							}
						}
						lgm.setAdjusting (false);
					}
				}	
			}
		};

		
		
		
		lgm = new LinkedGraphSelectionModel () {
			
			@Override
			public void furtherCheckedEventHandling (final GraphSelectionEvent gsEvent) {
				bridgeGraphSelectionStateToListModels (gsEvent);
				bridgeGraphSelectionStateToColumnModels (gsEvent);
			}				
			
			void bridgeGraphSelectionStateToListModels (final GraphSelectionEvent gsEvent) {
				if (!gsEvent.isValueAdjusting() && gsEvent.getSource() instanceof GraphSelectionModel) {
					final GraphSelectionModel gsm = (GraphSelectionModel) gsEvent.getSource ();
					lrm.setAdjusting (true);
					
					for (ListSelectionModel otherModel : lrm.getSelectionModelToTableMap().keySet()) {			
						otherModel.setValueIsAdjusting (true);
						final JTable otherTable = lrm.getSelectionModelToTableMap().get (otherModel);
						
						if (otherTable.getModel() instanceof MatrixTableModel) {
							final MatrixTableModel matrixModel = (MatrixTableModel)otherTable.getModel();
							LOGGER.debug ("Other Graph-based Listener: "+otherTable+" : "+matrixModel);
							Object selectedNode = gsEvent.getSelectedNode();
							
							if (gsEvent.getEventType() == GraphSelectionEvent.CLEAR) {
								otherModel.clearSelection ();
							}
							else if (selectedNode != null) {
								setRowSelected (otherTable, otherModel, matrixModel, selectedNode, gsm.isSelected (selectedNode));
							}
							else {
								final Collection<Object> selectedNodes = gsEvent.getSelectedNodes();
								if (selectedNodes != null && !selectedNodes.isEmpty()) {
									final boolean otherAdjusting = otherModel.getValueIsAdjusting();
									otherModel.setValueIsAdjusting (true);
									LOGGER.debug ("selecting multiple nodes");
									
									final Iterator<Object> selectedIterator = selectedNodes.iterator();
									while (selectedIterator.hasNext ()) {
										selectedNode = selectedIterator.next();
										setRowSelected (otherTable, otherModel, matrixModel, selectedNode, gsm.isSelected (selectedNode));
									}
									
									otherModel.setValueIsAdjusting (otherAdjusting);
								}
							}	
						}
						otherModel.setValueIsAdjusting (false);
					}
					lrm.setAdjusting (false);
				}
			}
		
			void setRowSelected (final JTable otherTable, final ListSelectionModel listModel, 
					final MatrixTableModel matrixModel, final Object node, final boolean selectState) {
				
				final int row = matrixModel.getRowIndex (node);
				if (row != -1) {
					final int viewRow = otherTable.convertRowIndexToView (row);
					if (viewRow != -1) {
						if (selectState) {
							listModel.addSelectionInterval (viewRow, viewRow);
						} else {
							listModel.removeSelectionInterval (viewRow, viewRow);
						}
					}		
				}
			}
			
			void bridgeGraphSelectionStateToColumnModels (final GraphSelectionEvent gsEvent) {
				if (!gsEvent.isValueAdjusting() && gsEvent.getSource() instanceof GraphSelectionModel) {
					final GraphSelectionModel gsm = (GraphSelectionModel) gsEvent.getSource ();
					lcm.setAdjusting (true);
					
					for (TableColumnModel otherModel : lcm.getTableColumnModelToTableMap().keySet()) {			
						final ListSelectionModel listSelectionModel = otherModel.getSelectionModel();
						listSelectionModel.setValueIsAdjusting (true);
						final JTable otherTable = lcm.getTableColumnModelToTableMap().get (otherModel);
						
						if (otherTable.getModel() instanceof MatrixTableModel) {
							final MatrixTableModel matrixModel = (MatrixTableModel)otherTable.getModel();
							LOGGER.debug ("Other col Graph-based Listener: "+otherTable+" : "+matrixModel);
							Object selectedNode = gsEvent.getSelectedNode();
							
							if (gsEvent.getEventType() == GraphSelectionEvent.CLEAR) {
								listSelectionModel.clearSelection ();
							}
							else if (selectedNode != null) {
								setColumnSelected (otherModel, listSelectionModel, matrixModel, selectedNode, gsm.isSelected (selectedNode));
							}
							else {
								final Collection<Object> selectedNodes = gsEvent.getSelectedNodes();
								if (selectedNodes != null && !selectedNodes.isEmpty()) {
									LOGGER.debug ("selected nodes: "+selectedNodes);
									
									final Iterator<Object> selectedIterator = selectedNodes.iterator();
									while (selectedIterator.hasNext ()) {
										selectedNode = selectedIterator.next();
										setColumnSelected (otherModel, listSelectionModel, matrixModel, selectedNode, gsm.isSelected (selectedNode));
									}
								}
							}	
						}
						listSelectionModel.setValueIsAdjusting (false);
					}
					lcm.setAdjusting (false);
				}
			}
			
			void setColumnSelected (final TableColumnModel tcm, final ListSelectionModel listModel, 
					final MatrixTableModel matrixModel, final Object node, final boolean selectState) {
				
				final int column = matrixModel.getColumnIndex (node);
				if (column != -1) {
					final int[] modelToViewIndex = lcm.getModelToViewOrderMap().get (tcm);
					final int viewColumn = modelToViewIndex [column];
					if (viewColumn != -1) {
						if (selectState) {
							listModel.addSelectionInterval (viewColumn, viewColumn);
						} else {
							listModel.removeSelectionInterval (viewColumn, viewColumn);
						}
					}		
				}
			}
		};
	}
	
	public void addJTable (final JTable table) {
		lrm.addJTable (table);
		lcm.addJTable (table);
	}
	
	public void removeJTable (final JTable table) {
		lrm.removeJTable (table);
		lcm.removeJTable (table);
	}
	
	public void addJGraph (final JGraph graph) {
		lgm.addJGraph (graph);
	}
	
	public void removeJGraph (final JGraph graph) {
		lgm.removeJGraph (graph);
	}
}
