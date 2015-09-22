package example.graph.renderers.node;

import java.awt.Component;

import swingPlus.graph.DefaultGraphCellRenderer;
import swingPlus.graph.JGraph;

public class RDFGraphCellRenderer extends DefaultGraphCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3266060648219643249L;

	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		super.getGraphCellRendererComponent (graph, value, isSelected, hasFocus);
		final String string = value.toString ();
		final int val = string.lastIndexOf ('#');
		this.setText (val == -1 ? string : string.substring (val + 1));
		return this;
	}
}
