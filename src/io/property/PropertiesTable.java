package io.property;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

public class PropertiesTable extends JTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8908477585803194994L;
	final static Logger LOGGER = Logger.getLogger (PropertiesTable.class);
	final static Comparator<Object[]> TO_STRING_COMPARATOR = new PropertiesTable.ToStringComparator ();
	
	Properties properties;
	
	public PropertiesTable (final Properties properties) {
		super (makeTableModelFromProperties (properties));
		this.properties = properties;
		setAutoCreateRowSorter (true);
	}
	
	@Override
	public boolean isCellEditable (final int row, final int column) {
		return this.convertColumnIndexToModel (column) != 0;
	}

	@Override
    public TableCellEditor getCellEditor (final int row, final int column) {
		// Picks up editors on a per-cell basis, rather then per-column
        TableCellEditor editor = getDefaultEditor (getValueAt (row, column).getClass());
        if (editor == null) {
            editor = super.getCellEditor (row, column);
        }
        return editor;
    }
	
	@Override
	public void tableChanged (final TableModelEvent tmEvent) {
		super.tableChanged (tmEvent);
		
		if (tmEvent.getType() == TableModelEvent.UPDATE && tmEvent.getFirstRow() != TableModelEvent.HEADER_ROW) {
			final int rowIndex = tmEvent.getFirstRow();
			final String key = this.getModel().getValueAt(rowIndex, 0).toString();
			final String value = this.getModel().getValueAt(rowIndex, 1).toString();
			properties.setProperty (key, value);
			LOGGER.debug ("Key: "+key+", Value: "+value);
		}
	}
	
	static Object[][] makeObjectArrayFromProperties (final Properties properties) {
		final Object[][] propStringsArray = new Object [properties.size()][2];
		//properties.
		final Set<Entry<Object, Object>> entrySet = properties.entrySet();
		int rowIndex = 0;
		for (Entry<Object, Object> entry : entrySet) {
			propStringsArray [rowIndex][0] = entry.getKey();
			propStringsArray [rowIndex][1] = entry.getValue();
			LOGGER.debug ("Class for "+entry.getKey()+" = "+entry.getValue().getClass());
			rowIndex++;
		}	
		
		Arrays.sort (propStringsArray, TO_STRING_COMPARATOR);
		return propStringsArray;
	}
	
	static TableModel makeTableModelFromProperties (final Properties properties) {
		final Object[][] data = makeObjectArrayFromProperties (properties);
		final Object[] columnNames = {"Property", "Value"};
		return new DefaultTableModel (data, columnNames);
	}
	
	
	static class ToStringComparator implements Comparator<Object[]> {
		@Override
		public int compare (final Object[] objArray1, final Object[] objArray2) {
			final Object obj1 = objArray1[0];
			final Object obj2 = objArray2[0];

			if (obj1 == null) {
				return (obj2 == null ? 0 : -1);
			}
			if (obj2 == null) {
				return 1;
			}
			return obj1.toString().compareTo (obj2.toString());
		}
	}
}
