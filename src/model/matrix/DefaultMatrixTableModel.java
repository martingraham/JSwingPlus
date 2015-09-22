package model.matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import model.graph.Edge;
import model.graph.GraphModel;
import model.graph.GraphModelEvent;
import model.graph.GraphModelListener;
import model.graph.impl.DirectedGraphInstance;
import model.shared.MultiComparator;

public class DefaultMatrixTableModel extends AbstractMatrixTableModel implements GraphModelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7102590840668145548L;
	private final static Logger LOGGER = Logger.getLogger (DefaultMatrixTableModel.class);
	
	// flag to decide whether a graphChanged call was caused by this class in the
	// first place and can therefore be safely ignored
	private boolean propagatingToGraphModel = false;
	
	GraphModel graph;
	List<Object> columns;
	List<Object> rows;
	
	
	NodeMetrics rowMetrics, columnMetrics;
	transient RowFilter<TableModel, Integer> initialRowFilter;
	
	
    public DefaultMatrixTableModel () {
        this (null);
    }
	
    public DefaultMatrixTableModel (final GraphModel gModel) {
    	super ();
        setData (gModel);
    }
    

	
	@Override
	public int getColumnCount() {
		return columns.size ();
	}

	@Override
	public int getRowCount() {
		return rows.size ();
	}

	@Override
    public Object getValueAt (final int row, final int column) {
		
		final Object rowNode = rows.get (row);
		Object obj = null;
		
		if (column == 0) {
			obj = rowNode;
		}
		else {
			final Object columnNode = columns.get (column);
			if (row == 0) {
				obj = columnNode;
			}
			else if (rowNode != null && columnNode != null) {
	            final Collection<Edge> edges = graph.getEdges (rowNode, columnNode);
	            obj = returnEdgeIfSingleEdgeCollection (edges);
	        }
		}
        return obj;
    }
	
	
	Object returnEdgeIfSingleEdgeCollection (final Collection<Edge> edges) {
		Object obj = edges;
		if (edges.size() == 1) {
        	final Iterator<Edge> edgeI = edges.iterator();
        	while (edgeI.hasNext()) {
        		obj = edgeI.next ();
        	}
        } 
		return obj;
	}

    /**
     * Sets the object value for the cell at <code>column</code> and
     * <code>row</code>.  <code>aValue</code> is the new value.  This method
     * will generate a <code>tableChanged</code> notification.
     *
     * @param   aValue          the new value; this can be null
     * @param   row             the row whose value is to be changed
     * @param   column          the column whose value is to be changed
     * @exception  ArrayIndexOutOfBoundsException  if an invalid row or
     *               column was given
     */
    @Override
	public void setValueAt (final Object aValue, final int row, final int column) {
    	final Object rowNode = rows.get (row);
    	final Object columnNode = columns.get (column);
        if (column > 0 && rowNode != null && columnNode != null) {
        	propagatingToGraphModel = true;
            graph.addEdge (rowNode, columnNode, aValue);
            fireTableCellUpdated (row, column);
            propagatingToGraphModel = false;
        }
    }
    
    
    /**
     * Returns the column name.
     *
     * @return a name for this column using the string value of the
     * appropriate member in <code>columnIdentifiers</code>.
     * If <code>columnIdentifiers</code> does not have an entry 
     * for this index, returns the default
     * name provided by the superclass.
     */
    @Override
	public String getColumnName (final int column) {
        return ((column >= 0 && column < columns.size() && columns.get(column) != null) ?
        		columns.get(column).toString() : (String)null);
    }

    /**
     * Returns true regardless of parameter values.
     *
     * @param   row             the row whose value is to be queried
     * @param   column          the column whose value is to be queried
     * @return                  true
     * @see #setValueAt
     */
    @Override
	public boolean isCellEditable (final int row, final int column) {
        return true;
    }

    /**
     *  Replaces the current <code>dataVector</code> instance variable 
     *  with the new <code>Vector</code> of rows, <code>dataVector</code>.
     *  Each row is represented in <code>dataVector</code> as a
     *  <code>Vector</code> of <code>Object</code> values.
     *  <code>columnIdentifiers</code> are the names of the new 
     *  columns.  The first name in <code>columnIdentifiers</code> is
     *  mapped to column 0 in <code>dataVector</code>. Each row in
     *  <code>dataVector</code> is adjusted to match the number of 
     *  columns in <code>columnIdentifiers</code>
     *  either by truncating the <code>Vector</code> if it is too long,
     *  or adding <code>null</code> values if it is too short.
     *  Note that passing in a <code>null</code> value for
     *  <code>dataVector</code> results in unspecified behavior,
     *  an possibly an exception.
     *
     * @param   dataVector         the new data vector
     * @param   columnIdentifiers     the names of the columns
     * @see #getDataVector
     */
    public final void setData (final GraphModel gModel) {
    	final GraphModel graphModel = (gModel == null) ?
    			new DirectedGraphInstance() : gModel;

    	if (graph != null) {
    		graph.removeGraphModelListener (this);
    	}
        this.graph = graphModel;
        rowMetrics = new NodeMetrics (graphModel);
        columnMetrics = new NodeMetrics (graphModel, true);
        initialRowFilter = new FirstRowFilter ();
        populateColumnsAndRows ();
        fireTableStructureChanged();
        graph.addGraphModelListener (this);
    }

    
    void populateColumnsAndRows () {
    	final Collection<Object> nodes = graph.getNodes ();
    	invColumns.clear();
    	invRows.clear ();
    	columns = new ArrayList<Object> ();
    	rows = new ArrayList<Object> ();
    	//columns.add (new String ("rowColumn"));
    	
    	final String rowColHeader = "rows";
    	columns.add (rowColHeader);
    	int counter = 0;
    	invColumns.put (rowColHeader, Integer.valueOf (counter));
    	counter++;
    	rows.add ("Columns");
    	
    	for (Object node : nodes) {
    		//Collection<Edge> edges = graph.getEdges (n);
    		final Integer counterInt = Integer.valueOf (counter);
    		
    		columns.add (node);
    		invColumns.put (node, counterInt);
    		
    		rows.add (node);
    		invRows.put (node, counterInt);
    		
    		counter++;
    	}
    }
    
    
    public MultiComparator getRowMetrics () { return rowMetrics; }
    
    public MultiComparator getColumnMetrics () { return columnMetrics; }
    
    
    
    public void flipAxes () {
    	LOGGER.debug ("pre flip axes. row 0: "+this.getRowData (this.getRowObject (0)));
    	final Map <Object, Integer> tempMap = invColumns;
    	invColumns = invRows;
    	invRows = tempMap;
    	
    	final List<Object> tempList = columns;
    	columns = rows;
    	rows = tempList;
    	
    	LOGGER.debug ("post flip axes. row 0: "+this.getRowData (this.getRowObject (0)));
    	this.fireTableDataChanged();
    	this.fireTableStructureChanged();
    }
    
    
    /**
     * 
     * @return	the <code>GraphModel</code> object that underlies this table model
     */
    public GraphModel getData() {
    	return graph;
    }
    
    
    @Override
    public Set<Edge> getRowData (final Object rowObj) {
    	return graph.getEdges (rowObj);
    }
    
    
    @Override
    public Set<Edge> getColumnData (final Object columnObj) {
    	return graph.getCoincidentEdges (columnObj);
    }
    
    /**
     * 
     * @param column	index of the model column
     * @return	the object that is represented by that column index
     */
    @Override
    public Object getColumnObject (final int column) {
    	return columns.get (column);
    }
    
    /**
     * 
     * @param column	index of the model row
     * @return	the object that is represented by that row index
     */
    @Override
    public Object getRowObject (final int row) {
    	return rows.get (row);
    }


    
    public RowFilter<TableModel, Integer> getRowFilter () { return initialRowFilter; }
    
    
    
    /**
     *  Equivalent to <code>fireTableChanged</code>.
     *
     * @param event  the change event 
     *
     */
    public void newDataAvailable (final TableModelEvent event) {
        fireTableChanged (event);
    }

    //
    // Manipulating rows
    // 


    /**
     *  Ensures that the new rows have the correct number of columns.
     *  This is accomplished by  using the <code>setSize</code> method in
     *  <code>Vector</code> which truncates vectors
     *  which are too long, and appends <code>null</code>s if they
     *  are too short.
     *  This method also sends out a <code>tableChanged</code>
     *  notification message to all the listeners.
     *
     * @param event         this <code>TableModelEvent</code> describes 
     *                           where the rows were added. 
     *				 If <code>null</code> it assumes
     *                           all the rows were newly added
     * @see #getDataVector
     */
    public void newRowsAdded (final TableModelEvent event) {
        fireTableChanged (event);
    }

    /**
     *  Equivalent to <code>fireTableChanged</code>.
     *
     *  @param event the change event
     *
     */
    public void rowsRemoved (final TableModelEvent event) {
        fireTableChanged (event);
    }



    /**
     *  Adds a row to the end of the model.  The new row will contain
     *  <code>null</code> values unless <code>rowData</code> is specified.
     *  Notification of the row being added will be generated.
     *
     * @param   rowData          optional data of the row being added
     */
    public void addRow (final Object node) {
        insertRow (getRowCount(), node);
    }


    /**
     *  Inserts a row at <code>row</code> in the model.  The new row
     *  will contain <code>null</code> values unless <code>rowData</code>
     *  is specified.  Notification of the row being added will be generated.
     *
     * @param   row             the row index of the row to be inserted
     * @param   rowData         optional data of the row being added
     * @exception  ArrayIndexOutOfBoundsException  if the row was invalid
     */
    public void insertRow (final int row, final Object node) {
    	propagatingToGraphModel = true;
    	graph.addNode (node);
		rows.add (node);
		invRows.put (node, Integer.valueOf (row));
        fireTableRowsInserted (row, row);
        propagatingToGraphModel = false;
    }



    /**
     *  Removes the row at <code>row</code> from the model.  Notification
     *  of the row being removed will be sent to all the listeners.
     *
     * @param   row      the row index of the row to be removed
     * @exception  ArrayIndexOutOfBoundsException  if the row was invalid
     */
    public void removeRow (final int row) {
    	propagatingToGraphModel = true;
    	final Object node = rows.get (row);
        graph.removeNode (node);
        populateColumnsAndRows ();
        fireTableRowsDeleted (row, row);
        propagatingToGraphModel = false;
    }

    //
    // Manipulating columns
    // 



    /**
     *  Adds a column to the model.  The new column will have the
     *  identifier <code>columnName</code>, which may be null.  This method
     *  will send a
     *  <code>tableChanged</code> notification message to all the listeners.
     *  This method is a cover for <code>addColumn(Object, Vector)</code> which
     *  uses <code>null</code> as the data vector.
     *
     * @param   columnName the identifier of the column being added
     */
    public void addColumn (final Object node) {
        addColumn (node, (Collection<Edge>)null);
    }

    /**
     *  Adds a column to the model.  The new column will have the
     *  identifier <code>columnName</code>, which may be null.
     *  <code>columnData</code> is the
     *  optional vector of data for the column.  If it is <code>null</code>
     *  the column is filled with <code>null</code> values.  Otherwise,
     *  the new data will be added to model starting with the first
     *  element going to row 0, etc.  This method will send a
     *  <code>tableChanged</code> notification message to all the listeners.
     *
     * @param   columnName the identifier of the column being added
     * @param   columnData       optional data of the column being added
     */
    public void addColumn (final Object columnName, final Collection<Edge> columnData) {
    	propagatingToGraphModel = true;
    	graph.addEdges (columnData);
    	populateColumnsAndRows ();
        fireTableStructureChanged();
        propagatingToGraphModel = false;
    }
    

	@Override
	public void graphChanged (final GraphModelEvent gModelEvent) {
		// if change wasn't from here, then recalc the matrix based on the changed graph
		if (!propagatingToGraphModel) {
			populateColumnsAndRows ();
		}
	}
	
	
	
	static class FirstRowFilter extends RowFilter<TableModel, Integer> {
		@Override
		public boolean include (final Entry<? extends TableModel, ? extends Integer> entry) {
			final int rowIndex = ((Integer)entry.getIdentifier()).intValue();
			return rowIndex != 0;
		}
	}
}