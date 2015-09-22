package swingPlus.graph;

import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

import model.graph.GraphModel;

public interface ObjectPlacementMapping {

	public void updateMapping (GraphModel g);
	
	public void clearMapping ();
	
	public void addMapping (Object obj);
	
	public void addMapping (Object obj, ObjectPlacement placement);
	
	public void removeMapping (Object obj);
	
	public ObjectPlacement getPlacement (Object obj);
	
	public Collection<ObjectPlacement> getAllPlacements ();
	
	public Set<Entry<Object, ObjectPlacement>> getAllEntries ();
}
