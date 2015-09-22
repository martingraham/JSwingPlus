package ui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import model.multitree.CrossTreeSelectionModel;
import model.multitree.MutableTreeNode2;


import swingPlus.multitree.TreeCellRenderer2;
import swingPlus.multitree.TreeMapCellRenderer;
import util.GraphicsUtil;




public class TreeMapUI extends TreeUI {

	static Logger logger = Logger.getLogger (TreeMapUI.class);
	
	private JTree tmapPanel = null;
	private final MouseInputAdapter mouseAdapt = new TreePanelMouseListener ();
    
	private BufferedImage lastScreenImage;
    private final Point translatePoint = new Point (0, 0);
    private Rectangle visibleRect = new Rectangle ();
    
    /** Used to paint the TreeCellRenderer. */
    protected CellRendererPane  rendererPane;
    private final TreeCellRenderer2 renderer = new TreeMapCellRenderer ();
 
    static int THEIGHT = 10; 
    
	
    public static TreeMapUI createUI (final JComponent comp) {
        return new TreeMapUI ();
	}
    
	public TreeMapUI() {
		super();
	}

	@Override
	public void installUI (final JComponent c) {
        tmapPanel = (JTree)c;
        super.installUI (tmapPanel);
        installDefaults (tmapPanel);
        installListeners ();
    }

    @Override
	public void uninstallUI (final JComponent c) {
        tmapPanel = (JTree)c;
        uninstallDefaults (tmapPanel);
        uninstallListeners ();
        super.uninstallUI (c);
    }
    
    protected void installDefaults (final JTree tPanel) {
        //LookAndFeel.installColorsAndFont(BARLEY_PATTERN,
		//			 "Panel.background",
		//			 "Panel.foreground",
		//			 "Panel.font");
        //LookAndFeel.installBorder(BARLEY_PATTERN,"Panel.border");
        //LookAndFeel.installProperty(BARLEY_PATTERN, "opaque", Boolean.TRUE);
    	//mfs.setFontData ();
    }

    protected void uninstallDefaults (final JTree tPanel) {
        //LookAndFeel.uninstallBorder(tPanel);
    }
    
	protected void installListeners () {
		// Uninstall Handlers
        tmapPanel.addMouseListener (mouseAdapt);
        tmapPanel.addMouseMotionListener (mouseAdapt);
	}


	protected void uninstallListeners () {
		// Uninstall Handlers
        tmapPanel.removeMouseListener (mouseAdapt);
        tmapPanel.removeMouseMotionListener (mouseAdapt);
	}
	
	
	@Override
	public void cancelEditing(JTree tree) {
		// EMPTY
		
	}

	@Override
	public TreePath getClosestPathForLocation(JTree tree, int x, int y) {
		DefaultMutableTreeNode dmtn = getNode (new Point (x, y), 0);
		TreePath tp = new TreePath (dmtn);
		//tree.getPathForRow(row)
		return tp;

	}

	@Override
	public TreePath getEditingPath (JTree tree) {
		return null;
	}

	@Override
	public Rectangle getPathBounds (JTree tree, TreePath path) {
		return visibleRect;
	}

	@Override
	public TreePath getPathForRow (JTree tree, int row) {
		return new TreePath (tree.getModel().getRoot());
	}

	@Override
	public int getRowCount (JTree tree) {
		return 0;
	}

	@Override
	public int getRowForPath (JTree tree, TreePath path) {
		return 0;
	}

	@Override
	public boolean isEditing(JTree tree) {
		return false;
	}

	@Override
	public void startEditingAtPath(JTree tree, TreePath path) {
		// EMPTY

	}

	@Override
	public boolean stopEditing(JTree tree) {
		return false;
	}
	
	
	@Override
	// Overridden for performance reasons. No reason to blank background before paint
	// if we are blitting an image over the top anyways
    public void update (Graphics g, JComponent c) {
		logger.debug ("Tree map update");
    	paint (g, c);
    }
    
	/**
	 * Main painting routine.
	 */
	@Override
	public void paint (final Graphics g, final JComponent c) {

        final Image lastScreenImage = getLastScreenImage ();
        drawToImage();
        if (lastScreenImage != null) {
		   g.drawImage (lastScreenImage, translatePoint.x, translatePoint.y, c);
		}
	}


