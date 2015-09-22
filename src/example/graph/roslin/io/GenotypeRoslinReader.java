package example.graph.roslin.io;

import io.AbstractGraphPopulater;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import model.graph.GraphModel;

import org.apache.log4j.Logger;

import example.graph.roslin.Animal;
import example.graph.roslin.MarkerMap;
import example.graph.roslin.SNPData;


public class GenotypeRoslinReader extends AbstractGraphPopulater {
	
	final static Logger LOGGER = Logger.getLogger (GenotypeRoslinReader.class);
	
	Collection<Object> nodes;
	Map<String, Object> nodeMap;
	MarkerMap mmap;
	
	public GenotypeRoslinReader (final InputStream iStream) {
		super (iStream);
		nodeMap = new HashMap<String, Object> ();
	}
	
	public void populate (final GraphModel graph, final MarkerMap mmap) {
		
		nodes = graph.getNodes ();
		nodeMap.clear ();
		for (Object node : nodes) {
			if (node instanceof Animal) {
				final Animal anode = (Animal)node;
				nodeMap.put (anode.getName(), anode);
			}
		}
		
		this.mmap = mmap;
		
		super.populate (graph, Pattern.compile ("\t"));
	}

	@Override
	protected void doStuff (final GraphModel graph, final String[] parts) {
		final String pedID = parts [0];
		final String SNPID = parts [1];
		final String baseA = parts [2];
		final String baseB = parts.length > 3 ? parts [3] : null;
		  
		final Animal aNode = (Animal)nodeMap.get (pedID);
		if (aNode.getData() == null) {
			final SNPData data = new SNPData (mmap);
			aNode.setData (data);
		}
		  
		final String ddata = baseA + baseB;
		aNode.getData().addMarker (SNPID, ddata);
	}

}
