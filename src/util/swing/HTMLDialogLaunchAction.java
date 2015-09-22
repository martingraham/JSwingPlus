package util.swing;

import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.Icon;

import util.PropertyPrefixBasedAction;


public class HTMLDialogLaunchAction extends PropertyPrefixBasedAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7368272268886374700L;

	protected URL url;
	protected int width;
	
	public HTMLDialogLaunchAction (final int keyCode, final String actionPrefix, final URL url) {
		this (keyCode, actionPrefix, url, 240);
	}
	
	public HTMLDialogLaunchAction (final int keyCode, final String actionPrefix, 
			final URL url, final int width) {
		super (keyCode, actionPrefix);
		this.url = url;
		this.width = width;
	}
	
	@Override
	public void actionPerformed (final ActionEvent event) {
		//final URL css = null;//DataPrep.getInstance().getRelativeURL ("example/sebe/about.css");
		new HTMLDialog ((Frame)null, true, true, url, (URL)null, 
				this.getValue(NAME).toString(), (Icon)this.getValue (SMALL_ICON), 
				MouseInfo.getPointerInfo().getLocation(), width);
	}
}
