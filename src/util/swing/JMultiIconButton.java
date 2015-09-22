package util.swing;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

import util.GraphicsUtil;

public class JMultiIconButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1020768791803605896L;

	
    public JMultiIconButton() {
        super (null, null);
    }

    public JMultiIconButton (final Icon icon) {
        super (null, icon);
    }

    public JMultiIconButton (final String text) {
        super (text, null);
    }

    public JMultiIconButton (final Action action) {
        super (action);
    }

    public JMultiIconButton (final String text, final Icon icon) {
        super (text, icon);
    }
	
	
	public void addIcon (final Icon extraIcon) {
		setIcon (this.getIcon() == null ? extraIcon
				: GraphicsUtil.mergeIcons (getIcon(), extraIcon, getIconTextGap(), this));
	}
}
