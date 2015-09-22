package util.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import util.IconCache;
import util.Messages;


public class DeleteFileButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5580875089699517885L;
	private final static Class<DeleteFileButton> CLS = DeleteFileButton.class;
	private final static ActionListener DELETE_LISTENER = new DeleteListener ();
	final static Icon ICON = IconCache.makeIcon (CLS, "icon");

	private final JFileChooser fileChooser;
	
	public DeleteFileButton (final JFileChooser jfc) {
		super (Messages.getString (CLS, "text"), ICON);
		fileChooser = jfc;
		this.setToolTipText (Messages.getString (CLS, "tooltipText"));
		this.addActionListener (DELETE_LISTENER);
	}
	
	JFileChooser getFileChooser () { return fileChooser; }
}

class DeleteListener implements ActionListener {

	private final MessageFormat mFormat = new MessageFormat (Messages.getString ("DeleteListener.text"));
	private final Object[] obj = new Object [1];
	
	@Override
	public void actionPerformed (final ActionEvent aEvent) {

		final DeleteFileButton dfb = (DeleteFileButton) aEvent.getSource();
		final JFileChooser jfc = dfb.getFileChooser ();
		final File file = jfc.getSelectedFile();
		if (file != null && file.isFile()) {
			obj [0] = file.getName();
			final String deleteString = mFormat.format (obj);
			final YesNoDialog ynd = new YesNoDialog (null, deleteString, DeleteFileButton.ICON, null);
			if (ynd.getYesNoValue() > 0 && file.delete ()) {
				jfc.rescanCurrentDirectory();
			}
		}
	}
}
