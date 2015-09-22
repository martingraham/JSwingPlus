package swingPlus.graph;

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JPanel;


public abstract class AbstractPanelGraphCellRenderer extends JPanel implements GraphCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5188024015280323507L;
	protected Object obj;
	
    public AbstractPanelGraphCellRenderer () {
    	super ();
    	setOpaque (true);
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
    	 if (this.getComponentCount() > 0) {
    		 super.validate();
    	 }
     }
     @Override
	public void invalidate() {
    	if (this.getComponentCount() > 0) {
    		super.invalidate();
    	}
     }
     @Override
	public void repaint() {
    	 //if (this.getComponentCount() > 0) {
    	//	 super.repaint();
    	 //}
     }
     @Override
	public void revalidate() {
    	 if (this.getComponentCount() > 0) {
    		 super.revalidate();
    	 }
     }
     @Override
	public void repaint (final long tm, final int x, final int y, final int width, final int height) {
    	// if (this.getComponentCount() > 0) {
    	//	 super.repaint(tm, x, y, width, height);
    	// }
    	 // EMPTY
     }
     @Override
	public void repaint (final Rectangle rect) {
    	// if (this.getComponentCount() > 0) {
    	//	 super.repaint(rect);
    	// }
     }

     @Override
	protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
    // Strings get interned...
    	 if ("text".equals (propertyName)) {
    		 super.firePropertyChange(propertyName, oldValue, newValue);
    	 }
    }

     @Override
	public void firePropertyChange(final String propertyName, final byte oldValue, final byte newValue) {}
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
