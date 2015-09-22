package swingPlus.shared.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.MatteBorder;

public class MatteBorder2 extends MatteBorder {

    /**
	 * 
	 */
	private static final long serialVersionUID = -361706778889291457L;
	protected transient Color leftColour, bottomColour, rightColour;

    /**
     * Creates a matte border with the specified insets and color.
     * @param top the top inset of the border
     * @param left the left inset of the border
     * @param bottom the bottom inset of the border
     * @param right the right inset of the border
     * @param matteColor the color rendered for the border
     */
    public MatteBorder2 (final int top, final int left, final int bottom, final int right, 
    		final Color topColour, final Color leftColour, final Color bottomColour, final Color rightColour)   {
        super (top, left, bottom, right, topColour);
        this.leftColour = leftColour;
        this.bottomColour = bottomColour;
        this.rightColour = rightColour;
    }

    /**
     * Creates a matte border with the specified insets and color.
     * @param borderInsets the insets of the border
     * @param matteColor the color rendered for the border
     * @since 1.3
     */
    public MatteBorder2 (final Insets borderInsets, 
    		final Color topColour, final Color leftColour, final Color bottomColour, final Color rightColour)   {
        super (borderInsets, topColour);
        this.leftColour = leftColour;
        this.bottomColour = bottomColour;
        this.rightColour = rightColour;
    }
    
    @Override
	public void paintBorder (final Component comp, final Graphics graphics, final int x, final int y, 
			final int width, final int height) {
        if (tileIcon == null) {
        	final Insets insets = getBorderInsets (comp);
        	final Color oldColor = graphics.getColor();
            graphics.translate (x, y);
            
            graphics.setColor (color == null ? oldColor : color);
            graphics.fillRect (0, 0, width - insets.right, insets.top);
            graphics.setColor (leftColour == null ? oldColor : leftColour);
            graphics.fillRect (0, insets.top, insets.left, height - insets.top);
            graphics.setColor (bottomColour == null ? oldColor : bottomColour);
            graphics.fillRect (insets.left, height - insets.bottom, width - insets.left, insets.bottom);
            graphics.setColor (rightColour == null ? oldColor : rightColour);
            graphics.fillRect (width - insets.right, 0, insets.right, height - insets.bottom);
            
            graphics.translate (-x, -y);
            graphics.setColor (oldColor);
        } else {
        	super.paintBorder (comp, graphics, x, y, width, height);
        }
    }
}
