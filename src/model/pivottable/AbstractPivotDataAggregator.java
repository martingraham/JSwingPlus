package model.pivottable;

import java.util.Map;

import javax.swing.table.TableModel;

public abstract class AbstractPivotDataAggregator implements PivotDataAggregator {
	

	protected SimplePivotTableModel pivotModel;
	
	public double[][] aggregate (final SimplePivotTableModel pivotTableModel) {
		final int[][] counts = new int [pivotTableModel.getColumnCount() - 1] [pivotTableModel.getRowCount()];
		final int[][] sums = new int [pivotTableModel.getColumnCount() - 1] [pivotTableModel.getRowCount()];
		final double[][] values = new double [pivotTableModel.getColumnCount() - 1] [pivotTableModel.getRowCount()];
	
		pivotModel = pivotTableModel;
		final TableModel rawTableModel = pivotTableModel.getRawTableData();
		final int columnXIndex = pivotTableModel.columnXIndex;
		final int columnYIndex = pivotTableModel.columnYIndex;
		final int columnDataIndex = pivotTableModel.columnDataIndex;
		final Map<Object, Integer> rowIndexMap = pivotTableModel.getRowIndexMap();
		final Map<Object, Integer> colIndexMap = pivotTableModel.getColIndexMap();
		
		for (int rowIndex = rawTableModel.getRowCount(); --rowIndex >= 0; ) {
			final Object valX = rawTableModel.getValueAt (rowIndex, columnXIndex);
			final Object valY = rawTableModel.getValueAt (rowIndex, columnYIndex);
			final int pivotColIndex = colIndexMap.get(valX).intValue();
			final int pivotRowIndex = rowIndexMap.get(valY).intValue();
			
			final Object dataPoint = rawTableModel.getValueAt (rowIndex, columnDataIndex);
			
			processDatum (dataPoint, pivotColIndex, pivotRowIndex, sums, counts);
		}
		
		calculateValues (sums, counts, values);
		
		return values;
	}
	
	
	public void processDatum (final Object datum, final int pivotColIndex, final int pivotRowIndex, 
			final int[][] sums, final int[][] counts) {
		final int valData = (datum instanceof Integer) ? ((Integer)datum).intValue() : 0;
		sums [pivotColIndex][pivotRowIndex] += valData;
		counts [pivotColIndex][pivotRowIndex] += 1;
	}

	public abstract void calculateValues (final int[][] sums, final int[][] counts, final double[][] values);
}