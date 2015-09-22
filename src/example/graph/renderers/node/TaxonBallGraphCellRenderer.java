package example.graph.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import swingPlus.graph.AbstractPanelGraphCellRenderer;
import swingPlus.graph.JGraph;


public class TaxonBallGraphCellRenderer extends AbstractPanelGraphCellRenderer  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8077146043053955659L;
	
	protected static final Color BACKGROUND = new Color (224, 224, 255);
	protected final static Color[] BALL_COLOURS = {Color.darkGray, Color.gray, Color.lightGray, Color.white};
	protected final static Color[] BALL_COLOURS2 = {new Color (0, 0, 128), Color.blue, new Color (128, 128, 255), Color.white};

	protected transient double scale;
	protected transient Insets insets = new Insets (0, 0, 0, 0);
	
	
	public TaxonBallGraphCellRenderer () {
		super ();
		setBorder (null);
		setBackground (BACKGROUND);
		setForeground (Color.black);
		setPreferredSize (new Dimension (16, 16));
		//setMaximumSize (new Dimension (64, 64));
	}
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		this.setEnabled (isSelected);
		//this.setBorder (isSelected ? labelBorder : b2);
		//this.setBackground (isSelected ? BACKGROUND : Color.lightGray);
		//this.setForeground (isSelected ? Color.black : Color.gray);
		return this;
	}
	
	@Override
	public void paintComponent (final Graphics gContext) {
		//((Graphics2D)gContext).scale (scale, scale);
		//super.paintComponent (gContext);
		//((Graphics2D)gContext).scale (1.0 / scale, 1.0 / scale);
		
		this.getInsets (insets);
		int width = this.getWidth ();
		int height = this.getHeight();
		int x = 0;
		int y = 0;
		final int xx = Math.max (1, (int)(width * 0.05));
		final int yy = Math.max (1, (int)(height * 0.05));
		for (int n = 0; n < BALL_COLOURS.length; n++) {
			gContext.setColor (isEnabled() ? BALL_COLOURS2 [n] : BALL_COLOURS [n]);
			gContext.fillOval (x, y, width, height);
			x += xx;
			y += yy;
			width *= 0.8;
			height *= 0.8;
		}
	}
}
