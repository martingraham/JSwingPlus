package example.graph.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import swingPlus.graph.AbstractPanelGraphCellRenderer;
import swingPlus.graph.GraphCellRenderer;
import swingPlus.graph.JGraph;
import util.GraphicsUtil;
import util.Messages;


public class SingleImageGraphCellRenderer extends AbstractPanelGraphCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8077146043053955659L;
	
	protected static final Color BACKGROUND = new Color (224, 224, 255);
	
	BufferedImage image;
	BufferedImage selectedImage;
	BufferedImage [] images;
	Image [] scaledImages;
	String iconName;
	
	protected transient int lastHeight = 0, lastWidth = 0;
	protected transient double scale;
	protected transient Insets insets = new Insets (0, 0, 0, 0);
	
	
	public SingleImageGraphCellRenderer (final String iconName) {
		super ();
		setBorder (null);
		setPreferredSize (new Dimension (20, 20));
		setBackground (BACKGROUND);
		setForeground (Color.black);
		//setMaximumSize (new Dimension (64, 64));
		this.iconName = iconName;
		image = GraphicsUtil.loadBufferedImage (
				"img/"+ (Messages.getString (GraphicsUtil.GRAPHICPROPS, iconName)));
		selectedImage = GraphicsUtil.makeTintedImage (new Color (128, 128, 255), image);
		images = new BufferedImage[] {image, selectedImage};
		scaledImages = new Image [images.length];
	}

	
	// Copy constructor
	public SingleImageGraphCellRenderer (final GraphCellRenderer cloneMe) {
		this (((SingleImageGraphCellRenderer)cloneMe).iconName);
	}
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		this.setEnabled (isSelected);
		return this;
	}
	
	@Override
	public void paintComponent (final Graphics gContext) {	
		this.getInsets (insets);
		final int width = this.getWidth ();
		final int height = this.getHeight();
		
		final int imageIndex = isEnabled() ? 1 : 0;
		Image scaledImage = scaledImages [imageIndex];
		
		if (scaledImage == null || height != scaledImage.getHeight(null) || width != scaledImage.getWidth(null)) {
			scaledImage = images[imageIndex].getScaledInstance (width, height, Image.SCALE_FAST);
			scaledImages[imageIndex] = GraphicsUtil.copyImage (scaledImage, images[imageIndex].getTransparency());
		}
		((Graphics2D)gContext).drawImage (scaledImages[imageIndex], 0, 0, null);
	}
}
