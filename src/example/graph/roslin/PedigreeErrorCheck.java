package example.graph.roslin;

import java.util.Collection;

import org.apache.log4j.Logger;

import model.graph.Edge;
import model.graph.EdgeDirection;
import model.graph.GraphModel;

public class PedigreeErrorCheck {

	final static Logger LOGGER = Logger.getLogger (PedigreeErrorCheck.class);
	
	transient GraphModel graph;
	
	public PedigreeErrorCheck (final GraphModel graph) {
		this.graph = graph;
	}
	
	public void check () {
		//Set<Object> nodes = graph.getNodes ();
			
		final Collection<Edge> edges = graph.getEdges();
		for (Edge edge : edges) {
			Animal parent = null, child = null;
			
			if (edge.getEdgeObject() == EdgeDirection.FROM) {
				parent = (Animal)edge.getNode1();
				child = (Animal)edge.getNode2();
			}
			else if (edge.getEdgeObject() == EdgeDirection.TO) {
				parent = (Animal)edge.getNode2();
				child = (Animal)edge.getNode1();
			}
			if (parent != null && child != null) {
				compareSNP (parent, child);
			}
		}
	}
	
	void compareSNP (final Animal parent, final Animal child) {
		final SNPData parentSNP = parent.getData ();
		final SNPData childSNP = child.getData ();
		if (parentSNP == null || childSNP == null) {
			if (childSNP == null) {
				
				LOGGER.debug ("Error. Child ["+child+"] data missing for all markers.");
			}
			if (parentSNP == null) {
				LOGGER.debug ("Error. Parent ["+parent+"] data missing for all markers.");
			}
		}
		else {
			for (int n = 0; n < parentSNP.getSize(); n++) {
				final String parentMarker = parentSNP.getMarker (n);
				final String childMarker = childSNP.getMarker (n);
				
				if (childMarker == null || parentMarker == null) {
					if (childMarker == null) {
						childSNP.markUnknown (n, true);
						LOGGER.debug ("Error. Child ["+child+"] data missing for marker "+parentSNP.getMarkerName (n));
					}
					if (parentMarker == null) {
						parentSNP.markUnknown (n, true);
						LOGGER.debug ("Error. Parent ["+parent+"] data missing for marker "+parentSNP.getMarkerName (n));
					}
				}
				else {
					if (parentMarker.charAt(0) == '?' || childMarker.charAt(0) == '?') {
						if (childMarker.charAt(0) == '?') {
							childSNP.markUnknown (n, true);
							LOGGER.debug ("Error. Child ["+child+"] data missing for marker "+parentSNP.getMarkerName (n));
						}
						if (parentMarker.charAt(0) == '?') {
							parentSNP.markUnknown (n, true);
							LOGGER.debug ("Error. Parent ["+parent+"] data missing for marker "+parentSNP.getMarkerName (n));
						}
					}
					else if (parentMarker.indexOf (childMarker.charAt(0)) == -1
						&& (childMarker.length() == 1 || parentMarker.indexOf (childMarker.charAt(1)) == -1)) {
						LOGGER.debug ("Error. Parent ["+parent+"]: "+parentMarker+" != "+childMarker+" :["+child+"] Child. MName: "+parentSNP.getMarkerName (n));
						parentSNP.markPossError (n, true);
						childSNP.markPossError (n, true);
					}
				}
			}
		}
	}
}
