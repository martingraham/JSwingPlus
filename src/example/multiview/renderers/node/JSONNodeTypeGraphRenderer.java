package example.multiview.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.codehaus.jackson.JsonNode;

import example.graph.renderers.node.MrManCellRenderer;

import swingPlus.graph.JGraph;
import util.IconCache;
import util.colour.ColorUtilities;

public class JSONNodeTypeGraphRenderer extends MrManCellRenderer {

	
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
	
	static final String IMG_DIR = "example\\multiview\\img";
	static final Icon PERSON_ICON = IconCache.makeIconFromFilename (IMG_DIR, "author2.png");
	static final Icon GRANT_ICON = IconCache.makeIconFromFilename (IMG_DIR, "grant.png");
	static final Icon PUB_ICON = IconCache.makeIconFromFilename (IMG_DIR, "publication.png");
	static Map<String, Icon> iconTypeMap = new HashMap<String, Icon> ();
	static {
		iconTypeMap.put ("people", PERSON_ICON);
		iconTypeMap.put ("grants", GRANT_ICON);
		iconTypeMap.put ("publications", PUB_ICON);
	}
	
	static Map<String, Boolean> showLabelTypeMap = new HashMap<String, Boolean> ();
	static {
		showLabelTypeMap.put ("people", Boolean.TRUE);
		showLabelTypeMap.put ("grants", Boolean.FALSE);
		showLabelTypeMap.put ("publications", Boolean.FALSE);
	}
	
	protected Map<JsonNode, String> nodeTypeMap;
	protected double zoom = 1.0;
	protected boolean selected;
	
	public JSONNodeTypeGraphRenderer (final Map<JsonNode, String> nodeTypeMap) {
		super ();
		this.nodeTypeMap = nodeTypeMap;
		this.setOpaque (false);
		this.setBorder (null);
	}
	
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		this.setEnabled (isSelected);

		selected = isSelected;
		//selected2 = isSelected;
		this.setBorder (null);
		
		if (selected) {
			imagePanel.setBackground(Color.yellow);
			imagePanel.setOpaque (true);
			this.setOpaque(true);
		} else {
			imagePanel.setOpaque (false);
			this.setOpaque(false);
		}
		setImageAndNameLabel (value);
		
		return this;
	}
	
	public void setImageAndNameLabel (final Object value) {
		if (value instanceof JsonNode) {
			final JsonNode jsonNode = (JsonNode)value;
			final String nodeType = nodeTypeMap.get (jsonNode);
			
			final Icon icon = (iconTypeMap.get (nodeType));
			image = (icon instanceof ImageIcon) ? (BufferedImage)(((ImageIcon)icon).getImage()) : null;
			
			this.getInsets (insets);
			
			final Dimension pDim = new Dimension (image.getWidth() + insets.left + insets.right, 
					image.getHeight() + insets.bottom + insets.top + 20);
			
			final Boolean bool = showLabelTypeMap.get (nodeType);
			if (Boolean.TRUE == bool || selected) {
				final String label = jsonNode.get("label").getTextValue();
				nameLabel.setText (label.length() <= 30 ? label : label.substring (0, 30));
			} else {
				nameLabel.setText (" ");
			}
			
			final JsonNode centreNode = jsonNode.get("centre");
			if (centreNode != null) {
				final String center = centreNode.getTextValue();
				nameLabel.setOpaque (true);
				nameLabel.setBackground (selected ? centreColours.get(center) : unselectedCentreColours.get (center));
			} else {
				nameLabel.setOpaque (false);
			}
			
			final JsonNode pubNode = jsonNode.get("publications");
			if (pubNode != null && nodeType == "people") {
				final double pubSqrt = 1 + Math.log10 (pubNode.size() + 1);
				pDim.setSize ((double)pDim.width * pubSqrt / 4.0, 
						((double)(pDim.height - 20) * pubSqrt / 4.0) + 20.0);
				
			}
			
			this.setPreferredSize (pDim);
			//this.setSize (pDim);
		}
	}
	
	
	@Override
	public void paintComponent (final Graphics gContext) {	
		super.paintComponent (gContext);
	}
}
