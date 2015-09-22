package example.tablelist.renderers;

import java.awt.Component;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import model.pivottable.SimplePivotTableModel;
import swingPlus.shared.JColumnHeader;
import swingPlus.shared.JRowHeader;

public class LabelSubTableCellRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2852935264793627807L;
	Map<String, Map<Object, String>> labelMap;
	SimplePivotTableModel pivotModel;
	
	
	public LabelSubTableCellRenderer (final SimplePivotTableModel pivotModel,
			final Map<String, Map<Object, String>> labelMap) {
		super ();
		this.pivotModel = pivotModel;
		this.labelMap = labelMap;
	}
	
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {
 
    	final Component comp = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
    	final int rawColumnIndex = (table instanceof JRowHeader) ? pivotModel.getColumnYIndex() : pivotModel.getColumnXIndex ();
    	final String rawColumnName = pivotModel.getRawTableData().getColumnName (rawColumnIndex);
    	
    	final Map<Object, String> columnLabelMap = labelMap.get (rawColumnName);
    	
    	
    	if (columnLabelMap != null && value != null) {
    		final String label;
    		if (table instanceof JColumnHeader) {
    			final String columnName = table.getModel().getColumnName (table.convertColumnIndexToModel (column));
         		label = columnLabelMap.get (columnName);
        	} else {
        		label = columnLabelMap.get (value.toString());
        	}
    		if (label != null) {
    			this.setValue (label);
    			this.setToolTipText (label);
    		}
    	}
    
    	return comp;
    }
}