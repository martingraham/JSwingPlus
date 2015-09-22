package example.tablelist;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.DefaultTableModel;


public class ColumnLabelMap {

	final Map<String, Map<Object, String>> labelMapMap;

	
	ColumnLabelMap () {
		labelMapMap = new HashMap<String, Map<Object, String>> ();
	}
	
	void parseModel (final DefaultTableModel tableModel) {
		final int columnNameIndex = tableModel.findColumn ("friendlyname");
		final int labelIndex = tableModel.findColumn ("outcomedescription");
		final int valueIndex = tableModel.findColumn ("value");
		
		for (int row = tableModel.getRowCount(); --row >= 0;) {
			final String columnName = tableModel.getValueAt (row, columnNameIndex).toString();
			if (labelMapMap.get(columnName) == null) {
				labelMapMap.put (columnName, new HashMap<Object, String> ());
			}
			
			final Map<Object, String> columnLabelMap = labelMapMap.get (columnName);	
			final Object value = tableModel.getValueAt(row, valueIndex);
			if (value != null) {
				final String label = tableModel.getValueAt (row, labelIndex).toString();
				columnLabelMap.put (value.toString(), label);
				//System.err.println (columnName+"\t"+value+", kls: "+value.getClass()+"\t"+label);
			}
		}
	}
	
	Map<String, Map<Object, String>> getMap () {
		return labelMapMap;
	}
}


