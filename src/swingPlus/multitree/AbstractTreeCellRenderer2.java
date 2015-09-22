package swingPlus.multitree;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public abstract class AbstractTreeCellRenderer2 extends JPanel implements TreeCellRenderer2 {

	/**
	 * 
	 */
	private static final long serialVersionUID = -36795248822777301L;

	Shape shape;
	boolean leaf;
	boolean root;
	boolean selected;
	DefaultMutableTreeNode node;
	JTree tree;
	
	@Override
	public void setShape (final Shape newShape) {
		shape = newShape;

	}

	@Override
	public Component getTreeCellRendererComponent (final JTree tree, final Object value,
			final boolean selected, final boolean expanded, final boolean leaf, final int row,
			final boolean hasFocus) {
		this.leaf = leaf;
		this.node = (DefaultMutableTreeNode)value;
		this.tree = tree;
		this.selected = selected;
		return this;
	}
	
	abstract public void draw (Graphics graphics);

  /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
	 @Override
    public void validate() {}

    @Override
   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    *
    * @since 1.5
    */
    public void invalidate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void revalidate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void repaint (Rectangle rect) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    *
    * @since 1.5
    */
    @Override
    public void repaint() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    protected void firePropertyChange (final String propertyName, final Object oldValue, 
    		final Object newValue) {	
	// Strings get interned...
	if ("text".equals (propertyName)
                || ("font".equals (propertyName) || "foreground".equals (propertyName))
                    && oldValue != newValue
                    && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null) {

	    super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
}
