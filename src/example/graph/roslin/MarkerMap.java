package example.graph.roslin;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkerMap {

	Map<String, Integer> markerNameMap;
	List<String> invMarkerNameMap;
	BitSet globallyActive;
	
	public MarkerMap () {
		markerNameMap = new HashMap <String, Integer> ();
		invMarkerNameMap = new ArrayList<String> ();
		globallyActive = new BitSet ();
	}
	
	public int addMarker (final String markerName) {
		Integer indexObj = markerNameMap.get (markerName);
		if (indexObj == null) {
			final int index = invMarkerNameMap.size();
			invMarkerNameMap.add (markerName);
			indexObj = Integer.valueOf (index);
			markerNameMap.put (markerName, indexObj);
			globallyActive.set (index);
			//System.err.println ("index: "+index+", b: "+globallyActive);
		}
		return indexObj.intValue();
	}
	
	public int getMarkerIndex (final String markerName, final boolean addIfNeeded) {
		Integer indexObj = markerNameMap.get (markerName);
		if (addIfNeeded && indexObj == null) {
			final int index = invMarkerNameMap.size();
			invMarkerNameMap.add (markerName);
			indexObj = Integer.valueOf (index);
			markerNameMap.put (markerName, indexObj);
			globallyActive.set (index);
		}
		return (indexObj == null ? -1 : indexObj.intValue());
	}
	
	public String getMarkerName (final int markerIndex) {
		if (markerIndex < 0 || markerIndex >= invMarkerNameMap.size()) {
			return null;
		}
		return invMarkerNameMap.get (markerIndex);
	}
	
	public final BitSet getGloballyActive () {
		return globallyActive;
	}
	
	public final void setGloballyActive (final int index, final boolean state) {
		globallyActive.set (index, state);
	}

	public int getSize () { return invMarkerNameMap.size(); }
}
