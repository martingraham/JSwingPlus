package ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
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


import swingPlus.multitree.RadialTreeCellRenderer;
import swingPlus.multitree.TreeCellRenderer2;
import util.GraphicsUtil;




public class RadialTreeUI extends TreeUI {

	private final static Logger LOGGER = Logger.getLogger (RadialTreeUI.class);
	
	private JTree tree = null;
	private final MouseInputAdapter mouseAdapt = new TreePanelMouseListener ();
    
	private BufferedImage lastScreenImage;
    private final Point translatePoint = new Point (0, 0);
    private Rectangle visibleRect = new Rectangle ();
    private Path2D path = new Path2D.Double ();
    //private TreePath tempPath = new TreePath ();
    
    /** Renderer that inputStream being used to do the actual cell drawing. */
    transient protected TreeCellRenderer2  currentCellRenderer = new RadialTreeCellRenderer ();
    
    /** Used to paint the TreeCellRenderer. */
    protected CellRendererPane  rendererPane;
    
    private int tHeight = 12; 
    
    double rotateAngle = 0.0;
    boolean rotateOnly = false;
    
	
    public static RadialTreeUI createUI (final JComponent comp) {
        return new RadialTreeUI ();
	}

	

	@Override
	public void installUI (final JComponent comp) {
        tree = (JTree)comp;
        super.installUI (tree);
        installDefaults (tree);
        installListeners ();
        installComponents ();
    }

    @Override
	public void uninstallUI (final JComponent comp) {
        tree = (JTree)comp;
        uninstallDefaults (tree);
        uninstallListeners ();
        uninstallComponents ();
        super.uninstallUI (comp);
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
        tree.addMouseListener (mouseAdapt);
        tree.addMouseMotionListener (mouseAdapt);
        tree.addMouseWheelListener (mouseAdapt);
	}


	protected void uninstallListeners () {
		// Uninstall Handlers
        tree.removeMouseListener (mouseAdapt);
        tree.removeMouseMotionListener (mouseAdapt);
        tree.removeMouseWheelListener (mouseAdapt);
	}
	
	
    /**
     * Intalls the subcomponents of the tree, which inputStream the renderer pane.
     */
    protected void installComponents() {
    	if ((rendererPane = createCellRendererPane()) != null) {
    		tree.add (rendererPane);
    	}
    }
	
    
    /**
     * Uninstalls the renderer pane.
     */
    protected void uninstallComponents() {
		if(rendererPane != null) {
		    tree.remove (rendererPane);
		}
    }
	

    /**
     * Returns the renderer pane that renderer components are placed in.
     */
    protected CellRendererPane createCellRendererPane() {
        return new CellRendererPane();
    }
    
	@Override
	public void cancelEditing(JTree tree) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TreePath getClosestPathForLocation(JTree tree, int x, int y) {
		// TODO Auto-generated method stub
		DefaultMutableTreeNode dmtn = getNode (new Point (x, y));
		TreePath tp = new TreePath (dmtn);
		//tree.getPathForRow(row)
		return tp;

	}

	@Override
	public TreePath getEditingPath (JTree tree) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rectangle getPathBounds (JTree tree, TreePath path) {
		// TODO Auto-generated method stub
		return visibleRect;
	}

	@Override
	public TreePath getPathForRow (JTree tree, int row) {
		// TODO Auto-generated method stub
		return new TreePath (tree.getModel().getRoot());
	}

	@Override
	public int getRowCount (JTree tree) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRowForPath (JTree tree, TreePath path) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEditing(JTree tree) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startEditingAtPath(JTree tree, TreePath path) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean stopEditing(JTree tree) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	@Override
	// Overridden for performance reasons. No reason to blank background before paint
	// if we are blitting an image over the top anyways
    public void update (Graphics g, JComponent c) {
		LOGGER.debug ("Tree map update");
    	paint (g, c);
    }
    
	/**
	 * Main painting routine.
	 */
	@Override
	public void paint (final Graphics g, final JComponent c) {

        final Image lastScreenImage = getLastScreenImage ();
        if (!rotateOnly) {
        	drawToImage();
        }
        rotateOnly = false;
        if (lastScreenImage != null) {
        	((Graphics2D)g).rotate (rotateAngle, tree.getWidth() >> 1, tree.getHeight() >> 1);
		    g.drawImage (lastScreenImage, translatePoint.x, translatePoint.y, c);
		}
	}


