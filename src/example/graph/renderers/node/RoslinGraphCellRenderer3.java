package example.graph.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.swing.SwingConstants;

import example.graph.roslin.Animal;
import example.graph.roslin.SNPData;

import swingPlus.graph.AbstractGraphCellRenderer;
import swingPlus.graph.JGraph;
import util.colour.ColorUtilities;

public class RoslinGraphCellRenderer3 extends AbstractGraphCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8077146043053955659L;
	
	private static final Color BACKGROUND = new Color (246, 240, 224);
	//private static final Icon ICON = IconCache.makeIcon ("BarleyIcon");

	protected transient Insets insets = new Insets (0, 0, 0, 0);
	
	Color male = ColorUtilities.mixColours (Color.blue, Color.gray, 0.25f);
	Color female = ColorUtilities.mixColours (Color.red, Color.gray, 0.25f);
	List<Color> greyList;
	Dimension standardSize = new Dimension (10, 10);
	Dimension currentSize = new Dimension (10, 10);
	BitSet tempErrors = new BitSet ();
	BitSet tempUnkErrors = new BitSet ();
	boolean selected;
	
	boolean orientation = true;	// true = horizontal
	
	public RoslinGraphCellRenderer3 (final int horizOrVert) {
		this ();
		orientation = (horizOrVert == SwingConstants.HORIZONTAL);
		setMaximumSize (new Dimension (orientation ? 128 : 32, orientation ? 32 : 128));
	}
	
	public RoslinGraphCellRenderer3 () {
		super ();
		setBorder (null);
		setOpaque (false);
		//setIcon (ICON);
		setBackground (BACKGROUND);
		setPreferredSize (standardSize);
		setMaximumSize (new Dimension (orientation ? 128 : 32, orientation ? 32 : 128));
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
			
			tempErrors.clear ();
			tempUnkErrors.clear ();
			
			final SNPData data = animal.getData();
			
			if (data != null) {
				BitSet errors = data.getPossErrors ();
				
				if (!errors.isEmpty()) {
					final BitSet bitset = data.getMarkerMap().getGloballyActive();
					tempErrors.or (errors);
					tempErrors.and (bitset);
					
					final int count = tempErrors.cardinality ();
					if (count > 0) {
						setForeground (isSelected ? Color.gray : getGreyShade (count, data.getSize()));
					}
				}
				
				errors = data.getUnknownMarkers ();
				if (!errors.isEmpty()) {
					final BitSet bitset = data.getMarkerMap().getGloballyActive();
					tempUnkErrors.or (errors);
					tempUnkErrors.andNot (bitset);
					
					final int count = tempUnkErrors.cardinality ();
					final double scale = 1.0 + Math.log1p (count);
					currentSize.width = (orientation ? standardSize.width : (int)((double)standardSize.width * scale));
					currentSize.height = (orientation ? (int)((double)standardSize.height * scale) : standardSize.height);
					setPreferredSize (currentSize);
				} else {
					setPreferredSize (standardSize);
				}

			} else {
				setPreferredSize (standardSize);
			}
		}
		selected = isSelected;
		
		return this;
	}
	
	
	Color getGreyShade (final int cardinality, final int size) {
		if (greyList == null) {
			lazyCreateGreyList (size);
		}
		
		return greyList.get (cardinality - 1);
	}
	
	
	void lazyCreateGreyList (final int size) {
		greyList = new ArrayList<Color> (size);
		
		for (int n = 0; n < size; n++) {
			final int greyScale = 128 - ((128 / size) * n);
			greyList.add (new Color (greyScale, greyScale, greyScale));
		}
	}
	
	@Override
	public void paintComponent (final Graphics gContext) {
		this.getInsets (insets);
		gContext.setColor (this.getForeground());	
	
		final Animal animal = (Animal)obj;
		final SNPData data = animal.getData();
		//if (data != null) {
		//	BitSet errors = data.getPossErrors ();
		//	if (!errors.isEmpty()) {
		//		gContext.setColor (Color.black);
		//	}
		//}
		
		// oval
		final int width = this.getWidth() - insets.left - insets.right;
		final int height = this.getHeight() - insets.top - insets.bottom;
		int x = insets.left;
		int y = insets.top;
		gContext.fillRect (x, y, width, height);
		
		if (selected) {
			gContext.setColor (animal.isMale() ? Color.blue : Color.pink);
			gContext.drawRect (x, y, width - 1, height - 1);		
		}

		if (data != null) {
			BitSet errors = tempErrors; //data.getPossErrors ();
			if (width > data.getSize() * 2 && !errors.isEmpty()) {
				final int gap = width / data.getSize();
				gContext.setColor (getForeground().darker());
				for (int i = errors.nextSetBit (0); i >= 0; i = errors.nextSetBit (i+1)) {
					gContext.fillRect (i * gap, 0, gap + 1, gap + 1);
				}
			}
			
			errors = tempUnkErrors; 
			if (width > data.getSize() * 2 && !errors.isEmpty()) {
				final int gap = width / data.getSize();
				gContext.setColor (getForeground().darker());
				for (int i = errors.nextSetBit (0); i >= 0; i = errors.nextSetBit (i+1)) {
					gContext.fillRect (i * gap, gap + 1, gap + 1, gap + 1);
				}
			}
		}
		
	}
}
