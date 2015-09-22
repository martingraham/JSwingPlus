package swingPlus.graph;

import java.awt.Component;

public interface GraphEdgeRenderer {
	
	Component getGraphEdgeRendererComponent (JGraph graph, Object value,
		    boolean isSelected, boolean hasFocus,
		    int x1, int y1, int x2, int y2);
}
