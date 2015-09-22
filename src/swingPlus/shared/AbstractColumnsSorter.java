package swingPlus.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

	/**
	 * Class that sorts columns according to either values in a particular row
	 * that cuts across all columns (via sort (int viewRow)) or by a set of
	 * comparators (MultiComparator) that compare properties of the objects that
	 * represent each column overall (via sort (Comparator<Object> objComp))
	 * @author cs22
	 *
	 */
public abstract class AbstractColumnsSorter implements Comparator<TableColumn> {

		
    protected List<TableColumn> viewToModel;

    /**
     * model -> view (JTable)
     */
    protected int modelRow;
	//protected ColumnsSorter2 cs2 = new ColumnsSorter2 ();
    protected JTable jTable;
	
	
	public AbstractColumnsSorter (final JTable jTable) {
		this.jTable = jTable;
	}
	
    public void sort (final int viewRow, final Comparator<TableColumn> comp) {
		final TableColumnModel tcm = jTable.getColumnModel();
		//Enumeration<TableColumn> columns = tcm.getColumns();
		viewToModel = new ArrayList<TableColumn> (tcm.getColumnCount());
		if (viewRow >= 0) {
			modelRow = jTable.convertRowIndexToModel (viewRow);
		}
		
		for (int n = 0; n < jTable.getColumnCount(); n++) {
			viewToModel.add (tcm.getColumn (n));
		}
		
		Collections.sort (viewToModel, comp);
		
		rearrangeColumns (tcm, viewToModel);
    }
    
    
    protected void rearrangeColumns (final TableColumnModel columnModel, final List<TableColumn> columnOrder) {
    	 	
    	for (int n = columnModel.getColumnCount(); --n >= 0;) {
    		//System.err.println ("remove: "+n+", column: "+columnModel.getColumn(n));
    		columnModel.removeColumn (columnModel.getColumn(n));
		}
		
		for (int n = 0; n < viewToModel.size(); n++) {
			columnModel.addColumn (viewToModel.get (n));
			//System.err.println ("add: "+n+", column: "+columnModel.getColumn(n));
		}
    }
    
   // public void sort (Comparator<Object> objComp) {
   // 	cs2.setComparator (objComp);
   // 	sort (-1, cs2);
   // }
    
	public void sort (final int viewRow) {
		sort (viewRow, this);
	}

	@Override
	abstract public int compare (final TableColumn tc1, final TableColumn tc2);
}