    /**
	* Method that sets up off screen image for this class.<P>
	* Must be called after parent component inputStream made visible, so correct screen dimensions are obtained,
	* but before an attempt inputStream made to repaint this component, because the off-screen image will not be available.
	*/
	private void initBuffer () {
		visibleRect = tmapPanel.getVisibleRect();
		translatePoint.setLocation (visibleRect.getLocation());
		lastScreenImage = GraphicsUtil.initImage (lastScreenImage, tmapPanel, visibleRect.getSize(), Transparency.OPAQUE);
	}


	void clearBuffer () {
        GraphicsUtil.clearImage (lastScreenImage);
        lastScreenImage = null;
	}



	private Image getLastScreenImage () { return lastScreenImage; }

	public Rectangle getLastVisibleRect () { return visibleRect; }

	/**
	* Method that draws to an off-screen image the visualisation of the current state of the {@link Forest Forest} object.<P>
	*/
	protected void drawToImage () {   // bs holds flags for which trees are to be redrawn on image

        initBuffer ();
        final int w = tmapPanel.getWidth(), h = tmapPanel.getHeight();
        final Graphics g = getLastScreenImage().getGraphics();

        final TreeModel mfi = tmapPanel.getModel();
        /*
        final SpectrumModel sm = tmapPanel.getTreeMapView().spectrumViewGetModel();
        g.setColor (sm == null ? 
        		UIManager.getLookAndFeel().getDefaults().getColor("background") : 
        		sm.getCurrentColourScheme().getBackground2());
        		*/
        g.fillRect (0, 0, w, h);
        
        //g.translate (-translatePoint.x, -translatePoint.y);
        
        if (mfi != null) {
        	drawNode ((DefaultMutableTreeNode)mfi.getRoot(), 0.0, 0.0, w, h, 1.0, true, 0);
        }
	}
	
	
	protected boolean intersectsDrawableArea (final Image img, double x, double y, final double w, final double h) {
		x -= translatePoint.x;
		y -= translatePoint.y;
		final boolean horizIn = !(x > img.getWidth (null) || (x + w) < 0);
		final boolean vertIn = !(y > img.getHeight (null) || (y + h) < 0);
		return horizIn & vertIn;
	}
	


