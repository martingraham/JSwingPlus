package model.shared;

import java.util.Collection;
import java.util.Vector;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;


public class TableUtils {

	
	final static Logger LOGGER = Logger.getLogger (TableUtils.class);

	
	/**
	 * Make a ListSelectionModel holding the column indexes in the model of all the currently
	 * visible columns in the collection of TableColumnModel's passed in.
	 * @param columnModels
	 * @return a ListSelectionModel holding an indication of model-indexed column presence
	 */
	static public ListSelectionModel combineColumnModels (final Collection<TableColumnModel> columnModels) {
		final ListSelectionModel csm = new DefaultListSelectionModel ();
		csm.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		for (TableColumnModel columnModel : columnModels) {
			for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); columnIndex++) {
				final int modelIndex = columnModel.getColumn(columnIndex).getModelIndex();
				csm.addSelectionInterval (modelIndex, modelIndex);
			}
		}
		return csm;
	}
	
	static public void makeAverageRows (final DefaultTableModel tModel) {
		final Vector<Vector<Object>> vectors = new Vector<Vector<Object>> ();
		final String[] statTypes = {"Mean", "Median"};
		for (int vct = 0; vct < statTypes.length; vct++) {
			final Vector<Object> vector = new Vector<Object> ();
			vector.add (statTypes [vct]);
			vectors.add (vector);
		}

		for (int column = 1; column < tModel.getColumnCount(); column++) {
			final SortedTableColumn<?> stc = new SortedTableColumn (tModel, column);
			stc.populateAndSort (tModel);
			final Class<?> columnClass = stc.getColumnClass();
			final Number mean = (Number)STCColumnStats.getInstance().getMean (stc);
			final Object median = STCColumnStats.getInstance().getMedian (stc);
			if (columnClass == Integer.class) {
				vectors.get(0).add (Integer.valueOf (mean.intValue()));
				vectors.get(1).add (Integer.valueOf (((Number)median).intValue()));	
			} else if (columnClass == Double.class) {
				vectors.get(0).add (Double.valueOf (mean.doubleValue()));
				vectors.get(1).add (Double.valueOf (((Number)median).doubleValue()));			}
		}
		
		for (Vector<Object> vector : vectors) {
			tModel.addRow (vector);
		}
	}
	
	static public void printTableModel (final TableModel tableModel) {
		final StringBuilder sBuild = new StringBuilder ();
		for (int colIndex = 0, colTotal = tableModel.getColumnCount(); colIndex < colTotal; colIndex++) {
			sBuild.append (tableModel.getColumnName(colIndex)+"\t");
		}
		LOGGER.info (sBuild.toString());
		
		for (int rowIndex = 0, rowTotal = tableModel.getRowCount(); rowIndex < rowTotal; rowIndex++) {
			sBuild.setLength (0);
			for (int colIndex = 0, colTotal = tableModel.getColumnCount(); colIndex < colTotal; colIndex++) {
				sBuild.append (tableModel.getValueAt(rowIndex, colIndex)+"\t");
			}
			LOGGER.info (sBuild.toString());
		}
	}
}
