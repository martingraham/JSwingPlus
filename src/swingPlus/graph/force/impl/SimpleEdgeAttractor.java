package swingPlus.graph.force.impl;

import java.awt.Point;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import swingPlus.graph.JGraph;
import swingPlus.graph.ObjectPlacement;
import swingPlus.graph.force.AttractiveForceCalculationInterface;

import model.graph.Edge;
import model.graph.GraphModel;


public class SimpleEdgeAttractor implements AttractiveForceCalculationInterface {

	
	final static Logger LOGGER = Logger.getLogger (SimpleEdgeAttractor.class);
	protected final static int DEFAULTSTIFFNESS = 200;
	protected final static double DEFAULTLINKLENGTH = 200;
	
	
	public int stiffness;
	public double linkLength;

	
	public SimpleEdgeAttractor () {
		this (DEFAULTSTIFFNESS, DEFAULTLINKLENGTH); // defaults
	}
	
	public SimpleEdgeAttractor (final int stiffness, final double linkLength) {
		setStiffness (stiffness);
		setLinkLength (linkLength);
	}
	
	@Override
	public void cleanup () {
		// EMPTY
	}
	
	

	@Override
	public void calculateAttractiveForces (final JGraph graph) {
		attract (graph);
	}

	
	
	protected void attract (final JGraph graph) {

		final GraphModel graphModel = graph.getFilteredModel();
		final Set<Object> nodes = graphModel.getNodes();
		final Iterator<Object> iter = nodes.iterator();
		
		while (iter.hasNext()) {
			attractNode (graph, graphModel, iter.next());
		}
	}
	


	protected void attractNode (final JGraph graph, final GraphModel graphModel, final Object obj) {

		final Set<Edge> edges = graphModel.getEdges (obj);
		final Iterator<Edge> edgeIterator = edges.iterator();
		//GraphFilter filter = graph.getGraphFilter();
		
		while (edgeIterator.hasNext()) {
			final Edge edge = edgeIterator.next();
			//if (filter.includeEdge (edge)) {
				if ( !(edge.getNode2() == obj) && (edge.getNode1() == obj || obj.equals (edge.getNode1()))) {
					final ObjectPlacement op1 = graph.getVisualRep (edge.getNode1());
					final ObjectPlacement op2 = graph.getVisualRep (edge.getNode2());
					if (op1 != op2) { // Self-edges cause NaN errors and are pointless to calc anyways
						final double lengthModifier = getLengthModifier (edge);
						calcAttractiveForce (op1, op2, lengthModifier); // Link to children with strength 1 (graph seperation)
					}
				}
			//}
		}
	}
	
	
	protected double getLengthModifier (final Edge edge) { return 1.0; }

	public void calcAttractiveForce (final ObjectPlacement op1, final ObjectPlacement op2, final double degreeOfSep) {


		final double dx = op1.getLocation().getX() - op2.getLocation().getX();
		final double dy = op1.getLocation().getY() - op2.getLocation().getY();
		final double d = Math.sqrt ((dx * dx) + (dy * dy));
		//d = (dx * dx) + (dy * dy);
		final double prefLength = linkLength * degreeOfSep;
		final double uf = (d - prefLength) / stiffness;
        if (LOGGER.isDebugEnabled()) {
        	LOGGER.debug ("uf: "+uf);
        }
		//if (uf > .01) { uf = .01; }
        final double afx = uf * (dx / d);
        final double afy = uf * (dy / d);
		//afx = uf * ((dx * dx) / d) * (dx >= 0.0 ? -1 : 1);
		//afy = uf * ((dy * dy) / d) * (dy >= 0.0 ? -1 : 1);

		op1.incForce (-afx, -afy);
		op2.incForce (afx, afy);
	}


	@Override
	public Object getNearestTo (final JGraph graph, final Point point) {
		return null;
	}


	public final int getStiffness() {
		return stiffness;
	}

	public final void setStiffness (final int stiffness) {
		this.stiffness = stiffness;
	}


	public final double getLinkLength() {
		return linkLength;
	}

	public final void setLinkLength (final double linkLength) {
		this.linkLength = linkLength;
	}
}
