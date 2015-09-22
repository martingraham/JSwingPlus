package io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVColumnObj {
	
    private List<String> columns;
    private final Map<String, Integer> columnHeaderToIndexMapping;

    public CSVColumnObj () {
        columns = new ArrayList<String> ();
        columnHeaderToIndexMapping = new HashMap<String, Integer> ();
    }
    
    public String getDataValue (final List<String> c, final String columnHeader) {
		final int col = getColumnIndex (columnHeader);
     	return (col >= 0 ? c.get (col) : null);
     }

    
     public void setDataValue (final List<String> c, final String columnHeader, final String setValue) {
		final int col = getColumnIndex (columnHeader);
     	if (col >= 0) {
     		c.set (col, setValue);
        }
     }

     public int getColumnIndex (final String columnHeader) {

		final Integer iObj = columnHeaderToIndexMapping.get (columnHeader);
     	return (iObj != null ? iObj.intValue() : -1);
     }

     public Map<String, Integer> getColumnHeaderIndices () { return columnHeaderToIndexMapping; }

     public List<String> getColumnList () { return columns; }
}
