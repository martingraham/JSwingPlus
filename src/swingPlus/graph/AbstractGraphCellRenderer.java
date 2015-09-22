package swingPlus.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JLabel;

public abstract class AbstractGraphCellRenderer extends JLabel implements GraphCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5188024015280323507L;
	protected Object obj;
	
	
    public AbstractGraphCellRenderer (final Icon icon) {
    	super (icon);
	    // Don't paint behind the component
	    setOpaque (true);
	    setBackground (Color.red); // garish colour just so we can be certain no background rendering is happening
    }
	
    public AbstractGraphCellRenderer () {
    	this ((Icon)null);
    }
	
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		obj = value;
		return this;
	}


    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
     @Override
	public void validate() {
    	 // EMPTY
    }
     
    @Override
	public void invalidate() {
    	// EMPTY
    }
    
    @Override
	public void repaint() {
    	// EMPTY
    }
    
    @Override
	public void revalidate() {
    	// EMPTY
    }
    
    @Override
	public void repaint (final long tm, final int x, final int y, final int width, final int height) {
    	// EMPTY
    }
    
    @Override
	public void repaint (final Rectangle rect) {
    	// EMPTY
    }

     @Override
	protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
    // Strings get interned...
    	 if ("text".equals (propertyName)) {
    		 super.firePropertyChange(propertyName, oldValue, newValue);
    	 }
    }

    @Override
	public void firePropertyChange(final String propertyName, final byte oldValue, final byte newValue) {
    	// EMPTY
    }
    
    @Override
	public void firePropertyChange(final String propertyName, final char oldValue, final char newValue) {}
     @Override
	public void firePropertyChange(final String propertyName, final short oldValue, final short newValue) {}
     @Override
	public void firePropertyChange(final String propertyName, final int oldValue, final int newValue) {}
     @Override
	public void firePropertyChange(final String propertyName, final long oldValue, final long newValue) {}
     @Override
	public void firePropertyChange(final String propertyName, final float oldValue, final float newValue) {}
     @Override
	public void firePropertyChange(final String propertyName, final double oldValue, final double newValue) {}
     @Override
	public void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {}
}
