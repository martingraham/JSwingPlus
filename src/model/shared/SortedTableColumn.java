package model.shared;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SortOrder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;


import org.apache.log4j.Logger;

import util.ClassUtils;
import util.collections.ArrayListUtil;


    public class SortedTableColumn<T extends Comparable<? super T>> extends TableColumn {

    	/**
		 * 
		 */
		private static final long serialVersionUID = 4212751837034240897L;
		private static final Logger LOGGER = Logger.getLogger (SortedTableColumn.class);
		
		private static final int OBJECT_INDEX_LIMIT = 20;
		
		transient Comparator<Integer> objectAndIndexComparator = new STCComparator ();
		transient Comparator<Integer> objectOnlyComparator = new KeyOnlyComparator ();
		
		transient Integer probeModelIndex = Integer.valueOf (-1);
		transient T probeKey = (T)null;
		
		List<Integer> ordering;
		List<Integer> filteredOrderedList;
		Map<T, Integer> discreteFirstIndices;
		Map<T, Integer> discreteLastIndices;
		Map<T, Integer> discreteObjectOrdering;	// For small discrete ranges, 20 or less. see OBJECT_INDEX_LIMIT
		List<Integer> invDiscrete;
		int sortType;
		Comparable minBound = null, maxBound = null;
		T storedMin, storedMax;
		TableModel model;
		
		// BitSet of the current RowFilter effect on model indices.
		// This needs to be updated from time to time, usually in the calling
		// JParCoord class which pulls a fresh version from ParCoordRowSorter when data
		// in the table model is changed or a column added.
		// Changing the RowFilter does this updating too.
		BitSet filterSet;
		
		// Closest common superclass of entries in this column
		// basically needed 'cos DefaultTableModel.getColumnClass just returns Object.class
		// all the time and we can't rely on others correcting this
		Class<?> superClass = Object.class;	
    	

		/*
    	public SortedTableColumn (TableModel model) {
    		this (model, 0);
    	}
    	*/
		
		
		public SortedTableColumn (final TableModel model, final int modelIndex) {
    		super (modelIndex);
    		
    		ordering = new ArrayList<Integer> (model.getRowCount());		
    		filteredOrderedList = new ArrayList<Integer> (model.getRowCount());
    		sortType = SortOrder.ASCENDING.ordinal();	
    		discreteFirstIndices = new HashMap <T, Integer> ();
    		discreteLastIndices = new HashMap <T, Integer> ();
    		discreteObjectOrdering = new HashMap <T, Integer> ();
    		invDiscrete = new ArrayList<Integer> ();
    		
    		//populateAndSort (model);
    	}
    	
		
    	public void copyFromSource (final TableModel newModel, final SortedTableColumn<?> copySource) {
    		setTableModel (newModel);
    		copyDetails (copySource);
    		refilterAndCategorise ();
    	}
    
    	
    	protected void setTableModel (final TableModel newModel) {
    		if (newModel != model) {
    			this.model = newModel;
    		}
    	}
    	
    	public void setFilterSet (final BitSet newFilterSet) {
    		this.filterSet = newFilterSet;
    	}
    	
    	
    	
    	public void populateAndSort (final TableModel newModel) {
    		setTableModel (newModel);
    		populateAndSort ();
    	}

    	
    	protected void populateAndSort () {
    		populate ();
    		sort (false);
    		refilterAndCategorise ();
    	}
    	
    	
    	public void refilterAndCategorise () {
    		refilteringStep ();
    		discretise ();
    		STCColumnStats.getInstance().statto (this, model);
    	}
    	
    	
    	void refilteringStep () {
    		filteredOrderedList.clear ();
    		filteredOrderedList.addAll (ordering);
       	  
    		/**
    		 * Might need to store filterSet BitSet for refiltering purposes
    		 */
    		LOGGER.debug ("filter set: "+filterSet);
    		if (filterSet != null) {
	    		for (int n = 0; n < filteredOrderedList.size(); n++) {
	    			if (! filterSet.get (filteredOrderedList.get(n).intValue())) {
	    				filteredOrderedList.set (n, null);
	    			}
	    		}
	    		ArrayListUtil.removeNulls (filteredOrderedList);
    		}
     	}
    	
    	protected boolean isNumeric () {
    		//Class<?> columnClass = model.getColumnClass (modelIndex);
    		final Class<?> columnClass = superClass;
    		return Number.class.isAssignableFrom (columnClass)
				|| Date.class.isAssignableFrom (columnClass);
    	}
    	
    	public boolean isA (final Class<?> classX) {
    		//Class<?> columnClass = model.getColumnClass (modelIndex);
    		final Class<?> columnClass = superClass;
    		return classX.isAssignableFrom (columnClass);		
    	}
    	
    	
    	public Class<?> getColumnClass () {
    		//return (Class<?>)model.getColumnClass (modelIndex);
    		return superClass;
    	}
    	
    	public final T getMin () { 
     		return isEmpty() ? null : getSortedIndexVal (0);
    	}
    	
    	public final T getMax () { 
     		return isEmpty() ? null : getSortedIndexVal (filteredOrderedList.size() - 1);
    	}
    	
    	public boolean isEmpty () {
    		return filteredOrderedList.isEmpty ();
    	}
    	
    	public int getDiscreteRange () {
    		int range = getDiscreteList().size();
    		if (range == 0 && isA (Integer.class)) {
    			final Comparable maxC = (Comparable)getMax();
    			final Comparable minC = (Comparable)getMin();
    			if (maxC != null && minC != null) {
    				range = Math.abs (((Integer)maxC).intValue() - ((Integer)minC).intValue()) + 1;
    			}
    		}
    		return range;
    	}
    	

    	
    	void populate () {
			ordering.clear ();
			final Set<Class<?>> detectedClasses = new HashSet<Class<?>> ();
    		
    		for (int n = 0; n < model.getRowCount(); n++) {
    			final T val = getVal (n);
				
				if (val != null) {
    				detectedClasses.add (val.getClass());
	    			ordering.add (Integer.valueOf (n));
				}
    		}
    		
    		LOGGER.debug ("Detected Classes: "+detectedClasses.toString());
    		
    		superClass = ClassUtils.getInstance().getCommonSuperclass (detectedClasses);
    	}
    	
    	
    	// Used with copy constructor
    	void copyDetails (final SortedTableColumn<?> stc) {
    		ordering = new ArrayList<Integer> (stc.getUnfilteredOrderedList());
    		superClass = stc.getColumnClass();
    	}
    	
    	protected T getSortedIndexVal (final int sortedRowIndex) {
    		return getVal (filteredOrderedList.get (sortedRowIndex).intValue());
    	}
    	
    	protected T getVal (final int modelRowIndex) {
    		return (T) model.getValueAt (modelRowIndex, modelIndex);
    	}
    	
    	protected void setProbe (final int modelRowIndex, final T objectVal) {
    		probeModelIndex = Integer.valueOf (modelRowIndex);
    		probeKey = objectVal;
    	}
    	
    	
    	public void flipSortOrder (final boolean nextSortType) {
    		if (!isA (Comparable.class)) {
    			setCurrentOrder (SortOrder.UNSORTED);
    		}
    		else if (nextSortType) {
    			setCurrentOrder (SortOrder.values() [(sortType + 1) % (SortOrder.values().length - 1)]);
    		}
    	}
    	
    	
    	public void sort (final boolean nextSortType) {
    		
    		flipSortOrder (nextSortType);

    		switch (getCurrentOrder()) {
    			case ASCENDING: case DESCENDING:	
    				long nano = System.nanoTime ();
    				Collections.sort (ordering, objectAndIndexComparator);
    				nano = System.nanoTime() - nano;
    				LOGGER.info ("Sorting column "+getHeaderValue()+" of "+ordering.size()+" objects took "+(nano/1E6)+" ms.");			
    				break;
    			case UNSORTED:
    				populate ();
    				break;
    			default:
    				break;		
    		}

    		if (LOGGER.isDebugEnabled()) {
    			LOGGER.debug ("ordering: "+ordering);
    		}
    	}
    	
    	public SortOrder getCurrentOrder () { 
    		flipSortOrder (false);
    		return SortOrder.values() [sortType];
    	}
    	
    	void setCurrentOrder (final SortOrder sOrder) {
    		sortType = sOrder.ordinal ();
    	}
    	
    	
    	
    	public int getSortedPosForModelIndex (final Integer modelIndex) {
    		return Collections.binarySearch (filteredOrderedList, modelIndex, objectAndIndexComparator);
    	}
    	
    	
    	public int getY (final Object val, final int availHeight) {
			int y = -1;
			
			if (val != null) {
				if (isA (Number.class)) {
					final Number min = (Number)getMinBound();
					final Number max = (Number)getMaxBound();
					final double range = max.doubleValue() - min.doubleValue();
					final Number valN = (Number)val;
					final double ratio = (valN.doubleValue() - min.doubleValue()) / range;
					y = (int)((double)(availHeight) * ratio);
				}
				else if (isA (Date.class)) {
					final Number min = ((Date)getMinBound()).getTime();
					final Number max = ((Date)getMaxBound()).getTime();
					final double range = max.doubleValue() - min.doubleValue();
					final Number valN = ((Date)val).getTime();
					final double ratio = (valN.doubleValue() - min.doubleValue()) / range;
					y = (int)((double)(availHeight) * ratio);
				}
				//else 
				else {
					T valT = (T)val;
					final Integer intObj = discreteObjectOrdering.get (valT);
					
					if (intObj != null) {
						final int index = intObj.intValue();
	    				final double ratio = (double)index / ((double)Math.max (invDiscrete.size() - 1, 1.0));
						y = (int)((double)(availHeight) * ratio);
					}
				}
			}
			return y;
	    }
    	
    	
    	

		public Comparable getValue (final double ratio) {

			final double perCent = Math.max (0.0, Math.min (1.0, ratio));
    		Comparable obj = null;
    		final Class<?> columnClass = getColumnClass ();
   		
    		if (isA (Number.class)) {
    			final boolean integers = (columnClass == Integer.class || columnClass == Short.class);
    			final Number min = (Number)getMinBound();
    			final Number max = (Number)getMaxBound();
    			final double range = max.doubleValue() - min.doubleValue();
    			final double val = (perCent * range) + min.doubleValue(); 
    			//int vali = (int)Math.round (val);
    			if (integers) {
    				if (columnClass == Integer.class) {
    					obj = Integer.valueOf ((int)Math.round (val));
    				}
    				else if (columnClass == Short.class) {
    					obj = Short.valueOf ((short)Math.round (val));
    				}
    			} else {
    				obj = Double.valueOf (val);
    			}
    			/*
    			try {
    				java.lang.reflect.Method m = columnClass.getMethod ("valueOf", integers ? int.class : double.class);
    				
    				if (integers) {
    					obj = (T)m.invoke (null, vali);
    				} else {
    					obj = (T)m.invoke (null, val);
    				}
    
    				 // this generates a weird bug-causing result, the ternary operator makes java
    				 // think it's a always a double even if it's the int that's chosen
    				// obj = (T)m.invoke (null, integers ? vali : val);
    				//
    			} catch (IllegalArgumentException iae) {
    				LOGGER.error (iae.getLocalizedMessage(), iae);
    			} catch (Exception e) {
    				LOGGER.error (e.getLocalizedMessage(), e);
    			}
    			*/
    		}
    		else if (isA (Date.class)) {
    			final Number min = ((Date)getMinBound()).getTime();
    			final Number max = ((Date)getMaxBound()).getTime();
    			final double range = max.doubleValue() - min.doubleValue();
    			final double val = (perCent * range) + min.doubleValue(); 
    			final long vall = (long)val;
    			obj = new Date (vall);
    		}
    		else if (!isEmpty()) {
    			//int row = (int)(ratio * (double)getOrderedList().size());
    			final int discretePoint = (int) Math.round (perCent * ((double)invDiscrete.size() - 1));
    			obj = getVal (invDiscrete.get(discretePoint).intValue());
    		}
    		return obj;
    	}
    	
    	
    	
    	void discretise () {

    		if (!isNumeric()) {
	    		T obj = null;
	    		discreteFirstIndices.clear ();
	    		discreteLastIndices.clear ();
	    		discreteObjectOrdering.clear ();
	    		invDiscrete.clear ();
	    		int discreteCount = 0;
	    		
	    		for (int n = 0; n < getFilteredOrderedList().size(); n++) {
	    			final Integer intObj = getFilteredOrderedList().get(n);
	    			final T nextObj = getVal (intObj.intValue());
	    			if (!nextObj.equals (obj)) {
	    				if (LOGGER.isDebugEnabled()) {
	    					LOGGER.debug ("obj: "+obj+", no: "+nextObj+", n: "+n+", I: "+intObj+", discreteCount: "+discreteCount);
	    				}
	    				discreteFirstIndices.put (nextObj, Integer.valueOf (n));
	    				invDiscrete.add (intObj);
	    				discreteObjectOrdering.put (nextObj, Integer.valueOf (discreteCount));
	    				discreteCount++;
	    				if (obj != null) {
	    					discreteLastIndices.put (obj, Integer.valueOf (n - 1));
	    				}
	    				obj = nextObj;

	    			}
	    		}
	    		
	    		if (obj != null) {
					discreteLastIndices.put (obj, Integer.valueOf (getFilteredOrderedList().size() - 1));
				}
	    		
	    		if (LOGGER.isDebugEnabled()) {
	    			LOGGER.debug ("filtered list size: "+getFilteredOrderedList().size());
		    		LOGGER.debug (discreteFirstIndices);
		    		LOGGER.debug (discreteLastIndices);
		    		LOGGER.debug (invDiscrete);
	    		}
    		}
    		
    		storedMin = (T)getMinBound ();
    		storedMax = (T)getMaxBound ();
    	}
    	
    	public List<Integer> getFilteredOrderedList () {
    		return filteredOrderedList;
    	}
    	
    	public List<Integer> getUnfilteredOrderedList () {
    		return ordering;
    	}
    	
    	public List<Integer> getDiscreteList () {
    		return invDiscrete;
    	}
    	

    	
    	public void getRange (final Dimension dim, final T value1, final T value2, 
    			final boolean inclusiveFlag1, final boolean inclusiveFlag2) {
    		
    		final int value1FirstIndex = getFirstIndexOf (value1);
    		final int value1LastIndex = getLastIndexOf (value1);
    		final int value2FirstIndex = getFirstIndexOf (value2);
    		final int value2LastIndex = getLastIndexOf (value2);
       		//System.err.println ("value1: "+value1+", "+value1FirstIndex+" to "+value1LastIndex);
       		//System.err.println ("value2: "+value2+", "+value2FirstIndex+" to "+value2LastIndex);
       		
    		final int firstIndex = (value1FirstIndex < value2FirstIndex || value1LastIndex < value2LastIndex)
       			? (inclusiveFlag1 ? value1FirstIndex : value1LastIndex + 1)
       			: (inclusiveFlag2 ? value2FirstIndex : value2LastIndex + 1);
       		
       		final int lastIndex = (value1FirstIndex < value2FirstIndex  || value1LastIndex < value2LastIndex)
       			? (inclusiveFlag2 ? value2LastIndex : value2FirstIndex - 1)
       			: (inclusiveFlag1 ? value1LastIndex : value1FirstIndex - 1);
       		
       		//System.err.println ("dim: "+firstIndex+" --> "+lastIndex);
       		dim.setSize (firstIndex, lastIndex);
    	}
    	
    	
    	
    	
		public void getRange (final Dimension dim,
				final T startValue, final T startValueUpper, final T endValue, final T endValueUpper) {

			final boolean ascendingOrder = getCurrentOrder().equals (SortOrder.ASCENDING);
	
			final T valueFrom = ascendingOrder ? startValue : startValueUpper;
			final T valueTo = ascendingOrder ? endValue : endValueUpper;
			final T valueFromUpper = ascendingOrder ? startValueUpper : startValue;
			final T valueToUpper = ascendingOrder ? endValueUpper : endValue;
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug ("Order: "+ascendingOrder);
				LOGGER.debug ("sv: "+startValue+"\tsvu: "+startValueUpper+"\tev:"+endValue+"\tevu:"+endValueUpper);
				LOGGER.debug ("valueFrom: "+valueFrom+"\tvalueFromUpper: "+valueFromUpper);
				LOGGER.debug ("valueTo: "+valueFrom+"\tvalueToUpper: "+valueFromUpper);
			}
			
			int lowerIndex = 0, upperIndex = 0;
			
			/*
			 * Null values occur when the column has no entries for any rows i.e. all values are null
			 * Not a useful column at all really, but if it isn't looked for it causes errors.
			 */
			if (startValue != null) {
				final int valueFromFirstIndex = getFirstIndexOf (valueFrom);
						
				lowerIndex = (valueFrom.equals (valueTo) ? valueFromFirstIndex
					: Math.min (valueFromFirstIndex, getFirstIndexOf (valueTo)));
				upperIndex = (valueFrom.equals (valueFromUpper)) 
					? getLastIndexOf (valueFrom)
					: getFirstIndexOf (valueFromUpper) - 1;
							
				if (!valueFromUpper.equals (valueToUpper)) {
					final int upperIndex2 = (valueTo.equals (valueToUpper)) 
	    				? getLastIndexOf (valueTo)
	    				: getFirstIndexOf (valueToUpper) - 1;
	    			upperIndex = Math.max (upperIndex, upperIndex2);
	    		}
				
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug ("valueFromFirstIndex: "+valueFromFirstIndex);
					LOGGER.debug ("lowerIndex: "+lowerIndex+", upperIndex: "+upperIndex);
				}
			}

			dim.width = lowerIndex;
        	dim.height = upperIndex;
		}
    	
    	
    	
    	public int getFirstIndexOf (final T obj) {
    		int index = -1;
    		   		
    		if (isNumeric ()) {
    		//if (discreteFirstIndices.isEmpty()) {
    			setProbe (-1, obj);
    			int alpha = getSortedPosForModelIndex (probeModelIndex);	
    			if (alpha < 0) { alpha = -alpha - 1; }
    			index = alpha; 		
    			// index may be at end of size range due to synching columns
    			// (i.e. mouse is beyond furthest data point)
    			//if (index >= ordering.size()) {
    			//	index = ordering.size() - 1;
    			//}
    			//LOGGER.debug ("first index obj: "+obj+", first alpha pos: "+index+", first alpha obj: "+ordering.get(index));
    		}
    		else {
    			final Integer intObj = discreteFirstIndices.get(obj);
    			if (intObj != null) {
    				index = intObj.intValue();
    			}
    		}
    		
    		return index;
    	}
    	
    	
    	
    	public int getLastIndexOf (final T obj) {
    		int index = -1;
   		
    		if (isNumeric ()) {
    			// ModelRowMapping<T> mrw = new ModelRowMapping<T> (ordering.size(), obj);
    			// ^^^ the above caused problems if the ordering list didn't include the full set of rows
    			// in the table (nulls were present in this column), as ordering.size() could then
    			// be less or equal to some full table row indexes we use in the ModelRowMapping objects, and
    			// would not be greater than them in the binary search below, thus causing us to return
    			// a false upper range on a query. MG 02/06/2009
    			
    			setProbe (Integer.MAX_VALUE, obj);
    			int alpha = getSortedPosForModelIndex (probeModelIndex);
    			if (alpha < 0) { alpha = -alpha - 1; }
    			if (alpha > 0) { alpha--; }
    			index = alpha;	
    			LOGGER.debug ("last index obj: "+obj+", last alpha pos: "+alpha+", last alpha obj: "+filteredOrderedList.get(alpha));
    		}	
    		else {
    			final Integer intObj = discreteLastIndices.get(obj);
    			if (intObj != null) {
    				index = intObj.intValue();
    			}
    		}
    		
    		return index;
    	}
    	
  	
    	
    	public void reindexDatum (final int row) {
    		reindexData (row, row);
    	}
    	
    	
    	
    	public void reindexData (final int startRow, final int endRow) {
    		
    		final int newEndRow = Math.min (endRow, model.getRowCount() - 1);
    		// roughly, finding and reindexing 'm' entries in a list of length 'n' takes 0.5*m*n operations
    		// roughly sorting a list of length 'n' takes n * log<sub>2</sub>n operations
    		// so if 0.5 * m > log<sub>2</sub> n then we're better off sorting the entire list
    		final int range = (newEndRow - startRow) + 1;
    		final double log2n = Math.log (model.getRowCount()) / Math.log (2.0);
    		
    		
    		if (range * 0.5 > log2n) {
    			populateAndSort ();
    		}
      		else {
      			
	    		for (int row = startRow; row <= newEndRow; row++) {
	        		int curIndex = -1;
	        		
	        		// Find row in ordering - O(n) operation - don't know old data value so has to be
	        		// a linear search
	        		for (int n = 0; n < ordering.size(); n++) {
	        			final Integer intObj = ordering.get (n);
	        			if (intObj.intValue() == row) {
	        				curIndex = n;
	        				break;
	        			}
	        		}
	        		
	        		// Find position for new value of row in ordering - O(log n) operation
	        		final Integer newI = Integer.valueOf (row);
	        		int insertPos = getSortedPosForModelIndex (newI);
		 			
	        		LOGGER.info ("e first row: "+startRow);
	        		LOGGER.info ("insertPos: "+(-insertPos -1));
	        		LOGGER.info ("curIndex: "+curIndex);
	           		// Shuffle in-between points along between the position of the old value and
	        		// the new value, quicker than re-sorting for a few values and half the cost of
	        		// a remove followed by insert - O(n) operation
		    		if (insertPos < 0) { // if insertPos > 0 it's the same data
		    			insertPos = -insertPos - 1;
		    			if (insertPos != curIndex) {
		    				final int dir = Integer.signum (insertPos - curIndex);
		    				final int insertPos2 = (dir == 1) ? insertPos - 1 : insertPos;
		    				for (int n = curIndex + dir; n != insertPos2 + dir; n += dir) {
		    					ordering.set (n - dir, ordering.get (n));
		    				}
		    			}
		    			ordering.set (insertPos, newI);
		    		}
	    		}
	    		refilterAndCategorise ();
    		}
    	}
    	
    	
    	
    	
    	public void removeDatum (final int row) {
    		removeData (row, row);
    	}
    	
    	
    	public void addDatum (final int row) {
    		addData (row, row);
    	}
    	
    	
    	
    	public void removeData (final int startRow, final int endRow) {
    		// endRow could be Integer.MAX_VALUE in some situations (ask TableModelEvent)
    		final int newEndRow = Math.min (endRow, model.getRowCount() - 1);
    		if (startRow == 0 && newEndRow == model.getRowCount() - 1) {
    			ordering.clear ();
    		}
    		else {
	    		for (int row = startRow; row <= newEndRow; row++) {
		    		int alpha = getSortedPosForModelIndex (Integer.valueOf (row));
		    		if (alpha < 0) { alpha = -alpha - 1; }
		 			ordering.remove (alpha);
	    		}
    		}
    		refilterAndCategorise ();
    	}
    	
    	
    	public void addData (final int startRow, final int endRow) {
    		final int newEndRow = Math.min (endRow, model.getRowCount() - 1);
    		for (int row = startRow; row <= newEndRow; row++) {
	    		int alpha = getSortedPosForModelIndex (Integer.valueOf (row));
	    		if (alpha < 0) { alpha = -alpha - 1; }
	 			ordering.add (alpha, Integer.valueOf (row));
    		}
 			refilterAndCategorise ();
    	}
    	
    	

    	
    	public String toString (final TableModel model) {
     		return "Ord: "+model.getColumnName(modelIndex)+", "+ordering.toString();
    	}

    	/**
    	 * Routines useful for numerical scales for aligning min and max values of that column
    	 */
    	
    	
		public final Comparable getMinBound() {
			if (storedMin != null) {
				return storedMin;
			}
			final T minT = getMin ();
			if (minBound != null && isNumeric()) {
				return (Comparable)minBound;
			}
			return minT;
		}

		public final Comparable getMaxBound() {
			if (storedMax != null) {
				return storedMax;
			}
			final T maxT = getMax ();
			if (maxBound != null && isNumeric()) {
				return (Comparable)maxBound;
			}
			return maxT;
		}

		public final void setMinBound (final Comparable minBound) {
			this.minBound = minBound;
			storedMin = (T)minBound;
			LOGGER.debug ("T: "+minBound.getClass());
		}

		public final void setMaxBound (final Comparable maxBound) {
			this.maxBound = maxBound;
			storedMax = (T)maxBound;
		}
		
		

		
		
		class STCComparator implements Comparator<Integer>, Serializable {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6916171675437957953L;

			@Override
			public int compare (final Integer intObj1, final Integer intObj2) {	
				final int diff = objectOnlyComparator.compare (intObj1, intObj2);
				return diff == 0 ? intObj1.compareTo (intObj2) : diff;
			} 
		}
		
		
		class KeyOnlyComparator implements Comparator<Integer>, Serializable {
			/**
			 * 
			 */
			private static final long serialVersionUID = 7661891660252412073L;

			@Override
			public int compare (final Integer intObj1, final Integer intObj2) {	
				final T tObj1 = (intObj1.equals (probeModelIndex)) ? probeKey : getVal (intObj1.intValue());
				final T tObj2 = (intObj2.equals (probeModelIndex)) ? probeKey : getVal (intObj2.intValue());	
				return tObj1.compareTo (tObj2);
			} 
		}
    }