package io.graphnet;

import io.AbstractGraphPopulater;
import io.RegexReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import model.graph.GraphModel;

import org.apache.log4j.Logger;



public class NetFileReader extends AbstractGraphPopulater {
	
	final static Logger LOGGER = Logger.getLogger (NetFileReader.class);

	
	public NetFileReader (final InputStream iStream) {
		super (iStream);
	}
	
	public void populate (final GraphModel graph) {
		if (regexReader == null) {
			regexReader = new RegexReader (inputStream);
		}
		List<String> indexedNameList = new ArrayList<String> ();
	
		final NetNodeReader nodeReader = new NetNodeReader (inputStream);
		final NetEdgeReader edgeReader = new NetEdgeReader (inputStream);
		
		nodeReader.setIndexedNameList (indexedNameList);
		edgeReader.setIndexedNameList (indexedNameList);
		
		final AbstractGraphPopulater[] chainPopulaters = {nodeReader, edgeReader};
		for (AbstractGraphPopulater populater : chainPopulaters) {
			populater.setReader (regexReader);
			populater.populate (graph, Pattern.compile ("\\s"));
		}
	}

	@Override
	protected void doStuff (GraphModel graph, String[] parts) {
		// EMPTY
	}
}
