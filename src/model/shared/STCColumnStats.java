package model.shared;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.table.TableModel;

import org.apache.log4j.Logger;


public final class STCColumnStats {

	
	private final static Logger LOGGER = Logger.getLogger (STCColumnStats.class);
	
	
	private final static STCColumnStats INSTANCE = new STCColumnStats ();
	
	private STCColumnStats () {}
	
	public static STCColumnStats getInstance () { return INSTANCE; }
	
	
	private final static String N_A = "N/A";
	
	public Set<Object> getModal (final SortedTableColumn<?> stc) {
		final Set<Object> objs = new HashSet<Object> ();
		
		if (!stc.isNumeric()) {
			final Set<?> keys = stc.discreteFirstIndices.keySet();
			int maxRange = -1;
			for (Object key : keys) {
				final int iFirst = stc.discreteFirstIndices.get(key).intValue();
				final int iLast = stc.discreteLastIndices.get(key).intValue();
				final int range = (iLast - iFirst) - 1;
				if (maxRange == range) {
					objs.add (key);
				}
				else if (maxRange < range) {
					objs.clear ();
					objs.add (key);
					maxRange = range;
				}
			}
		}
		return objs;
	}
	
	
	public Object getMedian (final SortedTableColumn<?> stc) {
		
		final int listSize = stc.getFilteredOrderedList().size();
		final int halfWay = (listSize - 1) / 2;
		final boolean odd = (listSize % 2 != 0);
		
		if (stc.isEmpty()) {
			return N_A;
		}
		
		
		if (odd || !stc.isNumeric()) {
			return stc.getVal (stc.getFilteredOrderedList().get (halfWay));
		}
		else {
			final Object justBeforeHalfway = stc.getVal (stc.getFilteredOrderedList().get (halfWay));
			final Object justAfterHalfway = stc.getVal (stc.getFilteredOrderedList().get (halfWay + 1));
			Object median = null;
			if (stc.isA (Date.class)) {
				final Date date1 = (Date)justBeforeHalfway;
				final Date date2 = (Date)justAfterHalfway;
				median = new Date ((date1.getTime() + date2.getTime()) / 2);
			}
			else {
				final Number number1 = (Number)justBeforeHalfway;
				final Number number2 = (Number)justAfterHalfway;
				median = new Double ((number1.doubleValue() + number2.doubleValue()) / 2.0);
			}
			return median;
		}
	}
	
	
	public Object getPercentile (final SortedTableColumn<?> stc, final int pHundred) {
		
		final int listSize = stc.getFilteredOrderedList().size();
		if (stc.isEmpty ()) {
			return N_A;
		}
		
		final double pPoint = ((listSize - 1) * pHundred) / 100.0;
		int pPointInt = (int) Math.round (pPoint);
		final int floor = (int)Math.floor (pPoint);
		if (Math.abs ((pPoint - (double)floor) - 0.5) < .000001) {
			pPointInt = (floor % 2 == 0) ? floor : floor + 1;
		} 

		return stc.getVal (stc.getFilteredOrderedList().get (pPointInt));
	}
	
	
	public Number getMean (final SortedTableColumn<?> stc) {
		Number num = null;
		if (stc.isNumeric()) {
			if (stc.isA (Date.class)) {
				double mean = 0.0;
				for (int n = stc.getFilteredOrderedList().size(); --n >= 0;) {
					final Object obj = stc.getVal (stc.getFilteredOrderedList().get (n));
					final Date date = (Date)obj;
					mean += date.getTime();
				}
				mean /= stc.getFilteredOrderedList().size();
				num = Long.valueOf ((long)mean);
			} else {
				double mean = 0.0;
				for (int n = stc.getFilteredOrderedList().size(); --n >= 0;) {
					final Number number = (Number) stc.getVal (stc.getFilteredOrderedList().get (n));
					mean += number.doubleValue ();
				}
				mean /= stc.getFilteredOrderedList().size();
				num = Double.valueOf (mean);
			}
		}
		return num;
	}
	
	
	public Number getVariance (final SortedTableColumn<?> stc) {
		Number variance = null;
		
		if (stc.isNumeric()) {	
			final boolean date = stc.isA (Date.class);
			final Number mean = getMean (stc);
			final double meanVal = mean.doubleValue ();
			
			double var = 0.0;
			for (int n = stc.getFilteredOrderedList().size(); --n >= 0;) {
				Number number = null;
				if (date) {
					final Object obj = stc.getVal (stc.getFilteredOrderedList().get (n));
					final Date date2 = (Date)obj;
					number = date2.getTime();
				} else {
					number = (Number) stc.getVal (stc.getFilteredOrderedList().get (n));
				}
				
				double doubleVal = number.doubleValue ();
				doubleVal -= meanVal;
				doubleVal *= doubleVal;
				var += doubleVal;
			}
			var /= (stc.getFilteredOrderedList().size() - 1);
			variance = new Double (var);
		}
		return variance;
	}
	
	
	public Number getStandardDev (final SortedTableColumn<?> stc) {
		final Number sDev = getVariance (stc);
		return sDev == null ? sDev : new Double (Math.sqrt (sDev.doubleValue()));
	}
	
	
	public void statto (final SortedTableColumn<?> stc, final TableModel model) {
		final StringBuilder strBuilder = new StringBuilder ();
		
		strBuilder.append ("Column: "+model.getColumnName (stc.getModelIndex())+"\t");
		strBuilder.append ("Median: "+STCColumnStats.getInstance().getMedian (stc)+"\t");
		strBuilder.append ("10%ile: "+STCColumnStats.getInstance().getPercentile (stc, 10)+"\t");
		strBuilder.append ("90%ile: "+STCColumnStats.getInstance().getPercentile (stc, 90)+"\t"); 		
		strBuilder.append ("Modal: "+STCColumnStats.getInstance().getModal (stc)+"\t");
		strBuilder.append ("Mean: "+STCColumnStats.getInstance().getMean (stc)+"\t");
		strBuilder.append ("S.D.: "+STCColumnStats.getInstance().getStandardDev (stc));
		
		LOGGER.info (strBuilder.toString());
	}
}
