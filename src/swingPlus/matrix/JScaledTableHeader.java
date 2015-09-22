package swingPlus.matrix;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import model.graph.Edge;
import model.matrix.MatrixTableModel;
import swingPlus.shared.ComponentScaler;
import swingPlus.shared.ScaledComponent;
import swingPlus.shared.tooltip.DefaultTableCellToolTipListener;
import ui.SelectTableHeaderUI;
import util.GraphicsUtil;
import util.colour.ColorUtilities;

public class JScaledTableHeader extends JTableHeader implements ScaledComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2072816164879802811L;
	ComponentScaler compScaler = new ComponentScaler (true, false);
	protected PropertyChangeListener scalePropertyListener = new ScalePropertyListener ();
	MatrixRendererToolTip rToolTip;
	
	public JScaledTableHeader (final TableColumnModel columnModel) {
		super (columnModel);
		
		this.setBackground (new Color (224, 224, 224));
		this.setDefaultRenderer (new ColumnHeaderRenderer ());	

		final MouseAdapter mAdapter = new DefaultTableCellToolTipListener ();
		this.addMouseListener (mAdapter);
		this.addMouseMotionListener (mAdapter);
		
		columnModel.getSelectionModel().addListSelectionListener(
			new ListSelectionListener () {
				@Override
				public void valueChanged (final ListSelectionEvent lse) {
					resizeAndRepaint();
				}	
			}
		);
	}
	
	@Override
	public void setTable (final JTable jTable) {
		if (getTable() != null) {
			getTable().removePropertyChangeListener ("masterScale", scalePropertyListener);
		}
		if (jTable != null) {
			jTable.addPropertyChangeListener ("masterScale", scalePropertyListener);
		}
		
		super.setTable (jTable);
		rToolTip = new MatrixRendererToolTip (jTable);
	}
	
	@Override
    public void updateUI() {
        ui = SelectTableHeaderUI.createUI (this);
        ui.installUI (this);
        invalidate();
    }

    @Override
	public SelectTableHeaderUI getUI() {
        return (SelectTableHeaderUI)ui;
    }

    @Override
    public String getUIClassID() {
        return "SelectTableHeaderUI";
    }


	@Override
	protected void processMouseEvent (final MouseEvent mEvent) {
		super.processMouseEvent (compScaler.scaleMouseEvent (mEvent));
	}
	
	@Override
	protected void processMouseMotionEvent (final MouseEvent mEvent) {
		super.processMouseMotionEvent (compScaler.scaleMouseEvent (mEvent));
	}
	
	@Override
    public void paintComponent (final Graphics graphics) {	
		compScaler.scaleGraphics (graphics);
		super.paintComponent (graphics);
		compScaler.unscaleGraphics (graphics);
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
        Point point = GraphicsUtil.scalePoint (mEvent.getPoint(), getComponentScaler().getScaleX(), getComponentScaler().getScaleY());
        point.y += 20;
        return point;
    }
	
    @Override
    public JToolTip createToolTip() {
    	return rToolTip;
    } 
	
	public ComponentScaler getComponentScaler() {
		return compScaler;
	}

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
	
	public class ColumnHeaderRenderer extends DefaultTableCellRenderer {
	    /**
		 * 
		 */
		private static final long serialVersionUID = -7106757247460619266L;

		Font[] fonts = new Font [8];
		int fontCutoff = 7;
		int edgeSetSize = 0;
		Color edgeSizeColour = ColorUtilities.mixColours (Color.blue, Color.white, 0.35f);
		Color selectionBackground = new Color (255, 255, 128, 160);
		
		public ColumnHeaderRenderer () {
			super ();
			this.setBorder (BorderFactory.createMatteBorder (0, 0, 1, 1, new Color (128, 128, 128, 128)));
			//this.setVerticalTextPosition (SwingConstants.TOP);
			this.setVerticalAlignment (SwingConstants.TOP);
			for (int n = 0; n < fonts.length; n++) {
				fonts [n] = this.getFont().deriveFont ((float)(fontCutoff + n));
			}
		}
		

		@Override
	   public Component getTableCellRendererComponent (final JTable table, final Object value,
			   final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		   ///super.getTableCellRendererComponent (table, value, isSelected, 
			//	   hasFocus, row, column);
		   final ListSelectionModel lsm = table.getColumnModel().getSelectionModel();
		   final boolean colSelected = lsm.isSelectedIndex(column);
		   setBackground (colSelected ? selectionBackground : JScaledTableHeader.this.getBackground());
		   setValue (value);
		   final TableModel tableModel = table.getModel ();
		   if (tableModel instanceof MatrixTableModel) {
			   final MatrixTableModel mtm = (MatrixTableModel)tableModel;
			   final Object colObj = mtm.getColumnObject (table.convertColumnIndexToModel (column));
			   final Set<Edge> edgeSet = mtm.getColumnData (colObj);
			   edgeSetSize = (edgeSet == null ? 0 : edgeSet.size());
			   
		   }
		   return this;
	   }

		

		@Override
		public void paintComponent (final Graphics gContext) {
	    	
			gContext.setColor (edgeSizeColour);
			gContext.fillRect (0, this.getHeight() - (edgeSetSize / 3), this.getWidth(), edgeSetSize / 3);
			
			if (this.getHeight() > fontCutoff && this.getWidth() > 4) {
				int fontSize = this.getHeight() - fontCutoff - 2;
				fontSize = Math.max (0, Math.min (fontSize, fonts.length - 1));
				gContext.setFont (fonts [fontSize]);
				super.paintComponent (gContext);
			}
	    }
		
	    /**
	     * Returns the string to be used as the tooltip for <insets>event</insets>.
	     * By default this returns any string set using
	     * <code>setToolTipText</code>.  If a component provides
	     * more extensive API to support differing tooltips at different locations,
	     * this method should be overridden.
	     */
		@Override
	    public String getToolTipText (final MouseEvent mEvent) {
	        return getText();
	    }
	}
}
