package model.shared.selection;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;

public class SharedTableRowSelectionModel extends DefaultListSelectionModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8523167655931976578L;

	
	public int getModelRow (final JTable table, final int viewRowIndex) {
		return table.convertRowIndexToModel (viewRowIndex);
	}
	
	public int getViewRow (final JTable table, final int modelRowIndex) {
		return table.convertRowIndexToView (modelRowIndex);
	}
}
