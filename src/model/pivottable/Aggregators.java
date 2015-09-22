package model.pivottable;

import java.util.ArrayList;
import java.util.List;

public class Aggregators {

	public static final AbstractPivotDataAggregator TOTALISER = new AbstractPivotDataAggregator () {
		public void calculateValues (final int[][] sums, final int[][] counts, final double[][] values) {
			for (int column = 0; column < counts.length; column++) {
				for (int row = 0; row < counts[column].length; row++) {
					values [column][row] = sums [column][row];
				}
			}
		}
		
		public String toString () { return "Sum"; }
	};
	
	
	public static final AbstractPivotDataAggregator COUNTER = new AbstractPivotDataAggregator () {
		public void calculateValues (final int[][] sums, final int[][] counts, final double[][] values) {
			for (int column = 0; column < counts.length; column++) {
				for (int row = 0; row < counts[column].length; row++) {
					values [column][row] = counts [column][row];
				}
			}
		}
		
		public String toString () { return "Count"; }
	};
	
	
	public static final AbstractPivotDataAggregator AVERAGER = new AbstractPivotDataAggregator () {
		public void calculateValues (final int[][] sums, final int[][] counts, final double[][] values) {
			for (int column = 0; column < counts.length; column++) {
				for (int row = 0; row < counts[column].length; row++) {
					final int count = counts [column][row];
					values [column][row] = (count == 0 ? 0 : (double)sums[column][row] / (double)count);
				}
			}
		}
		
		public String toString () { return "Average"; }
	};
	
	
	static private final Aggregators INSTANCE = new Aggregators ();
	
	
	
	final List<AbstractPivotDataAggregator> aggregatorList = new ArrayList <AbstractPivotDataAggregator> ();
	
	private Aggregators () {
		aggregatorList.add (COUNTER);
		//aggregatorList.add (TOTALISER);
		//aggregatorList.add (AVERAGER);
	}
	
	static public final Aggregators getInstance () { return INSTANCE; }
	
	
	public List<AbstractPivotDataAggregator> getList () { return aggregatorList; }
	
	public void addAggregator (final AbstractPivotDataAggregator aggregator) {
		if (! getList().contains (aggregator)) {
			getList().add (aggregator);
		}
	}
}
