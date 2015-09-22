package model.matrix;

import java.util.Set;

import model.graph.Edge;

public interface MatrixTableModel {

    /**
     * 
     * @param column	index of the model column
     * @return	the object that is represented by that column index
     */
    public Object getColumnObject (int column);
    
    /**
     * 
     * @param row	index of the model row
     * @return	the object that is represented by that row index
     */
    public Object getRowObject (int row);
    
    
    /**
     * 
     * @param colObj		The object to get the model column index for
     * @return	The model column index of the object, -1 if not found
     */
    public int getColumnIndex (final Object colObj);
    
    /**
     * 
     * @param rowObj		The object to get the model row index for
     * @return	The model row index of the object, -1 if not found
     */
    public int getRowIndex (final Object rowObj);
    
    
    
    /**
     * get the edges associated with a particular row object
     * In a directed graph these will be the edges where the node is the source
     * In an undirected (symmetric) graph these will be edges where the node is either source or sink
     * @param rowObj
     * @return Set<Edge> a set of edges for the argument node object
     */
    public Set<Edge> getRowData (final Object rowObj);
    
    
    /**
     * get the edges associated with a particular column object
     * In a directed graph these will be the edges where the node is the sink
     * In an undirected (symmetric) graph these will be edges where the node is either source or sink
     * @param columnObj
     * @return Set<Edge> a set of edges for the argument node object
     */
    public Set<Edge> getColumnData (final Object columnObj);
}
