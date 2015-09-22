package swingPlus.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.border.Border;

import util.colour.ColorUtilities;


public class DefaultGraphCellRenderer extends AbstractGraphCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8077146043053955659L;
	
	private static final Border BORDER = BorderFactory.createLineBorder (Color.black, 1); //new RoundedBorder (Color.black, 4);
	private static final Border BORDER2 = BorderFactory.createEmptyBorder();  //new RoundedBorder (Color.gray, 4);
	private static final Color BACKGROUND = new Color (224, 224, 255);
	private static final Color BACKGROUND2 = ColorUtilities.darkenSlightly (BACKGROUND, 0.25f);
	//private static final Icon ICON = IconCache.makeIcon ("BarleyIcon");

	protected transient Font[] fonts = new Font [20];
	protected transient int fontCutoff = 7;
	protected transient Insets insets = new Insets (0, 0, 0, 0);
	
	
	public DefaultGraphCellRenderer () {
		this (null);
	}
	
	
	public DefaultGraphCellRenderer (final Icon icon) {
		super (icon);
		setBorder (BORDER);
		setBackground (BACKGROUND);
		setForeground (Color.black);
		for (int n = 0; n < fonts.length; n++) {
			fonts [n] = this.getFont().deriveFont ((float)(fontCutoff + n));
		}
	}
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		super.getGraphCellRendererComponent (graph, value, isSelected, hasFocus);
		//this.setEnabled (isSelected);
		this.setBorder (isSelected ? BORDER : BORDER2);
		this.setBackground (isSelected ? BACKGROUND : BACKGROUND2);
		this.setForeground (isSelected ? Color.black : Color.gray);
		this.setText (value.toString());
		return this;
	}
	
	@Override
	public void paintComponent (final Graphics gContext) {
		this.getInsets (insets);
		int fontSize = this.getHeight() - fontCutoff - insets.top - insets.bottom - 4;
		fontSize = Math.max (0, Math.min (fontSize, fonts.length - 1));
		gContext.setFont (fonts [fontSize]);

		//((Graphics2D)gContext).scale (scale, scale);
		super.paintComponent (gContext);
		//((Graphics2D)gContext).scale (1.0 / scale, 1.0 / scale);
	}

}
