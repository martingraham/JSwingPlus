package example.graph.renderers.node.valuemakers;

import java.util.Set;

import model.graph.Edge;
import model.graph.EdgeSetValueMaker;

public class NodeDegreeValueMaker implements EdgeSetValueMaker {

	@Override
	public Object makeValue (final Set<Edge> edgeSet) {
		final int degree = edgeSet.size();
		final int diameter = 2 + (int)(10.0 * Math.sqrt (degree));
		return Integer.valueOf (diameter);
	}
}
