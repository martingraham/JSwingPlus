package model.pivottable;


public interface PivotDataAggregator {
	
	public double[][] aggregate (final SimplePivotTableModel pivotTableModel);
	
	public void processDatum (final Object datum, final int pivotColIndex, final int pivotRowIndex, 
			final int[][] sums, final int[][] counts);

	public void calculateValues (final int[][] sums, final int[][] counts, final double[][] values);
}