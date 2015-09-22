package util.collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class ArrayListUtil extends Object {

	private final static Logger LOGGER = Logger.getLogger (ArrayListUtil.class);
	
    public static void arrayListMinusHashSet (final List<Object> list, final Set<Object> set) {

        int shift = 0;

        for (int n = 0; n < list.size(); n++) {
            final Object obj = list.get (n);

            if (set.contains (obj)) {
               shift++;
            }
            else if (shift > 0) {
               list.set (n - shift, list.get(n));
            }
        }

        final int listEnd = list.size();
        for (int n = listEnd; --n >= listEnd - shift;) {
            list.remove (n);
        }
    }

    public static void removeNulls (final List list) {

        int shift = 0;
        
        if (LOGGER.isDebugEnabled()) {
        	LOGGER.debug ("list in: "+list);
        }
        
        for (int n = 0; n < list.size(); n++) {
            final Object obj = list.get (n);

            if (obj == null) {
               shift++;
            }
            else if (shift > 0) {
               list.set (n - shift, list.get(n));
            }
        }
        
        final int listEnd = list.size();
        for (int n = listEnd; --n >= listEnd - shift;) {
            list.remove (n);
        }
        
        if (LOGGER.isDebugEnabled()) {
        	LOGGER.debug ("list out: "+list);
        }
    }
    
    
    public static List removeNulls2 (final List list) {

        int shift = 0;
        
        if (LOGGER.isDebugEnabled()) {
        	LOGGER.debug ("list in: "+list);
        }

        for (int n = 0; n < list.size(); n++) {
            final Object obj = list.get (n);

            if (obj == null) {
               shift++;
            }
            else if (shift > 0) {
               list.set (n - shift, list.get (n));
            }
        }

        List newList = list.subList (0, list.size() - shift);
        
        if (LOGGER.isDebugEnabled()) {
        	LOGGER.debug ("list out: "+list);
        }
        
        return newList;
    }
    
    
    
    
    public static void riffle (final List<Object> list) {
    	final List<Object> tempList = new ArrayList<Object> (list);
    	for (int listIndex = list.size(); --listIndex >= 0;) {
    		final int riffleIndex = ((listIndex & 1) == 0) ? listIndex >> 1 : list.size() - 1 - (listIndex >> 1);
    		list.set (riffleIndex, tempList.get (listIndex));
    	}
    }

    
    public static void bridgeGapWithNulls (final List<?> list, final int index) {
    	if (index >= list.size()) {
    		for (int listIndex = index - list.size() + 1; --listIndex >= 0;) {
    			list.add (null);
    		}
    	}
    }
    
    
    public static <T> boolean listRequiresSorting (final List<T> list, final Comparator<? super T> comp) {
    	boolean outOfSequenceFound = false;
    	
    	for (int n = 0; n < list.size() - 1 && !outOfSequenceFound; n++) {
    		int compVal = comp.compare (list.get(n), list.get(n+1));
    		outOfSequenceFound = (compVal > 0);
    	}
    	
    	return outOfSequenceFound;
    }
}