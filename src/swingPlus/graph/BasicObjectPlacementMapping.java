package swingPlus.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import model.graph.GraphModel;

public class BasicObjectPlacementMapping implements ObjectPlacementMapping {

	protected Map<Object, ObjectPlacement> visualReps;
	protected Map<Object, ObjectPlacement> oldVisualReps; // stuff we might want to restore
	
	private final static Logger LOGGER = Logger.getLogger (BasicObjectPlacementMapping.class);
	
	public BasicObjectPlacementMapping (final GraphModel model) {
		visualReps = new HashMap<Object, ObjectPlacement> ();
		oldVisualReps = new HashMap<Object, ObjectPlacement> ();
		updateMapping (model);
	}
	
	@Override
	public void updateMapping (final GraphModel model) {
		
    	final Set<Object> nodes = model.getNodes();
		
    	if (!visualReps.isEmpty()) {
    		getAllEntries();
    		final Iterator<Object> iterExisting = visualReps.keySet().iterator();
    		while (iterExisting.hasNext()) {
    			final Object obj = iterExisting.next();
    			if (! nodes.contains (obj)) {
    				final ObjectPlacement placement = visualReps.get(obj);
    				iterExisting.remove();
    				oldVisualReps.put (obj, placement); // store incase we need again
    				//removeMapping (obj);
    			}
    		}
    	}

    	final Iterator<Object> iter = nodes.iterator();
    	while (iter.hasNext()) {
    		final Object obj = iter.next ();
    		if (getPlacement (obj) == null) {
    			final ObjectPlacement placement = oldVisualReps.get (obj);
    			if (placement == null) {
    				addMapping (obj);
    			} else {
    				addMapping (obj, placement);
    			}
    		}
    	}
	}

	@Override
	public void clearMapping () {
		visualReps.clear ();
		oldVisualReps.clear ();
	}
	
	
	@Override
	public void addMapping (final Object obj) {
		addMapping (obj, new BasicObjectPlacement (Math.random() * 1000.00, Math.random() * 1000.00));
	}
	
	@Override
	public void addMapping (final Object obj, final ObjectPlacement placement) {
		visualReps.put (obj, placement);
	}

	@Override
	public ObjectPlacement getPlacement (final Object obj) {
		return visualReps.get (obj);
	}

	@Override
	public void removeMapping (final Object obj) {
		visualReps.remove (obj);
	}

	@Override
	public Collection<ObjectPlacement> getAllPlacements() {
		return visualReps.values();
	}

	@Override
	public Set<Entry<Object, ObjectPlacement>> getAllEntries() {
		return visualReps.entrySet();
	}
}
