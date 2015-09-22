package swingPlus.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import swingPlus.shared.tooltip.AbstractRendererToolTip;

import model.graph.Edge;

/**
 * @author Martin Graham
 * @version 1.01 03/03/09
 *
 * A tooltip class that uses a JTable and its renderers to produce variable tooltips
 * depending on the Object is it passed as an argument
 */
public class GraphRendererToolTip extends AbstractRendererToolTip {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1462349250142881631L;
	
	private final static Logger LOGGER = Logger.getLogger (GraphRendererToolTip.class);
	
	
	JGraph graph;
	Map<Class<?>, GraphCellRenderer> renderMap = new HashMap <Class<?>, GraphCellRenderer> ();
	
	private final Border lineBorder = BorderFactory.createLineBorder (Color.black, 3);
	private final Border titleBorder = BorderFactory.createTitledBorder (lineBorder, "ToolTip");

	
	public GraphRendererToolTip (final JGraph graph) {
		super (graph);
		this.graph = graph;
		setBorder (titleBorder);
	}
	
	
	@Override
	protected void setTitle () {
		
		if (this.getBorder() instanceof TitledBorder) {
			if (obj instanceof Collection) {
				final Iterator<?> iter = ((Collection<?>)obj).iterator();
				obj = iter.next();
			}
			if (obj instanceof Edge) {
				final TitledBorder tBorder = (TitledBorder)this.getBorder();
				tBorder.setTitle (((Edge)obj).getNode2().toString());
			}
			else if (obj != null) {
				final TitledBorder tBorder = (TitledBorder)this.getBorder();
				tBorder.setTitle (obj.toString());
			}
		}
	}
	
	
	@Override
	public Dimension getObjectPreferredSize (final Object objArg) {

		Dimension cellSize = oneCellSize;
		
		if (objArg != null) {
			final Object object = (objArg instanceof Edge ? ((Edge)objArg).getEdgeObject() : objArg);
			final GraphCellRenderer gcr = getRenderer (object.getClass());
			if (gcr != null) {
				gcr.getGraphCellRendererComponent (graph, objArg, true, false);
			}
			
			if (gcr instanceof JComponent) {
				cellSize = ((JComponent)gcr).getPreferredSize();
				if (cellSize.width > 1000 || cellSize.height > 1000) {
					cellSize = oneCellSize;
				}
			}
		}
		
		return cellSize;
	}
	
	
	public void addRenderer (final Class<?> klass, final GraphCellRenderer gcr) {
		renderMap.put (klass, gcr);
	}
	
	public GraphCellRenderer getRenderer (final Class<?> klass) {
		GraphCellRenderer gcr = renderMap.get (klass);

		if (gcr == null) {	
			final GraphCellRenderer gcrGraph = graph.getDefaultNodeRenderer (klass);
			
			if (gcrGraph != null) {
				try {
					gcr = gcrGraph.getClass().newInstance();
					addRenderer (klass, gcr);
				} catch (InstantiationException e) {
					LOGGER.error ("No zero argument constructor for "+gcrGraph.getClass()+", try copy constructor next", e);
					try {
						final Constructor<? extends GraphCellRenderer> constructor =
							gcrGraph.getClass().getConstructor (GraphCellRenderer.class);
						gcr = constructor.newInstance (gcrGraph);
						addRenderer (klass, gcr);
						LOGGER.info ("Copy constructor for "+gcrGraph.getClass()+"(gcrGraph) successful.");
					} catch (SecurityException se) {
						LOGGER.error (se.toString(), se);
					} catch (NoSuchMethodException nsme) {
						LOGGER.error (nsme.toString(), nsme);
					} catch (IllegalArgumentException iae) {
						LOGGER.error (iae.toString(), iae);
					} catch (InstantiationException ie) {
						LOGGER.error (ie.toString(), ie);
					} catch (IllegalAccessException iae) {
						LOGGER.error (iae.toString(), iae);
					} catch (InvocationTargetException ine) {
						LOGGER.error (ine.toString(), ine);
					}
					
				} catch (IllegalAccessException e) {
					LOGGER.error (e.toString(), e);
				}
			}
		}
		
		return gcr;
	}
	
	
	public void setToolTipObject (final Object objArg, final int row, final int column) {
		setToolTipObject (objArg);
	}
	
	
	@Override
	public void paintRenderer (final Graphics graphics, final Object objArg, final Rectangle bounds) {
		final GraphCellRenderer gcr = (objArg == null ? null : getRenderer (objArg.getClass()));
		if (gcr != null) {
			final Component comp = gcr.getGraphCellRendererComponent (graph, obj, true, false);
			LOGGER.debug ("painting tooltip");
			//if (c instanceof JComponent) {
			//	((JComponent)c).setBorder(null);
			//}
			if (comp instanceof Container && ((Container)comp).getComponentCount() > 0) {
				pane.paintComponent (graphics, comp, this, bounds.x, bounds.y, bounds.width, bounds.height, true);
			} else {
				pane.paintComponent (graphics, comp, this, bounds);
			}
		}
	}
};