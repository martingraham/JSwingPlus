package util.swing;


import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.TabbedPaneUI;



public class TabbedMoveHandler extends MouseAdapter {
	private int startX;
	private int startY;
	private final Container rPane;
	private static final GhostGlassPane GLASS_PANE = new GhostGlassPane();
	protected boolean allowPaletteDialogs;
	

	public TabbedMoveHandler (final JFrame jFrame) {
		super ();
		rPane = jFrame.getContentPane();
		jFrame.setGlassPane (GLASS_PANE);
		allowPaletteDialogs = false;
	}
	/* (non-Javadoc)
	* @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	*/
	@Override
	public void mousePressed (final MouseEvent mEvent) {
		if(! mEvent.isPopupTrigger()) {
			startX = mEvent.getX();
			startY = mEvent.getY();
			GLASS_PANE.initGlassPane ((JTabbedPane) mEvent.getSource(), mEvent.getPoint());
		}
	}
	
	@Override
	public void mouseDragged (final MouseEvent mEvent) {
		if(! mEvent.isPopupTrigger()) {
			final Point point = SwingUtilities.convertPoint((JComponent)mEvent.getSource(), mEvent.getPoint(), GLASS_PANE);
			GLASS_PANE.setPoint (point);
			GLASS_PANE.setVisible (true);
			GLASS_PANE.repaint ();
		}
	}
	
	/* (non-Javadoc)
	* @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	*/
	@Override
	public void mouseReleased (final MouseEvent mEvent) {
		if (!mEvent.isPopupTrigger()) {
			final JTabbedPane tabbedPane = (JTabbedPane) mEvent.getSource();
			JTabbedPane destPane = null;
			GLASS_PANE.setVisible (false);
			GLASS_PANE.setImage (null);
			
			if (tabbedPane.contains (mEvent.getPoint())) {
				destPane = tabbedPane;
			}
			else {		
				Point point = SwingUtilities.convertPoint (tabbedPane, mEvent.getPoint(), rPane);
				//System.out.println ("p: "+p);
				Component comp = rPane.getComponentAt(point);
				point = SwingUtilities.convertPoint (rPane, point, comp);
				Container con = null;
				
				while (comp instanceof Container && comp != con && !(comp instanceof JTabbedPane)) {
					con = (Container)comp;
					comp = con.getComponentAt (point);
					point = SwingUtilities.convertPoint (con, point, comp);
				}
				
				if (comp instanceof JTabbedPane) {
					destPane = (JTabbedPane)comp;
				}
			}
			
			if (destPane != null) {	
				TabbedPaneUI tPaneui = tabbedPane.getUI();
				final int startIndex = tPaneui.tabForCoordinate (tabbedPane, startX, startY);	
				
				tPaneui = destPane.getUI();
				final Point point = SwingUtilities.convertPoint (tabbedPane, mEvent.getPoint(), destPane);		
				int endIndex = tPaneui.tabForCoordinate (destPane, point.x, point.y);
				
				if (endIndex == -1) {
					final boolean bool = tPaneui.contains (destPane, point.x, point.y);
					if (bool && (point.y < 20 || destPane.getTabCount() == 0)) {
						endIndex = destPane.getTabCount() - (destPane.equals(tabbedPane) ? 1 : 0);
					}
				}	
				
				if (startIndex != -1 && endIndex != -1 && (startIndex != endIndex || !destPane.equals(tabbedPane))) {
					moveTab (tabbedPane, destPane, startIndex, endIndex);
					destPane.setSelectedIndex(endIndex);
				}
			}
			else {
				final Point point = SwingUtilities.convertPoint (tabbedPane, mEvent.getPoint(), rPane);
				if (rPane.getComponentAt (point) == null) {
					draggedOutOfFrame (tabbedPane, point);
				}
			}
		}
	}
	
	/**
	* @param tabbedPane
	* @param startIndex
	* @param endIndex
	*/
	private void moveTab (final JTabbedPane pane, final JTabbedPane destPane, final int src, final int dst) {
		// Get all the properties
		final Component comp = pane.getComponentAt(src);
		final String label = pane.getTitleAt(src);
		final Icon icon = pane.getIconAt(src);
		final Icon iconDis = pane.getDisabledIconAt(src);
		final String toolTipText = pane.getToolTipTextAt(src);
		final boolean enabled = pane.isEnabledAt(src);
		final int keycode = pane.getMnemonicAt(src);
		final int mnemonicLoc = pane.getDisplayedMnemonicIndexAt(src);
		final Color foreground = pane.getForegroundAt(src);
		final Color background = pane.getBackgroundAt(src);
		
		// Remove the tab
		pane.remove (src);
		
		// Add a new tab	
		destPane.insertTab (label, icon, comp, toolTipText, dst);
	    
		// Restore remaining properties
	    destPane.setDisabledIconAt (dst, iconDis);
	    destPane.setEnabledAt (dst, enabled);
	    destPane.setMnemonicAt (dst, keycode);
	    destPane.setDisplayedMnemonicIndexAt (dst, mnemonicLoc);
	    destPane.setForegroundAt (dst, foreground);
	    destPane.setBackgroundAt (dst, background);
	}
	
	
	protected void draggedOutOfFrame (final JTabbedPane srcTabPane, final Point mouseLoc) {
		if (isAllowPaletteDialogs ()) {
			final TabbedPaneUI tPaneui = srcTabPane.getUI();
			final int srcIndex = tPaneui.tabForCoordinate (srcTabPane, startX, startY);	
			final String title = srcTabPane.getTitleAt (srcIndex);
			
			final TabRefFrame tearOffWindow = new TabRefFrame ((JFrame)GLASS_PANE.getTopLevelAncestor(), title);
			tearOffWindow.setSrcTabbedPane (srcTabPane);
			
			mouseLoc.x = Math.max (0, Math.min (mouseLoc.x, 
					Toolkit.getDefaultToolkit().getScreenSize().width - 32) 
			);
			mouseLoc.y = Math.max (0, Math.min (mouseLoc.y, 
					Toolkit.getDefaultToolkit().getScreenSize().height - 32) 
			);
			
			
			final Component comp = srcTabPane.getComponentAt (srcIndex);
			tearOffWindow.setTitle (title);		
			tearOffWindow.add (comp, "Center");
			tearOffWindow.setLocation (mouseLoc);
			tearOffWindow.setVisible (true);
		}
	}
	
