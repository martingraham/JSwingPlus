package swingPlus.shared;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


public class JBasicMatrix extends ScaledTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4597696192980691439L;

	protected JRowHeader rowHeader;
	protected JColumnHeader columnHeader;
	protected JLabel axesLabel;
	
    public JBasicMatrix () {
        this (null, null, null);
    }

    public JBasicMatrix (final TableModel tableModel) {
        this (tableModel, null, null);
    }

    public JBasicMatrix (final TableModel tableModel, final TableColumnModel columnModel) {
        this (tableModel, columnModel, null);
    }

    public JBasicMatrix (final TableModel tableModel, final TableColumnModel columnModel,
    		final ListSelectionModel selectionModel) {
    	super (tableModel, columnModel, selectionModel);
    	axesLabel = new JLabel ("axes");
		setRowHeader (new JRowHeader ());
		setColumnHeader (new JColumnHeader ());
    }
	
    
	
    public void setRowHeader (final JRowHeader newHeader) {
    	
    	if (rowHeader != null) {
    		// Clear connections and listeners of old row header
    		rowHeader.setTable (null);
	    	getModel().removeTableModelListener (rowHeader);
	    	if (getRowSorter() != null) {
	    		getRowSorter().removeRowSorterListener (rowHeader);
	    	}
			getSelectionModel().removeListSelectionListener (rowHeader);
    	}
    	
    	rowHeader = newHeader;
    	
    	if (rowHeader != null) {
	    	rowHeader.setTable (this);
    	}
    }
      
    
    public JTable getRowHeader () {
    	return rowHeader;
    }
    
    
    public void setColumnHeader (final JColumnHeader newHeader) {  	
    	columnHeader = newHeader;
    	if (columnHeader != null) {
    		columnHeader.setTable (this);
    	}
    }
    
    
    public JColumnHeader getColumnHeader () {
    	return columnHeader;
    }
    
    
    
    @Override
	protected void configureEnclosingScrollPane() {
    	final Container parent = getParent();
    	
    	if (parent instanceof JViewport) {
        	final Container grandParent = parent.getParent();
            
        	if (grandParent instanceof JScrollPane) {
        		final JScrollPane scrollPane = (JScrollPane)grandParent;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
        		final JViewport viewport = scrollPane.getViewport();
                if (viewport == null || viewport.getView() != this) {
                    return;
                }
                scrollPane.setRowHeaderView (getRowHeader());
                scrollPane.setCorner (ScrollPaneConstants.UPPER_LEFT_CORNER, axesLabel);
            
                final Dimension rowHeaderDim = getRowHeader().getPreferredScrollableViewportSize();
                rowHeaderDim.width = getRowHeader().getColumnModel().getColumn(0).getWidth(); //getPreferredSize().width;
                getRowHeader().setPreferredScrollableViewportSize (rowHeaderDim);
            }
        }
        
        super.configureEnclosingScrollPane();
        
        if (getColumnHeader() != null && parent instanceof JViewport) {
        	final Container grandParent = parent.getParent();
            if (grandParent instanceof JScrollPane) {
            	final JScrollPane scrollPane = (JScrollPane)grandParent;
            	if (scrollPane.getColumnHeader().getView() != getColumnHeader()) {
            		scrollPane.setColumnHeaderView (getColumnHeader ());
            	}

            	final Dimension colheaderDim = getColumnHeader().getPreferredScrollableViewportSize();
            	colheaderDim.height = getColumnHeader().getRowHeight (0); //.getPreferredSize().height;
            	getColumnHeader().setPreferredScrollableViewportSize (colheaderDim);
            }
        }
        
    }
}
