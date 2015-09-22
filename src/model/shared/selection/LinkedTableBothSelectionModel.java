package model.shared.selection;

import javax.swing.JTable;


public class LinkedTableBothSelectionModel {

	LinkedTableRowSelectionModel lrm;
	LinkedTableColumnSelectionModel lcm;
	
	public LinkedTableBothSelectionModel () {
		lrm = new LinkedTableRowSelectionModel ();
		lcm = new LinkedTableColumnSelectionModel ();
	}
	
	public void addJTable (final JTable table) {
		lrm.addJTable (table);
		lcm.addJTable (table);
	}

	public void removeJTable (final JTable table) {
		lrm.removeJTable (table);
		lcm.removeJTable (table);
	}
}
