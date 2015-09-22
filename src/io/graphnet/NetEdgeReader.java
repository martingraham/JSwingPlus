package io.graphnet;

import io.AbstractGraphPopulater;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import model.graph.Edge;
import model.graph.EdgeDirection;
import model.graph.GraphModel;
import model.graph.impl.EdgeInstance;


public class NetEdgeReader extends AbstractGraphPopulater {

	final static Logger LOGGER = Logger.getLogger (NetEdgeReader.class);
	List<String> indexedNameList;
	
	public NetEdgeReader (final InputStream iStream) {
		super (iStream);
	}
		
	@Override
	protected void doStuff (final GraphModel graph, final String[] parts) {
		if (Character.isDigit (parts[0].charAt(0))) {
			final int[] indices = new int [parts.length];
			for (int count = 0; count < indices.length; count++) {
				indices [count] = Integer.parseInt (parts [count]);
			}
			  
			final String fromNode = indexedNameList.get (indices [0]);
			for (int count = 1; count < parts.length; count++) {
				final String toNode = indexedNameList.get (indices [count]);
				final Edge edge = new EdgeInstance (fromNode, toNode, EdgeDirection.FROM);
				final Edge edge2 = new EdgeInstance (toNode, fromNode, EdgeDirection.FROM);
				if (!graph.containsEdge (edge2)) {
					graph.addEdge (edge);
				}			  
			}
			 
			LOGGER.debug ("parts: "+Arrays.toString (parts));
		}
	}
	
	
	void setIndexedNameList (final List<String> nameList) {
		indexedNameList = nameList;
	}
}
