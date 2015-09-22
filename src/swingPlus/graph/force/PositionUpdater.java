package swingPlus.graph.force;

import swingPlus.graph.JGraph;

public interface PositionUpdater {

	public void updatePositions (JGraph graph);

	public boolean haltLayout (JGraph graph);
}
