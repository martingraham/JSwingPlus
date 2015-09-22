package util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import swingPlus.shared.border.ShadowBorder;
import util.GraphicsUtil;
import util.Messages;



public abstract class MyDialog extends JDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1947076957365665448L;
	protected static final Font DIALOG_BUTTON_FONT = Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "buttonFont"));
	private static final Border BORDER = BorderFactory.createEmptyBorder (0, 5, 0, 5);
	private static final Border DEFAULT_POPUP_BORDER = UIManager.getBorder ("PopupMenu.border");
	private static final Insets DPBORDER_INSETS = DEFAULT_POPUP_BORDER.getBorderInsets (null);
	private static final Border CLOSE_BORDER = BorderFactory.createMatteBorder (DPBORDER_INSETS.top, DPBORDER_INSETS.left, DPBORDER_INSETS.bottom, DPBORDER_INSETS.right, Color.red);
	private static final Border ETCHEDSHADOW = BorderFactory.createCompoundBorder (
				ShadowBorder.BORDER, 
				BorderFactory.createCompoundBorder (DEFAULT_POPUP_BORDER, BORDER)
	);
	private static final Border CLOSESHADOW = BorderFactory.createCompoundBorder (
			ShadowBorder.BORDER, 
			BorderFactory.createCompoundBorder (CLOSE_BORDER, BORDER)
	);
	
	private static final ActionListener DEFAULT_CLOSE_ACTION = new ActionListener () {
        public void actionPerformed (final ActionEvent aEvent) {
        	if (aEvent.getSource() instanceof JComponent) {
	        	final JComponent comp = (JComponent)aEvent.getSource();
	        	final JDialog dialog = (JDialog)comp.getTopLevelAncestor();
	            dialog.setVisible (false);
	            dialog.dispose();
        	}
        }
	};
	
	private JComponent userContent, optionBox;
	protected JButton closeButton;
	private ActionListener closeAction;

	
	public MyDialog (final Frame frame, final boolean modal) {
        super (frame, modal);
        
        setUndecorated (true);
  
        final JPanel buttonContainer = new JPanel ();
        buttonContainer.setLayout (new BoxLayout (buttonContainer, BoxLayout.Y_AXIS));
        buttonContainer.setBorder (ETCHEDSHADOW);
        buttonContainer.setFont (DIALOG_BUTTON_FONT);
        //buttonContainer.setBackground (buttonContainer.getBackground().brighter());
        setContentPane (buttonContainer);
        
        userContent = new JPanel ();
        userContent.setLayout (new BoxLayout (userContent, BoxLayout.Y_AXIS));
        userContent.setOpaque (false);
        userContent.setAlignmentX (Component.CENTER_ALIGNMENT);
        add (userContent);
        
        optionBox = new JPanel ();  
        optionBox.setOpaque (false);
        closeButton = new MyButton (Messages.getString (MyDialog.class, "closeText"));
        closeButton.setOpaque (false);
        closeButton.setAlignmentX (Component.CENTER_ALIGNMENT);
        closeButton.addMouseListener (
        	new MouseAdapter () {
        		@Override
				public void mouseExited (final MouseEvent mEvent) {
        			buttonContainer.setBorder (ETCHEDSHADOW);
        		}
        		
        		@Override
				public void mouseEntered (final MouseEvent mEvent) {
        			buttonContainer.setBorder (CLOSESHADOW);
         		}
        	}
        );
        
        optionBox.add (closeButton);
        optionBox.setAlignmentX (Component.CENTER_ALIGNMENT);
        add (optionBox);

        setCloseAction (DEFAULT_CLOSE_ACTION); 
    }
	
	/**
	 * Configurable close action
	 * e.g. Maybe we only want to hide the dialog rather than dispose of it completely
	 * @param aListener
	 */
	public void setCloseAction (final ActionListener aListener) {
		if (closeAction != null) {
			closeButton.removeActionListener (closeAction);
		}
		closeAction = aListener;
		closeButton.addActionListener (closeAction); 
	}
	
	
	public JComponent getUserPanel () { return userContent; }
	
	public JComponent getOptionBox () { return optionBox; }
	
	public void makeVisible (final Point point) {
		pack ();
		final Point popupLoc = (point == null)
			? MouseInfo.getPointerInfo().getLocation()
			: point;

		adjustPopupLocationToFitScreen (popupLoc);
        setLocation (popupLoc);
		setVisible (true);	
	}
	
    /**
     * Returns an point which has been adjusted to take into account of the 
     * desktop bounds, taskbar and multi-monitor configuration.
     * <BARLEY_PATTERN>
     * Taken and adapted from JPopUpMenu class
     */
    private Point adjustPopupLocationToFitScreen (final Point point) {

        if (GraphicsEnvironment.isHeadless()) {
            return point;
        }

        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        Rectangle screenBounds;
        Insets screenInsets;
        GraphicsConfiguration gConfig = null;
        // Try to find GraphicsConfiguration, that includes mouse
        // pointer position
        final GraphicsEnvironment gEnviron =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gDevice = gEnviron.getScreenDevices();
        for(int i = 0; i < gDevice.length; i++) {
            if(gDevice[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                final GraphicsConfiguration dgc =
                    gDevice[i].getDefaultConfiguration();
                if(dgc.getBounds().contains(point)) {
                    gConfig = dgc;
                    break;
                }
            }
        }

        // If not found and we have invoker, ask invoker about his gc
        if(gConfig == null && this.getOwner() != null) {
            gConfig = getOwner().getGraphicsConfiguration();
        }

        if(gConfig != null) {
            // If we have GraphicsConfiguration use it to get
            // screen bounds and insets
            screenInsets = toolkit.getScreenInsets(gConfig);
            screenBounds = gConfig.getBounds();
        } else {
            // If we don't have GraphicsConfiguration use primary screen
            // and empty insets
            screenInsets = new Insets(0, 0, 0, 0);
            screenBounds = new Rectangle(toolkit.getScreenSize());
        }

        final int scrWidth = screenBounds.width -
                    Math.abs(screenInsets.left+screenInsets.right);
        final int scrHeight = screenBounds.height -
                    Math.abs(screenInsets.top+screenInsets.bottom);

        Dimension size;

        size = this.getPreferredSize();
        //if (size.height > scrHeight) {
        //	setPreferredSize (new Dimension (size.width, scrHeight - 64));
        //	size = this.getPreferredSize();
        //}

        // Use long variables to prevent overflow
        final long pWidth = (long) point.x + (long) size.width;
        final long pHeight = (long) point.y + (long) size.height;

        if( pWidth > screenBounds.x + scrWidth ) {
             point.x = screenBounds.x + scrWidth - size.width;
        }

        if( pHeight > screenBounds.y + scrHeight) {
             point.y = screenBounds.y + scrHeight - size.height;
        }

        /* Change is made to the desired (X,Y) values, when the
           PopupMenu is too tall OR too wide for the screen
        */
        if (point.x < screenBounds.x) {
            point.x = screenBounds.x ;
        }
        if (point.y < screenBounds.y) {
            point.y = screenBounds.y;
        }

        return point;
    }
	
	
	public void close () {
		closeButton.doClick ();
	}
	
	protected static class MyCheckBox extends JCheckBox {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5281088712621988314L;

		public MyCheckBox (final String name, final boolean state) {
			super (name, state);

			setMargin (GraphicsUtil.ZEROINSETS);
			setBorder (null);
			setFont (DIALOG_BUTTON_FONT);
		}
	}
}

