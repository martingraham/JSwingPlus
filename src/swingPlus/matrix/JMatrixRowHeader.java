package swingPlus.matrix;

import java.util.Set;

import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;

import swingPlus.shared.JRowHeader;

import model.graph.Edge;
import model.matrix.MatrixTableModel;


public class JMatrixRowHeader extends JRowHeader {

	/**
	 * 
	 */
	private static final long serialVersionUID = -626212979089742759L;

	final static Logger LOGGER = Logger.getLogger (JMatrixRowHeader.class);
	
	
	public JMatrixRowHeader () {
		super ();
		setRendererToolTip (new MatrixRendererToolTip (this));
	}

	
    @Override
    public void changeSelection (final int rowIndex, final int columnIndex, final boolean toggle, final boolean extend) {
    	super.changeSelection (rowIndex, columnIndex, toggle, extend);
    	
    	LOGGER.debug ("toggle: "+toggle+"\t extend: "+extend);
    	
    
    	if (this.getTable() != null && getModel() instanceof MatrixTableModel) {
    		final MatrixTableModel mtm = (MatrixTableModel)getModel();
    		final ListSelectionModel lsm = this.getTable().getColumnModel().getSelectionModel();
    		
    		if (lsm != null) {
    			lsm.setValueIsAdjusting (true);
    			if (!extend) {
    				lsm.clearSelection();
    			}
    			
    			final int modelRowIndex = this.getTable().convertRowIndexToModel (rowIndex);
    			final Object rowNode = mtm.getRowObject (modelRowIndex);
    			
    			if (rowNode != null) {
	    			final Set<Edge> edges = mtm.getRowData (rowNode);
	    			for (Edge edge : edges) {
	    				boolean isNode1 = isFirstNode (edge, rowNode);
	    				final Object otherNode = isNode1 ? edge.getNode2() : edge.getNode1();
	    				final int colIndex = mtm.getColumnIndex (otherNode);
	    				final int viewColIndex = this.getTable().convertColumnIndexToView (colIndex);
	    	    		lsm.addSelectionInterval (viewColIndex, viewColIndex);					
	    			}
    			}
    			
    			lsm.setValueIsAdjusting (false);
    		}
    	}
    }
    
	public boolean isFirstNode (final Edge edge, final Object rowNode) {
		if (edge.getNode1() == rowNode) {
			return true;
		}
		if (edge.getNode2() == rowNode) {
			return false;
		}
		return (rowNode.equals (edge.getNode1()));
	}
}