    /**
	* Method that sets up off screen image for this class.<P>
	* Must be called after parent component inputStream made visible, so correct screen dimensions are obtained,
	* but before an attempt inputStream made to repaint this component, because the off-screen image will not be available.
	*/
	private void initBuffer () {
		visibleRect = tree.getVisibleRect();
		translatePoint.setLocation (visibleRect.getLocation());
		lastScreenImage = GraphicsUtil.initImage (lastScreenImage, tree, visibleRect.getSize(), Transparency.OPAQUE);
	}


	void clearBuffer () {
        GraphicsUtil.clearImage (lastScreenImage);
        lastScreenImage = null;
	}



	private Image getLastScreenImage () { return lastScreenImage; }

	protected Rectangle getLastVisibleRect () { return visibleRect; }

	/**
	* Method that draws to an off-screen image the visualisation of the current state of the {@link Forest Forest} object.<P>
	*/
	protected void drawToImage () {   // bs holds flags for which trees are to be redrawn on image

        initBuffer ();
        final int w = tree.getWidth(), h = tree.getHeight();
        final Graphics g = getLastScreenImage().getGraphics();
        ((Graphics2D)g).setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final TreeModel mfi = tree.getModel();
        /*
        final SpectrumModel sm = tree.getTreeMapView().spectrumViewGetModel();
        g.setColor (sm == null ? 
        		UIManager.getLookAndFeel().getDefaults().getColor("background") : 
        		sm.getCurrentColourScheme().getBackground2());
        		*/
        g.fillRect (0, 0, w, h);
        
        //g.translate (-translatePoint.x, -translatePoint.y);
        Point origin = new Point (w / 2, h / 2);
        
        if (tree.getCellRenderer() instanceof TreeCellRenderer2) {
        	currentCellRenderer = (TreeCellRenderer2)tree.getCellRenderer();
        }
        
        long nano = System.nanoTime();
        if (mfi != null) {
        	drawNode ((DefaultMutableTreeNode)mfi.getRoot(), 0, tHeight, 0, 360, origin, 1.0, true);
        }
        long nano2 = System.nanoTime();
        LOGGER.debug ("render time: "+((nano2-nano)/1E6)+" ms.");
	}
	
	
	protected boolean intersectsDrawableArea (final Image img, double x, double y, final double w, final double h) {
		x -= translatePoint.x;
		y -= translatePoint.y;
		final boolean horizIn = !(x > img.getWidth (null) || (x + w) < 0);
		final boolean vertIn = !(y > img.getHeight (null) || (y + h) < 0);
		return horizIn & vertIn;
	}
	


