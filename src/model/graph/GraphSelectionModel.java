package model.graph;

import java.util.Collection;


public interface GraphSelectionModel {

	public boolean isSelected (Object obj);
	
	public void setSelected (Object obj, boolean selected);
	
	public void setSelected (Collection<Object> objs, boolean selected);
	
	public Collection<Object> getAllSelected ();
	
	public void clearSelection ();
	
	
	public void setValueIsAdjusting (boolean b);
	
	public boolean isValueAdjusting ();
	
	
	public GraphSelectionListener[] getGraphSelectionListeners();
	
    /**
     * Add a listener to the graph that's notified each time a change
     * to the selection occurs.
     * 
     * @param gsl the GraphSelectionListener
     */  
    void addGraphSelectionListener (GraphSelectionListener gsl);

    /**
     * Remove a listener from the graph that's notified each time a 
     * change to the selection occurs.
     * 
     * @param gsl the GraphSelectionListener
     */  
    void removeGraphSelectionListener (GraphSelectionListener gsl);
}
