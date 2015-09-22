package io.graphnet;

import io.AbstractGraphPopulater;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import util.collections.ArrayListUtil;

import model.graph.GraphModel;


public class NetNodeReader extends AbstractGraphPopulater {

	final static Logger LOGGER = Logger.getLogger (NetNodeReader.class);
	List<String> indexedNameList;
	
	public NetNodeReader (final InputStream iStream) {
		super (iStream);
	}
	
	
	@Override
	protected void doStuff (final GraphModel graph, final String[] parts) {
		if (Character.isDigit (parts[0].charAt(0))) {
			final int nodeId = Integer.parseInt (parts [0]);
			String nodeName = parts [1];
			if (nodeName.charAt(0) == '\"') {
				nodeName = nodeName.substring (1, nodeName.length() - 1);
			}
			graph.addNode (nodeName);
		  
			if (nodeId == indexedNameList.size()) {
				indexedNameList.add (nodeName);
			} else {
				ArrayListUtil.bridgeGapWithNulls (indexedNameList, nodeId);
				indexedNameList.set (nodeId, nodeName);
			}
		 
			LOGGER.debug ("parts: "+Arrays.toString (parts));
		}
	}
	
	protected boolean haltParse (final String[] parts) {
		return parts[0].startsWith ("*Arc");
	}
	
	void setIndexedNameList (final List<String> nameList) {
		indexedNameList = nameList;
	}
}