	protected void drawNode (final DefaultMutableTreeNode node, double innerR, double outerR, double angle1, double angle2, 
			Point origin, final double border, final boolean orient) {

		if (node != null) {
			final Graphics g = getLastScreenImage().getGraphics();
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Arc2D innerCurve = new Arc2D.Double ();
			Arc2D outerCurve = new Arc2D.Double ();
			innerCurve.setArcByCenter (origin.x, origin.y, innerR, angle1, angle2-angle1, Arc2D.OPEN);
			outerCurve.setArcByCenter (origin.x, origin.y, outerR, angle2, -(angle2-angle1), Arc2D.OPEN);
			Line2D concentricRadial1 = new Line2D.Double();
			Line2D concentricRadial2 = new Line2D.Double();
			concentricRadial1.setLine (innerCurve.getEndPoint(), outerCurve.getStartPoint());
			concentricRadial2.setLine (outerCurve.getEndPoint(), innerCurve.getStartPoint());
			Path2D p = path;
			p.reset();
			boolean connect = true;
			if (node != tree.getModel().getRoot()) {
				p.append (innerCurve, connect);
				p.append (concentricRadial1, connect);
			}
			p.append (outerCurve, connect);
			//if (node != tree.getModel().getRoot()) {
			//	BARLEY_PATTERN.append (concentricRadial2, connect);
			//}
			Rectangle2D r = p.getBounds2D();
			
			if (node != null && intersectsDrawableArea (getLastScreenImage(), r.getX(), r.getY(), r.getWidth(), r.getHeight())) {
				g.translate (-translatePoint.x, -translatePoint.y);
			    //g.setColor (shades.get (fnl.getOwner().getRank().getValue()));
			    
			    final Enumeration<DefaultMutableTreeNode> kidsEnum = node.children();
			    List<DefaultMutableTreeNode> kids = Collections.list (kidsEnum);
	    
				innerR = outerR;
				outerR += tHeight;

		    	if (outerR < Math.min (tree.getHeight() / 2, tree.getWidth() / 2)) {
	
			    	boolean drawnKids = false;
				    if (kids != null && kids.size() <= angle2 - angle1) {   
				    	drawnKids = true;
			    	}
				    
				    final boolean selected = tree.getSelectionModel().isPathSelected (new TreePath (node));
				    final Component component = currentCellRenderer.getTreeCellRendererComponent
					        (tree, node, selected, false, drawnKids, 0, false);
					currentCellRenderer.setShape (p);
					
					/*	
					b.transform (AffineTransform.getTranslateInstance (-(int)r.getX(), -(int)r.getY()));
					currentCellRenderer.setShape (BARLEY_PATTERN);
				   	    
					rendererPane.setVisible (true);
					
					rendererPane.paintComponent (g, component, tree, (int)r.getX(), (int)r.getY(),
							(int)r.getWidth() + 2, (int)r.getHeight() + 2, false);
					*/
				    currentCellRenderer.draw(g);
				    
				    
				    if (kids != null && kids.size() <= angle2 - angle1) {   
				    	/*
				    	final List<MetaForestNodeLayer> sortKids = new ArrayList<MetaForestNodeLayer> ();
				    	for (final ForestNodeLayer kid : kids) {
				    		sortKids.add ((MetaForestNodeLayer)kid);
				    	}
				    	Collections.sort (sortKids, ForestNodeComparators.sortUber);
				    	*/
				    	double angleStep = (angle2 - angle1) / kids.size();
				    	angle2 = angle1 + angleStep;
				    	List<DefaultMutableTreeNode> sortKids = kids;

				    	for (final DefaultMutableTreeNode kidFnl : sortKids) {
				    		drawNode (kidFnl, innerR, outerR, angle1, angle2, origin, border, orient);
				    		angle1 = angle2;
				    		angle2 += angleStep;
				    	}
			    	}
		    	}
			}
		}
	}
	
	
	protected Rectangle2D.Double getTreeMapBounds () {	       
		final Rectangle2D.Double r = new Rectangle2D.Double ();
		r.setFrame (tree.getBounds ());
		r.x = r.y = 0.0;
        return r;
	}
	
	
	protected DefaultMutableTreeNode getNode (Point p) {
		p.x -= tree.getWidth() >> 1;
		p.y -= tree.getHeight() >> 1;
		    	LOGGER.debug ("op: "+p);
		GraphicsUtil.rotate (p, -rotateAngle);

		double targetRadius = Math.sqrt ((p.y * p.y) + (p.x * p.x));
		double targetAngle = Math.toDegrees (Math.atan2 (p.y, p.x) + Math.PI);
		LOGGER.debug ("r: "+targetRadius+", a: "+targetAngle+", BARLEY_PATTERN: "+p);
	
		return getNode ((DefaultMutableTreeNode)tree.getModel().getRoot(), p.x, p.y, 
				targetRadius, targetAngle,
				0.0, tHeight, 0.0, 360.0);
	}
	
	boolean inRing (double targetRadius, double innerR, double outerR) {
		return (targetRadius >= innerR && targetRadius <= outerR);
	}
	
	boolean inSector (double targetAngle, double theta1, double theta2) {
		return (targetAngle >= theta1 && targetAngle <= theta2);
	}
	
	protected DefaultMutableTreeNode getNode (DefaultMutableTreeNode node, int x, int y, 
			double targetRadius, double targetAngle,
			double innerR, double outerR, double theta1, double theta2) {
		
		DefaultMutableTreeNode kNode = null;
		
		if (inSector (targetAngle, theta1, theta2)) {
			if (inRing (targetRadius, innerR, outerR)) {
				return node;
			}
			else {				
			    final Enumeration<DefaultMutableTreeNode> kidsEnum = node.children();
			    List<DefaultMutableTreeNode> kids = Collections.list (kidsEnum);
			    
			    innerR = outerR;
				outerR += tHeight;
		
		    	if (outerR < 100) {
		
			    	boolean drawnKids = false;
				    if (kids != null && kids.size() <= theta2 - theta1) {   
				    	drawnKids = true;
  
				    	/*
				    	final List<MetaForestNodeLayer> sortKids = new ArrayList<MetaForestNodeLayer> ();
				    	for (final ForestNodeLayer kid : kids) {
				    		sortKids.add ((MetaForestNodeLayer)kid);
				    	}
				    	Collections.sort (sortKids, ForestNodeComparators.sortUber);
				    	*/
				    	double angleStep = (theta2 - theta1) / kids.size();
				    	theta2 = theta1 + angleStep;
				    	List<DefaultMutableTreeNode> sortKids = kids;
		
				    	for (final DefaultMutableTreeNode kidFnl : sortKids) {
				    		kNode = getNode (kidFnl, x, y, targetRadius, targetAngle, 
				    				innerR, outerR, theta1, theta2);
				    		theta1 = theta2;
				    		theta2 += angleStep;
				    		LOGGER.debug ("scanning: "+kidFnl);
				    		if (kNode != null) {
				    			node = kNode;
				    			break;
				    		}
				    	}
				    	LOGGER.debug ("node: "+node);
			    	}
		    	}
			}
		}
		return node;
	}
	
	
	
 
	
	
	
