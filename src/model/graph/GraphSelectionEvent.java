package model.graph;

import java.util.Collection;
import java.util.EventObject;

public class GraphSelectionEvent extends EventObject  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5154537099881877090L;

	public static final int SELECTED = 1, UNSELECTED = 2, CHANGED = 3, CLEAR = 4, OTHER = 5;
    
	
	private Collection<Object> nodes;
    private Object singleNode;
    private int eventType;
    private boolean isAdjusting;
    

    /** 
     * Represents a change in selection status between {@code firstIndex} and
     * {@code lastIndex}, inclusive. {@code firstIndex} is less than or equal to
     * {@code lastIndex}. The selection of at least one index within the range will
     * have changed. 
     * 
     * @param firstIndex the first index in the range, &lt;= lastIndex
     * @param lastIndex the last index in the range, &gt;= firstIndex
     * @param isAdjusting whether or not this is one in a series of
     *        multiple events, where changes are still being made
     */
    public GraphSelectionEvent (final Object source, final Collection<Object> selectedNodes,
    		final int eventType, final boolean isAdjusting)
    {
		super(source);
		nodes = selectedNodes;
		singleNode = null;
		this.eventType = eventType;
		this.isAdjusting = isAdjusting;
    }
    
    public GraphSelectionEvent (final Object source, final Object selectedEdge,
    		final int eventType, final boolean isAdjusting)
	{
		super(source);
		nodes = null;
		singleNode = selectedEdge;
		this.isAdjusting = isAdjusting;
	}

    /**
     * Returns the index of the first row whose selection may have changed.
     * {@code getFirstIndex() &lt;= getLastIndex()}
     *
     * @return the first row whose selection value may have changed,
     *         where zero is the first row
     */
    public Collection<Object> getSelectedNodes() { return nodes; }

    /**
     * Returns the index of the last row whose selection may have changed.
     * {@code getLastIndex() &gt;= getFirstIndex()}
     *
     * @return the last row whose selection value may have changed,
     *         where zero is the first row
     */
    public Object getSelectedNode() { return singleNode; }

    public int getEventType () { return eventType; }
    
    /**
     * Returns whether or not this is one in a series of multiple events,
     * where changes are still being made. See the documentation for
     * {@link javax.swing.ListSelectionModel#setValueIsAdjusting} for
     * more details on how this is used.
     *
     * @return {@code true} if this is one in a series of multiple events,
     *         where changes are still being made
     */
    public boolean isValueAdjusting() { return isAdjusting; }

    /**
     * Returns a {@code String} that displays and identifies this
     * object's properties.
     *
     * @return a String representation of this object
     */
    @Override
	public String toString() {
    	final String properties = 
	    " source=" + getSource() +  
            " nodes= " + nodes + 
            " singleNode= " + singleNode + 
	    " isAdjusting= " + isAdjusting +
            " ";
        return getClass().getName() + "[" + properties + "]";
    }
}
