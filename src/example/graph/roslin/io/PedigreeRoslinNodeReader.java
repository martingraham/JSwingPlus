package example.graph.roslin.io;

import io.AbstractGraphPopulater;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import model.graph.GraphModel;

import org.apache.log4j.Logger;

import example.graph.roslin.Animal;


public class PedigreeRoslinNodeReader extends AbstractGraphPopulater {
	
	final static Logger LOGGER = Logger.getLogger (PedigreeRoslinNodeReader.class);;
	final Map<String, Object> stringMap = new HashMap<String, Object> ();
	
	public PedigreeRoslinNodeReader (final InputStream iStream) {
		super (iStream);
	}
	

	@Override
	protected void doStuff (final GraphModel graph, final String[] parts) {
		if (parts.length > 2) {
			  final Object obj = new Animal (parts[0], parts[3].equals ("M"));
			  graph.addNode (obj);
			  stringMap.put (parts[0], obj);
		  }
	}
	
	public Map<String, Object> getNodeMap () { return stringMap; }
}
