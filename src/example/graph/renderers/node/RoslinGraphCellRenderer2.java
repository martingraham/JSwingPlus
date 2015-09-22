package example.graph.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.BitSet;

import example.graph.roslin.Animal;
import example.graph.roslin.SNPData;

import swingPlus.graph.AbstractGraphCellRenderer;
import swingPlus.graph.JGraph;
import util.colour.ColorUtilities;

public class RoslinGraphCellRenderer2 extends AbstractGraphCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8077146043053955659L;
	
	private static final Color BACKGROUND = new Color (246, 240, 224);
	//private static final Icon ICON = IconCache.makeIcon ("BarleyIcon");

	protected transient Insets insets = new Insets (0, 0, 0, 0);
	
	Color male = ColorUtilities.mixColours (Color.blue, Color.gray, 0.25f);
	Color female = ColorUtilities.mixColours (Color.red, Color.gray, 0.25f);
	Dimension standardSize = new Dimension (20, 10);
	Dimension currentSize = new Dimension (20, 10);
	BitSet tempErrors = new BitSet ();
	
	public RoslinGraphCellRenderer2 () {
		super ();
		setBorder (null);
		setOpaque (false);
		//setIcon (ICON);
		setBackground (BACKGROUND);
		setPreferredSize (standardSize);
	}
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		super.getGraphCellRendererComponent (graph, value, isSelected, hasFocus);
		this.setEnabled (isSelected);
		
		//this.setForeground (isSelected ? Color.black : Color.gray);
		if (value instanceof Animal) {
			final Animal animal = (Animal)value;
			this.setForeground (animal.isMale() 
					? (isSelected ? Color.cyan : Color.blue)
					: (isSelected ? Color.pink.brighter() : Color.pink)
			);
			
			final SNPData data = animal.getData();
			
			if (data != null) {
				final BitSet errors = data.getPossErrors ();
				
				if (!errors.isEmpty()) {
					final BitSet bitset = data.getMarkerMap().getGloballyActive();
					tempErrors.clear ();
					tempErrors.or (errors);
					tempErrors.and (bitset);
					
					final int count = tempErrors.cardinality ();
					if (count > 0) {
						setForeground (Color.black);
					}
					final double scale = 1.0 + Math.log1p (count);
					currentSize.width = (int)((double)standardSize.width * scale);
					currentSize.height = (int)((double)standardSize.height * scale);
					setPreferredSize (currentSize);
				} else {
					setPreferredSize (standardSize);
				}
			} else {
				setPreferredSize (standardSize);
			}
		}
		return this;
	}
	
	@Override
	public void paintComponent (final Graphics gContext) {
		this.getInsets (insets);
		gContext.setColor (this.getForeground());	
		// piggywinkle
		final Animal animal = (Animal)obj;
		final SNPData data = animal.getData();
		//if (data != null) {
		//	BitSet errors = data.getPossErrors ();
		//	if (!errors.isEmpty()) {
		//		gContext.setColor (Color.black);
		//	}
		//}
		
		// body
		final int width = this.getWidth() - insets.left - insets.right;
		final int height = this.getHeight() - insets.top - insets.bottom;
		int x = insets.left + (int)(width * 0.25);
		int y = insets.top + (int)(height * 0.2);
		gContext.fillOval (x, y, (int)(width * 0.7), (int)(height * 0.6));
		
		// head
		x = insets.left + (int)(width * 0.8);
		y = insets.top;
		gContext.fillOval (x, y, (int)(width * 0.2), (int)(width * 0.2));

		// earz
		final int eyeR = (int)(width * 0.05);
		if (eyeR > 0) {
			final int[] xx = {insets.left + (int)(width * 0.8), insets.left + (int)(width * 0.8), insets.left + (int)(width * 0.85)};
			final int[] yy = {insets.top, insets.top + (int)(height * 0.1), insets.top + (int)(height * 0.1)};
			gContext.fillPolygon (xx, yy, 3);
			
			final int[] xx2 = {insets.left + (int)(width), insets.left + (int)(width), insets.left + (int)(width * 0.95)};
			gContext.fillPolygon (xx2, yy, 3);	
		}
		
		// leggies
		for (int n = 0; n < 4; n++) {
			x = insets.left + (int)(width * 0.35) + (int)(width * 0.15 * n);
			y = insets.top + (int)(height * 0.5);
			gContext.fillRect (x, y, 3, (int)(height * 0.5));
		}
		
		// curly tail
		x = insets.left + (int)(width * 0.125);
		y = insets.top ;//+ (int)(height * 0.5);
		gContext.drawArc (x, y, (int)(width * 0.25), (int)(width * 0.25), 135, 135);
		
		// eyes
		if (eyeR > 0) {
			gContext.setColor (Color.black);
			x = insets.left + (int)(width * 0.85);
			y = insets.top + (int)(height * 0.15);
			gContext.fillOval (x, y, eyeR, eyeR);
			
			x = insets.left + (int)(width * 0.95);
			gContext.fillOval (x, y, eyeR, eyeR);
			
			if (eyeR > 3) {
				//this.setForeground (Color.black);
				//this.setBackground (Color.black);
				//super.paintComponent (gContext);
			}
		}
		

		if (data != null) {
			final BitSet errors = tempErrors; //data.getPossErrors ();
			if (width > data.getSize() * 2 && !errors.isEmpty()) {
				final int gap = width / data.getSize();
				gContext.setColor (getForeground().darker());
				for (int i = errors.nextSetBit (0); i >= 0; i = errors.nextSetBit (i+1)) {
					gContext.fillRect (i * gap, 0, gap + 1, gap + 1);
				}
			}
		}
		
	}
}
