package swingPlus.scatterplot;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.PanelUI;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import model.shared.SortedTableColumn;

import swingPlus.shared.CrossFilteredTable;
import ui.ScatterPlotUI;



public class JScatterPlot extends JPanel implements TableModelListener, 
					ListSelectionListener, PropertyChangeListener, TableCellRenderer { 
	/**
	 * 
	 */
	private static final long serialVersionUID = -7680885888229040680L;
	private final static Logger LOGGER = Logger.getLogger (JScatterPlot.class);
	private final static String UICLASSID = "ScatterPlotUI";
	static {
		UIManager.put (UICLASSID, "ui.ScatterPlotUI");
	}
	
	JTable slaveTable;
	SortedTableColumn<?> xAxis, yAxis;
	int alpha = 128;
	boolean replottingNecessary;




	public JScatterPlot () {
		this (null);
	}
	
	public JScatterPlot (final JTable table) {
		setSlaveTable (table);
	}

	@Override
    public void updateUI() {
		setUI((PanelUI)UIManager.getUI(this));
    }

    @Override
	public ScatterPlotUI getUI() {
        return (ScatterPlotUI)ui;
    }

    @Override
    public String getUIClassID() {
        return UICLASSID;
    }
    
    
	
	public final JTable getSlaveTable() {
		return slaveTable;
	}

	public final void setSlaveTable (final JTable slaveTable) {
		if (this.slaveTable != slaveTable) {
		
			if (slaveTable != null) {
				slaveTable.removePropertyChangeListener ("model", this);
				slaveTable.removePropertyChangeListener ("selectionModel", this);
				slaveTable.removePropertyChangeListener ("uberSelectionModel", this);
				removeOldTableModelListener (slaveTable.getModel ());
				removeOldListSelectionListener (slaveTable.getSelectionModel());
				if (slaveTable instanceof CrossFilteredTable) {
					removeOldListSelectionListener (((CrossFilteredTable)slaveTable).getUberSelection());
				}
			}
			
			this.slaveTable = slaveTable;
			
			if (slaveTable != null) {
				slaveTable.addPropertyChangeListener ("model", this);
				slaveTable.addPropertyChangeListener ("selectionModel", this);
				slaveTable.addPropertyChangeListener ("uberSelectionModel", this);
				addNewTableModelListener (slaveTable.getModel());
				addNewListSelectionListener (slaveTable.getSelectionModel());
				if (slaveTable instanceof CrossFilteredTable) {
					addNewListSelectionListener (((CrossFilteredTable)slaveTable).getUberSelection());
				}
			}
		}
	}
	

    @Override
	public void tableChanged (final TableModelEvent event) {
    	resizeAndRepaint ();
    }
    
    
	@Override
	public void valueChanged (final ListSelectionEvent event) {	
		//System.out.println (event.getSource().hashCode()+" event");
		if (!event.getValueIsAdjusting()) {
			resizeAndRepaint ();
		}	
	}
    
	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if ("model".equals (evt.getPropertyName())) {
			removeOldTableModelListener ((TableModel)evt.getOldValue());
			addNewTableModelListener ((TableModel)evt.getNewValue());
			setxAxis ((SortedTableColumn<?>) slaveTable.getColumnModel().getColumn (0));
			setyAxis ((SortedTableColumn<?>) slaveTable.getColumnModel().getColumn (1));
		}
		else if ("selectionModel".equals (evt.getPropertyName())
				|| "uberSelectionModel".equals (evt.getPropertyName()) ) {
			removeOldListSelectionListener ((ListSelectionModel)evt.getOldValue());
			addNewListSelectionListener ((ListSelectionModel)evt.getNewValue());
		}
	}
	
	void removeOldTableModelListener (final TableModel oldModel) {
		if (oldModel != null){
			oldModel.removeTableModelListener (this);
		}
	}
	
	void addNewTableModelListener (final TableModel newModel) {
		if (newModel != null) {
			newModel.addTableModelListener (this);
		}
	}
	
	void removeOldListSelectionListener (final ListSelectionModel oldModel) {
		if (oldModel != null){
			oldModel.removeListSelectionListener (this);
		}
	}
	
	void addNewListSelectionListener (final ListSelectionModel newModel) {
		if (newModel != null) {
			newModel.addListSelectionListener (this);
		}
	}
	
	public final SortedTableColumn getxAxis() {
		return xAxis;
	}

	public final SortedTableColumn getyAxis() {
		return yAxis;
	}

	public final void setxAxis (final SortedTableColumn<?> xAxis) {
		setReplottingNecessary (true);
		this.firePropertyChange ("xAxis", this.xAxis, xAxis);
		this.xAxis = xAxis;
	}

	public final void setyAxis (final SortedTableColumn<?> yAxis) {
		setReplottingNecessary (true);
		this.firePropertyChange ("yAxis", this.yAxis, yAxis);
		this.yAxis = yAxis;
	}
	
	
	
    public void resizeAndRepaint () {
    	setReplottingNecessary (true);
        repaint();
    }

	
	public final int getAlpha() {
		return alpha;
	}

	public final void setAlpha (final int alpha) {
		this.alpha = alpha;
	}

	public final boolean isReplottingNecessary() {
		return replottingNecessary;
	}

	public final void setReplottingNecessary (final boolean replottingNecessary) {
		this.replottingNecessary = replottingNecessary;
	}

	@Override
	public Component getTableCellRendererComponent (final JTable table, final Object value,
			final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		
		setAlpha (255);
		setSlaveTable (table);
		setxAxis ((SortedTableColumn<?>) table.getColumnModel().getColumn (column));
		setyAxis ((SortedTableColumn<?>) table.getColumnModel().getColumn (row));
		return this;
	}
}
