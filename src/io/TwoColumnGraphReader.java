package io;


import java.io.InputStream;
import java.util.Arrays;

import org.apache.log4j.Logger;

import model.graph.EdgeDirection;
import model.graph.GraphModel;


public class TwoColumnGraphReader extends AbstractGraphPopulater {

	final static Logger LOGGER = Logger.getLogger (TwoColumnGraphReader.class);
	
	public TwoColumnGraphReader (final InputStream iStream) {
		super (iStream);
	}
	
	
	@Override
	protected void doStuff (final GraphModel graph, final String[] parts) {
		
		if (parts[0].charAt(0) != '#') {
			for (int n = 0; n < parts.length - 1; n++) {
				graph.addEdge (parts[n], parts[n + 1], EdgeDirection.FROM);				  
			}
			LOGGER.debug ("parts: "+Arrays.toString (parts));
		}
	}
}