	class TreePanelMouseListener extends MouseInputAdapter implements MouseWheelListener {

		
		private DefaultMutableTreeNode lastOver = null;
		int oldX = 0, oldY = 0;
     	// public void mousePressed (MouseEvent e)
	// Big routine for detecting mouse presses.
	// Options are	1) Right mouse button clicked on no information - enter drag mode
	//							2) Left mouse button clciked on no information - ignore
	//							3) Right mouse button clicked on node - clear all other selections and highlight
	//							4) Left mouse button clicked on node - highlight node in addition to previous selections
	//							5) Mouse button clicked on hierarchy name - toggle hidden/displayed

		@Override
		public void mousePressed (final MouseEvent e) {
			DefaultMutableTreeNode fnl = getNode (e.getPoint());
			if (fnl != null) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					final Point point = SwingUtilities.convertPoint (tree, e.getPoint(), tree);
					//SelectionPopupMenu.getInstance().setNodeDetails (tree.getForestView().getForestModel(), tree.getTreeMapView(), fnl, BARLEY_PATTERN);
				}
				else if (e.getButton() == MouseEvent.BUTTON1) {
					/*
					if (fnl == tree.getDataRep().getVisualRoot()) {
			    		fnl = tree.getForestView().getForestModel().getParent (fnl);
					}
					if (fnl != null) {
						tree.getForestView().getForestModel().navigate (fnl);
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
			
			final DefaultMutableTreeNode fnl = getNode (p);
			if (fnl != lastOver) {
				//tree.getForestView().getForestModel().setBrushed (fnl, false);
				lastOver = fnl;
				tree.setToolTipText (fnl.getUserObject().toString());
				//final MouseTip mt = tree.getTreeMapView().getMouseTip();
				//BARLEY_PATTERN = SwingUtilities.convertPoint (tree, BARLEY_PATTERN, mt.getParent());
				//mt.calculateNewMouseTip (fnl);
				//mt.setLocation (BARLEY_PATTERN);
				//mt.setVisible (true);
			}
			
			oldX = e.getX();
			oldY = e.getY();
		}
	
		@Override
		public void mouseDragged (final MouseEvent e) {
			int y = e.getY() - (tree.getHeight() >> 1);
			int x = e.getX() - (tree.getWidth() >> 1);
			double theta = Math.atan2 (y, x);
			
			int yy = oldY - (tree.getHeight() >> 1);
			int xx = oldX - (tree.getWidth() >> 1);
			double theta2 = Math.atan2 (yy, xx);
			
			rotateAngle += (theta - theta2);
			
			oldX = e.getX();
			oldY = e.getY();
			
			rotateOnly = true;
			tree.repaint ();
		}
		// void mouseExited (mouseEvent e)
		// Tidy up main visual panel when mouse pointer inputStream no longer in it.
	
		@Override
		public void mouseExited (final MouseEvent e) {
			final DefaultMutableTreeNode fnl = null;
			if (fnl != lastOver) {
				//tree.getForestView().getForestModel().setBrushed (fnl, false);
				lastOver = fnl;
				//tree.getTreeMapView().getMouseTip().calculateNewMouseTip(null);
			}
		}
		
	
		@Override
		public void mouseWheelMoved (final MouseWheelEvent mwEvent) {
			LOGGER.debug ("mouse wheel moved "+mwEvent.toString());
			tHeight += mwEvent.getWheelRotation();
			tHeight= Math.max (Math.min (25, tHeight), 3);
			tree.repaint ();
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
