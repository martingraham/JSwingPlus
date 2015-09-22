package swingPlus.pivottable;


import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import swingPlus.shared.AbstractColumnsSorter;
import swingPlus.shared.ColumnSortableTable;
import swingPlus.shared.ComparableObjectColumnsSorter;
import swingPlus.shared.JBasicMatrix;


public class JPivotTable extends JBasicMatrix implements ColumnSortableTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	AbstractColumnsSorter colSort;

	public JPivotTable (final TableModel tableModel) {
		super (tableModel);
		colSort = new ComparableObjectColumnsSorter (this);
		axesLabel.setText ("Axes");
	}
	
	public AbstractColumnsSorter getColumnsSorter () {
		return colSort;
	}

	
	
	@Override
	public void tableChanged (final TableModelEvent tmEvent) {
		super.tableChanged (tmEvent);
		
		if (tmEvent.getFirstRow() == TableModelEvent.HEADER_ROW) {
			final TableColumnModel columnModel = this.getColumnModel();
			if (columnModel != null && columnModel.getColumnCount() > 0) {
				final TableColumn firstColumn = columnModel.getColumn(0);
				if (firstColumn.getModelIndex() == 0) {
					columnModel.removeColumn (firstColumn);
				}
			}
		}
	}
	
	@Override
	public void setColumnModel (final TableColumnModel columnModel) {
		super.setColumnModel (columnModel);
		if (columnModel != null && columnModel.getColumnCount() > 0) {
			final TableColumn firstColumn = columnModel.getColumn(0);
			if (firstColumn.getModelIndex() == 0) {
				columnModel.removeColumn (firstColumn);
			}
		}
		//System.err.println ("colModel: "+columnModel);
	}
    
	
	
	@Override
	public boolean isCellEditable (final int row, final int column) {
		return false;
	}
}