	public void addToTabbedPane (final JTabbedPane jtp) {
		jtp.addMouseListener (this);
		jtp.addMouseMotionListener (this);
	}
	
	public final boolean isAllowPaletteDialogs() {
		return allowPaletteDialogs;
	}
	
	/**
	 * Decides whether the contents of tabs dragged out of their parent frame get 
	 * turned into floating palette dialogs (which can be closed to restore as tabs)
	 * @param allowPaletteDialogs
	 */
	public final void setAllowPaletteDialogs (final boolean allowPaletteDialogs) {
		this.allowPaletteDialogs = allowPaletteDialogs;
	}
	
}

class GhostGlassPane extends JPanel {
	  /**
	 * 
	 */
	private static final long serialVersionUID = -6027589877442887236L;
	private final AlphaComposite composite;
	private Point location = new Point(0, 0);
	private BufferedImage dragged = null;
	  
	public GhostGlassPane() {
		 super ();
	      setOpaque(false);
	      composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	  }
	  protected void setImage(final BufferedImage dragged) {
	      this.dragged = dragged;
	  }
	  protected void setPoint(final Point location) {
	      this.location = location;
	  }
	  
	  @Override
	public void paintComponent(final Graphics graphics) {
	      if (dragged == null) return;
	      final Graphics2D graphics2D = (Graphics2D) graphics;
	      graphics2D.setComposite(composite);
	      final int draggedTabX = location.x - (dragged.getWidth (this) / 2);
	      final int draggedTabY = location.y - (dragged.getHeight (this) / 2);
	      graphics2D.drawImage(dragged, draggedTabX, draggedTabY , null);
	  }
	  
	  void initGlassPane (final JTabbedPane jtp, final Point point) {
	      //if(!hasGhost()) return;
	      final int dragTabIndex = jtp.indexAtLocation (point.x, point.y);

	      if (dragTabIndex >= 0) {
	          Rectangle rect = jtp.getBoundsAt (dragTabIndex);
	          BufferedImage image = new BufferedImage (jtp.getWidth(), jtp.getHeight(), BufferedImage.TYPE_INT_ARGB);
	          final Graphics graphics = image.getGraphics();
	          jtp.paint(graphics);
	          rect = rect.intersection (new Rectangle (0, 0, image.getWidth(), image.getHeight()));
	          image = image.getSubimage (rect.x, rect.y, rect.width, rect.height);
	          setImage(image);
	      }
	  }
	}


class TabRefFrame extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 809177297269202932L;
	WeakReference<JTabbedPane> srcTabbedPaneRef;
	Component tornComp;

	public TabRefFrame (final JFrame owner, final String title) {
		super (owner);
		setLayout (new BorderLayout());
	}
	
	public void setSrcTabbedPane (final JTabbedPane tabbedPane) {
		srcTabbedPaneRef = new WeakReference<JTabbedPane> (tabbedPane);
		
		this.addWindowListener (
			new WindowAdapter () {
				/**
			     * Invoked when a window is in the process of being closed.
			     * The close operation can be overridden at this point.
			     */
			    public void windowClosing (final WindowEvent wEvent) {
			    	if (srcTabbedPaneRef.get() != null) {
			    		srcTabbedPaneRef.get().addTab (TabRefFrame.this.getTitle(), tornComp);
			    		//srcTabbedPaneRef.get().addTab (TabRefFrame.this.getName(), tornComp);
			    		TabRefFrame.this.remove (tornComp);
			    		tornComp = null;
			    		TabRefFrame.this.dispose();
			    	}
			    }
			}
		);
	}
	
	public JTabbedPane getSrcTabbedPane () {
		return srcTabbedPaneRef.get();
	}
	
	@Override
	public void add (final Component comp, final Object constraints) {
		if (constraints == BorderLayout.CENTER) {
			tornComp = comp;
		}
		this.setSize (comp.getSize());
		super.add (comp, constraints);
	}
}

