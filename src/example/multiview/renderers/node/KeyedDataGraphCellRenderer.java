package example.multiview.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;

import model.graph.EdgeSetValueMaker;

import example.graph.renderers.node.NodeRadiusValueCellRenderer;
import example.multiview.KeyedData;

import swingPlus.graph.JGraph;


public class KeyedDataGraphCellRenderer extends NodeRadiusValueCellRenderer  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8077146043053955659L;
	
	protected KeyedData kdata;
	protected int degree;
	
	public KeyedDataGraphCellRenderer () {
		super ();
	}
	
	
	public KeyedDataGraphCellRenderer (final EdgeSetValueMaker newValueMaker) {
		super (newValueMaker);
	}
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		super.getGraphCellRendererComponent (graph, value, isSelected, hasFocus);
		kdata = (KeyedData)value;
		degree = graph.getModel().getEdges(value).size();
		Color c = Color.decode (kdata.getData().get(4).toString());
		this.setForeground (selected ? c.brighter() : c);
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
		g2D.fill (e2d);
		
		if (selected && width > 2) {
			selectede2d.setFrame (1, 1, width - 3, height - 3);
			gContext.setColor (selected ? Color.black : Color.darkGray);
			g2D.setStroke (selected ? SELECTED_STROKE : STROKE);
			g2D.draw (selectede2d);
			g2D.setStroke (STROKE);
			
			g2D.setColor (Color.black);
			FontRenderContext frc = g2D.getFontRenderContext();
			
			String s1 = kdata.getData().get(1).toString();
			int s1length = (int)g2D.getFont().getStringBounds(s1, frc).getWidth();
			g2D.drawString (s1, Math.max (5, (getWidth() - s1length) / 2), (getHeight() / 2));
			
			String s2 = Integer.toString(degree)+" co-collaborators";
			int s2length = (int)g2D.getFont().getStringBounds(s2, frc).getWidth();
			g2D.drawString (s2, Math.max (5, (getWidth() - s2length) / 2), (getHeight() / 2) + 14);
		}
	}
}
