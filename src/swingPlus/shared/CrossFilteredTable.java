package swingPlus.shared;

import java.awt.Color;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import model.graph.GraphSelectionEvent;
import model.graph.GraphSelectionListener;

public class CrossFilteredTable extends JTableST implements GraphSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5033474340344808319L;
	private ListSelectionModel uberSelection;
	private Color uberSelectedColour = Color.pink;
	
	public CrossFilteredTable () {
		this (null);
	}
	

	public CrossFilteredTable (final TableModel tableModel) {
		this (tableModel, null);	
	}
	
	
	public CrossFilteredTable (final TableModel tableModel, final TableColumnModel columnModel) {
		super (tableModel, columnModel);
	}
	       
	public final ListSelectionModel getUberSelection () {
		return uberSelection;
	}

	public final void setUberSelection (final ListSelectionModel uberSelection) {
		final ListSelectionModel oldModel = this.uberSelection;
		this.uberSelection = uberSelection;
		this.firePropertyChange ("uberSelectionModel", oldModel, this.uberSelection);
	}
	
	
	public final Color getUberSelectedColour() {
		return uberSelectedColour == null ? Color.pink : uberSelectedColour;
	}

	public final void setUberSelectedColour (final Color uberSelectedColour) {
		if (this.uberSelectedColour != uberSelectedColour) {
			final Color old = this.uberSelectedColour;
			this.uberSelectedColour = uberSelectedColour;
			this.firePropertyChange ("uberSelectionForeground", old, uberSelectedColour);
		}
	}


	@Override
	public void valueChanged (final GraphSelectionEvent gsEvent) {
		// TODO Auto-generated method stub
		
	}
}

