package example.multiview.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.text.View;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ValueNode;

import swingPlus.graph.AbstractGraphCellRenderer;
import swingPlus.graph.JGraph;
import util.colour.ColorUtilities;

public class JSONTooltipGraphCellRenderer extends AbstractGraphCellRenderer {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6417446901746199601L;
	private static final Logger LOGGER = Logger.getLogger (JSONTooltipGraphCellRenderer.class);

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
	
	Font font = Font.decode ("Gill-Sans-MT-Plain-24");
	boolean selected;
	
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		super.getGraphCellRendererComponent (graph, value, isSelected, hasFocus);
		//this.setEnabled (isSelected);

		this.setForeground (isSelected ? Color.black : Color.gray);
		this.setBackground (Color.white);
		this.setText (value.toString());
		
		selected = isSelected;
		
		if (value instanceof JsonNode) {
			final JsonNode jsonNode = (JsonNode)value;

			final StringBuilder sb = new StringBuilder ("<HTML>");
			final Iterator<Entry<String, JsonNode>> rowContentIter = jsonNode.getFields();		
			while (rowContentIter.hasNext()) {
				final Entry<String, JsonNode> entry = rowContentIter.next();
				final JsonNode cell = entry.getValue();
				if (cell instanceof ValueNode) {
					sb.append(entry.getKey()+":\t"+cell.getTextValue()).append("<br>");
				}
			}
			sb.append ("</HTML>");
			this.setText (sb.toString());
			final JsonNode centreNode = jsonNode.get ("centre");
			if (centreNode != null) {
				final String center = centreNode.getTextValue();
				this.setBackground (selected ? centreColours.get(center) : unselectedCentreColours.get (center));
			}
		}
		
	    final View htmlView = (View) this.getClientProperty ("html");
	    if (LOGGER.isDebugEnabled()) {
	    	LOGGER.debug ("html width: "+htmlView.getPreferredSpan (View.X_AXIS));
	    	LOGGER.debug ("html height:"+ htmlView.getPreferredSpan (View.Y_AXIS));
	    }
		
		this.setPreferredSize (new Dimension ((int)htmlView.getPreferredSpan(View.X_AXIS), 
				(int)htmlView.getPreferredSpan(View.Y_AXIS)));
		return this;
	}
	
	
	@Override
	public void paintComponent (final Graphics gContext) {		
		gContext.setFont (font);
		
		super.paintComponent (gContext);
	}

}
