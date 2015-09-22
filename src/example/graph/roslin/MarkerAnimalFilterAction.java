package example.graph.roslin;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.BitSet;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import swingPlus.graph.JGraph;
import swingPlus.graph.SelectionPopupMenu;
import util.DataSpecificAction;
import util.Messages;



public class MarkerAnimalFilterAction extends DataSpecificAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1838464220778110561L;
	Class<?> klass = MarkerAnimalFilterAction.class;
	Animal currentAnimal;
	
	MarkerAnimalFilterAction () {
		super ();
		final Icon handleIcon = null;//IconCache.makeIcon (cl, "icon");
		putValue (Action.NAME, Messages.getString ("example.graph.roslin.roslin", klass, "name"));
		putValue (Action.MNEMONIC_KEY, Integer.valueOf (KeyEvent.VK_F) );
		putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_F, InputEvent.ALT_MASK) );
		putValue (Action.SMALL_ICON, handleIcon);
		final String descriptionString = Messages.getString ("example.graph.roslin.roslin", klass, "description");
		putValue (Action.SHORT_DESCRIPTION, descriptionString+this.makeAccString (this));
		putValue ("LargeIcon", handleIcon);	
	}
	
	@Override
	public void actionPerformed (final ActionEvent aEvent) {
		if (currentAnimal != null && currentAnimal.getData() != null) {
			final BitSet errors = currentAnimal.getData().getPossErrors ();
			currentAnimal.getData().restrainMarkerMap (errors);
			final JGraph jgraph = (JGraph)SelectionPopupMenu.getInstance().getInvoker();
			jgraph.updateFilteredModel ();
			jgraph.getTopLevelAncestor().repaint ();
		}
		SelectionPopupMenu.getInstance().setVisible (false);
	}
	
	@Override
	public void setObject (final Object animal) {
		currentAnimal = (Animal)animal;
		setEnabled (currentAnimal != null && currentAnimal.getData() != null);
	}
		
}
