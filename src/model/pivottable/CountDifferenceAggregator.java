package model.pivottable;

import java.util.Map;

import org.apache.log4j.Logger;



public class CountDifferenceAggregator extends AbstractPivotDataAggregator {

	final static Logger LOGGER = Logger.getLogger (CountDifferenceAggregator.class);
	
	PairwiseProbabilityMap probabilityMapHolder;
	
	public CountDifferenceAggregator (final PairwiseProbabilityMap probabilityMapHolder) {
		super ();
		this.probabilityMapHolder = probabilityMapHolder;
	}
	
	
	@Override
	public void calculateValues (final int[][] sums, final int[][] counts, final double[][] values) {
		
		final int totalCount = pivotModel.getRawTableData().getRowCount();
		final String columnNameX = pivotModel.getRawTableData().getColumnName (pivotModel.getColumnXIndex());
		final String columnNameY = pivotModel.getRawTableData().getColumnName (pivotModel.getColumnYIndex());
		
		boolean xMappedFirst = true;
		Map<String, Map <Object, Map <Object, Double>>> columnValuesMap = probabilityMapHolder.getMap().get(columnNameX);
		Map <Object, Map <Object, Double>> valuesMap = null;
		if (columnValuesMap != null) {
			valuesMap = columnValuesMap.get (columnNameY);
		}
		
		if (valuesMap == null) {
			columnValuesMap = probabilityMapHolder.getMap().get (columnNameY);
			if (columnValuesMap != null) {
				valuesMap = columnValuesMap.get (columnNameX);
			}
			xMappedFirst = false;
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("total raw data Row Count: "+totalCount);
			LOGGER.debug ("Current Pivot table X axis column: "+columnNameX);
			LOGGER.debug ("Current Pivot table Y axis column: "+columnNameY);
			LOGGER.debug ("Found column to column map: "+columnValuesMap);
			LOGGER.debug ("Found column to column to values map: "+valuesMap);
		}
		
		if (valuesMap == null) {
			for (int column = 0; column < counts.length; column++) {
				for (int row = 0; row < counts[column].length; row++) {
					values[column][row] = 0.0;
				}
			}
		} else {
			for (int column = 0; column < counts.length; column++) {
				final String valXCol = pivotModel.getColumnName (column + 1);
				
				for (int row = 0; row < counts[column].length; row++) {
					final Object valYCol = pivotModel.getValueAt(row, 0);
					final String valYColStr = (valYCol == null ? null : valYCol.toString());
					
					final Map <Object, Double> valueMap = valuesMap.get (xMappedFirst ? valXCol : valYColStr);
					final Double prob = (valueMap == null ? null : valueMap.get (xMappedFirst ? valYColStr : valXCol));
					
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug ("Found values to values map: "+valueMap);
						LOGGER.debug ("Mapped probability: "+prob);
					}

					values[column][row] = (prob == null ? 0.0 : prob.doubleValue());
				}
			}
			
			for (int column = 0; column < counts.length; column++) {
				for (int row = 0; row < counts[column].length; row++) {
					if (values[column][row] != 0.0) {
						if (counts[column][row] < (totalCount / 100)) {
							values[column][row] = 0.0;
						} else {
							final double actualProp = (double)counts[column][row] / totalCount;
							final double difference = ((actualProp / values[column][row]) * 100.0) - 100.0;
							values[column][row] = difference;
						}
					}
				}
			}
		}
	}
	
	public String toString () { return "% Diff From Population"; }
}
