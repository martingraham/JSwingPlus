package example.graph.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.BitSet;

import javax.swing.SwingConstants;

import example.graph.roslin.Animal;
import example.graph.roslin.SNPData;

import swingPlus.graph.AbstractGraphCellRenderer;
import swingPlus.graph.JGraph;
import util.colour.ColorUtilities;

public class RoslinGraphCellRenderer4 extends AbstractGraphCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8077146043053955659L;
	
	private static final Color BACKGROUND = new Color (246, 240, 224);
	//private static final Icon ICON = IconCache.makeIcon ("BarleyIcon");

	protected transient Insets insets = new Insets (0, 0, 0, 0);
	
	Color male = ColorUtilities.mixColours (Color.blue, Color.gray, 0.25f);
	Color female = ColorUtilities.mixColours (Color.red, Color.gray, 0.25f);
	transient BitSet tempErrors = new BitSet ();
	transient BitSet tempUnkErrors = new BitSet ();
	boolean selected;
	boolean orientation;	// true = horizontal
	
	public RoslinGraphCellRenderer4 (final int horizOrVert) {
		super ();
		setBorder (null);
		setOpaque (false);
		//setIcon (ICON);
		setBackground (BACKGROUND);

		orientation = (horizOrVert == SwingConstants.HORIZONTAL);
		setMaximumSize (new Dimension (orientation ? 128 : 32, orientation ? 32 : 128));
		setPreferredSize (new Dimension (orientation ? 12 : 48, orientation ? 48 : 12));
	}
	
	public RoslinGraphCellRenderer4 () {
		this (SwingConstants.HORIZONTAL);
	}
	
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		super.getGraphCellRendererComponent (graph, value, isSelected, hasFocus);
		this.setEnabled (isSelected);
		
		if (value instanceof Animal) {
			final Animal animal = (Animal)value;
			this.setForeground (animal.isMale() 
					? (isSelected ? Color.cyan : Color.blue)
					: (isSelected ? Color.pink.brighter() : Color.pink)
			);
			
			tempErrors.clear ();
			tempUnkErrors.clear ();
			
			final SNPData data = animal.getData();
			if (data != null) {
				BitSet errors = data.getPossErrors ();
				if (!errors.isEmpty()) {
					final BitSet active = data.getMarkerMap().getGloballyActive();
					tempErrors.or (errors);
					tempErrors.and (active);
				}
				
				errors = data.getUnknownMarkers ();
				if (!errors.isEmpty()) {
					final BitSet active = data.getMarkerMap().getGloballyActive();
					tempUnkErrors.or (errors);
					tempUnkErrors.andNot (active);
				}
			} 
		}
		selected = isSelected;
		
		return this;
	}
	

	
	@Override
	public void paintComponent (final Graphics gContext) {
		this.getInsets (insets);
		gContext.setColor (this.getForeground());	
	
		final Animal animal = (Animal)obj;
		final SNPData data = animal.getData();
		
		// oval
		final int width = this.getWidth() - insets.left - insets.right;
		final int height = this.getHeight() - insets.top - insets.bottom;
		final int x = insets.left;
		final int y = insets.top;
		gContext.fillRect (x, y, width / 2, height);
		if (height > 28 && width > 60) {
			gContext.setColor (Color.gray);
			gContext.drawString (animal.getName(), x + 4, y + (height / 2) + 5);
		}
		
		if (selected) {
			gContext.setColor (animal.isMale() ? Color.blue : Color.pink);
			gContext.drawRect (x, y, (width / 2) - 1, height - 1);		
		}

		if (data != null) {
			final int maxSize = data.getMarkerMap().getGloballyActive().cardinality();
			if (maxSize > 0) {
				BitSet errors = tempErrors; //data.getPossErrors ();
				if (!errors.isEmpty()) {
					gContext.setColor (Color.gray);
					gContext.drawRect (x + (width / 2), y, (width / 2) - 1, (height / 2) - 1);
					gContext.setColor (Color.black);
					final int errorNo = errors.cardinality();
					gContext.fillRect (x + (width / 2) + 1, y + 1, ((width / 2) - 1) * errorNo / maxSize, (height / 2) - 2);
					if (height > 28 && width > 60) {
						gContext.setColor (Color.gray);
						gContext.drawString ("Errors", x + (width / 2) + 1, y + 10);
					}
				}
				
				errors = tempUnkErrors; //data.getPossErrors ();
				if (!errors.isEmpty()) {
					gContext.setColor (Color.lightGray);
					gContext.drawRect (x + (width / 2), y + (height / 2), (width / 2) - 1, (height / 2) - 1);
					gContext.setColor (Color.gray);
					final int errorNo = errors.cardinality();
					gContext.fillRect (x + (width / 2) + 1, y + 1 + (height / 2), ((width / 2) - 1) * errorNo / maxSize, (height / 2) - 2);
					if (height > 28 && width > 60) {
						gContext.setColor (Color.lightGray);
						gContext.drawString ("Unknowns", x + (width / 2) + 1, y + (height / 2) + 10);
					}
				}
			}
		}
		
	}
}
