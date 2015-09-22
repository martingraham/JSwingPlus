package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.CellRendererPane;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.PanelUI;

import org.apache.log4j.Logger;

import model.graph.Edge;
import model.graph.GraphModel;
import model.graph.GraphSelectionModel;


import swingPlus.graph.GraphCellRenderer;
import swingPlus.graph.GraphEdgeRenderer;
import swingPlus.graph.JGraph;
import swingPlus.graph.ObjectPlacement;
import swingPlus.shared.CellRendererPane2;
import swingPlus.shared.JLogSlider;
import swingPlus.shared.KineticCoast;
import swingPlus.shared.border.DashedBorder;
import util.GraphicsUtil;
import util.IconCache;

import util.Messages;
import util.anim.AnimActionBase;
import util.anim.AnimTimer;
import util.anim.RangeTransformer;
import util.anim.SineTransform;
import util.colour.ColorUtilities;
import util.colour.ColourScheme;


public class GraphUI extends PanelUI {
	
	private static final Class<GraphUI> CLASSTYPE = GraphUI.class;
	private static final Logger LOGGER = Logger.getLogger (CLASSTYPE);
    private static final Cursor AREASELECTCURSOR = Toolkit.getDefaultToolkit().createCustomCursor
		(((ImageIcon)IconCache.makeIcon ("AreaSelectCursor")).getImage(), new Point (3, 2), "AreaSelect");
    private final static double RADDEG = 360.0 / (2.0 * Math.PI);
    public final static double SCALE_MIN = 0.03, SCALE_MAX = 10.0;   
    
    public final static int NO_EDGES_SMALLER_THAN = 144; // 12 squared

    
    private final MouseAdapter mouseAdapt = new TreePanelMouseListener ();
    private final ComponentAdapter compAdapt = new MyComponentAdapter ();
    //private final BasicStroke rectOutline = new BasicStroke (2.0f);

    private JGraph graph;
    //private Line2D line = new Line2D.Double ();
    private boolean antiAliasFlag = false;
    private BufferedImage lastScreenImage, fadeInImage, fadeOutImage;
    private final JLabel dragLabel = new JLabel ();
    
    private final Point translatePoint = new Point (0, 0);
    private double oldScale = 1.0, scale = 1.0;
    private final Point offsetPoint = new Point (0, 0);
    private final ScaledMovement sMove = new ScaledMovement ();
    
    private OffScreenIndicator offScreenIndicatorInstance;
     
    private List<Rectangle> labelAreas;
    private List<Object> objectDrawOrder;
 	
    protected CellRendererPane rendererPane;
    
    private static Color transBrushLink = new Color (255, 255, 0, 128);
    
    private JLogSlider scaleSlider;
	ChangeListener sliderListener = new ChangeListener () {
		@Override
		public void stateChanged (final ChangeEvent event) {
			final JLogSlider jsl = (JLogSlider)event.getSource();
			final double scale = jsl.getScaleFromValue ();
			setScale (scale, new Point (graph.getWidth() / 2, graph.getHeight()  / 2));
			LOGGER.debug (jsl.getScaleFromValue());
		}
	};
	private final Double ZERO = Double.valueOf (0.0);
	
    protected Insets edgeInsets = new Insets (0, 0, 0, 0);
    
	public static GraphUI createUI (final JComponent comp) {
		return new GraphUI();
	}


	@Override
	public void installUI (final JComponent comp) {
        super.installUI (comp);
        graph = (JGraph)comp;
        installDefaults (graph);
        installListeners ();
    }

    @Override
	public void uninstallUI (final JComponent comp) {
    	graph = (JGraph)comp;
        uninstallDefaults (graph);
        uninstallListeners ();
        super.uninstallUI (comp);
        graph = null;
    }