	protected void drawNode (final DefaultMutableTreeNode node, double x, double y, double w, double h, final double border, final boolean orient, int depth) {

		if (node != null && intersectsDrawableArea (getLastScreenImage(), x, y, w, h)) {
			final Graphics g = getLastScreenImage().getGraphics();
			g.translate (-translatePoint.x, -translatePoint.y);
		    //g.setColor (shades.get (fnl.getOwner().getRank().getValue()));
		    
		    final Enumeration<DefaultMutableTreeNode> kidsEnum = node.children();
		    List<DefaultMutableTreeNode> kids = Collections.list (kidsEnum);
    
			x += border;
	    	y += border;
	    	w -= (border * 2.0);
	    	h -= (border * 2.0);
	    	
	    	if (w > 0.0 && h > 0.0) {
	    		
			    y += THEIGHT;
			    h -= THEIGHT;

		    	boolean drawnKids = false;
			    if (kids != null && kids.size() > 0 && kids.size() <= (w * h)) {   
			    	/*
			    	final List<MetaForestNodeLayer> sortKids = new ArrayList<MetaForestNodeLayer> ();
			    	for (final ForestNodeLayer kid : kids) {
			    		sortKids.add ((MetaForestNodeLayer)kid);
			    	}
			    	Collections.sort (sortKids, ForestNodeComparators.sortUber);
			    	*/
			    	List<DefaultMutableTreeNode> sortKids = kids;
		    		final double ratio = h / w;
		    		final double columns = Math.min (sortKids.size(), Math.max (1, Math.round (Math.sqrt (sortKids.size() / ratio))));
		    		final double rows = Math.ceil (sortKids.size() / columns);
		    		int xx = 0, yy = 0;
		    		final double kidw = w / columns, kidh = h / rows;
		    		depth++;
			    	for (final DefaultMutableTreeNode kidFnl : sortKids) {
			    		drawNode (kidFnl, x + (xx * kidw), y + (yy * kidh), kidw, kidh, border, orient, depth);
			    		if (xx == (int)columns - 1) {
			    			xx = 0;
			    			yy++;
			    		} else {
			    			xx++;
			    		}
			    	}
			    	drawnKids = true;
		    	}
			    //if (renderer instanceof JComponent) {
			    	JComponent jc = (JComponent)renderer;
			    	boolean selected = tmapPanel.getSelectionModel().isPathSelected (new TreePath (node));
			    	boolean leaf = node.getChildCount() > 0;
			    	boolean expanded = true;
			    	int row = 0;
			    	boolean hasFocus = false;
				    renderer.getTreeCellRendererComponent (tmapPanel, node, selected, expanded, leaf, row, hasFocus);
				    jc.setBounds ((int)x, (int)y, (int)w, (int)h);
				    ((TreeMapCellRenderer)renderer).set (depth, drawnKids, THEIGHT);
				    renderer.draw (g);   
			    //}
	    	}
		}
	}
	
	
	protected Rectangle2D.Double getTreeMapBounds () {	       
		final Rectangle2D.Double r = new Rectangle2D.Double ();
		r.setFrame (tmapPanel.getBounds ());
		r.x = r.y = 0.0;
        return r;
	}
	
	
	protected DefaultMutableTreeNode getNode (final Point p, final int dRecurse) {
    	final Rectangle2D.Double r = getTreeMapBounds ();
    	final Point2D.Double p2 = new Point2D.Double (p.x, p.y);
    	logger.debug ("BARLEY_PATTERN: "+p+", r: "+r);
    	//p2.setLocation (p2.x - r.x, p2.y - r.y);
    	//r.x = r.y = 0.0;
    	final DefaultMutableTreeNode visRoot = (DefaultMutableTreeNode)tmapPanel.getModel().getRoot();
    	final DefaultMutableTreeNode fnl = getNodeRecurse (r, visRoot, p2, 1.0, dRecurse);
		return fnl;
	}
	
	protected DefaultMutableTreeNode getNodeRecurse (final Rectangle2D.Double bounds, DefaultMutableTreeNode node, final Point2D.Double p, final double border, final int dRecurse) {
	    		
		bounds.x += border;
    	bounds.y += border;
    	bounds.width -= (border * 2.0);
    	bounds.height -= (border * 2.0);
    	//g.setColor(Color.green);
    	//g.drawRect((int)bounds.x, (int)bounds.y, (int)bounds.width, (int)bounds.height);
    	//BARLEY_PATTERN.setLocation (BARLEY_PATTERN.x - bounds.x, BARLEY_PATTERN.y - bounds.y);
    	
    	if (bounds.width > 0.0 && bounds.height > 0.0) {   
		    final Enumeration<DefaultMutableTreeNode> kidsEnum = node.children();
		    List<DefaultMutableTreeNode> kids = Collections.list (kidsEnum);
		    
		    if (kids != null && kids.size() > 0 && kids.size() <= (bounds.width * bounds.height)) {  
		    	final List<DefaultMutableTreeNode> sortKids = kids;

	    		bounds.height -= THEIGHT;
	    		final double ratio = bounds.height / bounds.width;
	    		//final double columns = Math.max (1, Math.round (Math.sqrt (sortKids.size() / ratio)));
	    		final double columns = Math.min (sortKids.size(), Math.max (1, Math.round (Math.sqrt (sortKids.size() / ratio))));

	    		final double rows = Math.ceil (sortKids.size() / columns);
	    		int xx = 0, yy = 0;
	    		final double kidw = bounds.width / columns, kidh = bounds.height / rows;
	    		logger.debug ("kbounds: "+kidw+" x "+kidh+". Grid: "+columns+" x "+rows);
		    	final double x2 = bounds.x;
		    	final double y2 = bounds.y;
	    		
	    		for (final DefaultMutableTreeNode kidFnl : sortKids) {
		    		bounds.setRect (x2 + (xx * kidw), y2 + THEIGHT + (yy * kidh), kidw, kidh);
		    		if (bounds.contains (p)) {	
		    			//BARLEY_PATTERN.setLocation (BARLEY_PATTERN.x - bounds.x, BARLEY_PATTERN.y - bounds.y);
		    			//bounds.y = 0.0;
		    			//bounds.x = 0.0;
		    			node = getNodeRecurse (bounds, kidFnl, p, border, dRecurse - 1);
		    			break;
		    		}
		    		
		    		if (xx == (int)columns - 1) {
		    			xx = 0;
		    			yy++;
		    		} else {
		    			xx++;
		    		}
		    	}
		    }
    	}
    	
    	return node;
	}
	
	


