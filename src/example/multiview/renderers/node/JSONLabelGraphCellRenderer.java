package example.multiview.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;

import example.graph.renderers.node.StringGraphCellRenderer2;

import swingPlus.graph.JGraph;
import util.colour.ColorUtilities;

public class JSONLabelGraphCellRenderer extends StringGraphCellRenderer2 {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6417446901746199601L;

	static Map<String, Color> centreColours = new HashMap<String, Color> ();
	static {
		centreColours.put ("CISS", Color.green);
		centreColours.put ("CSI", Color.blue);
		centreColours.put ("CID", Color.red);
		centreColours.put ("CEC", Color.yellow);
		centreColours.put ("CDCS", Color.cyan);
		centreColours.put ("IIDI", Color.lightGray);
	}
	
	static Map<String, Color> unselectedCentreColours = new HashMap<String, Color> ();
	static {
		final Iterator<Entry<String, Color>> entryIter = centreColours.entrySet().iterator();
		while (entryIter.hasNext ()) {
			final Entry<String, Color> entry = entryIter.next();
			final Color muted = ColorUtilities.mixColours (entry.getValue(), new Color (128, 128, 128), 0.5f);
			unselectedCentreColours.put (entry.getKey(), muted);
		}
	}
	
	
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		super.getGraphCellRendererComponent (graph, value, isSelected, hasFocus);
		//this.setEnabled (isSelected);

		if (value instanceof JsonNode) {
			final JsonNode jsonNode = (JsonNode)value;

			final String label = jsonNode.get("label").getTextValue();
			this.setText (label.length() <= 30 ? label : label.substring (0, 30));
			final JsonNode centreNode = jsonNode.get("centre");
			if (centreNode != null) {
				final String center = centreNode.getTextValue();
				this.setBackground (selected ? centreColours.get(center) : unselectedCentreColours.get (center));
			}
		}
		return this;
	}
	
	
	@Override
	public void paintComponent (final Graphics gContext) {		
		selected = true;
		super.paintComponent (gContext);
	}

}
