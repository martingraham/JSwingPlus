package util;

import java.awt.event.InputEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

public abstract class PropertyPrefixBasedAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1865016357316715163L;

	protected String textPropertyFile = "general"; 
	
	
	public PropertyPrefixBasedAction () {
		super ();
	}
	
	public PropertyPrefixBasedAction (final int keyCode, final String actionPrefix) {
		super ();
		setup (keyCode, actionPrefix);	
	}
	
	
	protected void setup (final int keyCode, final String actionPrefix) {
		final Icon handleIcon = IconCache.makeIcon (actionPrefix + "Icon");
		putValue (Action.NAME, Messages.getString (textPropertyFile, actionPrefix + "ActionName"));
 		putValue (Action.MNEMONIC_KEY, Integer.valueOf (keyCode));
		putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (keyCode, InputEvent.ALT_MASK) );
		putValue (Action.SMALL_ICON, handleIcon);
   		putValue (Action.SHORT_DESCRIPTION, Messages.getString (textPropertyFile, actionPrefix + "ActionTooltip")
   					+ StringUtils.getInstance().makeAccString (this));
		putValue ("LargeIcon", handleIcon);	
	}
	
	public void setTextPropertyFile (final String newTextPropertyFile) {
		textPropertyFile = newTextPropertyFile;
	}
}