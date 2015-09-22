package swingPlus.matrix;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import model.graph.Edge;

/**
 * Class that renders a collection of objects by referring to those object types renderers
 * and overlaying them like a stack of cards.
 * @author cs22
 *
 */
public class CollectionRenderer extends AbstractEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3798174105063665889L;

	protected final static Border SELECT_SET_BORDER = BorderFactory.createLineBorder (Color.red, 2);
	//protected final static Border OVERLAY_BORDER = BorderFactory.createMatteBorder (1, 1, 1, 1, Color.gray);
	protected final static Border OVERLAY_BORDER = BorderFactory.createLineBorder (Color.gray, 1);

	final static double SUBSCALE = 0.7;
	
	CellRendererPane cellPane = new CellRendererPane ();
	Collection<Edge> edgeCollection;
	JTable table;
	boolean isSelected, hasFocus;
	int row, column;
	
	public CollectionRenderer () {
		setLayout (null);
		this.add (cellPane);
	}
	
    @Override
	public Component getTableCellRendererComponent (final JTable table, final Object value,
			final boolean isSelected, final boolean hasFocus, final int row, final int column) {

    	edgeCollection = (Collection<Edge>)value;
    	this.table = table;
    	this.row = row;
    	this.column = column;
    	this.isSelected = isSelected;
    	this.hasFocus = hasFocus;
    	//edge = value instanceof Edge ? (Edge)value : null; 
    	//setBorder (isSelected ? SELECT_SET_BORDER : MULTIEDGE_BORDER);
    	setBorder (isSelected ? SELECT_SET_BORDER : null);
    	return this;
    }
    
    @Override
	public void paintComponent (final Graphics gContext) {
    	final int width = this.getWidth ();
    	final int height = this.getHeight ();
    	
    	final int subWidth = (int)Math.ceil (width * SUBSCALE);
    	final int subHeight = (int)Math.ceil (height * SUBSCALE);
    	
    	final int setSize = edgeCollection.size();
    	if (setSize < width * height / 9) {	
    		int x = 0, y = 0;	
    		final int hoffset = (width - subWidth) / (setSize - 1);
    		final int voffset = (height - subHeight) / (setSize - 1);
    		//List<Edge> edgeList = new ArrayList<Edge> (edgeSet);
    		//Collections.sort (edgeList);
    		
    		final Iterator<Edge> edgeIterator = edgeCollection.iterator();
    		
    		while (edgeIterator.hasNext ()) {	
    			final Edge edge = edgeIterator.next ();
    			final Object obj = edge.getEdgeObject ();
    			final TableCellRenderer tcr = table.getDefaultRenderer (obj.getClass());
    			final Component comp = tcr.getTableCellRendererComponent (table, edge, isSelected, hasFocus, row, column);
    			cellPane.add (comp);
    			if (comp instanceof JComponent) {
    				((JComponent)comp).setBorder (OVERLAY_BORDER);				
    			}
    			cellPane.paintComponent (gContext, comp, this, x, y, subWidth, subHeight);
     			x += hoffset;
    			y += voffset;
    		}
    	} else {
    		gContext.setColor (Color.gray);
    		gContext.fillRect (0, 0, width, height);
    	}
    }

}
