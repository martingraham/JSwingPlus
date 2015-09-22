package util.swing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import util.IconCache;


public class CloseableTabComponent extends JPanel {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7421248927072908899L;
	
	private final static Icon CLOSE_ICON = IconCache.makeIcon ("DeleteTabIcon");
	private final static Icon CLOSE_OVER_ICON = IconCache.makeIcon ("DeleteTabIcon");

	public CloseableTabComponent (final JTabbedPane jtp, final JComponent component, final String title, final Icon icon) {
		super ();

		final JLabel label = new JLabel (title);
		label.setIcon (icon);
		
		final JButton button = new MyButton (CLOSE_ICON);	
		button.setRolloverIcon (CLOSE_OVER_ICON);
		button.setBorder(null);
		button.setOpaque (false);
		button.setIconTextGap (0);
		button.setToolTipText ("Close");
		button.setPreferredSize (new Dimension (button.getIcon().getIconWidth(), button.getIcon().getIconHeight()));
		
		setLayout (new FlowLayout (FlowLayout.CENTER, 4, 0));
		
		setOpaque (false);
		add (label);
        add (button);
 
        button.addActionListener(
    		new ActionListener () {
    			public void actionPerformed (final ActionEvent aEvent) {
    				final int tabIndex = jtp.indexOfComponent (component);
    				jtp.removeTabAt (tabIndex);
    			}
    		}
        );
	}
	
	static public void addCloseableTab (final JTabbedPane jtp, final JComponent component, final String title, final Icon icon) {
		jtp.addTab (title, icon, component);
		final int index = jtp.indexOfComponent (component);
		//System.err.println ("comp count: "+jtp.getComponentCount()+", index: "+index);
		//for (int n = 0; n < jtp.getComponentCount(); n++) {
		//	System.err.println ("TAbComponent "+n+" : "+jtp.getComponentAt(n));
		//}
		final CloseableTabComponent closeTab = new CloseableTabComponent (jtp, component, title, icon);
		jtp.setTabComponentAt (index, closeTab);
	}
}
