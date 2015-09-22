package swingPlus.shared;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.View;

import swingPlus.shared.tooltip.AbstractRendererToolTip;
import swingPlus.shared.tooltip.DefaultTableCellToolTipListener;
import util.GraphicsUtil;

public class ScaledTable extends JTableST implements ScaledComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6168621613250583947L;

	protected ComponentScaler compScaler = new ComponentScaler ();
	protected AbstractRendererToolTip rToolTip;
	// used to receive new scale settings from other sources
	protected transient PropertyChangeListener scalePropertyListener = new ScalePropertyListener ();
	
    public ScaledTable () {
        this (null, null, null);
    }

    public ScaledTable (final TableModel tableModel) {
        this (tableModel, null, null);
    }

    public ScaledTable (final TableModel tableModel, final TableColumnModel columnModel) {
        this (tableModel, columnModel, null);
    }

    public ScaledTable (final TableModel tableModel, final TableColumnModel columnModel, final ListSelectionModel selectionModel) {
    	super (tableModel, columnModel, selectionModel);
    	
    	final MouseAdapter toolTipListener = new DefaultTableCellToolTipListener ();
		this.addMouseListener (toolTipListener);
		this.addMouseMotionListener (toolTipListener);
		this.setRendererToolTip (new DefaultRendererToolTip (this));
    }
	
    
    
	@Override
	protected void processMouseEvent (final MouseEvent mEvent) {
		super.processMouseEvent (compScaler.scaleMouseEvent (mEvent));
	}
	
	
	@Override
	protected void processMouseMotionEvent (final MouseEvent mEvent) {
		super.processMouseMotionEvent (compScaler.scaleMouseEvent (mEvent));
	}
	
	
    /**
     * Returns the tooltip location in this component's coordinate system.
     * If <code>null</code> inputStream returned, Swing will choose a location.
     * The default implementation returns <code>null</code>.
     *
     * @param mEvent  the <code>MouseEvent</code> that caused the
     *		<code>ToolTipManager</code> to show the tooltip
     * @return always returns <code>null</code>
     */
	@Override
    public Point getToolTipLocation (final MouseEvent mEvent) {
    	//return event.getPoint ();
		final Point toolTipPoint = GraphicsUtil.scalePoint (mEvent.getPoint(), compScaler.getScaleX(), compScaler.getScaleY());
        toolTipPoint.y += 20;
        toolTipPoint.x += 5;
        return toolTipPoint;
        
    }
    
    @Override
    public JToolTip createToolTip() {
    	return rToolTip;
    } 
    
	public void setRendererToolTip (final AbstractRendererToolTip newToolTip) {
		rToolTip = newToolTip;
	}
    
	
	@Override
    public void paintComponent (final Graphics graphics) {	
		compScaler.scaleGraphics (graphics);
		super.paintComponent (graphics);
		compScaler.unscaleGraphics (graphics);
    }

	
	
	@Override
	public ComponentScaler getComponentScaler() {
		return compScaler;
	}

	@Override
	public void setComponentScaler (final ComponentScaler newCompScaler) {
		this.compScaler = newCompScaler;
	}
	
	
	
	class ScalePropertyListener implements PropertyChangeListener {
		@Override
		public void propertyChange (final PropertyChangeEvent propEvent) {
			if (propEvent != null && "masterScale".equals (propEvent.getPropertyName())) {
				final Point2D newScale = (Point2D)propEvent.getNewValue();
				getComponentScaler().setScale (newScale);
			}
		}
	}
	
	
    protected class DefaultRendererToolTip extends AbstractRendererToolTip {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5345537326112031940L;
    	
		protected final JLabel label = new JLabel ();
    	protected final FontRenderContext frc = new FontRenderContext (null, false, false);
    	protected final Dimension labelSize = new Dimension ();
    	
		public DefaultRendererToolTip (final JComponent jComponent) {
			super (jComponent);
			label.setBorder (BorderFactory.createEmptyBorder (1, 3, 1, 3));
			label.setOpaque (true);
			this.add (label);
		}

		@Override
		public void setToolTipObject (final Object obj, final int row, final int column) {
	        if ((column != -1) && (row != -1)) {
	            final TableCellRenderer renderer = getCellRenderer (row, column);
	            final Component component = prepareRenderer (renderer, row, column);
	            if (component instanceof JComponent) {
	            	label.setText (((JComponent)component).getToolTipText());
	            } 
	        } 
	        
	        if (label.getText() == null) {
	        	label.setText (obj.toString());	
	        }
	        
	        adjustText (obj, row, column);
	        
	        
	        if (label.getText() != null) {
	        	final Insets insets = label.getInsets ();
	    	    final View htmlView = (View) label.getClientProperty ("html");
	    	    if (htmlView != null) {    	    	
	    			labelSize.setSize ((int)htmlView.getPreferredSpan(View.X_AXIS) + insets.left + insets.right + 6, 
	    				(int)htmlView.getPreferredSpan(View.Y_AXIS) + insets.top + insets.bottom);
	    	    }
	    	    else {
					final Rectangle2D bounds = label.getFont().getStringBounds (label.getText(), frc);
					labelSize.setSize (
						(int)bounds.getWidth() + 4 + insets.left + insets.right + 1,
						(int)bounds.getHeight() + insets.top + insets.bottom
					);
	    	    }
	
				this.setPreferredSize (labelSize);
	        }
		}
		
		
		/**
		 * Opportunity to add extras to text tooltip after being populated with initial object tooltiptext
		 * @param obj
		 * @param row
		 * @param column
		 */
		protected void adjustText (final Object obj, final int row, final int column) {
			// EMPTY
		}
		
		@Override
		public void setTitle () {
			// EMPTY
		}

		@Override
		public Dimension getObjectPreferredSize (final Object obj) {
			return curSize;
		}


		@Override
		public void paintRenderer (final Graphics graphics, final Object obj, final Rectangle bounds) {
			// EMPTY
		}
    }
}
