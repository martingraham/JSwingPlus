package swingPlus.graph;

import java.awt.Component;


public interface GraphCellRenderer {
	
	Component getGraphCellRendererComponent (JGraph graph, Object value,
						    boolean isSelected, boolean hasFocus);
}
