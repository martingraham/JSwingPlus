package model.matrix;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

import model.graph.Edge;
import model.graph.GraphModel;
import model.shared.MultiComparator;

import util.collections.PropertyChangeArrayList;

/**
 * Collection of comparators that sorts node objects
 * 
 * @author cs22
 * @param model.matrix.Graph a class fulfilling the GraphModel interface
 * @version 1.0 21/01/09
 */

public class NodeMetrics extends MultiComparator<Object> {

	GraphModel graph;
	Comparator<Object> outDegreeComp = new NodeMetrics.OutDegreeComparator ();
	Comparator<Object> inDegreeComp = new NodeMetrics.InDegreeComparator ();
	Comparator<Object> nodeComp = new NodeComparator ();
	Comparator<Object> alphaComp = new AlphaComparator ();
	Comparator<Object> edgeSetComp = new EdgeSetComparator ();
	
	@SuppressWarnings("unchecked")
	protected NodeMetrics (final GraphModel gModel) {
		setComparatorList (new PropertyChangeArrayList<Comparator<Object>> 
			(Arrays.asList (outDegreeComp, nodeComp, alphaComp, edgeSetComp)));
		graph = gModel;
	}
	
	protected NodeMetrics (final GraphModel gModel, final boolean inDegreeVersion) {
		setComparatorList (new PropertyChangeArrayList<Comparator<Object>> 
			(Arrays.asList (inDegreeVersion ? inDegreeComp : outDegreeComp, 
					nodeComp, alphaComp, edgeSetComp)));
		graph = gModel;
	}
	
	
	static class NodeComparator implements Comparator<Object> {

		@SuppressWarnings("unchecked")
		@Override
		public int compare (final Object obj1, final Object obj2) {
			if (obj1 instanceof Comparable && obj2 instanceof Comparable) {
				return ((Comparable)obj1).compareTo (obj2);
			}
			return 0;
		}
		
		@Override
		public String toString () { return "Node Properties"; }
	}
	
	
	static class AlphaComparator implements Comparator<Object> {

		@Override
		public int compare (final Object obj1, final Object obj2) {
			return obj1.toString().compareTo (obj2.toString());
		}
		
		@Override
		public String toString () { return "Alphabetic"; }
	}
	
	
	class OutDegreeComparator implements Comparator<Object> {

		@Override
		public int compare (final Object obj1, final Object obj2) {
			final Collection<Edge> coll1 = graph.getEdges (obj1);
			final Collection<Edge> coll2 = graph.getEdges (obj2);
			final int deg1 = (coll1 == null ? 0 : coll1.size());
			final int deg2 = (coll2 == null ? 0 : coll2.size());
			return deg1 - deg2;
		}
		
		@Override
		public String toString () { return "Edge Set Size"; }
	}
	
	
	class InDegreeComparator implements Comparator<Object> {

		@Override
		public int compare (final Object obj1, final Object obj2) {
			final Collection<Edge> coll1 = graph.getCoincidentEdges (obj1);
			final Collection<Edge> coll2 = graph.getCoincidentEdges (obj2);
			final int deg1 = (coll1 == null ? 0 : coll1.size());
			final int deg2 = (coll2 == null ? 0 : coll2.size());
			return deg1 - deg2;
		}
		
		@Override
		public String toString () { return "Edge Set Size"; }
	}
	
	
	class EdgeSetComparator implements Comparator<Object> {

		@Override
		public int compare (final Object obj1, final Object obj2) {
			final Set<Edge> set1 = graph.getEdges (obj1);
			final Set<Edge> set2 = graph.getEdges (obj2);	
			final int deg1 = (set1 == null ? 0 : set1.size());
			final int deg2 = (set2 == null ? 0 : set2.size());
			return deg1 - deg2;
		}
		
		@Override
		public String toString () { return "Edge Set Properties"; }
	}

}
