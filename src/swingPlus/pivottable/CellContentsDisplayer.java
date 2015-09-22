package swingPlus.pivottable;

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import util.swing.CloseableTabComponent;

import model.pivottable.SimplePivotTableModel;

public class CellContentsDisplayer implements ListSelectionListener {

	JTabbedPane tabbedPane;
	JPivotTable pivotTable;
	
	public CellContentsDisplayer (final JPivotTable pivotTable, final JTabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
		this.pivotTable = pivotTable;
	}
	
	@Override
	public void valueChanged (final ListSelectionEvent lsEvent) {
		
		if (!pivotTable.getSelectionModel().getValueIsAdjusting() &&
				!pivotTable.getColumnModel().getSelectionModel().getValueIsAdjusting()
				&& pivotTable.getMousePosition() != null) {
		//if (! lsEvent.getValueIsAdjusting() ) {
			final SimplePivotTableModel pivotTableModel = (SimplePivotTableModel)pivotTable.getModel ();
			
			final JTable jTable = createPartTable ();
			final Integer[] colIndices = {pivotTableModel.getColumnXIndex(), pivotTableModel.getColumnYIndex(),
					pivotTableModel.getColumnDataIndex()};
			final List<Integer> colIndList = Arrays.asList (colIndices);
			final TableCellRenderer shader = new ColumnShaderRenderer ();
			final TableColumnModel columnModel = jTable.getColumnModel();
	
			for (int col = 0; col < columnModel.getColumnCount(); col++) {
				final TableColumn column = columnModel.getColumn (col);
				final int mindex = column.getModelIndex();
				if (colIndList.contains (Integer.valueOf (mindex))) {
					column.setCellRenderer (shader);
					column.setHeaderRenderer (shader);
				}
			}
			
			CloseableTabComponent.addCloseableTab (tabbedPane, new TableTab (jTable), "Partial Table", null);
			//tabbedPane.addTab (, new TableTab (jTable));
		}
	}
	

	
	
	static class ColumnShaderRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 262938127985439486L;
		Border border = BorderFactory.createBevelBorder (BevelBorder.RAISED);
		
		
		@Override
	    public Component getTableCellRendererComponent (final JTable table, final Object value,
	    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {
			 final Component comp = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
			 comp.setBackground (Color.yellow);
			 setBorder (border);
			 return comp;
		}
	}



	public JTable createPartTable () {
		
		final SimplePivotTableModel pivotTableModel = (SimplePivotTableModel)pivotTable.getModel();
		final int[] rowSelection = pivotTable.getSelectedRows();
		final int[] columnSelection = pivotTable.getColumnModel().getSelectedColumns();
		
		final Set<Object> rowObjects = new HashSet<Object> ();
		final Set<Object> columnObjects = new HashSet<Object> ();
		
		for (int selectedRow = rowSelection.length; --selectedRow >= 0;) {
			final int mrow = pivotTable.convertRowIndexToModel (rowSelection [selectedRow]);
			final Object rowVal = pivotTableModel.getValueAt (mrow, 0);
			rowObjects.add (rowVal);
		}
		
		for (int selectedColumn = columnSelection.length; --selectedColumn >= 0;) {
			final int mcol = pivotTable.convertColumnIndexToModel (columnSelection [selectedColumn]);
			final Object columnVal = pivotTableModel.getColumnObject (mcol);
			columnObjects.add (columnVal);
		}
		
		
		final DefaultTableModel rawTableModel = (DefaultTableModel)pivotTableModel.getRawTableData();
		final int rowCol = pivotTableModel.getColumnYIndex();
		final int colCol = pivotTableModel.getColumnXIndex();
		
		final DefaultTableModel dtm = new DefaultTableModel ();
		
		for (int rawcol = 0; rawcol < rawTableModel.getColumnCount(); rawcol++) {
			dtm.addColumn (rawTableModel.getColumnName(rawcol));
		}
		
		for (int rawrow = 0; rawrow < rawTableModel.getRowCount(); rawrow++) {
			if (rowObjects.contains (rawTableModel.getValueAt (rawrow, rowCol))
					&& columnObjects.contains (rawTableModel.getValueAt (rawrow, colCol))) {
				dtm.addRow ((Vector)rawTableModel.getDataVector().get(rawrow));
				//System.err.println ("match at row: "+rawrow);
			}
		}

		return new JTable (dtm);
	}
}
