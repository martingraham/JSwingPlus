package swingPlus.matrix;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

import swingPlus.shared.border.OrthogonalTitlesBorder;
import swingPlus.shared.tooltip.AbstractRendererToolTip;


import model.graph.Edge;
import model.shared.EdgeComparator;

/**
 * @author Martin Graham
 * @version 1.01 03/03/09
 *
 * A tooltip class that uses a JTable and its renderers to produce variable tooltips
 * depending on the Object inputStream it passed as an argument
 */
public class MatrixRendererToolTip extends AbstractRendererToolTip {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1462349250142881631L;
	
	final static Logger LOGGER = Logger.getLogger (MatrixRendererToolTip.class);
	
	
	
	JTable table;
	int row = -1, column = -1;
	Map<Class<?>, TableCellRenderer> renderMap = new HashMap <Class<?>, TableCellRenderer> ();

	
	public MatrixRendererToolTip (final JTable table) {
		super (table);
		this.table = table;
		setSorter (EdgeComparator.getInstance());
	}
	
	@Override
	protected void setTitle () {
		
		if (this.getBorder() instanceof TitledBorder) {
			Object firstEdge = obj;
			if (firstEdge instanceof Collection) {
				final Iterator<?> iter = ((Collection<?>)obj).iterator();
				firstEdge = iter.next();
			}
			if (firstEdge instanceof Edge) {
				final TitledBorder tBorder = (TitledBorder)this.getBorder();
				tBorder.setTitle (((Edge)firstEdge).getNode2().toString());
				if (this.getBorder() instanceof OrthogonalTitlesBorder) {
					final OrthogonalTitlesBorder mBorder = (OrthogonalTitlesBorder)tBorder;
					mBorder.setSecondTitle (((Edge)firstEdge).getNode1().toString());
				}
			}

		}
	}
	
	
	@Override
	public void setToolTipObject (final Object object, final int row, final int column) {
		this.row = row;
		this.column = column;
		setToolTipObject (object);
	}
	
	
	@Override
	public Dimension getObjectPreferredSize (final Object obj) {
		if (obj == null) {
			return oneCellSize;
		}
		final Object object = (obj instanceof Edge ? ((Edge)obj).getEdgeObject() : obj);
		final TableCellRenderer tcr = getRenderer (object.getClass());
		if (tcr != null) {
			tcr.getTableCellRendererComponent (table, obj, true, false, row, column);
		}
		return (tcr instanceof JComponent) ?
				((JComponent)tcr).getPreferredSize() : oneCellSize;
	}
	
	
	public void addRenderer (final Class<?> klass, final TableCellRenderer tcr) {
		renderMap.put (klass, tcr);
	}
	
	public TableCellRenderer getRenderer (final Class<?> klass) {
		TableCellRenderer tcr = renderMap.get (klass);
		if (tcr == null) {
			final TableCellRenderer tcrTable = table.getDefaultRenderer(klass);
			if (tcrTable != null) {
				try {
					tcr = tcrTable.getClass().newInstance();
					addRenderer (klass, tcr);
				} catch (InstantiationException e) {
					//LOGGER.error (e.toString(), e);
					tcr = new DefaultTableCellRenderer ();
					addRenderer (klass, tcr);
				} catch (IllegalAccessException e) {
					//LOGGER.error (e.toString(), e);
					tcr = new DefaultTableCellRenderer ();
					addRenderer (klass, tcr);
				}
			}
		}
		
		return tcr;
	}
	
	
	
	@Override
	public void paintRenderer (final Graphics graphics, final Object obj, final Rectangle bounds) {
		Object obj2 = obj;
		if (obj2 instanceof Edge) {
			obj2 = ((Edge)obj).getEdgeObject ();
		}
		final TableCellRenderer tcr = (obj2 == null ? null : getRenderer (obj2.getClass()));
		if (tcr != null) {
			final Component comp = tcr.getTableCellRendererComponent (table, obj, true, false, row, column);
			LOGGER.debug ("painting tooltip");
			if (comp instanceof JComponent) {
				((JComponent)comp).setBorder(null);
			}
			pane.paintComponent (graphics, comp, this, bounds);
		}
	}
};