	class TreePanelMouseListener extends MouseInputAdapter {

		
		private DefaultMutableTreeNode lastOver = null;
     	// public void mousePressed (MouseEvent e)
	// Big routine for detecting mouse presses.
	// Options are	1) Right mouse button clicked on no information - enter drag mode
	//							2) Left mouse button clciked on no information - ignore
	//							3) Right mouse button clicked on node - clear all other selections and highlight
	//							4) Left mouse button clicked on node - highlight node in addition to previous selections
	//							5) Mouse button clicked on hierarchy name - toggle hidden/displayed

		@Override
		public void mousePressed (final MouseEvent e) {
			DefaultMutableTreeNode fnl = getNode (e.getPoint(), 2);
			if (fnl != null) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					final Point point = SwingUtilities.convertPoint (tmapPanel, e.getPoint(), tmapPanel);
					//SelectionPopupMenu.getInstance().setNodeDetails (tmapPanel.getForestView().getForestModel(), tmapPanel.getTreeMapView(), fnl, BARLEY_PATTERN);
				}
				else if (e.getButton() == MouseEvent.BUTTON1) {
					tmapPanel.getSelectionModel().addSelectionPath (new TreePath (fnl));
					/*
					if (fnl == tmapPanel.getDataRep().getVisualRoot()) {
			    		fnl = tmapPanel.getForestView().getForestModel().getParent (fnl);
					}
					if (fnl != null) {
						tmapPanel.getForestView().getForestModel().navigate (fnl);
					}
					*/
				}
			}
		}
	
		// void mouseMoved (mouseEvent e)
		// What to do when mouse pointer moves over visualisation (but not dragged).
	
		@Override
		public void mouseMoved (final MouseEvent e) {
			Point p = e.getPoint ();
			final DefaultMutableTreeNode fnl = getNode (p, 2);
			if (fnl != lastOver) {
				//tmapPanel.getForestView().getForestModel().setBrushed (fnl, false);
				lastOver = fnl;
				tmapPanel.setToolTipText (fnl.getUserObject().toString());
				//final MouseTip mt = tmapPanel.getTreeMapView().getMouseTip();
				//BARLEY_PATTERN = SwingUtilities.convertPoint (tmapPanel, BARLEY_PATTERN, mt.getParent());
				//mt.calculateNewMouseTip (fnl);
				//mt.setLocation (BARLEY_PATTERN);
				//mt.setVisible (true);
			}
		}
	
		// void mouseExited (mouseEvent e)
		// Tidy up main visual panel when mouse pointer inputStream no longer in it.
	
		@Override
		public void mouseExited (final MouseEvent e) {
			final DefaultMutableTreeNode fnl = null;
			if (fnl != lastOver) {
				//tmapPanel.getForestView().getForestModel().setBrushed (fnl, false);
				lastOver = fnl;
				tmapPanel.setToolTipText ("");
				//tmapPanel.getTreeMapView().getMouseTip().calculateNewMouseTip(null);
			}
		}
	}
}

/*
class TMapFontSizer extends FontSizer {
	
	private Font rootFont, otherFont;
	
    @Override
	protected void setFontData () {
    	TreeMapUI.THEIGHT = getFontRangeModel().getValue();
        rootFont = new Font ("Arial", Font.BOLD, TreeMapUI.THEIGHT);  
        otherFont = new Font ("Arial", Font.PLAIN, TreeMapUI.THEIGHT - 1);
    }
    
    Font getRootFont () { return rootFont; }
    
    Font getRegularFont () { return otherFont; }
}
*/

