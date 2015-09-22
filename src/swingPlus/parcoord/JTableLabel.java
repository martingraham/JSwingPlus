package swingPlus.parcoord;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.Messages;

public class JTableLabel extends JLabel implements PropertyChangeListener, ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -66596517701097437L;
	
	JTable table;
	private final MessageFormat labelFormat = new MessageFormat (Messages.getString (this.getClass(), "text"));
	private final Object[] formatValues = new Object [4];

	
	public JTableLabel (final JTable jTable) {
		super ();
		this.table = jTable;
		if (jTable.getSelectionModel() != null) {
			jTable.getSelectionModel().addListSelectionListener (this);
		}
		jTable.addPropertyChangeListener (this);
	}
	

	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals ("model")
				|| evt.getPropertyName().equals ("selectionModel")) {
			makeText ();
		}
	}
	
	public void makeText () {
		if (table != null) {
			formatValues [0] = table.getRowCount();
			formatValues [1] = table.getSelectedRowCount();
			final String text = labelFormat.format (formatValues);
			this.setText (text);
		}
	}

	@Override
	public void valueChanged (final ListSelectionEvent lsEvent) {
		if (!lsEvent.getValueIsAdjusting ()) {
			makeText ();
		}
	}
}
