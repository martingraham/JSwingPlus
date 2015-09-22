package example.graph.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

import javax.swing.border.Border;

import swingPlus.graph.AbstractGraphCellRenderer;
import swingPlus.graph.JGraph;
import swingPlus.shared.border.RoundedBorder;
import util.GraphicsUtil;

public class StringGraphCellRenderer extends AbstractGraphCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8077146043053955659L;
	
	protected static final Border SELECTED_BORDER = new RoundedBorder (Color.black);
	protected static final Border UNSELECTED_BORDER = new RoundedBorder (Color.gray);  //new RoundedBorder (Color.gray, 4);
	protected static final Color BACKGROUND = new Color (206, 200, 184);
	protected static final Color UNSELECTED_BACKGROUND = new Color (246, 240, 224);

	Font[] fonts = new Font [50];
	int fontCutoff = 4;
	Insets insets = new Insets (0, 0, 0, 0);
	
	Path2D path;
	Arc2D arc;

	
	public StringGraphCellRenderer () {
		setBorder (SELECTED_BORDER);
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
		//this.setEnabled (isSelected);
		this.setBorder (isSelected ? SELECTED_BORDER : UNSELECTED_BORDER);
		this.setBackground (isSelected ? BACKGROUND : UNSELECTED_BACKGROUND);
		this.setForeground (isSelected ? Color.black : Color.gray);

		this.setText (value.toString());
		return this;
	}
	
	@Override
	public void paintComponent (final Graphics gContext) {		
		//if (isEnabled()) {
			gContext.setColor (getBackground());
			GraphicsUtil.makeRoundedBox (path, arc, this.getHeight(), this.getWidth());
			((Graphics2D)gContext).fill (path);
			gContext.setColor (getForeground());
		//}
		this.getInsets (insets);
		int fontSize = (int)((double)(this.getHeight() - insets.top - insets.bottom) * 0.55);
		fontSize = Math.max (0, Math.min (fontSize, fonts.length - 1));
		gContext.setFont (fonts [fontSize]);
		
		super.paintComponent (gContext);
	}
}
