package model.shared.selection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import model.graph.GraphSelectionEvent;
import model.graph.GraphSelectionListener;
import model.graph.GraphSelectionModel;
import swingPlus.graph.JGraph;

/**
 * Class that links together different <code>GraphSelectionModel</code> objects
 * 
 * Always easier to share the same GraphSelectionModel across <code>JGraph</code>
 * instances if they use the same underlying <code>Graph</code> model, but could be
 * useful when different Graph models sharing some overlapping objects as nodes are in
 * circulation.
 * @author cs22
 *
 */
public class LinkedGraphSelectionModel implements GraphSelectionListener, PropertyChangeListener {

	Set<JGraph> graphs;
	Map<GraphSelectionModel, JGraph> modelGraphMap;
	private boolean isAdjusting;
	
	public LinkedGraphSelectionModel () {
		this (null);
	}
	
	public LinkedGraphSelectionModel (final JGraph graph) {
		graphs = new HashSet<JGraph> ();
		modelGraphMap = new HashMap<GraphSelectionModel, JGraph> ();
		setAdjusting (false);
		addJGraph (graph);
	}
	
	public void addJGraph (final JGraph graph) {
		if (graph != null) {
			if (graphs.add (graph)) {
				graph.addPropertyChangeListener ("selectionModel", this);
				addGraphSelectionListener (graph, graph.getSelectionModel());
				generateGraphSelectionModelToGraphMap ();
			}
		}
	}
	
	public void removeJGraph (final JGraph graph) {
		if (graph != null) {	
			if (graphs.remove (graph)) {
				graph.removePropertyChangeListener ("selectionModel", this);	
				removeGraphSelectionListener (graph, graph.getSelectionModel());
				generateGraphSelectionModelToGraphMap ();
			}
		}
	}
	
	
	@Override
	public void valueChanged (final GraphSelectionEvent gsEvent) {
		if (!isAdjusting()) {	// stops recursive calls of the following routine 
			// as this object inputStream a listener on all the GraphSelectionModels
			setAdjusting (true);
			if (!gsEvent.isValueAdjusting() && gsEvent.getSource() instanceof GraphSelectionModel) {		
				passSelectionStateToOtherGraphModels (gsEvent);
				furtherCheckedEventHandling (gsEvent);
			}
			setAdjusting (false);
		}
	}
	
	/**
	 *  When extending the class whack any other GraphSelectionEvent processing in here
	 * @param e	GraphSelectionEvent
	 */
	public void furtherCheckedEventHandling (final GraphSelectionEvent gsEvent) {
		// EMPTY
	}
	
	
	void passSelectionStateToOtherGraphModels (final GraphSelectionEvent gsEvent) {	
		final GraphSelectionModel originatingSelectionModel = (GraphSelectionModel) gsEvent.getSource ();
		
		for (GraphSelectionModel otherGsm : modelGraphMap.keySet()) {
			
			if (otherGsm != originatingSelectionModel) {
				if (gsEvent.getEventType() == GraphSelectionEvent.SELECTED) {
					otherGsm.setSelected (
							gsEvent.getSelectedNode() != null ? gsEvent.getSelectedNode() : gsEvent.getSelectedNodes(),
							true
					);
				}
				else if (gsEvent.getEventType() == GraphSelectionEvent.UNSELECTED) {
					otherGsm.setSelected (
							gsEvent.getSelectedNode() != null ? gsEvent.getSelectedNode() : gsEvent.getSelectedNodes(),
							false
					);
				}
				else if (gsEvent.getEventType() == GraphSelectionEvent.CHANGED) {
					Object selectedNode = gsEvent.getSelectedNode();
					
					if (selectedNode != null) {
						otherGsm.setSelected (selectedNode, originatingSelectionModel.isSelected (selectedNode));
					}
					else {
						final Collection<Object> selectedNodes = gsEvent.getSelectedNodes();
						if (selectedNodes != null && !selectedNodes.isEmpty()) {
							final boolean otherAdjusting = otherGsm.isValueAdjusting();
							otherGsm.setValueIsAdjusting (true);
							
							final Iterator<Object> selectedIterator = selectedNodes.iterator();
							while (selectedIterator.hasNext ()) {
								selectedNode = selectedIterator.next();
								otherGsm.setSelected (selectedNode, originatingSelectionModel.isSelected (selectedNode));
							}
							
							otherGsm.setValueIsAdjusting (otherAdjusting);
						}
					}	
				}
			}
		}
	}
	
	
	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if (evt.getSource() instanceof JGraph) {
			final JGraph graph = (JGraph)evt.getSource ();
			removeGraphSelectionListener (graph, (GraphSelectionModel)evt.getOldValue());		
			addGraphSelectionListener (graph, (GraphSelectionModel)evt.getNewValue());
			generateGraphSelectionModelToGraphMap ();
		}
	}
	
	
	Map<GraphSelectionModel, JGraph> generateGraphSelectionModelToGraphMap () {
		modelGraphMap.clear ();
		for (JGraph graph : graphs) {
			final GraphSelectionModel model = graph.getSelectionModel();
			if (model != null) {
				modelGraphMap.put (model, graph);
			}
		}
		return modelGraphMap;
	}
	
	public Map<GraphSelectionModel, JGraph> getSelectionModelToGraphMap () {
		return modelGraphMap;
	}
	
	
	void addGraphSelectionListener (final JGraph graph, final GraphSelectionModel gsm) {
		if (gsm != null && !isModelUsedByOtherGraphs (graph, gsm)) {
			gsm.addGraphSelectionListener (this);
		}
	}
	
	
	void removeGraphSelectionListener (final JGraph graph, final GraphSelectionModel gsm) {
		if (gsm != null && !isModelUsedByOtherGraphs (graph, gsm)) {
			gsm.removeGraphSelectionListener (this);
		}
	}
	
	
	boolean isModelUsedByOtherGraphs (final JGraph graph, final GraphSelectionModel gsm) {
		final Iterator<JGraph> graphIterator = graphs.iterator();
		while (graphIterator.hasNext()) {
			final JGraph otherGraph = graphIterator.next ();
			if (otherGraph != graph) {
				final GraphSelectionModel otherSelectionModel = otherGraph.getSelectionModel();
				if (gsm == otherSelectionModel) {
					return true;
				}
			}
		}
		return false;
	}

	public final boolean isAdjusting() {
		return isAdjusting;
	}

	public final void setAdjusting (final boolean isAdjusting) {
		this.isAdjusting = isAdjusting;
	}
}
