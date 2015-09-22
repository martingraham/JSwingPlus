package model.parcoord;

import io.OrdinalDataSource;
import io.json.EmptyOrdinalDataSource;
import io.parcoord.MakeTableModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import model.graph.Edge;
import model.matrix.MatrixTableModel;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ValueNode;


public class JSONBasedTableModel extends ColumnTypedTableModel implements MatrixTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9151932484362759450L;
	protected Map<Object, Integer> keys;
	protected List<Object> invKeys;
	
	
	public JSONBasedTableModel () {	
		keys = new HashMap <Object, Integer> ();
		invKeys = new ArrayList<Object> ();
	}
	
	public JSONBasedTableModel (final JsonNode rowCollection) {
		this ();
		setColumnIdentifiers (rowCollection);
		setData (rowCollection);
		
	}
	
    public void setColumnIdentifiers (final JsonNode rowCollection) {
    	final Iterator<Entry<String, JsonNode>> rowIter = rowCollection.getFields();
		final Entry<String, JsonNode> firstRow = rowIter.next();
		final Iterator<Entry<String, JsonNode>> firstRowContentIter = firstRow.getValue().getFields();
		final Vector<String> headerData = new Vector<String> ();
		while (firstRowContentIter.hasNext()) {
			final Entry<String, JsonNode> firstRowEntry = firstRowContentIter.next();
			final JsonNode cell = firstRowEntry.getValue();
			if (cell instanceof ValueNode) {
				headerData.add (firstRowEntry.getKey());
			}
			else if (cell instanceof ArrayNode) {
				headerData.add (firstRowEntry.getKey()+" (count)");
			}
		}
	    setColumnIdentifiers (headerData);
    }
	
	public void setData (final JsonNode rowCollection) {
		final MakeTableModel mtm = new MakeTableModel ();
		final OrdinalDataSource emptyOrdinalData = new EmptyOrdinalDataSource ();
		
		final Iterator<Entry<String, JsonNode>> rowIter = rowCollection.getFields();
		
		while (rowIter.hasNext()) {
			final Entry<String, JsonNode> row = rowIter.next();
			final Iterator<Entry<String, JsonNode>> rowContentIter = row.getValue().getFields();
			final List<String> rowData = new ArrayList<String> ();
			
			while (rowContentIter.hasNext()) {
				final JsonNode cell = rowContentIter.next().getValue();
				if (cell instanceof ValueNode) {
					rowData.add (cell.getValueAsText());
				}
				else if (cell instanceof ArrayNode) {
					rowData.add (Integer.toString (cell.size()));
				}
			}
			
			final int rowCount = this.getRowCount();
			final Object key = row.getValue();
			keys.put (key, Integer.valueOf (rowCount));
			invKeys.add (key);
			mtm.addRowToTable (rowData, this, emptyOrdinalData, this.getColumnCount());
		}
	}
	

	@Override
    public int getRowIndex (final Object key) {
    	final Integer intObj = keys.get (key);
    	return (intObj == null ? -1 : intObj.intValue());
    }
    
	@Override
    public Object getRowObject (final int rowIndex) {
    	return invKeys.get (rowIndex);
    }
	
	@Override
	public int getColumnIndex (final Object colObj) {
		return -1;
	}
	
	@Override
    public Object getColumnObject (final int columnIndex) {
    	return null;
    }
	
	@Override
	public Set<Edge> getRowData (final Object obj) {
		return null;
	}
	
	@Override
	public Set<Edge> getColumnData (final Object obj) {
		return null;
	}
}
