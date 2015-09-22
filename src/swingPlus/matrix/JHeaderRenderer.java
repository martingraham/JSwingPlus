package swingPlus.matrix;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import model.graph.Edge;
import model.matrix.MatrixTableModel;
import swingPlus.shared.JColumnHeader;
import swingPlus.shared.JRowHeader;
import util.colour.ColorUtilities;

/**
 * Renders a <code>JRowHeader</code> cell with node info and a filled bar according to
 * the number of edges associated with the node
 * @author cs22
 *
 */
public class JHeaderRenderer extends DefaultTableCellRenderer {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7106757247460619266L;
	
	Font[] fonts = new Font [8];
	Font defaultFont;
	FontMetrics defaultMetrics;
	int fontCutoff = 8;
	
	int edgeSetSize = 0;
	Color edgeSizeColour = ColorUtilities.mixColours (Color.blue, Color.white, 0.35f);
	Color selectionBackground = ColorUtilities.addAlpha (UIManager.getDefaults().getColor ("Table.selectionBackground"), 160);
	Dimension prefSize = new Dimension (0, 0);
	boolean orientHoriz = true;
	
	public JHeaderRenderer () {
		super ();
		this.setBorder(null);
		this.setVerticalTextPosition (SwingConstants.TOP);
		//this.setVerticalTextPosition (SwingConstants.TOP);
		this.setVerticalAlignment (SwingConstants.TOP);
		for (int n = 0; n < fonts.length; n++) {
			fonts [n] = this.getFont().deriveFont ((float)(fontCutoff + n));
		}
		defaultFont = fonts [fonts.length - 1];
		defaultMetrics = this.getFontMetrics (defaultFont);
	}
	
	public void setSelectionBackground (final JTable jTable) {
		selectionBackground = ColorUtilities.addAlpha (jTable.getSelectionBackground(), 160);
	}
	
   @Override
   public Component getTableCellRendererComponent (final JTable table, final Object value,
		   final boolean isSelected, final boolean hasFocus, final int row, final int column) {
	   ///super.getTableCellRendererComponent (table, value, isSelected, 
		//	   hasFocus, row, column);
	   setBackground (isSelected ? selectionBackground : table.getBackground());
	   setValue (value);
	   final TableModel tableModel = table.getModel ();
	   if (tableModel instanceof MatrixTableModel) {
		   final MatrixTableModel mtm = (MatrixTableModel)tableModel;
		   Set<Edge> edgeSet = null;
		   if (table instanceof JRowHeader) {
			   final Object rowObj = mtm.getRowObject (table.convertRowIndexToModel (row));
			   edgeSet = mtm.getRowData (rowObj);	   
			   orientHoriz = true;
		   }
		   else if (table instanceof JColumnHeader) {
			   final Object colObj = mtm.getColumnObject (table.convertColumnIndexToModel (column));
			   edgeSet = mtm.getColumnData (colObj);
			   orientHoriz = false;
		   }
		   edgeSetSize = (edgeSet == null ? 0 : edgeSet.size());
	   }
	   return this;
   }

	

	@Override
	public void paintComponent (final Graphics gContext) {
    		
		gContext.setColor (edgeSizeColour);
		if (orientHoriz) {
			gContext.fillRect (0, 0, edgeSetSize, this.getHeight());
		} else {
			gContext.fillRect (0, this.getHeight() - (edgeSetSize / 3), this.getWidth(), edgeSetSize / 3);
		}
		
		final int maxDim = Math.max (this.getHeight(), this.getWidth());
		final int minDim = Math.min (this.getHeight(), this.getWidth());
		
		if (minDim > fontCutoff && maxDim > 8) {
			int fontSize = minDim - fontCutoff - 4;
			fontSize = Math.max (0, Math.min (fontSize, fonts.length - 1));
			gContext.setFont (fonts [fontSize]);
			super.paintComponent (gContext);
		}
    }
	
    /**
     * Returns the string to be used as the tooltip for <insets>event</insets>.
     * By default this returns any string set using
     * <code>setToolTipText</code>.  If a component provides
     * more extensive API to support differing tooltips at different locations,
     * this method should be overridden.
     */
	@Override
    public String getToolTipText (final MouseEvent mEvent) {
        return getText();
    }
	
	
	@Override
	public Dimension getPreferredSize () {
		setFont (defaultFont);
		return ui.getPreferredSize (this);
	}
}
