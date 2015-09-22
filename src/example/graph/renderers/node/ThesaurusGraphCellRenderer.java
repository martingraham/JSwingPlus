package example.graph.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import swingPlus.graph.AbstractGraphCellRenderer;
import swingPlus.graph.JGraph;
import swingPlus.shared.border.RoundedBorder;
import util.GraphicsUtil;

public class ThesaurusGraphCellRenderer extends AbstractGraphCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8077146043053955659L;
	
	private static final Border BORDER = new RoundedBorder (Color.black);
	//private static final Border BORDER = BorderFactory.createLineBorder (Color.black, 1);
	private static final Border BORDER2 = BorderFactory.createEmptyBorder();  //new RoundedBorder (Color.gray, 4);
	private static final Color BACKGROUND = new Color (246, 240, 224);

	Font[] fonts = new Font [50];
	int fontCutoff = 4;
	Insets insets = new Insets (0, 0, 0, 0);
	
	Path2D path;
	Arc2D arc;

	
	public ThesaurusGraphCellRenderer () {
		setBorder (BORDER);
		setIcon (null);
		setBackground (BACKGROUND);
		setForeground (Color.black);
		setOpaque (false);
		for (int n = 0; n < fonts.length; n++) {
			fonts [n] = this.getFont().deriveFont ((float)(fontCutoff + n));
		}
		
		path = new Path2D.Double ();
		arc = new Arc2D.Double ();
	}
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		super.getGraphCellRendererComponent (graph, value, isSelected, hasFocus);
		this.setEnabled (isSelected);
		this.setBorder (isSelected ? BORDER : BORDER2);
		this.setBackground (isSelected ? BACKGROUND : Color.lightGray);
		//this.setForeground (isSelected ? Color.black : Color.gray);

		this.setText (isSelected ? "   "+value.toString() : value.toString());
		return this;
	}
	
	@Override
	public void paintComponent (final Graphics gContext) {
		if (isEnabled()) {
			gContext.setColor (BACKGROUND);
			GraphicsUtil.makeRoundedBox (path, arc, this.getHeight(), this.getWidth());
			((Graphics2D)gContext).fill (path);
			gContext.setColor (getForeground());
		}
		this.getInsets (insets);
		int fontSize = (int)((double)(this.getHeight() - insets.top - insets.bottom) * 0.55);
		fontSize = Math.max (0, Math.min (fontSize, fonts.length - 1));
		gContext.setFont (fonts [fontSize]);

		//((Graphics2D)gContext).scale (scale, scale);
		super.paintComponent (gContext);
		//((Graphics2D)gContext).scale (1.0 / scale, 1.0 / scale);
		
		if (isEnabled ()) {
			gContext.setFont (fonts [Math.max (0, fontSize / 2)]);
			gContext.setColor (isEnabled() ? Color.red : Color.lightGray);
			gContext.drawString ("A", 4, (this.getHeight() / 2));
			
			gContext.setColor (isEnabled() ? Color.blue : Color.gray);
			gContext.drawString ("Z", 4 + (fontSize / 2), (this.getHeight() / 2) + (fontSize / 2));
		}
	}
}
