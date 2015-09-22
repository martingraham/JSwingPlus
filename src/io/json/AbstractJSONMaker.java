package io.json;

import org.codehaus.jackson.JsonNode;

abstract public class AbstractJSONMaker {

	
	protected JsonNode rootNode;
	
	public AbstractJSONMaker (final JsonNode root) {
		setRoot (root);
	}
	
	public final JsonNode getRoot () {
		return rootNode;
	}
	
	public final void setRoot (final JsonNode newRootNode) {
		rootNode = newRootNode;
	}
}
