package util;

import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;


public abstract class DataSpecificAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2387825001111040483L;

	
	abstract public void setObject (Object obj);
    
    protected String makeAccString (final Action action) {
    	return (action == null ? null : makeAccString ((KeyStroke)action.getValue(Action.ACCELERATOR_KEY)));
    }
    
    protected String makeAccString (final KeyStroke keyStroke) {
    	final StringBuilder sBuilder = new StringBuilder (" [");
    	sBuilder.append (KeyEvent.getKeyModifiersText(keyStroke.getModifiers()));
    	if (keyStroke.getModifiers() > 0) {
    		sBuilder.append('+');
    	}
    	sBuilder.append(KeyEvent.getKeyText(keyStroke.getKeyCode()));
    	sBuilder.append(']');
    	return sBuilder.toString ();
    }

}
