package example.graph.roslin.io;

import io.AbstractGraphPopulater;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import model.graph.EdgeDirection;
import model.graph.GraphModel;

import org.apache.log4j.Logger;


public class PedigreeRoslinEdgeReader extends AbstractGraphPopulater {
	
	final static Logger LOGGER = Logger.getLogger (PedigreeRoslinEdgeReader.class);
	final Map<String, Object> stringMap = new HashMap<String, Object> ();
	
	public PedigreeRoslinEdgeReader (final InputStream iStream) {
		super (iStream);
	}
	

	@Override
	protected void doStuff (final GraphModel graph, final String[] parts) {
		if (parts.length > 2) {
			  final Object obj = stringMap.get (parts[0]);
			  
			  if (!parts[1].equals ("0")) {
				  graph.addEdge (stringMap.get (parts[1]), obj, EdgeDirection.FROM);	
			  }
			 	
			  if (!parts[2].equals ("0")) {
				  graph.addEdge (stringMap.get (parts[2]), obj, EdgeDirection.FROM);	
			  }
		  }
	}
	
	public void setNodeMap (final Map<String, Object> nodeMap) {
		stringMap.putAll (nodeMap);
	}
}
