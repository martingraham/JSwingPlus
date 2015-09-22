package util.swing;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;


public class JPasswordBox  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7948976426922040561L;


	private final static Logger LOGGER = Logger.getLogger (JPasswordBox.class);
	
	protected transient JDialog jDialog;
	protected transient JTextField usernameField;
	private transient JPasswordField passwordField;
	protected transient JButton submitButton;
	private transient char[] password;
	protected transient String username;
	final private transient Object syncher = new Object ();
	
	
	public JPasswordBox () {
		this ("", new char[0]);
	}
	
	public JPasswordBox (final String preUsername) {
		this (preUsername, new char[0]);
	}
		
	public JPasswordBox (final char[] prePassword) {
		this ("", prePassword);
	}
	
	
	public JPasswordBox (final String preUsername, final char[] prePassword) {
		
		jDialog = new JDialog ((Dialog)null, true);
		
		usernameField = new JTextField (preUsername);
		passwordField = new JPasswordField (new String (prePassword));
		submitButton = new JButton ("OK");
		
		jDialog.setTitle ("DB Connect");
		final Container container = jDialog.getContentPane();
		container.setLayout (new GridLayout (3, 2, 5, 0));
		container.add (new JLabel ("Username", SwingConstants.RIGHT));
		container.add (usernameField);
		container.add (new JLabel ("Password", SwingConstants.RIGHT));
		container.add (passwordField);
		//c.add (new );
		container.add (submitButton);
		
		submitButton.addActionListener(
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent event) {
					okPressed ();
				}
			}
		);
	}
	
	public final JDialog getDialog () {
		return jDialog;
	}
	
	public final String getUsername() {
		return username;
	}
	
	public final char[] getPassword () {
		return password;
	}

	public final JButton getSubmitButton() {
		return submitButton;
	}
	
	public void okPressed () {
		password = passwordField.getPassword();
		username = usernameField.getText();
		
		unlock ();
		
		jDialog.setVisible (false);
		jDialog.dispose ();
	}
	
	// blocking methods for non-model dialog
	
	// call lock from one thread to block activity on that thread
	// call unlock from another thread to let the locked thread go on its merry way
	
	// usually call lock() from non-swing-event thread (insets.e. database querying thread)
	// call unlock() from swing thread (via the ok action button listener ())
	// this allows the first thread to wait until passwordField inputStream entered and Ok'ed
	
	public void lock () {
		if (! SwingUtilities.isEventDispatchThread()) {
			synchronized (syncher) {
				try {
					syncher.wait();
				} catch (InterruptedException e) {
					LOGGER.error ("Threading error", e);
				}
			}
		}
	}
	
	public void unlock () {
		synchronized (syncher) {
			syncher.notifyAll();
		}
	}
}
