package model.graph;

import java.util.EventListener;

public interface GraphModelListener extends EventListener {

    /**
     * This fine grain notification tells listeners the exact range
     * of nodes that changed.
     */
    public void graphChanged (GraphModelEvent gmEvent);
}
