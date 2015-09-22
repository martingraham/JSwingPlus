package swingPlus.shared.border;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.border.LineBorder;

public class DashedBorder extends LineBorder {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4790789953735498469L;
	private transient Stroke stroke;
    /** 
     * Creates a line border with the specified color and a 
     * thickness = 1.
     * @param color the color for the border
     */
    public DashedBorder (final Color color, final BasicStroke stroke) {
        this (color, false, stroke);
    }

    /**
     * Creates a line border with the specified color, thickness,
     * and corner shape.
     * @param color the color of the border
     * @param thickness the thickness of the border
     * @param roundedCorners whether or not border corners should be round
     * @since 1.3
     */
    public DashedBorder (final Color color, final boolean roundedCorners, final BasicStroke stroke)  {
        super (color, (int)stroke.getLineWidth(), roundedCorners);
        this.stroke = stroke;
    }
    
    @Override
	public void paintBorder (final Component comp, final Graphics graphics, final int x, final int y, 
			final int width, final int height) {
        final Color oldColor = graphics.getColor();
        final Graphics2D g2D = (Graphics2D) graphics;
        final Stroke oldStroke = g2D.getStroke();

	/// PENDING(klobad) How/should do we support Roundtangles?
        g2D.setColor (lineColor);
        g2D.setStroke (stroke);

	    if (roundedCorners) {
			final int arcWidth = Math.min (4, width / 2);
			final int arcHeight = Math.min (4, height / 2);
			g2D.drawRoundRect (x, y, width-thickness, height-thickness, arcWidth, arcHeight);
		} else {
			g2D.drawRect(x + (thickness >> 1), y + (thickness >> 1), width - thickness, height - thickness);
		}

	    g2D.setStroke (oldStroke);
	    
	    //g2D.setColor (Color.black);
	    //g2D.drawRect (x, y, width - 1, height - 1);
        g2D.setColor (oldColor);

    }
    
    public Stroke getBasicStroke() { return stroke; }
}
