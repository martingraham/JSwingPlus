package model.pivottable;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;



public class PairwiseProbabilityMap {

	final static Logger LOGGER = Logger.getLogger (PairwiseProbabilityMap.class);
	
	
	final Map<String, Map<String, Map <Object, Map <Object, Double>>>> columnColumnValuesMap;

	
	public PairwiseProbabilityMap () {
		columnColumnValuesMap = new HashMap<String, Map<String, Map <Object, Map <Object, Double>>>> ();
	}
	
	public void parseModel (final DefaultTableModel tableModel) {
		final int columnNameIndex1 = tableModel.findColumn ("Var1");
		final int columnNameIndex2 = tableModel.findColumn ("Var2");
		final int valueIndex1 = tableModel.findColumn ("Value1");
		final int valueIndex2 = tableModel.findColumn ("Val2");
		final int probIndex = tableModel.findColumn ("Probability");
		
		// Have to String-ify most of the columns as there isn't a consistent type throughout this type of file
		
		for (int row = 0; row < tableModel.getRowCount(); row++) {
			final String columnName1 = tableModel.getValueAt (row, columnNameIndex1).toString();
			if (columnColumnValuesMap.get(columnName1) == null) {
				columnColumnValuesMap.put (columnName1, new HashMap<String, Map <Object, Map<Object, Double>>> ());
			}
			final Map<String, Map <Object, Map<Object, Double>>> columnValuesMap = columnColumnValuesMap.get (columnName1);	
			
			final String columnName2 = tableModel.getValueAt (row, columnNameIndex2).toString();
			if (columnValuesMap.get(columnName2) == null) {
				columnValuesMap.put (columnName2, new HashMap <Object, Map <Object, Double>> ());
			}
			final Map <Object, Map<Object, Double>> valuesMap = columnValuesMap.get (columnName2);
			
			final Object value1 = tableModel.getValueAt(row, valueIndex1);
			final String value1Str = (value1 == null ? null : value1.toString());
			if (valuesMap.get(value1Str) == null) {
				valuesMap.put (value1Str, new HashMap <Object, Double> ());
			}
			final Map<Object, Double> probMap = valuesMap.get (value1);

			final Object value2 = tableModel.getValueAt (row, valueIndex2);
			final String value2Str = (value2 == null ? null : value2.toString());
			final Double prob = (Double)tableModel.getValueAt (row, probIndex);
			probMap.put (value2Str, prob);
			
			LOGGER.debug (columnName1+"\t"+columnName2+"\t"+value1+"\t"+value2+"\t"+prob);
		}
	}
	
	Map<String, Map<String, Map <Object, Map <Object, Double>>>> getMap () {
		return columnColumnValuesMap;
	}
}


