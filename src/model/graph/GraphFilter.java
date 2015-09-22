package model.graph;

public interface GraphFilter {

	public boolean includeNode (Object obj);

	public boolean includeEdge (Edge edge);
}