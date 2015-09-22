package swingPlus.shared.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

public class ShadowBorder extends AbstractBorder {
	/**
	 * 
	 */
	private static final long serialVersionUID = 305658888138684478L;
	private final transient int xoff, yoff;
	private final transient Insets insets;
	private static final Color SHADOW = new Color (0, 0, 0, 160);
	
	public final static ShadowBorder BORDER = new ShadowBorder (3, 3); 
	
	public ShadowBorder (final int x, final int y) {
		super ();
		xoff = x;
		yoff = y;
		insets = new Insets (0, 0, yoff, xoff);
	}
	
	@Override
	public Insets getBorderInsets (final Component comp) {
		return insets;
	}
	
	@Override
	public void paintBorder (final Component comp, final Graphics graphics, final int x, final int y, 
			final int width, final int height) {
		graphics.setColor (SHADOW);
		graphics.translate (x, y);
		graphics.fillRect (width - xoff, yoff, xoff, height - yoff - yoff);
		graphics.fillRect (xoff, height - yoff, width - xoff, yoff);
		//g.setColor (CLEAR);
		//g.fillRect (width - xoff, 0, xoff, yoff);
		//g.fillRect (0, height - yoff, xoff, yoff);
		graphics.translate (-x, -y);
	}
}
