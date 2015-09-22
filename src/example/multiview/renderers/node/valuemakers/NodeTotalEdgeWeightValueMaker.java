package example.multiview.renderers.node.valuemakers;

import java.util.Set;

import model.graph.Edge;
import model.graph.EdgeSetValueMaker;

public class NodeTotalEdgeWeightValueMaker implements EdgeSetValueMaker {

	@Override
	public Object makeValue (final Set<Edge> edgeSet) {
		double total = 0.0;
		for (Edge edge : edgeSet) {
			if (edge.getEdgeObject() instanceof Number) {
				total += ((Number)edge.getEdgeObject()).doubleValue();
			}
		}
		
		return Integer.valueOf (2 + (int)Math.sqrt (total * 16.0));
	}
}
