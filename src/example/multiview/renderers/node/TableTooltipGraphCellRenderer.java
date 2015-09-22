package example.multiview.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.text.View;

import model.graph.Edge;

import org.apache.log4j.Logger;

import example.multiview.KeyedData;

import swingPlus.graph.AbstractGraphCellRenderer;
import swingPlus.graph.JGraph;


public class TableTooltipGraphCellRenderer extends AbstractGraphCellRenderer {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6417446901746199601L;
	private static final Logger LOGGER = Logger.getLogger (TableTooltipGraphCellRenderer.class);
	static Comparator<Edge> edgeComparator = new EdgeIntComparator ();

	Font font = Font.decode ("Gill-Sans-MT-Plain-24");
	boolean selected;
	
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		super.getGraphCellRendererComponent (graph, value, isSelected, hasFocus);
		//this.setEnabled (isSelected);

		KeyedData kdata = (KeyedData)value;
		this.setForeground (isSelected ? Color.black : Color.gray);
		this.setBackground (Color.white);
		this.setText (value.toString());
		
		selected = isSelected;
		

		final StringBuilder sb = new StringBuilder ("<HTML>");
		sb.append (kdata.getData().get(1).toString());
		
		sb.append ("<br><Table><tr><th>Collaborator</th><th>Times</th></tr>");
		
		
		Set<Edge> edges = graph.getModel().getEdges (value);
		List<Edge> useEdges = new ArrayList<Edge> (edges);
		Collections.sort (useEdges, edgeComparator);
		
		for (Edge edge : useEdges) {
			Object otherNode = (edge.getNode1() == value ? edge.getNode2() : edge.getNode1());
			if (otherNode instanceof KeyedData) {
				KeyedData otherData = (KeyedData)otherNode;
				sb.append ("<tr><td>")
					.append (otherData.getData().get(1).toString())
					.append ("</td><td>")
					.append (edge.getEdgeObject().toString())
					.append ("</td></tr>");
			}
		}
		
		sb.append ("</table>");
		sb.append ("</HTML>");
		this.setText (sb.toString());
		
	    final View htmlView = (View) this.getClientProperty ("html");
	    if (LOGGER.isDebugEnabled()) {
	    	LOGGER.info ("html width: "+htmlView.getPreferredSpan (View.X_AXIS));
	    	LOGGER.info ("html height:"+ htmlView.getPreferredSpan (View.Y_AXIS));
	    }
		
		this.setPreferredSize (new Dimension ((int)htmlView.getPreferredSpan(View.X_AXIS), 
				(int)htmlView.getPreferredSpan(View.Y_AXIS)));
		return this;
	}
	
	
	@Override
	public void paintComponent (final Graphics gContext) {		
		gContext.setFont (font);
		
		super.paintComponent (gContext);
	}
}

class EdgeIntComparator implements Comparator<Edge> {

	@Override
	public int compare (Edge e1, Edge e2) {
		Object eobj1 = e1.getEdgeObject();
		Object eobj2 = e2.getEdgeObject();
		if (eobj1 instanceof Comparable && eobj2 instanceof Comparable) {
			Comparable cobj1 = (Comparable)eobj1;
			Comparable cobj2 = (Comparable)eobj2;
			return -cobj1.compareTo(cobj2);
		}
		if (eobj1 == null && eobj2 == null) {
			return 0;
		}
		return (eobj1 == null ? 1 : -1);
	}
}
