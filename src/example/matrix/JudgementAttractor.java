package example.matrix;

import model.graph.Edge;
import swingPlus.graph.force.impl.SimpleEdgeAttractor;

public class JudgementAttractor extends SimpleEdgeAttractor {

	
	protected double getLengthModifier (final Edge edge) {
		double lengthModifier = 1.0;
		if (edge.getEdgeObject() instanceof IndividualJudgement) {
			final IndividualJudgement judge = (IndividualJudgement)edge.getEdgeObject ();
			lengthModifier = 6.0 - judge.getOverallRating();
		}
		return lengthModifier;
	}
}
