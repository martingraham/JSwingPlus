package util.swing;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;


public class YesNoDialog extends MyDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4789297336559527877L;

	private int yesNoValue = -1;
	
	public YesNoDialog (final Frame frame, final String title, final Icon icon, final Point location) {
		super (frame, true);
		
		closeButton.setVisible (false);
		
        final JLabel jtitle = new JLabel (title, icon, SwingConstants.LEFT);
        jtitle.setAlignmentX (0.5f);
        jtitle.setBorder (BorderFactory.createEmptyBorder (0, 0, 6, 0));
        getUserPanel().add (jtitle);
		
        final Locale locale = this.getLocale();
		final JButton yesButton = new MyButton (UIManager.getString ("OptionPane.yesButtonText", locale));
		final JButton noButton = new MyButton (UIManager.getString ("OptionPane.noButtonText", locale));
		yesButton.setMnemonic (getMnemonic ("OptionPane.yesButtonMnemonic", locale));
		noButton.setMnemonic (getMnemonic ("OptionPane.noButtonMnemonic", locale));
		this.getOptionBox().setLayout (new BoxLayout (this.getOptionBox(), BoxLayout.X_AXIS));
		this.getOptionBox().add (yesButton);
		this.getOptionBox().add (Box.createHorizontalGlue());
		this.getOptionBox().add (noButton);
		
		final ActionListener yesNoListener = new ActionListener () {
			public void actionPerformed (final ActionEvent aEvent) {
				yesNoValue = (aEvent.getSource() == yesButton ? 1 : (aEvent.getSource() == noButton ? 0 : -1));
				close ();
			}
		};
		
		yesButton.addActionListener (yesNoListener);
		noButton.addActionListener (yesNoListener);
		
		makeVisible (location);
	}
	
	
    private int getMnemonic (final String key, final Locale locale) {
        final String value = (String)UIManager.get(key, locale);

        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        }
        catch (final NumberFormatException nfe) { 
        	//EMPTY
        }
        return 0;
    }
	
	public int getYesNoValue () { return yesNoValue; }
}