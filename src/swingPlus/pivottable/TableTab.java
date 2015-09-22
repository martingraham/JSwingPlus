package swingPlus.pivottable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class TableTab extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5686719198372148517L;

	public TableTab (final JTable jTable) {
		super ();
		final JScrollPane newTablePane = new JScrollPane ();
		newTablePane.setViewportView (jTable);
		this.add (newTablePane, "Center");
		this.setVisible (true);
	}
}
