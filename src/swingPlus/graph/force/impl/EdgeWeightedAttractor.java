package swingPlus.graph.force.impl;

import model.graph.Edge;

public class EdgeWeightedAttractor extends SimpleEdgeAttractor {

	public EdgeWeightedAttractor () {
		this (DEFAULTSTIFFNESS, DEFAULTLINKLENGTH); // defaults
	}
	
	public EdgeWeightedAttractor (final int stiffness, final double linkLength) {
		super (stiffness, linkLength);
	}
	
	
	protected double getLengthModifier (final Edge edge) {
		double modifier = super.getLengthModifier (edge);
		final Object edgeObject = edge.getEdgeObject();
		if (edgeObject instanceof Number) {
			final Number edgeValue = (Number)edgeObject;
			modifier /= edgeValue.doubleValue();
		}
		return modifier;
	}
}
