package model.parcoord;

import java.util.BitSet;

import javax.swing.RowFilter;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import model.shared.SortedTableColumn;


public class ParCoordRowSorter<M extends TableModel> extends TableRowSorter<M> {

	static final Logger LOGGER = Logger.getLogger (ParCoordRowSorter.class);
	
	TableColumnModel columnModel;
	FilterEntry filterEntry;
	//TableRowSorter;
	//DefaultRowSorter;
	
	public ParCoordRowSorter () {
		this (null, null);
	}
	
	public ParCoordRowSorter (final M model) {
		this (model, null);
	}
	
	public ParCoordRowSorter (final M model, final TableColumnModel columnModel) {
		super (model);
		setTableColumnModel (columnModel);
		filterEntry = new FilterEntry ();
	}
	
    
    public final void setTableColumnModel (final TableColumnModel columnModel) {
    	this.columnModel = columnModel;
    }

    /**
     * Sets the filter that determines which rows, if any, should be
     * hidden from the view.  The filter is applied before sorting.  A value
     * of <code>null</code> indicates all values from the model should be
     * included.
     *
     * <code>RowFilter</code>'s <code>include</code> method is passed an
     * <code>Entry</code> that wraps the underlying model.  The number
     * of columns in the <code>Entry</code> corresponds to the
     * number of columns in the <code>ModelWrapper</code>.  The identifier
     * comes from the <code>ModelWrapper</code> as well.
     *
     * This method triggers a sort.
     *
     * @param filter the filter used to determine what entries should be
     *        included
     */
    @Override
    public void setRowFilter (final RowFilter<? super M, ? super Integer> filter) {
    	this.setSortKeys (null);
    	super.setRowFilter (filter);
    	repromptSortedColumnsToFilter ();
    }
    
    
    public void repromptSortedColumnsToFilter () {
    	// Using a Bitset means we don't redo the same filter calculations (getFilterEntry)
    	// for every column in the table when going through the SortedTableColumns
    	final BitSet bitset = makeBitSetFromFilter ();
    	for (int column = 0; column < columnModel.getColumnCount(); column++) {
    		final SortedTableColumn<?> sortedColumn = (SortedTableColumn<?>) columnModel.getColumn (column);
    		sortedColumn.setFilterSet (bitset);
    		sortedColumn.refilterAndCategorise ();
    	}
    }
    


    public BitSet makeBitSetFromFilter () {
    	final RowFilter<? super M, ? super Integer> filter = this.getRowFilter();
    	if (filter == null) {
    		return (BitSet)null;
    	}
    	final BitSet bitset = new BitSet (this.getModelRowCount());
    	for (int modelRowIndex = 0; modelRowIndex < this.getModelRowCount(); modelRowIndex++) {
    		final FilterEntry fEntry = getFilterEntry (modelRowIndex);
    		bitset.set (modelRowIndex, filter.include (fEntry));
    	}
    	return bitset;
    }
	
    
    
	@Override
	public void toggleSortOrder (final int column) {
		//super.toggleSortOrder (column);
		
		final String columnIdentifier = this.getModel().getColumnName (column);
		final int viewColumnIndex = columnModel.getColumnIndex (columnIdentifier);
		final SortedTableColumn<?> sortedColumn = (SortedTableColumn<?>) columnModel.getColumn (viewColumnIndex);
		sortedColumn.flipSortOrder (true);
		fireSortOrderChanged ();
	}
    
	
	protected FilterEntry getFilterEntry (final int modelIndex) {
		filterEntry.modelIndex = modelIndex;
		return filterEntry;
	}
	
	/**
     * RowFilter.Entry implementation that delegates to the ModelWrapper.
     * getFilterEntry(int) creates the single instance of this that is
     * passed to the Filter.  Only call getFilterEntry(int) to get
     * the instance.
     */
    class FilterEntry extends RowFilter.Entry<M,Integer> {
        /**
         * The index into the model, set in getFilterEntry
         */
        int modelIndex;

        @Override
		public M getModel () {
            return getModelWrapper().getModel();
        }

        @Override
		public int getValueCount () {
            return getModelWrapper().getColumnCount();
        }

        @Override
		public Object getValue (final int index) {
            return getModelWrapper().getValueAt (modelIndex, index);
        }

        @Override
		public String getStringValue (final int index) {
            return getModelWrapper().getStringValueAt (modelIndex, index);
        }

        @Override
		public Integer getIdentifier () {
            return getModelWrapper().getIdentifier (modelIndex);
        }
    }
}
