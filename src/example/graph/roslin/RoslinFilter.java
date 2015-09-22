package example.graph.roslin;

import java.util.BitSet;

import model.graph.Edge;
import model.graph.impl.DefaultGraphFilter;

public class RoslinFilter extends DefaultGraphFilter {
	
	MarkerMap map;
	BitSet temp = new BitSet ();
	
	public RoslinFilter (final MarkerMap mmap) {
		super ();
		map = mmap;
	}
	
	@Override
	public boolean includeEdge (final Edge edge) {
		return edge == null ? false : includeNode (edge.getNode1()) && includeNode (edge.getNode2());
	}	
	
	@Override
	public boolean includeNode (final Object obj) {
		if (obj == null) {
			return false;
		}
		final Animal animal = (Animal)obj;
		final SNPData data = animal.getData ();
		temp.clear ();
		if (data != null) {
			final BitSet globalFilter = map.getGloballyActive();
			temp.or (data.getPossErrors());
			temp.and (globalFilter);
		} else {
			temp.set (1);
		}
		
		return !temp.isEmpty ();
	}
}
