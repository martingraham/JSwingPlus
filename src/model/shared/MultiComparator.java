package model.shared;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MultiComparator<T> implements Comparator<T> {

	protected List<Comparator<Object>> comparatorList;
	
	/**
	 * Empty constructor. Can fill layer with setComparatorList method.
	 */
	public MultiComparator () { /* Empty */ }
	
	public MultiComparator (final List<Comparator<Object>> comparatorList) {
		this.comparatorList = comparatorList;
	}
	
	public MultiComparator (final MultiComparator<T> multiComp) {
		final List<Comparator<Object>> deepListCopy = new ArrayList<Comparator<Object>> (multiComp.getComparatorList ());
		this.comparatorList = deepListCopy;
	}
	
	
	
	public List<Comparator<Object>> getComparatorList () {
		return comparatorList;
	}
	
	public void setComparatorList (final List<Comparator<Object>> newList) {
		comparatorList = newList;
	}
	
	@Override
	public int compare (final T obj1, final T obj2) {
		
		// Whack the null check here and the Comparators in the comparatorList
		// don't need to keep re-checking
		
		if (obj1 == obj2) { return 0; }
		
		if (obj1 == null) {
			return 1;
		}
		else if (obj2 == null) {
			return -1;
		}
		
		int diff = 0;
		for (int n = 0, listSize = comparatorList.size(); n < listSize && diff == 0; n++) {
			diff = comparatorList.get(n).compare (obj1, obj2);
		}
		return diff;
	}
}
