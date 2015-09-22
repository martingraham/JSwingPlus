package util.swing;

import java.awt.Color;

import util.GraphicsUtil;
import util.Messages;
import util.colour.ColorUtilities;

public class MouseTipBaseRenderer {

    protected Color foreground, background;


    public MouseTipBaseRenderer () {
       foreground = Color.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "mouseTextColour"));
       background = ColorUtilities.decodeWithAlpha (Messages.getString (GraphicsUtil.GRAPHICPROPS, "mouseBackgroundColour"));
    }


	protected String format (final Object obj) {
		return "<html>"+obj.toString()+"</html>";
	}

	public Color getForeground () { return foreground; }
	public Color getBackground () { return background; }
}