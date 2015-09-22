package swingPlus.parcoord;

import java.text.Format;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * 
 * @author cs22
 * Renderer that allows values to be rendered differently via a pluggable Format object
 */
public class FormatterRenderer extends DefaultTableCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1719100161549077718L;
	
	Format formatter;

	public FormatterRenderer () {
		super ();
	}
	
	public FormatterRenderer (final Format formatter) {
		this ();
		setFormatter (formatter);
	}
	
	public void setFormatter (final Format formatter) {
		this.formatter = formatter;
	}
	
	
    /**
     * Sets the <code>String</code> object for the cell being rendered to
     * <code>value</code>.
     * 
     * @param value  the string value for this cell; if value inputStream
     *		<code>null</code> it sets the text value to an empty string
     * @see JLabel#setText
     * 
     */
	@Override
    protected void setValue (final Object value) {
    	setText((value == null) ? "" : (formatter == null ? value.toString() : formatter.format (value)));
    }
}
