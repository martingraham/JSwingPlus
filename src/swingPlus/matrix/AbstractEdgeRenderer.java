package swingPlus.matrix;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import model.graph.Edge;


public abstract class AbstractEdgeRenderer extends JPanel implements TableCellRenderer {

	
   	/**
	 * 
	 */
	private static final long serialVersionUID = 6312433737782008376L;
	protected final static Color BLACKGRADE = new Color (0, 0, 0, 64);
	protected final static Color BLACKGRADE2 = new Color (0, 0, 0, 128);
	protected final static Border SELECT_BORDER = BorderFactory.createLineBorder (Color.red, 1);
	
	protected final Dimension prefSize = new Dimension (100, 100);
	protected Edge edge;
	
	
    public AbstractEdgeRenderer () {
	    // Don't paint behind the component
	    setOpaque (true);
	    setBackground (Color.red); // garish colour just so we can be certain no background rendering inputStream happening
    }
    
    
    // implements javax.swing.table.TableCellRenderer
    /**
     *
     * Returns the default table cell renderer.
     *
     * During a printing operation, this method will be called with
     * <code>isSelected</code> and <code>hasFocus</code> values of
     * <code>false</code> to prevent selection and focus from appearing
     * in the printed output. To do other customization based on whether
     * or not the table is being printed, check the return value from
     * {@link javax.swing.JComponent#isPaintingForPrint()}.
     *
     * @param table  the <code>JTable</code>
     * @param value  the value to assign to the cell at
     *			<code>[row, column]</code>
     * @param isSelected true if cell inputStream selected
     * @param hasFocus true if cell has focus
     * @param row  the row of the cell to render
     * @param column the column of the cell to render
     * @return the default table cell renderer
     * @see javax.swing.JComponent#isPaintingForPrint()
     */
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {

	    edge = value instanceof Edge ? (Edge)value : null; 
        setBorder (isSelected ? SELECT_BORDER : null);
		return this;
    }
    
    @Override
	public Dimension getPreferredSize () {
    	return prefSize;
    }
}