    protected void installDefaults (final JComponent tPanel) {
        //LookAndFeel.installColorsAndFont(BARLEY_PATTERN,
		//			 "Panel.background",
		//			 "Panel.foreground",
		//			 "Panel.font");
        //LookAndFeel.installBorder(BARLEY_PATTERN,"Panel.border");
        //LookAndFeel.installProperty(BARLEY_PATTERN, "opaque", Boolean.TRUE);
    	//setColours ();
    	
    	graph.add (dragLabel);
    	setDragLabel ();
		dragLabel.setFont (Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, CLASSTYPE, "selectBoxFont")));
		dragLabel.setHorizontalAlignment (SwingConstants.CENTER);
		
        rendererPane = new CellRendererPane2();
        graph.add (rendererPane);
		//dragLabel.
        setSlider (new JLogSlider (JSlider.VERTICAL, -5, 3));
        
        offScreenIndicatorInstance = new OffScreenIndicator ();
        
        labelAreas = new ArrayList<Rectangle> ();
        objectDrawOrder = new ArrayList<Object> ();
    }

    protected void uninstallDefaults (final JComponent tPanel) {
        //LookAndFeel.uninstallBorder(tPanel);
    	graph.remove (dragLabel);
        graph.remove (rendererPane);
        rendererPane = null;
        removeSlider ();
    }


	protected void installListeners () {
		// Uninstall Handlers
		graph.addMouseListener (mouseAdapt);
		graph.addMouseMotionListener (mouseAdapt);
		graph.addMouseWheelListener (mouseAdapt);
		graph.addComponentListener (compAdapt);
	}


	protected void uninstallListeners () {
		// Uninstall Handlers
		graph.removeMouseListener (mouseAdapt);
		graph.removeMouseMotionListener (mouseAdapt);
		graph.removeMouseWheelListener (mouseAdapt);
		graph.removeComponentListener (compAdapt);
	}

	protected void uninstallKeyboardActions() {
		// empty method
	}

	/**
	 * Uninstalls the renderer pane.
	 */
	protected void uninstallComponents() {
		// empty method
	}

	public void setSlider (final JLogSlider newSlider) {
		if (scaleSlider != null) {
			newSlider.setMinimum (scaleSlider.getMinimum ());
			newSlider.setMaximum (scaleSlider.getMaximum ());
			newSlider.setValue (scaleSlider.getValue ());
			removeSlider ();
		}
		
		scaleSlider = newSlider;
		
		if (scaleSlider != null) {
			scaleSlider.addChangeListener (sliderListener);
			if (scaleSlider.getParent() != null) {
				scaleSlider.getParent().remove (scaleSlider);
			}
			graph.add (scaleSlider);
			graph.addPropertyChangeListener (scaleSlider);
			scaleSlider.setLocation (20, 20);
			scaleSlider.setSize (30, 128);
		}
	}
	
	void removeSlider () {
		scaleSlider.removeChangeListener (sliderListener);
		graph.removePropertyChangeListener (scaleSlider);
		graph.remove (scaleSlider);
	}
	
	//
	// Painting routines.
	//

	@Override
	// Overridden for performance reasons. No reason to blank background before paint
	// if we are blitting an image over the top anyways
    public void update (final Graphics graphics, final JComponent comp) {
    	paint (graphics, comp);
    }
	
	
	/**
	 * Main painting routine.
	 */
	@Override
	public void paint (final Graphics graphics, final JComponent comp) { 
		initBuffer ();
		LOGGER.debug ("drawing");
		final Graphics gDraw = GraphicsUtil.primeImageGraphics (graphics, graphics.getClipBounds(), lastScreenImage, graph.getBackground(), UIConstants.ALL);
		antiAliasFlag = GraphicsUtil.isNonScreenImage ((Graphics2D)graphics);
		drawToImage (gDraw);

		if (!GraphicsUtil.isSVGGraphics (graphics)) {
			graphics.drawImage (lastScreenImage, translatePoint.x, translatePoint.y, comp);
		}
	}


    /**
	* Method that sets up off screen image for this class.<P>
	* Must be called after parent component inputStream made visible, so correct screen dimensions are obtained,
	* but before an attempt inputStream made to repaint this component, because the off-screen image will not be available.
	*/
	protected void initBuffer () {
		final Rectangle rect = new Rectangle (0, 0, 0, 0);
		if (graph != null) {
			graph.computeVisibleRect (rect);
		}
		final Dimension dims = rect.getSize();
		translatePoint.setLocation (rect.getLocation());	
		lastScreenImage = GraphicsUtil.initImage (lastScreenImage, graph, dims, Transparency.OPAQUE);
		fadeInImage = GraphicsUtil.initImage (fadeInImage, graph, dims, Transparency.TRANSLUCENT);
		fadeOutImage = GraphicsUtil.initImage (fadeOutImage, graph, dims, Transparency.TRANSLUCENT);
		
		//logger.info ("lastScreenImage "+lastScreenImage.getCapabilities (gc))
	}


	void clearBuffer () {
        GraphicsUtil.clearImage (lastScreenImage);
        GraphicsUtil.clearImage (fadeInImage);
        GraphicsUtil.clearImage (fadeOutImage);
        lastScreenImage = null;
        fadeInImage = null;
        fadeOutImage = null;
	}


	Image getLastScreenImage () { return lastScreenImage; }

	
	/**
	* Method that draws to an off-screen image the visualisation of the current state of the {@link Forest Forest} object.<P>
	*/
	protected void drawToImage (final Graphics gDraw) {
		
        //synchronized (graph) {
        	
		final long nano = System.nanoTime ();

	        final Graphics2D g2D = (Graphics2D)gDraw;
	        setAntiAlias (g2D, antiAliasFlag);

			// AlphaComposite.SrcAtop causes Batik SVG error
			// See https://issues.apache.org/bugzilla/show_bug.cgi?id=26466
	        // g2D.setComposite (AlphaComposite.SrcAtop);     
 
	        g2D.translate (translatePoint.x, translatePoint.y);
       
	        offScreenIndicatorInstance.reset ();
	        objectDrawOrder.clear ();
	        labelAreas.clear ();
	        
	        drawEdges (g2D, false);
	        final long nano3 = System.nanoTime();
	        drawNodes (g2D, false);
	        final long nano4 = System.nanoTime();
	        drawEdges (g2D, true);
	        final long nano5 = System.nanoTime();
	        drawNodes (g2D, true);
	        //long nano6 = System.nanoTime();
	        //graph.getRepulsiveForceCalculator().reveal (g2D, this, new Point2D.Double (0, 0));
	        
	        offScreenIndicatorInstance.draw (g2D);
	        /*
	        if (!antiAliasFlag) {
	        	
	    		final Image images[] = {fadeOutImage, fadeInImage};
	    		final AlphaComposite composites[] = {TransparentLabelUI.getFadeOut(), TransparentLabelUI.getFadeIn()};
	           
	    		for (int n = 0; n < images.length; n++) {
	    			final Image element = images [n];
	    			if (element != null && composites[n].getAlpha() > 0.0f) {
	    				g2D.setComposite (composites [n]);
	    				g2D.drawImage (element, 0, 0, panel);		
	    			}
	    		}
	    		//g2D.setComposite (AlphaComposite.SrcAtop);      	
	        }
	        g2D.setComposite (AlphaComposite.SrcAtop);    
	        */  
	        	        
	        setAntiAlias (g2D, false);
	        
	       // long nano2 = System.nanoTime();
	        if (LOGGER.isDebugEnabled()) {
		        LOGGER.debug (((nano3-nano)+(nano5-nano4))/1e6+" milliseconds for repaint edges");
		        LOGGER.debug ((nano4-nano3)/1e6+" milliseconds for repaint nodes");
	        }
        //}
	}
	
	
	protected void drawEdges (final Graphics2D g2D, final boolean isSelected) {
		
		final GraphModel graphModel = graph.getFilteredModel();
		final Set<Object> nodes = graphModel.getNodes ();
		final Iterator<Object> nodeIterator = nodes.iterator();
        int edgeCount = 0;
        Rectangle rect = g2D.getClipBounds ();
        if (rect == null) {
        	rect = graph.getVisibleRect ();
        }
        scaleandPanRect2NodeRect (rect);
        final GraphSelectionModel gsm = graph.getSelectionModel();
        final boolean showAllNodeEdges = graph.isShowEdges();
        g2D.setColor (Color.black);
        
        if (graph.getModel().getEdgeCount() < 500) {
            g2D.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
        while (nodeIterator.hasNext()) {
        	final Object node = nodeIterator.next ();
        	final boolean showThisNodeEdges = gsm.isSelected (node);
        	
        	if ((!isSelected && showAllNodeEdges) || (isSelected && showThisNodeEdges)) {
        		final Set<Edge> edges = graphModel.getEdges (node);
        		final Iterator<Edge> edgeIterator = edges.iterator ();
	        	
	        	while (edgeIterator.hasNext()) {
	        		final Edge edge = edgeIterator.next ();
	        		final boolean isSourceNode = !(edge.getNode2() == node) && (edge.getNode1() == node || edge.getNode1().equals (node));
	        		final boolean showOtherNodeEdges = gsm.isSelected (isSourceNode ? edge.getNode2() : edge.getNode1());
	        		final boolean bothNodesSelected = showThisNodeEdges & showOtherNodeEdges;
	        		//if (isSourceNode || 
	        		//	(isSelected && !isSourceNode && !showAllNodeEdges && showThisNodeEdges && !gsm.isSelected (edge.getNode1()))
	        		//	) { // don't draw edges twice
	        		if ((bothNodesSelected && isSelected) ^ (!isSelected && !bothNodesSelected && showAllNodeEdges)) {
		        		//gsm.setSelected (edge, bothNodesSelected);
	        			drawEdge (g2D, edge, rect);
		        		edgeCount++;
	        		}
	        	}
        	}
        }
        
        rendererPane.removeAll ();
        
        g2D.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    	
        if (LOGGER.isDebugEnabled()) {
        	LOGGER.debug ("Edgecount: "+edgeCount+" : "+isSelected);
        }
	}
	
	
	protected void drawEdge (final Graphics2D g2D, final Edge edge, final Rectangle visibleGraphArea) {
		final Object node1 = edge.getNode1();
		final Object node2 = edge.getNode2();
		
		final ObjectPlacement op1 = graph.getVisualRep (node1);
		final ObjectPlacement op2 = graph.getVisualRep (node2);
		
		final Point2D.Double loc1 = op1.getLocation ();
		final Point2D.Double loc2 = op2.getLocation ();
		
		if (visibleGraphArea.intersectsLine (loc1.x, loc1.y, loc2.x, loc2.y)) {
			final int x1 = nodeXtoScaleandPanX (loc1.getX());
			final int y1 = nodeYtoScaleandPanY (loc1.getY());
			final int x2 = nodeXtoScaleandPanX (loc2.getX());
			final int y2 = nodeYtoScaleandPanY (loc2.getY());
			final int d2 = ((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1));
			if (d2 >= NO_EDGES_SMALLER_THAN) {	// don't draw really small edges, occluded by the node reps usually anyways
				final GraphEdgeRenderer renderer = graph.getDefaultEdgeRenderer (edge.getEdgeObject().getClass());
				final Component component = graph.prepareEdgeRenderer (renderer, edge, x1, y1, x2, y2);
				edgeInsets = ((JComponent)component).getInsets (edgeInsets);
		        
		        rendererPane.paintComponent (g2D, component, graph, 
		        	Math.min (x1, x2) - edgeInsets.left, 
		        	Math.min (y1, y2) - edgeInsets.top,
		        	Math.abs (x1 - x2) + 1 + edgeInsets.left + edgeInsets.right,
		        	Math.abs (y1 - y2) + 1 + edgeInsets.top + edgeInsets.bottom);
			}
		}
	}
	
	
	protected void drawNodes (final Graphics2D g2D, final boolean isSelected) {
		final GraphModel graphModel = graph.getFilteredModel();
		final Set<Object> nodes = graphModel.getNodes ();
        LOGGER.debug ("Filtered nodes: "+nodes.size());
        final Iterator<Object> nodeIterator = nodes.iterator(); 
        Rectangle rect = g2D.getClipBounds ();
        if (rect == null) {
        	rect = graph.getVisibleRect ();
        }
        scaleandPanRect2NodeRect (rect);
        final GraphSelectionModel gsm = graph.getSelectionModel();
        
        g2D.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        while (nodeIterator.hasNext()) {
        	final Object node = nodeIterator.next ();
        	if (gsm.isSelected (node) == isSelected) {
        		drawNode (g2D, node, rect);
        	}
        }
        
        g2D.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        rendererPane.removeAll ();
	}
	
	protected void drawNode (final Graphics2D g2D, final Object node, final Rectangle visibleGraphArea) {
		final ObjectPlacement objPlacement = graph.getVisualRep (node);
		if (objPlacement != null) {
			final Point2D.Double location = objPlacement.getLocation ();
			
			if (visibleGraphArea.contains (location) && node != null) {
				//System.err.println ("node: "+node);
				final GraphCellRenderer renderer = graph.getDefaultNodeRenderer (node.getClass());
				final Component component = graph.prepareNodeRenderer (renderer, node);
				final Dimension prefSize = component.getPreferredSize();
				final int width = prefSize.width;
				final int height = prefSize.height;
				
				final double minZoomX = component.getMinimumSize().getWidth() / width;
				final double minZoomY = component.getMinimumSize().getHeight() / height;
				final double scaleMin = Math.max (scale, Math.min (minZoomX, minZoomY));
				//System.out.println ("sm: "+scaleMin);
		        //g2D.scale (scale, scale);
				final int scaledWidth = (int)(width * scaleMin);
				final int scaledHeight = (int)(height * scaleMin);
				final Rectangle rect = new Rectangle (nodeXtoScaleandPanX ((int)location.getX()) - (scaledWidth >> 1), 
		        		nodeYtoScaleandPanY ((int)location.getY()) - (scaledHeight >> 1),
		        		scaledWidth, scaledHeight);
				
				// Validate renderers that have sub-components so they get the correct sizes
				if (component instanceof Container && ((Container)component).getComponentCount() > 0) {
					rendererPane.paintComponent (g2D, component, graph, rect.x, rect.y, rect.width, rect.height, true);
				} else {
					rendererPane.paintComponent (g2D, component, graph, rect);
				}
		       
		        labelAreas.add (rect);
		        objectDrawOrder.add (node);
		        //g2D.scale(1.0/scale, 1.0/scale);
			}
			else {
				final int outcode = visibleGraphArea.outcode (location);
				offScreenIndicatorInstance.incOutcodeCounter (outcode);
			}
		}
	}
	

	
	/*
	final void setBeforeAfterShots (final Set<DisplayRepGraph> add, final Set<DisplayRepGraph> remove) {
		
		final Dimension d = panel.getVisibleRect().getSize();
		fadeInImage = GraphicsUtil.initImage (fadeInImage, panel, d, Transparency.TRANSLUCENT);
		fadeOutImage = GraphicsUtil.initImage (fadeOutImage, panel, d, Transparency.TRANSLUCENT);
		
		final Graphics2D gOut = setEmptyBackground (fadeOutImage, new Color (0, 0, 0, 0));
        final Graphics2D gIn = setEmptyBackground (fadeInImage, new Color (0, 0, 0, 0));
        gIn.translate (-translatePoint.x, -translatePoint.y);
        gOut.translate (-translatePoint.x, -translatePoint.y);
        final Container BARLEY_PATTERN = new Container ();
        BARLEY_PATTERN.setSize (fadeInImage.getWidth (panel), fadeInImage.getHeight (panel));
        BARLEY_PATTERN.setBounds (0, 0, fadeInImage.getWidth (panel), fadeInImage.getHeight (panel));
        
        for (final DisplayRepGraph drg : add) {
        	//LOGGER.debug ("add drg: "+drg.getBounds());
        	final DisplayRepPlus drp = (DisplayRepPlus) drg;
        	SwingUtilities.paintComponent (gIn, drp, BARLEY_PATTERN, drp.getBounds());
        	//drg.setBounds (drg.getBounds (0.0));
        	//LOGGER.debug ("add drg a: "+drg.getBounds());
        }
        
        for (final DisplayRepGraph drg : remove) {
        	final DisplayRepPlus drp = (DisplayRepPlus) drg;
        	final Rectangle r = drp.getBounds();
        	SwingUtilities.paintComponent (gOut, drp, BARLEY_PATTERN, drp.getBounds());	
        	drp.setBounds (r);
        }	
        
        gIn.translate (translatePoint.x, translatePoint.y);
        gOut.translate (translatePoint.x, translatePoint.y);
        
        //LOGGER.debug ("images+ :"+fadeInImage.getWidth()+", "+fadeOutImage.getWidth());
	}
	*/

	final void setAntiAlias (final Graphics2D g2d, final boolean bool) {		
		g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, 
				bool ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
	}
	
	final void setAntiAliasFlag (final boolean bool) {		
		antiAliasFlag = bool;
	}

	
	final void setColours (final ColourScheme colourScheme) {
	    transBrushLink = new Color (255, 255, 0, 128);
	    setDragLabel ();
	}
	
	final void setDragLabel () {
	    dragLabel.setBackground (transBrushLink);
		dragLabel.setForeground (Color.white);
		dragLabel.setBorder (new DashedBorder (transBrushLink, 
			new BasicStroke (2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.5f, new float[] {8.0f, 8.0f}, 0.0f)) );
	}
	
	
	public void nodePoint2ScaleandPanPoint (final Point point) {
        point.x = nodeXtoScaleandPanX (point.x);
        point.y = nodeYtoScaleandPanY (point.y);
	}
	
	public void scaleandPanPoint2NodePoint (final Point point) {
        point.x = scaleandPanXtoNodeX (point.x);
        point.y = scaleandPanYtoNodeY (point.y);
	}
	
	
	protected void nodeRect2ScaleandPanRect (final Rectangle rect) {
        rect.x = nodeXtoScaleandPanX (rect.x);
        rect.y = nodeYtoScaleandPanY (rect.y);
        rect.width = (int)(rect.width * scale);
        rect.height = (int)(rect.height * scale);
	}
	
	protected void scaleandPanRect2NodeRect (final Rectangle rect) {
        rect.x = scaleandPanXtoNodeX (rect.x);
        rect.y = scaleandPanYtoNodeY (rect.y);
        rect.width = (int)(rect.width / scale);
        rect.height = (int)(rect.height / scale);
	}

	
	private int nodeXtoScaleandPanX (final double x) {
		return (int)((x - offsetPoint.x) * scale);
	}

	private int nodeYtoScaleandPanY (final double y) {
		return (int)((y - offsetPoint.y) * scale);
	}

	private int scaleandPanXtoNodeX (final int x) {
		return (int) (x / scale) + offsetPoint.x;
	}

	private int scaleandPanYtoNodeY (final int y) {
		return (int) (y / scale) + offsetPoint.y;
	}
	
	public int logic2ViewCoordX (final double x) {
		return nodeXtoScaleandPanX (x);
	}

	public int logic2ViewCoordY (final double y) {
		return nodeYtoScaleandPanY (y);
	}

	
	protected void rotate (final Point2D.Double loc, final int angleDegrees, final int aboutx, final int abouty) {
		final double angleRad = angleDegrees / RADDEG;
		final double cos = Math.cos (angleRad);
		final double sin = Math.sin (angleRad);
		final double xx = ((loc.x - aboutx) * cos) - ((loc.y - abouty) * sin);
		final double yy = ((loc.x - aboutx) * sin) + ((loc.y - abouty) * cos);
		loc.setLocation (xx + aboutx, yy + abouty);
	}

	protected void rotateAll (final int angleDegrees) {

		final int x = graph.getWidth() / 2;
		final int y = graph.getHeight () / 2;
		final int aboutx = scaleandPanXtoNodeX (x);
		final int abouty = scaleandPanYtoNodeY (y);
		
		final Collection<ObjectPlacement> nodeLocations = graph.getObjectPlacementMapping().getAllPlacements();
		final Iterator<ObjectPlacement> nodeIterator = nodeLocations.iterator();
        
        while (nodeIterator.hasNext()) {
        	final ObjectPlacement op = nodeIterator.next();
        	rotate (op.getLocation(), angleDegrees, aboutx, abouty);
        }
	}
	
	public Object getNodeAt (final Point point) {
		Object obj = null;
		for (int n = objectDrawOrder.size(); --n >= 0 && obj == null;) {
			if (labelAreas.get(n).contains(point)) {
				obj = objectDrawOrder.get (n);
			}
		}
		//Object obj = graph.getNearestTo (scaleandPanPoint2NodePoint (BARLEY_PATTERN));
		return obj;
	}

	
	public void setScale (final double newScale, final Point zoomPoint) {
		scale = Math.max (SCALE_MIN, Math.min (SCALE_MAX, newScale));
		LOGGER.debug ("scale: "+scale+"\t oldScale: "+oldScale);
		final double deltaScale = (scale / oldScale) - 1.0;
		offsetPoint.setLocation (
			offsetPoint.getX() + ((zoomPoint.getX() * deltaScale) / scale),
			offsetPoint.getY() + ((zoomPoint.getY() * deltaScale) / scale)
		);
		//System.err.println ("sclae: "+scale);
		oldScale = scale;
		graph.firePropertyChange ("scale", ZERO, Double.valueOf (scale));
		graph.repaint ();
	}
	
	
	public double getScale () { return scale; } 
	
	public void moveTo (final Object obj, final Point objToHere) {
		sMove.moveTo (obj, objToHere);
	}

	public void fitTo (final Collection<Object> objs) {
		sMove.moveTo (objs);
	}
	
	protected void setMouseOverObject (final Object newObj) {
		((TreePanelMouseListener)mouseAdapt).setMouseOverObject(newObj);
	}
	
	
	
	class TreePanelMouseListener extends MouseAdapter {

		
		private Point lastPoint = null, secondLastPoint = new Point(), startPoint = null, endPoint = null;
		private long mouseEventTime, lastMouseEventTime;
		private Object selectedObject = null;
		private Object mouseOverObject = null;
		private boolean wasDragging = false;
		KineticCoast kTimer = new KineticCoast (20, 0.05, true);
     	// public void mousePressed (MouseEvent e)
		// Big routine for detecting mouse presses.
		// Options are	1) Right mouse button clicked on no information - enter drag mode
		//							2) Left mouse button clciked on no information - ignore
		//							3) Right mouse button clicked on node - clear all other selections and highlight
		//							4) Left mouse button clicked on node - highlight node in addition to previous selections
		//							5) Mouse button clicked on hierarchy name - toggle hidden/displayed
	
		@Override
		public void mousePressed (final MouseEvent e) {
			kTimer.stop ();
			
			startPoint = e.getPoint ();
			lastPoint = e.getPoint ();
			//if (dragLabel.getParent() != graph) {
			//	graph.add (dragLabel);
			//	graph.setComponentZOrder (dragLabel, 0);
			//}
			//dragLabel.setVisible (true);
			
			final Point point = e.getPoint ();
			scaleandPanPoint2NodePoint (point); // adjust screen coords to node coords

			final Object obj = getNodeAt (e.getPoint ());	
			selectedObject = obj;
			if (obj != null) {
				final ObjectPlacement op = graph.getObjectPlacementMapping().getPlacement (obj);
				//GraphUI.this.scaleandPanPoint2NodePoint(BARLEY_PATTERN);
				if (op != null) {
					op.setForceInactive (true);
					point.setLocation (op.getLocation());
					nodePoint2ScaleandPanPoint (point);
					final double dsq = e.getPoint().distanceSq (point);
					LOGGER.debug ("BARLEY_PATTERN: "+point+"\td: "+dsq+"\t ");
					selectedObject = (dsq < 400.0 ? obj : null);
				}
			}
			LOGGER.debug ("so: "+selectedObject);
		}
	
		// void mouseMoved (mouseEvent e)
		// What to do when mouse pointer moves over visualisation (but not dragged).
		@Override
		public void mouseMoved (final MouseEvent event) {
			
			
			final Object obj = getNodeAt (event.getPoint ());	
			if (obj != mouseOverObject) {
				mouseOverObject = obj;
				//graph.setToolTipText (obj == null ? "" : obj.toString());
				
				if (obj != null) {
					final Graphics graphics = graph.getGraphics();
			        Rectangle r = graphics.getClipBounds ();
			        if (r == null) {
			        	r = graph.getVisibleRect ();
			        }
			        scaleandPanRect2NodeRect (r);
			        drawNode ((Graphics2D)graphics, obj, r);
				}
			}
 		}
	
		
		@Override
		public void mouseClicked (final MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON1) {
				if (selectedObject != null) {
					final GraphSelectionModel gsm = graph.getSelectionModel();
					gsm.setValueIsAdjusting (true);
					final boolean newSelectState = !gsm.isSelected (selectedObject);
					gsm.setSelected (selectedObject, newSelectState);
					
					final Set<Edge> edges = graph.getModel().getEdges (selectedObject);
					final Iterator<Edge> edgeIterator = edges.iterator ();
					while (edgeIterator.hasNext()) {
						final Edge edge = edgeIterator.next ();
						final Object otherNode = edge.getNode1().equals (selectedObject) ? edge.getNode2() : edge.getNode1();
						gsm.setSelected (otherNode, newSelectState);
					}
					
					gsm.setValueIsAdjusting (false);
				}
			}
			else if (event.getButton() == MouseEvent.BUTTON3) {
				if (mouseOverObject != null) {
					final Set<Edge> edges = graph.getFilteredModel().getEdges (mouseOverObject);
					
					if (!edges.isEmpty ()) {		
						graph.getRightClickMenu().setNodeDetails (graph, event.getPoint(), mouseOverObject);
					}
				}
			}
		}
		
		// void mouseExited (mouseEvent e)
		// Tidy up main visual panel when mouse pointer inputStream no longer in it.
	
		@Override
		public void mouseExited (final MouseEvent event) {
			final Component comp = event.getComponent();
			comp.setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));
			selectedObject = null;
		}
		
		@Override
		public void mouseEntered (final MouseEvent event) {
			LOGGER.info ("Entering DAG Panel");
			final Component comp = event.getComponent();
			comp.setCursor (AREASELECTCURSOR);
			selectedObject = null;
		}
		
		@Override
		public void mouseDragged (final MouseEvent event) {

			wasDragging = true;
			final Component comp = event.getComponent();
			comp.setCursor (Cursor.getPredefinedCursor (Cursor.MOVE_CURSOR));
			
			final int modifiers = event.getModifiersEx();
			
			if (selectedObject == null) {
				final double dragxoffset = (lastPoint.getX() - event.getX());
				final double dragyoffset = (lastPoint.getY() - event.getY());
				secondLastPoint.setLocation (lastPoint);
				
				lastMouseEventTime = (mouseEventTime == 0 ? event.getWhen() - 20 : mouseEventTime);
				mouseEventTime = event.getWhen();
				
				lastPoint.setLocation (event.getX(), event.getY());
				if ((modifiers & MouseEvent.BUTTON1_DOWN_MASK) > 0) {	// Action for left mouse drag on background
					offsetPoint.setLocation (offsetPoint.getX() + (dragxoffset / scale),
							offsetPoint.getY() + (dragyoffset / scale));
					graph.repaint();
				}
			}
			else if ((modifiers & MouseEvent.BUTTON1_DOWN_MASK) > 0) { // Action for left mouse drag on node
				final ObjectPlacement op = graph.getObjectPlacementMapping().getPlacement (selectedObject);
				if (op != null) {
					op.getLocation().setLocation (scaleandPanXtoNodeX (event.getX()), scaleandPanYtoNodeY (event.getY()));
				}
				graph.repaint ();
			}
		}

		
		@Override
		public void mouseReleased (final MouseEvent event) {
			final Component comp = event.getComponent();
			comp.setCursor (AREASELECTCURSOR);
			
			dragLabel.setVisible (false);
			endPoint = event.getPoint();
			
			if (selectedObject != null && !startPoint.equals(endPoint)) {
				final ObjectPlacement op = graph.getObjectPlacementMapping().getPlacement (selectedObject);	
				op.getLocation().setLocation (scaleandPanXtoNodeX (event.getX()), scaleandPanYtoNodeY (event.getY()));
				op.setForceInactive (false);
				graph.restartWorker ();
			} else if (wasDragging) {
				wasDragging = false;
				final double dragxoffset = (secondLastPoint.getX() - event.getX());
				final double dragyoffset = (secondLastPoint.getY() - event.getY());

				kTimer.startCoast (dragxoffset, dragyoffset, event.getWhen() - lastMouseEventTime, offsetPoint, scale, graph);
			}
		}

		@Override
		public void mouseWheelMoved (final MouseWheelEvent event) {
			if (scale >= SCALE_MIN && scale <= SCALE_MAX) {
				scale /= Math.pow (2, event.getWheelRotation() / 15.0);
				//System.err.println ("scale b : "+scale+", wr: "+event.getWheelRotation());
				scaleSlider.removeChangeListener (sliderListener);
				setScale (scale, event.getPoint());
				scaleSlider.addChangeListener (sliderListener);
				//System.err.println ("scale a: "+scale+", wr: "+event.getWheelRotation());
			}
		}
		
		public void setMouseOverObject (final Object newObj) {
			mouseOverObject = newObj;
		}
	}
	
	class MyComponentAdapter extends ComponentAdapter {
		
		final private Dimension oldSize = new Dimension (0, 0);
		@Override
		public void componentResized (final ComponentEvent event) {
			final Dimension newSize = event.getComponent().getSize();
			if (!oldSize.equals (newSize)) {
				oldSize.setSize (newSize);
				graph.repaint ();
			}
		}
	}
	

	

	
	/**
	 * 
	 * @author cs22
	 *
	 * Class that draws triangle arrows at compass points on screen
	 * along with a text version of counter stating how many nodes are
	 * off-screen in each particular direction
	 */
	class OffScreenIndicator {
		
		private final int[] outcodeCounters = new int [16];
		final private Shape outCodeTriangle = new Polygon (new int [] {0, 20, 0}, new int [] {0, 0, 20}, 3);
		final private Shape[] outCodeTriangles = new Shape [8];
		final private int[] outCode2TriangleIndex = new int[] {-1, 7, 1, 0, 3, -1, 2, -1, 5, 6, -1, -1, 4, -1, -1, -1};
		final private Color triangleFillColour = ColorUtilities.decodeWithAlpha (Messages.getString (GraphicsUtil.GRAPHICPROPS, GraphUI.class, "TriangleFillColour"));
		final private Color triangleDrawColour = ColorUtilities.decodeWithAlpha (Messages.getString (GraphicsUtil.GRAPHICPROPS, GraphUI.class, "TriangleDrawColour"));
		
		OffScreenIndicator () {
			final AffineTransform at = new AffineTransform ();
	        for (int n = 0; n < outCodeTriangles.length; n++) {
	        	outCodeTriangles [n] = at.createTransformedShape (outCodeTriangle);
	        	at.rotate (Math.PI / 4.0);
	        }
		}
		
		void reset () {
	        for (int n = 0; n < outcodeCounters.length; n++) {
	        	outcodeCounters [n] = 0;
	        }
		}
		
		void incOutcodeCounter (final int outcode) {
			outcodeCounters [outcode]++;
		}
		
		protected void draw (final Graphics2D g2D) {
			
			final AffineTransform originalTransform = g2D.getTransform();
			
			for (int n = 0; n < outcodeCounters.length; n++) {
				final int count = outcodeCounters [n];
				if (count > 0) {	
					
					final int x = ((n & Rectangle2D.OUT_LEFT) > 0) ? 0 :
							((n & Rectangle2D.OUT_RIGHT) > 0) ? graph.getWidth() - 1: graph.getWidth() / 2;
					final int y = ((n & Rectangle2D.OUT_TOP) > 0) ? 0 :
							((n & Rectangle2D.OUT_BOTTOM) > 0) ? graph.getHeight() - 1: graph.getHeight() / 2;
					
					g2D.translate(x, y);
					final int index = outCode2TriangleIndex [n];
					if (index >= 0 && index < outCodeTriangles.length) {
						final Shape s = outCodeTriangles [outCode2TriangleIndex [n]];
						g2D.setColor (triangleFillColour);
						g2D.fill (s);
						g2D.setColor (triangleDrawColour);
						g2D.draw (s);
						//g2D.translate(-x, -y);
						
						final String countString = Integer.toString (count);
						final Rectangle2D stringBounds = g2D.getFontMetrics().getStringBounds (countString, g2D);
						g2D.setColor (Color.black);
						final int dx = ((n & Rectangle2D.OUT_LEFT) > 0) ? 0 :
							((n & Rectangle2D.OUT_RIGHT) > 0) ? (int)-stringBounds.getWidth() : (int)-stringBounds.getWidth() / 2;
						final int dy = (int)(stringBounds.getHeight() * 3.0 / 4.0) + 
							(((n & Rectangle2D.OUT_TOP) > 0) ? 0 :
							((n & Rectangle2D.OUT_BOTTOM) > 0) ? (int)-stringBounds.getHeight() : (int)-stringBounds.getHeight() / 2
						);
								
						g2D.translate(-x, -y);
						g2D.drawString (countString, x + dx, y + dy);
						
					}
				}
			}
			
			g2D.setTransform (originalTransform);
		}
	}
	
	
	
	
	public class ScaledMovement {
		
		Point startCenter, endCenter, curCenter;
		double startZoom, endZoom;
		Rectangle visRect;
		Dimension viewMargin = new Dimension (180, 140);	// Boundary around rectangle when fitting to a collection of objects
		Timer anim;
		
		ScaledMovement () {
			startCenter = new Point ();
			endCenter = new Point ();
			curCenter = new Point ();
			startZoom = 1.0;
			endZoom = 1.0;
			
			anim = new AnimTimer (25, new MoveOffsetAnim (0.02, new SineTransform ()));
		}
		
		public void moveTo (final Object obj, Point underPoint) {
			if (obj != null) {
				final ObjectPlacement placement = graph.getObjectPlacementMapping().getPlacement (obj);
				
				if (placement != null) {
					final Point p = new Point ((int)placement.getLocation().getX(), (int)placement.getLocation().getY());
					
					visRect = graph.getVisibleRect ();
					
					if (underPoint == null) {
						underPoint = new Point ((int)visRect.getCenterX(), (int)visRect.getCenterY());
					}
					scaleandPanPoint2NodePoint (underPoint);
					scaleandPanRect2NodeRect (visRect);
					
					startCenter.setLocation (visRect.getCenterX(), visRect.getCenterY());
					endCenter.setLocation (visRect.getCenterX(), visRect.getCenterY());
					endCenter.translate (p.x - underPoint.x, p.y - underPoint.y);
					
					LOGGER.debug ("p: "+p);
					LOGGER.debug ("midPoint: "+underPoint);
					LOGGER.debug ("offsetPoint: "+offsetPoint);
					//offsetPoint.translate (p.x - underPoint.x, p.y - underPoint.y);
					LOGGER.debug ("offsetPoint: "+offsetPoint);
					
					startZoom = scale;
					endZoom = scale;
					
					// Make the target object the new mouseover object, so it is set up for the right click menu
					GraphUI.this.setMouseOverObject (obj);
					
					anim.restart ();
				}
			}
		}
		
		
		public void moveTo (final Collection<Object> objs) {
			
			if (objs != null) {
				Rectangle rect = null;
				final Iterator<Object> iObj = objs.iterator ();
				
				while (iObj.hasNext ()) {
					final Object node = iObj.next ();
					final ObjectPlacement placement = graph.getObjectPlacementMapping().getPlacement (node); 
					
					if (placement != null) {
						if (rect == null) {
							rect = new Rectangle ((int)placement.getLocation().getX(),
									(int)placement.getLocation().getY(),
									1, 1);
						}
						else {
							rect.add (placement.getLocation ());
						}
					}
				}

				
				if (rect != null) {	
					rect.grow (viewMargin.width, viewMargin.height);
					
					visRect = graph.getVisibleRect ();
					// Change screen bound coordinates to node space coordinates
					scaleandPanRect2NodeRect (visRect);
					
					final double yRatio = rect.getHeight() / visRect.getHeight();
					final double xRatio = rect.getWidth() / visRect.getWidth();
					
					startZoom = scale;
					endZoom = scale / Math.max (yRatio, xRatio);
	
					// Adjust end zoom to fit an exact point on JSlider, so no 'jerks' at end of animation
					endZoom = scaleSlider.roundedValue (endZoom);
								
					startCenter.setLocation ((int)visRect.getCenterX(), (int)visRect.getCenterY());
					endCenter.setLocation ((int)rect.getCenterX(), (int)rect.getCenterY());
							
					anim.restart ();
				}
			}
		}
		
		protected void setViewMargin (final Dimension newMargin) {
			viewMargin.setSize (newMargin);
		}
		
		protected Dimension getViewMargin () {
			return viewMargin;
		}
		
		
		
		class MoveOffsetAnim extends AnimActionBase {

	    	/**
			 * 
			 */
			private static final long serialVersionUID = 5718087044458098875L;

	    	MoveOffsetAnim (final double stepVal, final RangeTransformer newrt) {
	    		super (stepVal, newrt);
	    	}
	    	
	        @Override
			public void doWork (final ActionEvent evt) {
	        	final double per = this.getPercentTransition (MOVE) / 100.0;
	        	
	        	// Interpolate between the two center of view coordinates
	        	final int x = (int)((endCenter.x - startCenter.x) * per);
	        	final int y = (int)((endCenter.y - startCenter.y) * per); 	
	        	curCenter.setLocation (startCenter.x + x, startCenter.y + y);
	        	
	        	// Interpolate the zoom value
	        	final double zoom = (endZoom - startZoom) * per;
	        	scale = startZoom + zoom;
	        	oldScale = scale;
	        	
	        	// Using the current interpolated zoom value and center point
	        	// work out what the current top-left point would be (as that
	        	// is what node space coordinates are based on)
	        	final double curHalfWidth = (visRect.width * startZoom / scale) / 2.0;
	        	final double curHalfHeight = (visRect.height * startZoom / scale) / 2.0;
	        	offsetPoint.setLocation (curCenter.x - (int)curHalfWidth, 
	        			curCenter.y - (int)curHalfHeight);
	        	
	        	// Repaint
	            graph.repaint ();
	        }
	        
	        @Override
	        public void animationFinished () {
	        	//EMPTY
	        }
	        
	        @Override
			public void cleanUp () {
	        	super.cleanUp ();
	        	graph.firePropertyChange ("scale", ZERO, Double.valueOf (scale));
	        	graph.repaint ();
	        }
	    }
	}
}