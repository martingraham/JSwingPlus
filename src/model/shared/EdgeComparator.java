package model.shared;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.log4j.Logger;

import model.graph.Edge;

public class EdgeComparator implements Comparator<Object> {

	final static Logger LOGGER = Logger.getLogger (EdgeComparator.class);
	
	private final static EdgeComparator INSTANCE = new EdgeComparator ();
	
	static public EdgeComparator getInstance() {
		return INSTANCE;
	}
	
	
	// If nullTest() != 0, then return nullTest() - 2;
	public int nullTest (final Object obj1, final Object obj2) {
		if (obj1 == null) {
			LOGGER.debug ("obj1: null, obj2: "+obj2);
			return obj2 == null ? 2 : 1;
		}
		return obj2 == null ? 3 : 0;
	}
	
	
	@Override
	public int compare (final Object obj1, final Object obj2) {		
	
		final int nullTest = nullTest (obj1, obj2);
		if (nullTest > 0) {
			return nullTest - 2;
		}
		
		Edge edge1 = null;
		Edge edge2 = null;
		Collection<Edge> col1 = null;
		Collection<Edge> col2 = null;
		Object eObj1 = null;
		Object eObj2 = null;
		
		if (obj1 instanceof Edge) {
			edge1 = (Edge)obj1;
			eObj1 = edge1.getEdgeObject();
		}
		else if (obj1 instanceof Collection) {
			col1 = (Collection<Edge>)obj1;
			eObj1 = getFirstFromCollection (col1);
		}
		
		if (obj2 instanceof Edge) {
			edge2 = (Edge)obj2;
			eObj2 = edge2.getEdgeObject();
		}
		else if (obj2 instanceof Collection) {
			col2 = (Collection<Edge>)obj2;
			eObj2 = getFirstFromCollection (col2);
		}
		

		int diff = 0;

		if (nullTest (eObj1, eObj2) == 0) {
			if (Comparable.class.isAssignableFrom (eObj1.getClass()) && eObj1.getClass().equals(eObj2.getClass())) {
				LOGGER.debug ("eobj1: "+eObj1+", eobj2: "+eObj2);
				diff = ((Comparable)eObj1).compareTo((Comparable)eObj2);
				LOGGER.debug ("diff: "+diff);
			}
			else {
				String objString1, objString2;
				if (eObj1.getClass() == eObj2.getClass()) {
					objString1 = eObj1.toString();
					objString2 = eObj2.toString();
				} else {
					objString1 = eObj1.getClass().getName();
					objString2 = eObj2.getClass().getName();
				}
				diff = objString1.compareTo (objString2);
			}
		}

		
		return diff;
	}
	
	
	Object getFirstFromCollection (final Collection<Edge> col) {
		final Iterator<Edge> iter = col.iterator();
		return iter.hasNext() ? iter.next().getEdgeObject() : col;
	}

}
