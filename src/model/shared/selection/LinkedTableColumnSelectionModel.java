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
public class LinkedTableColumnSelectionModel implements TableColumnModelListener,
										PropertyChangeListener {

	private final static Logger LOGGER = Logger.getLogger (LinkedTableColumnSelectionModel.class);
	
	protected Set<JTable> tables;
	protected Map<TableColumnModel, JTable> columnModelTableMap;
	protected Map<TableColumnModel, int[]> modelToViewOrderMap;
	
	private boolean isAdjusting;

	public LinkedTableColumnSelectionModel () {
		this (null);
	}
	
	public LinkedTableColumnSelectionModel (final JTable table) {
		tables = new HashSet<JTable> ();
		columnModelTableMap = new HashMap<TableColumnModel, JTable> ();
		modelToViewOrderMap = new HashMap<TableColumnModel, int[]> ();
		setAdjusting (false);
		addJTable (table);
	}
	
	public final void addJTable (final JTable table) {
		if (table != null && tables.add (table)) {
			table.addPropertyChangeListener ("columnModel", this);
			LOGGER.debug ("table: "+table+", col model: "+table.getColumnModel());
			addTableColumnModelListener (table, table.getColumnModel());
			generateTableColumnModelToTableMap ();
		}
	}
	
	public void removeJTable (final JTable table) {
		if (table != null && tables.remove (table)) {	
			table.removePropertyChangeListener ("columnModel", this);
			removeTableColumnModelListener (table, table.getColumnModel());
			generateTableColumnModelToTableMap ();
		}
	}
	
	
	
	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if (evt.getSource() instanceof JTable) {
			final JTable table = (JTable)evt.getSource ();
			
			if (evt.getPropertyName().equals ("columnModel")) {
				final TableColumnModel oldModel = (TableColumnModel)evt.getOldValue();
				final TableColumnModel newModel = (TableColumnModel)evt.getNewValue();
				removeTableColumnModelListener (table, oldModel);		
				addTableColumnModelListener (table, newModel);
				generateTableColumnModelToTableMap ();
			}
		}
	}

	
	
	
	void addTableColumnModelListener (final JTable table, final TableColumnModel tcm) {
		if (tcm != null && !isModelUsedByOtherTables (table, tcm)) {
			tcm.addColumnModelListener (this);
			final int[] modelToViewIndex = buildColumnIndexTable (table);
			modelToViewOrderMap.put (tcm, modelToViewIndex);
		}
	}
	
	void removeTableColumnModelListener (final JTable table, final TableColumnModel tcm) {
		if (tcm != null && !isModelUsedByOtherTables (table, tcm)) {
			tcm.removeColumnModelListener (this);
			modelToViewOrderMap.remove (tcm);
		}
	}
	
	boolean isModelUsedByOtherTables (final JTable table, final TableColumnModel tcm) {
		final Iterator<JTable> tableIterator = tables.iterator();
		
		while (tableIterator.hasNext()) {
			final JTable otherTable = tableIterator.next ();
			
			if (otherTable != table) {
				final TableColumnModel otherColumnModel = otherTable.getColumnModel();
				
				if (tcm == otherColumnModel) {
					return true;
				}
			}
		}
		return false;
	}
	
	Map<TableColumnModel, JTable> generateTableColumnModelToTableMap () {
		columnModelTableMap.clear ();
		
		for (JTable table : tables) {
			final TableColumnModel model = table.getColumnModel();
			if (model != null) {
				columnModelTableMap.put (model, table);
			}
		}
		return columnModelTableMap;
	}
	
	public Map<TableColumnModel, JTable> getTableColumnModelToTableMap () {
		return columnModelTableMap;
	}
	
	public Map<TableColumnModel, int[]> getModelToViewOrderMap () {
		return modelToViewOrderMap;
	}
	// TableColumnModelListener routines
	@Override
	public void columnAdded (final TableColumnModelEvent tcmEvent) {
		rebuildIndex (tcmEvent);
	}

	@Override
	public void columnMarginChanged (final ChangeEvent cEvent) {
		// EMPTY
	}

	@Override
	public void columnMoved (final TableColumnModelEvent tcmEvent) {
		if (tcmEvent.getFromIndex() != tcmEvent.getToIndex ()) {
			rebuildIndex (tcmEvent);
		}
	}

	@Override
	public void columnRemoved (final TableColumnModelEvent tcmEvent) {
		if (tcmEvent.getFromIndex() >= 0) {
			rebuildIndex (tcmEvent);
		}
	}

	@Override
	public void columnSelectionChanged (final ListSelectionEvent lsEvent) {
		if (!isAdjusting()) {	// stops recursive calls of the following routine;
			setAdjusting (true);
			// Has column selection actually changed or inputStream this getting called
			// because of the DefaultTableColumnModel.moveColumn () routine?
			
			if (!lsEvent.getValueIsAdjusting() && lsEvent.getSource() instanceof ListSelectionModel) {
				LOGGER.debug ("List Selection Event: "+lsEvent);
				passColumnSelectionStateToOtherTableModels (lsEvent);
				furtherCheckedEventHandling (lsEvent);
			}	
			setAdjusting (false);
		}
	}
	
	public void furtherCheckedEventHandling (final ListSelectionEvent lsEvent) {
		// EMPTY
	}
	
	
	public void rebuildIndex (final TableColumnModelEvent tcmEvent) {
		final TableColumnModel tcm = (TableColumnModel) tcmEvent.getSource();
		LOGGER.debug ("rebuilding index for "+tcm);
		final int[] modelToViewIndex = buildColumnIndexTable (tcm);
		modelToViewOrderMap.put (tcm, modelToViewIndex);
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

							for (int m = lsEvent.getFirstIndex(); m <= maxIndex; m++) {
								final boolean state = colSelectionModel.isSelectedIndex (m);			
								final int mIndex = table.convertColumnIndexToModel (m);
								//modelIndex.set (mIndex, state);
								
								if (mIndex >= 0 && mIndex < otherModelToViewIndex.length) {
									final int otherViewIndex = otherModelToViewIndex [mIndex];
									//int otherViewIndex = otherTable.convertColumnIndexToView (mIndex);
									if (state) {
										otherModel.addSelectionInterval (otherViewIndex, otherViewIndex);
									} else {
										otherModel.removeSelectionInterval (otherViewIndex, otherViewIndex);
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
	
	
    int[] buildColumnIndexTable (final JTable table) {
        return buildColumnIndexTable (table, table.getColumnModel());
    }
	
    int[] buildColumnIndexTable (final TableColumnModel columnModel) {
    	final JTable table = columnModelTableMap.get(columnModel);
    	return buildColumnIndexTable (table, table.getColumnModel());
    }
    
    int[] buildColumnIndexTable (final JTable table, final TableColumnModel columnModel) {
    	LOGGER.debug ("buildColumnIndexTable table: "+table);
    	int[] index = new int [table.getModel().getColumnCount()];
        for (int column = 0; column < columnModel.getColumnCount(); column++) {
        	index [columnModel.getColumn(column).getModelIndex()] = column;
        }
        return index;
    }
	
    
	public final boolean isAdjusting() {
		return isAdjusting;
	}

	public final void setAdjusting (final boolean isAdjusting) {
		this.isAdjusting = isAdjusting;
	}
}