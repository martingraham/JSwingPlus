package io.report;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collection;

import util.PropertyPrefixBasedAction;

public class ReportGeneratorAction extends PropertyPrefixBasedAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6446761719963664424L;
	final private Collection<Component> comps;
	
	public ReportGeneratorAction (final int keyCode, final String actionPrefix, final Collection<Component> components) {
		super (keyCode, actionPrefix);
		this.comps = components;
	}
	
	public void actionPerformed (final ActionEvent aEvent) {	
		ReportDialog.showDialog (comps);
	}
}
