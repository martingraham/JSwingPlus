package example.graph.roslin;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.log4j.Logger;

import util.collections.ArrayListUtil;

public class SNPData {

	MarkerMap markerMap;
	List<String> data;
	BitSet possErrors;
	BitSet unknownMarkers;
	
	final static Logger LOGGER = Logger.getLogger (SNPData.class);
	
	public SNPData (final MarkerMap markerMap) {
		this.markerMap = markerMap;
		
		data = new ArrayList<String> ();
		possErrors = new BitSet ();
		unknownMarkers = new BitSet ();
	}
	
	public boolean addMarker (final String markerName, final String value) {
		final int index = markerMap.getMarkerIndex (markerName, true);
		if (index == -1) {
			return false;
		}
		return addMarker (index, value);
	}
	
	public boolean addMarker (final int markerIndex, final String value) {
		ArrayListUtil.bridgeGapWithNulls (data, markerIndex);
		data.set (markerIndex, value);
		return true;
	}
	
	
	public String getMarker (final String markerName) {
		final int index = markerMap.getMarkerIndex (markerName, false);
		if (index == -1) {
			return null;
		}
		return getMarker (index);
	}
	
	public String getMarker (final int markerIndex) {
		if (markerIndex < 0 || markerIndex >= data.size()) {
			return null;
		}
		return data.get (markerIndex);
	}
	
	public String getMarkerName (final int markerIndex) {
		return markerMap.getMarkerName (markerIndex);
	}
	 
	public MarkerMap getMarkerMap () { return markerMap; }
	
	public void markPossError (final String markerName, final boolean error) {
		final int markerIndex = markerMap.getMarkerIndex (markerName, false);
		if (markerIndex != -1) {
			markPossError (markerIndex, error);
		}
	}
	
	public void markPossError (final int markerIndex, final boolean error) {
		possErrors.set (markerIndex, error);
	}
	
	
	public void markUnknown (final String markerName, final boolean error) {
		final int markerIndex = markerMap.getMarkerIndex (markerName, false);
		if (markerIndex != -1) {
			markPossError (markerIndex, error);
		}
	}
	
	public void markUnknown (final int markerIndex, final boolean error) {
		unknownMarkers.set (markerIndex, error);
	}
	
	public int getSize() { return data.size(); }
	
	public List<String> getAllMarkers () { return data; }
	
	public BitSet getPossErrors () { return possErrors; }
	
	public BitSet getUnknownMarkers () { return unknownMarkers; }
	
	public void restrainMarkerMap (final BitSet newGlobal) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("newGlobal: "+newGlobal);
			LOGGER.debug ("oldGlobal: "+markerMap.getGloballyActive());
			markerMap.getGloballyActive().clear ();
			LOGGER.debug ("oldGlobal: "+markerMap.getGloballyActive());
			markerMap.getGloballyActive().or (newGlobal);
			LOGGER.debug ("setGlobal: "+markerMap.getGloballyActive());
		} else {
			markerMap.getGloballyActive().clear ();
			markerMap.getGloballyActive().or (newGlobal);
		}
	}
}
