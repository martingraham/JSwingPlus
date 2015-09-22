package io;

import java.awt.event.ActionEvent;
import java.util.Collection;

import swingPlus.parcoord.JParCoord;
import util.PropertyPrefixBasedAction;

public class SaveSelectedAction extends PropertyPrefixBasedAction {


	private static final long serialVersionUID = -6446761719963664424L;
	private Collection<JParCoord> parCoords;
	
	public SaveSelectedAction (final int keyCode, final String actionPrefix, final Collection<JParCoord> parCoords) {
		super (keyCode, actionPrefix);
		this.parCoords = parCoords;
	}
	
	public void actionPerformed (final ActionEvent aEvent) {	
		new LaunchSaveFileDialog (parCoords);
	}
}
