package example.graph.rdf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import model.graph.AbstractGraphModel;
import model.graph.Edge;
import model.graph.GraphModel;
import model.graph.impl.EdgeInstance;

public class JenaRDFGraphWrapper extends AbstractGraphModel {

	
	Model graph; 
	int edgeCount = 0, nodeCount = 0;
	
	public JenaRDFGraphWrapper (final Model data) {
		super ();
		graph = data;
	}
	
	@Override
	public boolean addGraph (final GraphModel otherModel) {
		return false;
	}
	
	@Override
	public boolean addEdge (final Object node1, final Object node2, final Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addEdge (final Edge edge) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addEdges (final Collection<Edge> edges) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addNode (final Object node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<Edge> getEdges() {
		final StmtIterator sIter = graph.listStatements ();
		final Set<Edge> edges = new HashSet<Edge> ();
		while (sIter.hasNext()) {
			final Statement state = sIter.next();
			edges.add (new EdgeInstance (state.getSubject(), state.getPredicate(), state.getObject()));
		}
		return edges;
	}

	@Override
	public Set<Edge> getEdges (final Object node) {
		StmtIterator sIter = graph.listStatements ((Resource)node, (Property)null, (RDFNode)null);
		final Set<Edge> edges = new HashSet<Edge> ();
		while (sIter.hasNext()) {
			final Statement state = sIter.next();
			edges.add (new EdgeInstance (state.getSubject(), state.getPredicate(), state.getObject()));
		}
		
		sIter = graph.listStatements ((Resource)null, (Property)null, (RDFNode)node);
		while (sIter.hasNext()) {
			final Statement state = sIter.next();
			edges.add (new EdgeInstance (state.getSubject(), state.getPredicate(), state.getObject()));
		}
		return edges;
	}
	
	@Override
	public Set<Edge> getCoincidentEdges (final Object node) {
		return getEdges (node);
	}

	@Override
	public Set<Edge> getEdges (final Object node1, final Object node2) {

		StmtIterator sIter = graph.listStatements ((Resource)node1, (Property)null, (RDFNode)node2);
		final Set<Edge> edges = new HashSet<Edge> ();
		while (sIter.hasNext()) {
			final Statement state = sIter.next();
			edges.add (new EdgeInstance (state.getSubject(), state.getPredicate(), state.getObject()));
		}
		
		sIter = graph.listStatements ((Resource)node2, (Property)null, (RDFNode)node1);
		while (sIter.hasNext()) {
			final Statement state = sIter.next();
			edges.add (new EdgeInstance (state.getSubject(), state.getPredicate(), state.getObject()));
		}
		return edges;
	}

	@Override
	public Set<Object> getNodes() {
		final Set<Edge> edges = getEdges();
		final Set<Object> nodes = new HashSet<Object> ();
		final Iterator<Edge> eIter = edges.iterator();
		while (eIter.hasNext()) {
			final Edge edge = eIter.next();
			nodes.add (edge.getNode1());
			nodes.add (edge.getNode2());
		}
		return nodes;
	}

	@Override
	public Edge removeEdge (final Edge edge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Edge removeEdge (final Object node1, final Object node2, final Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Edge> removeEdges (final Collection<Edge> edges) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Edge> removeEdges (final Object node1, final Object node2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeNode (final Object node) {
		//graph.get
		return false;
	}

	@Override
	public int getEdgeCount() {
		return 0;
	}

	@Override
	public int getNodeCount() {
		return 0;
	}

	
	public boolean containsNode (final Object node) {
		final StmtIterator sIter = graph.listStatements ();
		boolean foundNode = false;
		while (sIter.hasNext() && !foundNode) {
			final Statement state = sIter.next();
			foundNode = (node.equals (state.getSubject()) || node.equals (state.getObject()));
		}
		return foundNode;
	}
	
	public boolean containsEdge (final Edge edge) {
		final Collection<Edge> edges = getEdges (edge.getNode1());
		return edges.contains (edge);
	}
	
	public void clear () {
		graph.removeAll();
		edgeCount = 0;
		nodeCount = 0;
	}
}
