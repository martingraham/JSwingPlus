package swingPlus.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class AbstractGraphEdgeRenderer extends JPanel implements GraphEdgeRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 798766036534644861L;
	protected Object obj;
	public int fromX, fromY, toX, toY;
	protected Insets insets = new Insets (0, 0, 0, 0);
	
	public AbstractGraphEdgeRenderer () {
		super ();
		 // Don't paint behind the component
		 setOpaque (false);
		 setBackground (Color.red); // garish colour just so we can be certain no background rendering inputStream happening
		 //setBorder (BorderFactory.createMatteBorder (10, 10, 10, 10, new Color (0, 0, 0, 128))); 
		 setBorder (BorderFactory.createEmptyBorder (10, 10, 10, 10));
	}
	
	@Override
	public Component getGraphEdgeRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus,
			final int x1, final int y1, final int x2, final int y2) {
		obj = value;
		fromX = x1;
		fromY = y1;
		toX = x2;
		toY = y2;
		insets = getInsets (insets);
		return this;
	}
	
	public void translateToOrigin (final Graphics graphics) {
		graphics.translate (-(this.getX()), -(this.getY()));
		//g.translate (-(this.getX() + insets.left), -(this.getY() + insets.top));
	}
	
	
	public void translateBack (final Graphics graphics) {
		graphics.translate (this.getX(), this.getY());
	}
	
	   /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
	 /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
     @Override
     public void validate() {
    	 //empty
     }
    
     @Override
     public void invalidate() {
    	 //empty
     }
     
     @Override
     public void repaint() {
    	 //empty
     }
     
    @Override
	public void revalidate() {
    	 //empty
    }
    
    @Override
	public void repaint (final long time, final int x, final int y, final int width, final int height) {
    	 //empty
    }
    
    @Override
	public void repaint (final Rectangle rect) {
    	 //empty
    }

    @Override
	protected void firePropertyChange (final String propertyName, final Object oldValue, final Object newValue) {
     // Strings get interned...
    	 if ("text".equals (propertyName)) {
    		 super.firePropertyChange (propertyName, oldValue, newValue);
    	 }
    }

     @Override
     public void firePropertyChange (final String propertyName, final byte oldValue, final byte newValue) {
    	 //empty
     }
     
     @Override
     public void firePropertyChange (final String propertyName, final char oldValue, final char newValue) {
    	 //empty
     }
     
     @Override
     public void firePropertyChange (final String propertyName, final short oldValue, final short newValue) {
    	 //empty
     }
     
     @Override
     public void firePropertyChange (final String propertyName, final int oldValue, final int newValue) {
    	 //empty
     }
     
     @Override
     public void firePropertyChange (final String propertyName, final long oldValue, final long newValue) {
    	 //empty
     }
     
     @Override
     public void firePropertyChange (final String propertyName, final float oldValue, final float newValue) {
    	 //empty
     }
     
     @Override
     public void firePropertyChange (final String propertyName, final double oldValue, final double newValue) {
    	 //empty
     }
     
     @Override
     public void firePropertyChange (final String propertyName, final boolean oldValue, final boolean newValue) {
    	 //empty
     }
}
