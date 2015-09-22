package swingPlus.matrix;

import java.util.Set;

import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;

import swingPlus.shared.JColumnHeader;

import model.graph.Edge;
import model.matrix.MatrixTableModel;


public class JMatrixColumnHeader extends JColumnHeader {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1686227012161681926L;

	final static Logger LOGGER = Logger.getLogger (JMatrixColumnHeader.class);
	
	public JMatrixColumnHeader () {
		super ();
		setRendererToolTip (new MatrixRendererToolTip (this));
	}
    
	
    @Override
    public void changeSelection (final int rowIndex, final int columnIndex, final boolean toggle, final boolean extend) {
    	super.changeSelection (rowIndex, columnIndex, toggle, extend);
    	
    	LOGGER.debug ("toggle: "+toggle+"\t extend: "+extend);
    	
    	if (getTable() != null && getModel() instanceof MatrixTableModel) {
    		final MatrixTableModel mtm = (MatrixTableModel)getModel();
    		final ListSelectionModel lsm = getTable().getSelectionModel();
    		
    		if (lsm != null) {
    			lsm.setValueIsAdjusting (true);
    			if (!extend) {
    				lsm.clearSelection();
    			}
    			
    			final int modelColumnIndex = getTable().convertColumnIndexToModel (columnIndex);
    			final Object columnNode = mtm.getColumnObject (modelColumnIndex);
    			
    			if (columnNode != null) {
    				final Set<Edge> edges = mtm.getColumnData (columnNode);
	    			for (Edge edge : edges) {
	    				boolean isNode2 = isSecondNode (edge, columnNode);
	    				final Object otherNode = isNode2 ? edge.getNode1() : edge.getNode2();
	    				final int linkedRowIndex = mtm.getRowIndex (otherNode);
	    				final int viewRowIndex = getTable().convertRowIndexToView (linkedRowIndex);
	    	    		lsm.addSelectionInterval (viewRowIndex, viewRowIndex);					
	    			}
    			}
    			
    			lsm.setValueIsAdjusting (false);
    		}
    	}
    }

	public boolean isSecondNode (final Edge edge, final Object columnNode) {
		if (edge.getNode2() == columnNode) {
			return true;
		}
		if (edge.getNode1() == columnNode) {
			return false;
		}
		return (columnNode.equals (edge.getNode2()));
	}
}
