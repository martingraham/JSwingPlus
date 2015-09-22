package swingPlus.shared;

import javax.swing.JTable;
import javax.swing.table.TableColumn;


public class ComparableObjectColumnsSorter extends AbstractColumnsSorter {


	public ComparableObjectColumnsSorter (final JTable jTable) {
		super (jTable);
	}

	@Override
	public int compare (final TableColumn tc1, final TableColumn tc2) {
		final Object obj1 = jTable.getModel().getValueAt (modelRow, tc1.getModelIndex());
		final Object obj2 = jTable.getModel().getValueAt (modelRow, tc2.getModelIndex());
		if (obj1 instanceof Comparable && obj2 instanceof Comparable) {
			if (obj1.getClass() == obj2.getClass() || obj1.getClass().isAssignableFrom (obj2.getClass())) {
				return -((Comparable)obj1).compareTo((Comparable)obj2);
			} else if (obj2.getClass().isAssignableFrom (obj1.getClass())) {
				return -((Comparable)obj2).compareTo((Comparable)obj1);
			}
		}
		return 0;
	}
}
