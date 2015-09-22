package model.graph;

public enum EdgeDirection {
	
	UNDIRECTED {@Override public EdgeDirection getInverse () { return UNDIRECTED; }}, 
	FROM {@Override public EdgeDirection getInverse () { return TO; }}, 
	TO {@Override public EdgeDirection getInverse () { return FROM; }};
	
	abstract public EdgeDirection getInverse ();
}
