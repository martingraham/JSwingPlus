package model.pivottable;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import model.shared.TableUtils;

import org.apache.log4j.Logger;


public class SimplePivotTableModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8300489761673614207L;
	final static Logger LOGGER = Logger.getLogger (SimplePivotTableModel.class);
	
	
	TableModel rawTableModel;
	TableModelListener rawTableModelListener;
	int columnXIndex, columnYIndex, columnDataIndex;
	AbstractPivotDataAggregator aggregator;
	double min, max;
	Map<Object, Integer> colIndexMap, rowIndexMap;
	PropertyChangeSupport propertySupport;

	
	public SimplePivotTableModel (final TableModel tableModel) {
		super ();
		
		propertySupport = new PropertyChangeSupport (this);
		
		rawTableModel = tableModel;
		rawTableModelListener = new RawTableModelListener ();
		rawTableModel.addTableModelListener (rawTableModelListener);
		
		columnXIndex = columnYIndex = columnDataIndex = -1;
		this.getColumnCount();
	}
	
	
	public int getColumnXIndex () { return columnXIndex; }

	public void setColumnXIndex (final int columnXIndex) { 
		final int oldIndex = this.columnXIndex;
		this.columnXIndex = columnXIndex;
		if (oldIndex != columnXIndex) {
			recalculateModel ();
		}
	}

	
	public int getColumnYIndex () { return columnYIndex; }

	public void setColumnYIndex (final int columnYIndex) {
		final int oldIndex = this.columnYIndex;
		this.columnYIndex = columnYIndex;
		if (oldIndex != columnYIndex) {
			recalculateModel ();
		}
	}

	
	public int getColumnDataIndex () { return columnDataIndex; }

	public void setColumnDataIndex (final int columnDataIndex) {
		final int oldIndex = this.columnDataIndex;
		this.columnDataIndex = columnDataIndex;
		if (oldIndex != columnDataIndex && aggregator != null) {
			fillTable ();
		}
	}
	
	
	public AbstractPivotDataAggregator getAggregator () { return aggregator; }

	public void setAggregator (final AbstractPivotDataAggregator newAggregator) {
		final AbstractPivotDataAggregator oldAggregator = aggregator;
		this.aggregator = newAggregator;
		if (oldAggregator != newAggregator) {
			fillTable ();
		}
	}
	
	

	
	public void recalculateModel () {
		if (columnXIndex >= 0 && columnYIndex >= 0) {
			reconstructTable ();
			
			if (columnDataIndex >= 0) {
				fillTable ();
			}
			//TableUtils.printTableModel (this);
		}
	}
	

	public void reconstructTable () {
		final Set<Object> columnX = categorise (columnXIndex, getRawTableData());
		final Set<Object> columnY = categorise (columnYIndex, getRawTableData());
		final List<Object> columnXList = new ArrayList<Object> (columnX);
		final List<Object> columnYList = new ArrayList<Object> (columnY);
		fillAxes (columnXList, columnYList);
		colIndexMap = makeObjectIndex (columnXList);
		rowIndexMap = makeObjectIndex (columnYList);
	}
	
	
	public Set<Object> categorise (final int columnIndex, final TableModel tableModel) {
		final Set<Object> items = new HashSet<Object> ();
		for (int row = tableModel.getRowCount(); --row >= 0; ) {
			final Object item = tableModel.getValueAt (row, columnIndex);
			if (!items.contains (item)) {
				items.add (item);
			}
		}
		LOGGER.info ("column: "+tableModel.getColumnName(columnIndex)+"\titems: "+items);
		return items;
	}
	
	
	protected void fillAxes (final List<Object> columnXList, final List<Object> columnYList) {
		
		// Add one column as column headers in pivot table
		final List<Object> columnHeaderList = new ArrayList<Object> ();
		columnHeaderList.add ("Dimensions");
		columnHeaderList.addAll (columnXList);
		final Object[] columnHeaders = columnHeaderList.toArray();
		
		// Add other column as first row
		final Object[][] firstRowDataVector = new Object [columnYList.size()][1];
		for (int row = 0; row < columnYList.size(); row++) {
			firstRowDataVector [row][0] = columnYList.get (row);
		}
		
		this.setDataVector (firstRowDataVector, columnHeaders);
	}
	

	
	protected Map<Object,Integer> makeObjectIndex (final List<Object> itemList) {
		final Map<Object,Integer> objectIndex = new HashMap<Object,Integer> ();
		for (int index = itemList.size(); --index >= 0;) {
			objectIndex.put (itemList.get (index), Integer.valueOf (index));
		}
		return objectIndex;
	}
	
	
	protected void fillTable () {
		final double[][] pivotData = aggregator.aggregate (this);
		fillTable (pivotData);
	}
	
	
	protected void fillTable (final double[][] data) {
		final Point2D oldMinMax = new Point2D.Double (getMin(), getMax());
		
		setMin (Double.MAX_VALUE);
		setMax (Double.NEGATIVE_INFINITY);
		for (int colIndex = data.length; --colIndex >= 0; ) {
			for (int rowIndex = data[0].length; -- rowIndex >= 0; ) {
				final double value = data[colIndex][rowIndex];
				this.setValueAt (Double.valueOf (value), rowIndex, colIndex + 1);
				if (value > getMax()) {
					setMax (value);
				}
				if (value < getMin()) {
					setMin (value);
				}
			}
		}
		
		final Point2D newMinMax = new Point2D.Double (getMin(), getMax());
		
		propertySupport.firePropertyChange ("minmax", oldMinMax, newMinMax);
	}
	
	
	public double getMin() { return min; }

	public double getMax() { return max; }

	protected void setMin (final double min) {
		this.min = min;
	}

	protected void setMax (final double max) {
		this.max = max;
	}


	@Override
	public Class<?> getColumnClass (final int columnIndex) {
		if (columnIndex == 0) {
			return getRawTableData().getColumnClass (columnYIndex);
		}
		
		return Double.class;
	}
	
	
	public Object getColumnObject (final int columnIndex) {
		return columnIdentifiers.get (columnIndex);
	}
	
	
	
	public Map<Object, Integer> getColIndexMap() {
		return colIndexMap;
	}


	public Map<Object, Integer> getRowIndexMap() {
		return rowIndexMap;
	}
	
	
	public TableModel getRawTableData () {
		return rawTableModel;
	}
	
	
	public PropertyChangeSupport getPropertyChangeSupport () {
		return propertySupport;
	}
	
	
	public void addPropertyChangeListener (final PropertyChangeListener renderer) {
		getPropertyChangeSupport().addPropertyChangeListener (renderer);
		propertySupport.firePropertyChange ("minmax", null, new Point2D.Double (getMin(), getMax()));
	}
	
	
	
	class RawTableModelListener implements TableModelListener {
		@Override
		public void tableChanged (final TableModelEvent tmEvent) {
			if (tmEvent != null) {
				final int column = tmEvent.getColumn();
				
				if (column == TableModelEvent.ALL_COLUMNS
					|| column == columnXIndex
					|| column == columnYIndex
					|| column == columnDataIndex) {
					
					if (column != columnDataIndex) {
						reconstructTable ();
					}
					
					fillTable ();
				}
			}
		}
	}
}
