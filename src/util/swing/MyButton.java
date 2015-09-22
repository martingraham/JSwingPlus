package util.swing;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import util.GraphicsUtil;

public class MyButton extends JButton {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6510554613561682849L;

	public MyButton (final String label) {
        this (label, null);
    }
	
	public MyButton (final Icon icon) {
        this (null, icon);
	}

	public MyButton (final String label, final Icon icon) {
        super (label, icon);
	    setMargin (GraphicsUtil.ZEROINSETS);
	    setVerticalTextPosition (SwingConstants.CENTER);
    }
}

