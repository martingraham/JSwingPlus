package model.graph;

import java.util.Set;

public interface EdgeSetValueMaker {

	public Object makeValue (final Set<Edge> edgeSet);
}
