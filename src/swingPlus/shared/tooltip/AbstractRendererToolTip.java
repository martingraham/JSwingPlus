package swingPlus.shared.tooltip;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;

import org.apache.log4j.Logger;

import swingPlus.shared.CellRendererPane2;


public abstract class AbstractRendererToolTip extends JToolTip {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8209194780716877752L;
	
	final static Logger LOGGER = Logger.getLogger (AbstractRendererToolTip.class);

	protected Object obj;
	
	protected CellRendererPane pane;
	protected final static int CELL_WIDTH = 100, CELL_HEIGHT = 100;
	protected Dimension oneCellSize = new Dimension (CELL_WIDTH, CELL_HEIGHT);
	protected Dimension curSize = new Dimension ();
	
	protected Insets borderInsets = new Insets (0, 0, 0, 0);
	protected Rectangle renderBounds = new Rectangle ();
	
	protected Comparator sorter;

	
	public AbstractRendererToolTip (final JComponent jComponent) {
		super ();
		setLayout (new BorderLayout());
		pane = new CellRendererPane2 ();
		//pane.setBackground (Color.green);
		add (pane);
		
		setBorder (BorderFactory.createLineBorder (Color.gray, 1));
		setPreferredSize (oneCellSize);
		
		ToolTipManager.sharedInstance().unregisterComponent (jComponent); // remove standard tooltip from component
	}
	
	
	
	public void setToolTipObject (final Object object) {
		LOGGER.debug ("object: "+object);
		setVisible (object != null);
		
		if (obj != object) {
			obj = object;	
			
			if (obj instanceof Collection) {
				
				final Collection<Object> collection = (Collection<Object>)obj;
				if (!collection.isEmpty()) {
					final Iterator<Object> collectionIterator = collection.iterator();
					
					if (collection.size() > 1) {
						final List<Object> list = new ArrayList<Object> (collection);
						if (sorter != null) {
							Collections.sort (list, sorter);
						}
						obj = list;
						curSize.setSize (0, 0);
						while (collectionIterator.hasNext()) {
							final Dimension oPrefSize = getObjectPreferredSize (collectionIterator.next());
							curSize.width += oPrefSize.width + 10;
							curSize.height = Math.max (curSize.height, oPrefSize.height);
						}
						curSize.width -= 10;
					} else if (collection.size() == 1) {
						curSize.setSize (getObjectPreferredSize (collectionIterator.next()));
					}
				}
				else {
					setVisible (false);
				}
			}
			else {
				curSize.setSize (getObjectPreferredSize (obj));
			}
			
			
			setTitle ();

			
			borderInsets = this.getInsets (borderInsets);
			curSize.width += borderInsets.left + borderInsets.right;
			curSize.height += borderInsets.top + borderInsets.bottom;
			setPreferredSize (curSize);
			
			LOGGER.debug (this.isVisible()+" "+this);
		}
	}
	
	
	abstract protected void setTitle ();
	
	
	
	@Override
	public void paintComponent (final Graphics graphics) {
		renderBounds.setLocation (borderInsets.left, borderInsets.top);
		
		if (obj instanceof Collection) {	
			final Collection<Object> collection = (Collection<Object>)obj;
			final Iterator<Object> iter = collection.iterator();
			while (iter.hasNext()) {
				final Object obj = iter.next();
				renderBounds.setSize (getObjectPreferredSize (obj));
				paintRenderer (graphics, obj, renderBounds);
				renderBounds.translate (renderBounds.width + 10, 0);
			}
		}
		else {
			renderBounds.setSize (getObjectPreferredSize (obj));
			paintRenderer (graphics, obj, renderBounds);
		}
	}
	
	
	abstract public void setToolTipObject (Object obj, int row, int column);
	
	abstract public Dimension getObjectPreferredSize (Object obj);

	abstract public void paintRenderer (Graphics graphics, Object obj, Rectangle bounds);
	
	public final void setSorter (Comparator sorter) {
		this.sorter = sorter;
	}
	
	public final Comparator getSorter () { return sorter; }
}
