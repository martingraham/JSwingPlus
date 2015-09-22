package example.graph.renderers.node;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import swingPlus.graph.AbstractPanelGraphCellRenderer;
import swingPlus.graph.JGraph;


public class NodeDegreeGraphCellRenderer extends AbstractPanelGraphCellRenderer  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8077146043053955659L;
	
	public static final Color BACKGROUND = new Color (224, 224, 255);
 	static final public Stroke STROKE = new BasicStroke (1.0f);
 	static final public Stroke SELECTED_STROKE = new BasicStroke (2.0f);
	
	protected transient double scale;
	protected transient Insets insets = new Insets (0, 0, 0, 0);
	protected Dimension degreeDim = new Dimension ();
	protected Ellipse2D e2d = new Ellipse2D.Float ();
	protected Ellipse2D selectede2d = new Ellipse2D.Float ();
	protected boolean selected;
	protected double multiplier;
	protected int degree;
	
	
	public NodeDegreeGraphCellRenderer () {
		this (1.0);
	}
	
	public NodeDegreeGraphCellRenderer (final double multiplier) {
		super ();
		setBorder (null);
		setBackground (BACKGROUND);
		setForeground (Color.black);
		setPreferredSize (new Dimension (16, 16));
		setMinimumSize (new Dimension (3, 3));
		this.multiplier = multiplier;
		//setMaximumSize (new Dimension (64, 64));
	}
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		super.getGraphCellRendererComponent (graph, value, isSelected, hasFocus);
		selected = isSelected;
		degree = graph.getModel().getEdges(value).size();
		final int diameter = 2 + (int)(multiplier * Math.sqrt (degree));
		degreeDim.setSize (diameter + 1, diameter + 1);
		this.setPreferredSize (degreeDim);
		setForeground (selected ? Color.orange : Color.gray);
		//this.setBorder (isSelected ? labelBorder : b2);
		//this.setBackground (isSelected ? BACKGROUND : Color.lightGray);
		//this.setForeground (isSelected ? Color.black : Color.gray);
		return this;
	}
	
	@Override
	public void paintComponent (final Graphics gContext) {

		final Graphics2D g2D = (Graphics2D)gContext;
		
		//this.getInsets (insets);
		final int width = this.getWidth ();
		final int height = this.getHeight();
		e2d.setFrame (0, 0, width - 1, height - 1);
		gContext.setColor (selected ? Color.orange : Color.gray);
		g2D.fill (e2d);
		
		if (selected && width > 2) {
			selectede2d.setFrame (1, 1, width - 3, height - 3);
			gContext.setColor (selected ? Color.black : Color.darkGray);
			g2D.setStroke (selected ? SELECTED_STROKE : STROKE);
			g2D.draw (selectede2d);
			g2D.setStroke (STROKE);
		}
	}
}
