package example.graph.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

import swingPlus.graph.AbstractPanelGraphCellRenderer;
import swingPlus.graph.JGraph;


public class ImageGraphCellRenderer extends AbstractPanelGraphCellRenderer  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8077146043053955659L;
	
	protected static final Color BACKGROUND = new Color (224, 224, 255);
	
	protected BufferedImage image;
	protected transient Insets insets = new Insets (0, 0, 0, 0);
	protected Dimension preferredSize = new Dimension ();
	protected Border border = BorderFactory.createLineBorder (Color.gray, 1);
	protected Border borderSelected = BorderFactory.createRaisedBevelBorder();
	
	public ImageGraphCellRenderer () {
		super ();
		setBorder (null);
		setPreferredSize (new Dimension (20, 20));
		setMinimumSize (new Dimension (12, 12));
		setMaximumSize (new Dimension (400, 400));
		setBackground (BACKGROUND);
		setForeground (Color.black);
		//setMaximumSize (new Dimension (64, 64));
	}
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		this.setEnabled (isSelected);
		image = (BufferedImage)value;
		this.setBorder (isSelected ? borderSelected : border);
		this.getInsets (insets);
		preferredSize.setSize (image.getWidth() + insets.left + insets.right, 
				image.getHeight() + insets.bottom + insets.top);
		this.setPreferredSize (preferredSize);
		
		return this;
	}
	
	@Override
	public void paintComponent (final Graphics gContext) {	
		drawImage (gContext, this);
	}
	
	protected void drawImage (final Graphics gContext, final JComponent jcomp) {
		jcomp.getInsets (insets);
		final int width = jcomp.getWidth () - insets.left - insets.right;
		final int height = jcomp.getHeight() - insets.top - insets.bottom;
		final int x = insets.left;
		final int y = insets.top;
		//System.err.println ("width: "+width+", img width: "+image.getWidth());
		
		if (width == image.getWidth()) {
			((Graphics2D)gContext).drawImage (image, x, y, null);
		} else {
			((Graphics2D)gContext).drawImage (image, x, y, width, height, null);
		}
	}
}
