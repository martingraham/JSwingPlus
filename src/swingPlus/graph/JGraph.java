package swingPlus.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.PanelUI;

import org.apache.log4j.Logger;

import model.graph.Edge;
import model.graph.GraphFilter;
import model.graph.GraphModel;
import model.graph.GraphModelEvent;
import model.graph.GraphModelListener;
import model.graph.GraphSelectionEvent;
import model.graph.GraphSelectionListener;
import model.graph.GraphSelectionModel;
import model.graph.impl.DefaultGraphFilter;
import model.graph.impl.DefaultGraphSelectionModel;
import swingPlus.graph.force.AttractiveForceCalculationInterface;
import swingPlus.graph.force.PositionUpdater;
import swingPlus.graph.force.RepulsiveForceCalculationInterface;
import swingPlus.graph.force.impl.BarnesHut2DForceCalculator;
import swingPlus.graph.force.impl.ForceBasedPositionUpdater;
import swingPlus.graph.force.impl.SimpleEdgeAttractor;
import ui.GraphUI;

public class JGraph extends JPanel implements GraphModelListener, GraphSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5336662906393060692L;
	private static final Logger LOGGER = Logger.getLogger (JGraph.class);
	
	private final static String UICLASSID = "GraphUI";
	static {
		UIManager.put (UICLASSID, "ui.GraphUI");
	}
	
	private Map <Class<?>, GraphCellRenderer> nodeRendererMap;
	private Map <Class<?>, GraphEdgeRenderer> edgeRendererMap;
	GraphCellRenderer defaultNodeRenderer = new DefaultGraphCellRenderer ();
	GraphEdgeRenderer defaultEdgeRenderer = new DefaultGraphEdgeRenderer ();
	GraphRendererToolTip rToolTip;
	
	GraphSelectionModel selectionModel;
	GraphFilter filter;

	RepulsiveForceCalculationInterface repulsiveForceCalculator;
	AttractiveForceCalculationInterface attractiveForceCalculator;
	PositionUpdater positionUpdater;

	GraphModel graph;
	GraphModel filteredGraph;

	DisplayThread calcThread;
	SwingWorker<Void, Void> worker;
	WindowListener winListener;
	
	SelectionPopupMenu rightClickMenu;
	
	ObjectPlacementMapping placementMapping;
	boolean showAllEdges;

	double strainLimit;
	
	
	Object lock = new Object ();

	
	public JGraph () {
		this (null);
	}
	
	public JGraph (final GraphModel model) {
		this (model, new BarnesHut2DForceCalculator (), new SimpleEdgeAttractor (), new ForceBasedPositionUpdater ());
	}
	
	public JGraph (final GraphModel model, final RepulsiveForceCalculationInterface rfCalc, 
			final AttractiveForceCalculationInterface afCalc, final PositionUpdater pUpdater) {
		super (false);
		
		calcThread = new DisplayThread (this);
		winListener = new GraphWindowAdapter ();
		
		final MouseAdapter mouseAdapter = new DefaultGraphCellToolTipListener ();
		this.addMouseListener (mouseAdapter);
		this.addMouseMotionListener (mouseAdapter);
		
		rToolTip = new GraphRendererToolTip (this);
		setModel (model);
		nodeRendererMap = new HashMap <Class<?>, GraphCellRenderer> ();
		edgeRendererMap = new HashMap <Class<?>, GraphEdgeRenderer> ();
		
		repulsiveForceCalculator = rfCalc;
		attractiveForceCalculator = afCalc;
		positionUpdater = pUpdater;
		showAllEdges = true;
		
		setRightClickMenu (SelectionPopupMenu.getInstance());
		
		setLayout (null);
	}
	
	
	@Override
    public void updateUI() {
		setUI((PanelUI)UIManager.getUI(this));
    }

    @Override
	public GraphUI getUI() {
        return (GraphUI)ui;
    }

    @Override
    public String getUIClassID() {
        return UICLASSID;
    }
    
    // http://forums.sun.com/thread.jspa?forumID=257&threadID=481481
    // DrLaszloJamf
    //called when added to window
    @Override
    public void addNotify() {
        super.addNotify();
        LOGGER.debug ("addNotify");
        SwingUtilities.windowForComponent(this).addWindowListener (winListener);
    }
 
    @Override
    public void removeNotify() {
        super.removeNotify();
        LOGGER.debug ("removeNotify");
        SwingUtilities.windowForComponent(this).removeWindowListener (winListener);
    }


    
    
	public void setModel (final GraphModel newGraph) {
		setStrainLimit (1.0);
		
		if (graph != null) {
			/*
			if (worker != null) {
				worker.cancel (true);
				worker = null;
			}
			*/
			calcThread.suspendMe ();
			selectionModel.clearSelection();
			getObjectPlacementMapping().clearMapping ();
		}
		
		graph = newGraph;
		
		if (graph != null) {
			try {
				filteredGraph = graph.getClass().newInstance();
			} catch (InstantiationException e) {
				LOGGER.error (e.toString(), e);
			} catch (IllegalAccessException e) {
				LOGGER.error (e.toString(), e);
			} //new DirectedGraphInstance ();
			setGraphFilter (new DefaultGraphFilter ());
			setSelectionModel (new DefaultGraphSelectionModel ());
			setObjectPlacementMapping (new BasicObjectPlacementMapping (getFilteredModel ()));
			//setObjectPlacementMapping (new BasicObjectPlacementMapping (graph));
			//worker = new ForceWorker<Void> (this);
			//worker.execute();
			
			if (calcThread.isSuspended()) {
				calcThread.resumeMe ();
			} else {
				calcThread.start();
			}
		}
	}
	
	
	
	public GraphModel getModel () { return graph; }
	
	
	public final GraphSelectionModel getSelectionModel() {
		return selectionModel;
	}

	public final void setSelectionModel (final GraphSelectionModel newModel) {
		final GraphSelectionModel oldModel = this.selectionModel;
		
		if (this.selectionModel != newModel
				&& this.selectionModel != null) {
			this.selectionModel.clearSelection();
			this.selectionModel.removeGraphSelectionListener (this);
		}
		this.selectionModel = newModel;
		this.selectionModel.addGraphSelectionListener (this);
		
		firePropertyChange ("selectionModel", oldModel, newModel);
	}
	
	
	public final GraphFilter getGraphFilter () {
		return filter;
	}

	public final void setGraphFilter (final GraphFilter filter) {
		if (filter != null) {
			final GraphFilter oldFilter = this.filter;
			this.filter = filter;
			updateFilteredModel ();
			firePropertyChange ("graphFilter", oldFilter, filter);
		}
	}
	
	
	public final GraphModel getFilteredModel () {
		return filteredGraph;
	}

	protected final void setFilteredModel (final GraphModel filteredGraph) {
		this.filteredGraph = filteredGraph;
	}
	
	
	public ObjectPlacementMapping getObjectPlacementMapping () {
		return placementMapping;
	}
	
	protected void setObjectPlacementMapping (final ObjectPlacementMapping opm) {
		placementMapping = opm;
	}
	

	
	public final void updateFilteredModel () {
		synchronized (lock) {
			calcFilteredModel ();
			if (getObjectPlacementMapping() != null) {
				getObjectPlacementMapping().updateMapping (getFilteredModel ());
			}
			firePropertyChange ("graphFilterModel", null, getFilteredModel ());
		}
	}
	
	
	protected void calcFilteredModel () {
		
		if (filteredGraph == null) {
			try {
				filteredGraph = graph.getClass().newInstance();
			} catch (InstantiationException e) {
				LOGGER.error (e.toString(), e);
			} catch (IllegalAccessException e) {
				LOGGER.error (e.toString(), e);
			}
		}
		
		final Set<Object> nodes = graph.getNodes();
		for (Object node : nodes) {
			if (filter.includeNode (node)) {
				filteredGraph.addNode (node);
			} else {
				filteredGraph.removeNode (node);
			}
		}
		
		final Set<Edge> edges = graph.getEdges();
		for (Edge edge : edges) {
			if (filter.includeEdge (edge)) {
				filteredGraph.addEdge (edge);
			} else {
				filteredGraph.removeEdge (edge);
			}
		}
	}
	
	public void setDefaultNodeRenderer (final Class<?> klass, final GraphCellRenderer klassRenderer) {
		nodeRendererMap.put (klass, klassRenderer);
	}
	
	public GraphCellRenderer getDefaultNodeRenderer (final Class<?> klass) {
		GraphCellRenderer klassRenderer = nodeRendererMap.get (klass);
		if (klassRenderer == null) {
			klassRenderer = defaultNodeRenderer;
		}
		return klassRenderer;
	}
	
    /**
     * Prepares the renderer by querying the data model for the
     * value and selection state
     * of the cell at <code>row</code>, <code>column</code>.
     * Returns the component (may be a <code>Component</code>
     * or a <code>JComponent</code>) under the event location.
     * 
     * During a printing operation, this method will configure the
     * renderer without indicating selection or focus, to prevent
     * them from appearing in the printed output. To do other
     * customizations based on whether or not the table is being
     * printed, you can check the value of
     * {@link javax.swing.JComponent#isPaintingForPrint()}, either here
     * or within custom renderers.
     * 
     * <labelBorder>Note:</labelBorder>
     * Throughout the table package, the internal implementations always
     * use this method to prepare renderers so that this default behavior
     * can be safely overridden by a subclass.
     *
     * @param renderer  the <code>GraphCellRenderer</code> to prepare
     * @param value     the Object to render
     * @return          the <code>Component</code> under the event location
     */
    public Component prepareNodeRenderer (final GraphCellRenderer renderer, final Object value) {

        boolean isSelected = false;
        boolean hasFocus = false;

        // Only indicate the selection and focused cell if not printing
        if (!isPaintingForPrint()) {
            isSelected = this.getSelectionModel().isSelected (value); //isCellSelected (row, column);
            hasFocus = isFocusOwner();
        }

        return renderer.getGraphCellRendererComponent (this, value, isSelected, hasFocus);
    }
	
	public ObjectPlacement getVisualRep (final Object obj) {
		return placementMapping.getPlacement (obj);
	}
	
	
	
	
	
	public void setDefaultEdgeRenderer (final Class<?> klass, final GraphEdgeRenderer klassRenderer) {
		edgeRendererMap.put (klass, klassRenderer);
	}
	
	public GraphEdgeRenderer getDefaultEdgeRenderer (final Class<?> klass) {
		GraphEdgeRenderer klassRenderer = edgeRendererMap.get (klass);
		
		if (klassRenderer == null) {
			klassRenderer = defaultEdgeRenderer;
		}
		return klassRenderer;
	}
	
    /**
     * Prepares the renderer by querying the data model for the
     * value and selection state
     * of the cell at <code>row</code>, <code>column</code>.
     * Returns the component (may be a <code>Component</code>
     * or a <code>JComponent</code>) under the event location.
     * 
     * During a printing operation, this method will configure the
     * renderer without indicating selection or focus, to prevent
     * them from appearing in the printed output. To do other
     * customizations based on whether or not the table is being
     * printed, you can check the value of
     * {@link javax.swing.JComponent#isPaintingForPrint()}, either here
     * or within custom renderers.
     * 
     * <labelBorder>Note:</labelBorder>
     * Throughout the table package, the internal implementations always
     * use this method to prepare renderers so that this default behavior
     * can be safely overridden by a subclass.
     *
     * @param renderer  the <code>GraphCellRenderer</code> to prepare
     * @param value     the Object to render
     * @return          the <code>Component</code> under the event location
     */
    public Component prepareEdgeRenderer (final GraphEdgeRenderer renderer, final Object value,
    		final int x1, final int y1, final int x2, final int y2) {

        boolean isSelected = false;
        boolean hasFocus = false;


        // Only indicate the selection and focused cell if not printing
        if (!isPaintingForPrint()) {
            //isSelected = this.getSelectionModel().isSelected (value); //isCellSelected (row, column);
            
            if (value instanceof Edge) {
            	final Edge edge = (Edge)value;
            	final Object obj1 = edge.getNode1();
            	final Object obj2 = edge.getNode2();
            	isSelected = (selectionModel.isSelected (obj1) && selectionModel.isSelected (obj2));
            }
           
            hasFocus = isFocusOwner();
        }

        return renderer.getGraphEdgeRendererComponent (this, value,
	                                              isSelected, hasFocus,
	                                              x1, y1, x2, y2);
    }
    
    @Override
    public JToolTip createToolTip() {
    	return rToolTip;
    } 
	
    
	public void setDefaultNodeToolTipRenderer (final Class<?> klass, final GraphCellRenderer klassRenderer) {
		rToolTip.addRenderer (klass, klassRenderer);
	}
    
	
	public final boolean isShowEdges () {
		return showAllEdges;
	}

	public final void setShowEdges (final boolean showEdges) {
		this.showAllEdges = showEdges;
	}
	
	
	public final RepulsiveForceCalculationInterface getRepulsiveForceCalculator() {
		return repulsiveForceCalculator;
	}

	public final void setRepulsiveForceCalculator (final RepulsiveForceCalculationInterface rfCalc) {
		this.repulsiveForceCalculator = rfCalc;
	}
	
	public final AttractiveForceCalculationInterface getAttractiveForceCalculator() {
		return attractiveForceCalculator;
	}

	public final void setAttractiveForceCalculator (final AttractiveForceCalculationInterface afCalc) {
		this.attractiveForceCalculator = afCalc;
	}
	
	public final PositionUpdater getPositionUpdater () {
		return positionUpdater;
	}

	public final void setPositionUpdater (final PositionUpdater positionUpdater) {
		this.positionUpdater = positionUpdater;
	}
	
	/*
	 * Calculate forces using whatever calculators we have
	 */
	public void calculateForces () {
		synchronized (lock) {
			if (repulsiveForceCalculator != null) {
				repulsiveForceCalculator.calculateRepulsiveForces (this);
			}
			if (attractiveForceCalculator != null) {
				attractiveForceCalculator.calculateAttractiveForces (this);
			}
		}
	}
	
	/**
	 * 
	 * @param BARLEY_PATTERN - location in logical coordinates (insets.e. node, not screen co-ords)
	 * @return
	 */
	public Object getNearestTo (final Point point) {
		Object obj;
		
		long nano1 = System.nanoTime();
		obj = getRepulsiveForceCalculator().getNearestTo (this, point);
		long nano2 = System.nanoTime();
		LOGGER.debug ("rfc object: "+obj+", found in "+((nano2-nano1)/1E6)+" ms.");
		
		if (true /*obj == null*/) {
			obj = getAttractiveForceCalculator().getNearestTo (this, point);
			
			if (obj == null) {
			//if (true) {
				nano1 = System.nanoTime();
				obj = bruteForceSearch (point);
				nano2 = System.nanoTime();
				LOGGER.debug ("brute force object: "+obj+", found in "+((nano2-nano1)/1E6)+" ms.");
			}
		}
		
		return obj;
	}
	
	
	Object bruteForceSearch (final Point point) {
		final Set<Object> nodes = graph.getNodes();
		final Point2D p2d = new Point2D.Double (point.x, point.y);
		final Iterator<Object> iter = nodes.iterator();
    	double distSq = Double.MAX_VALUE;
    	Object nearestNode = null;
    	
    	while (iter.hasNext()) {
    		final Object node = iter.next();
    		final ObjectPlacement objPlacement = placementMapping.getPlacement (node);
    		final Point2D.Double point2 = objPlacement.getLocation ();
			final double curDistSq = p2d.distanceSq (point2);
			if (curDistSq < distSq) {
				distSq = curDistSq;
				nearestNode = node;
				if (curDistSq < 16) {
					break;
				}
			}
		}
    	
    	return nearestNode;
	}
	

	public void moveTo (final Object obj, final Point objToHere) {
		this.getUI().moveTo (obj, objToHere);
	}
	
	public void fitTo (final Collection<Object> objs) {
		this.getUI().fitTo (objs);
	}
	
	
	@Override
	public void repaint () {
		LOGGER.debug ("calcThread: "+calcThread);
		LOGGER.debug ("calling Thread: "+Thread.currentThread());
		if (calcThread != null) {
			LOGGER.debug ("calcThread sus: "+calcThread.isSuspended());
		}
		//if (calcThread == null || calcThread.isSuspended()) { // don't bother calling repaint if recalc thread inputStream alive - it will call repaint
			super.repaint ();
		//}
		
		LOGGER.debug ("worker: "+worker);
		//if (worker == null || worker.isDone() || worker.isCancelled()) {
			
		//}
	}
	
	@Override
    public void repaint (final int x, final int y, final int width, final int height) {
    	LOGGER.debug ("rp (x,y,w,h) calling Thread: "+Thread.currentThread());
        super.repaint (x, y, width, height);
    }
	
	
	/**
	 * Restart (unsuspend) the graph calculation thread if it is currently suspended
	 */
	public void restartWorker () {
		if (calcThread != null && calcThread.isSuspended()) {
			calcThread.resumeMe ();
		}
		/*
		if (worker == null || worker.isDone() || worker.isCancelled()) {
			worker = new ForceWorker<Void> (this);	
			worker.execute();
		}
		*/
	}
	
	
	
	/**
	 * Pause (suspend) the graph calculation thread if it is currently not frozen
	 */
	public void pauseWorker () {
		if (calcThread != null && !calcThread.isSuspended()) {
			calcThread.suspendMe();
		}
	}
	
	
	

	@Override
	public void graphChanged (final GraphModelEvent event) {
		if (event.getNodes() != null || event.getNode () != null) {
			// Node-based event
			if (event.getNode() != null) {
				doPlacementOperation (event.getNode(), event);
			}
			if (event.getNodes() != null) {
				final Set<Object> nodes = event.getNodes ();
				final Iterator<Object> nodeIterator = nodes.iterator();
				while (nodeIterator.hasNext ()) {
					doPlacementOperation (nodeIterator.next(), event);
				}		
			}
			restartWorker ();
		}
	}
	
	
	void doPlacementOperation (final Object obj, final GraphModelEvent event) {
		if (event.getType() == GraphModelEvent.DELETE) {
			placementMapping.removeMapping (obj);
		}
		else if (event.getType() == GraphModelEvent.INSERT) {
			placementMapping.addMapping (obj);
		}
	}
	
	

	public final double getStrainLimit () {
		return strainLimit;
	}

	/**
	 * Set the strain limit that acts as a cut off for the combined force calculations
	 * @param strainLimit
	 */
	public final void setStrainLimit (final double strainLimit) {
		this.strainLimit = strainLimit;
	}
	
	public SelectionPopupMenu getRightClickMenu() {
		return rightClickMenu;
	}

	public void setRightClickMenu (final SelectionPopupMenu rightClickMenu) {
		this.rightClickMenu = rightClickMenu;
	}

	/**
	 * Deals with changes to graph selection model (usually just repaint)
	 */
	@Override
	public void valueChanged (final GraphSelectionEvent event) {
		repaint ();
	}


	
	static class DefaultGraphCellRenderer extends AbstractGraphCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9001041333361237754L;
		private final Border defaultBorder = BorderFactory.createLineBorder (Color.black, 1);

		public DefaultGraphCellRenderer () {
			super ();
			setBorder (defaultBorder);
			setBackground (Color.blue.brighter());
			setForeground (Color.white);
			//setFont (Font.decode ("Arial-plain-11"));
		}
		
		@Override
		public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
				final boolean isSelected, final boolean hasFocus) {
			super.getGraphCellRendererComponent (graph, value, isSelected, hasFocus);
			this.setText (value.toString());
			return this;
		}
	}
	
	
	static class DefaultGraphEdgeRenderer extends AbstractGraphEdgeRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9001041333361237754L;

		public DefaultGraphEdgeRenderer () {
			super ();
			setBorder (null);
			setBackground (Color.blue.brighter());
			setForeground (Color.black);
			//setFont (Font.decode ("Arial-plain-11"));
		}
		
		@Override
		public Component getGraphEdgeRendererComponent (final JGraph graph, final Object value,
				final boolean isSelected, final boolean hasFocus,
				final int x1, final int y1, final int x2, final int y2) {
			super.getGraphEdgeRendererComponent (graph, value, isSelected, hasFocus,
					x1, y1, x2, y2);
			return this;
		}
		
		@Override
		public void paintComponent (final Graphics graphics) {
			//g.setColor (Color.black);
			graphics.drawLine (fromX - this.getX(), fromY - this.getY(), toX - this.getX(), toY - this.getY());
		}
	}
	
	
	
	class GraphWindowAdapter extends WindowAdapter {
		@Override
		public void windowClosing (final WindowEvent event) {
			if (calcThread != null) {
				calcThread.stopMe ();
			}
			if (repulsiveForceCalculator != null) {
				repulsiveForceCalculator.cleanup();
			}
			if (attractiveForceCalculator != null) {
				attractiveForceCalculator.cleanup();
			}
		}	
		
		@Override
		public void windowIconified (final WindowEvent event) {
			windowDeiconified (event);
		}	
		
		@Override
		public void windowDeiconified (final WindowEvent event) {
			calcThread.setPaintOn (event.getID() == WindowEvent.WINDOW_DEICONIFIED);
		}	
	};
	
	
	
	class ForceWorker<Void> extends SwingWorker<Void, Void> {

		JGraph jgraph;
		
		ForceWorker (final JGraph jgraph) {
			super ();
			this.jgraph = jgraph;
		}
		
		@Override
		protected Void doInBackground() throws Exception {	
			boolean halt = false;
			while (!halt && !isCancelled()) {
				//final long nTime = System.nanoTime();
				jgraph.calculateForces();
				//final long nTime2 = System.nanoTime();
				positionUpdater.updatePositions (jgraph);
				halt = positionUpdater.haltLayout (jgraph);
				//final long nTime3 = System.nanoTime();
				
				//jgraph.repaint ();   
				publish ();
			}

			return null;
		}
		
		/**
		 * Process inputStream called as a result of this worker thread's calling the
		 * publish method. This method runs on the event dispatch thread.
		 *
		 * As image thumbnails are retrieved, the worker adds them to the
		 * list model.
		 *
		 */
		@Override
		protected void process (final List<Void> list) {
			jgraph.repaint ();   
		}
		
		@Override
		protected void done() {
			if (isCancelled()) {
				return;
			}
			
			jgraph.repaint ();
		}

		
	}
	
	
	/**
	 * 
	 * @author cs22
	 * Thread class that calculates and updates layout and strain in a JGraph
	 * 
	 */
	class DisplayThread extends Thread {

		JGraph graph;
		long start, start4;
		int iter;
		boolean suspended, happy, paintOn;

		public DisplayThread (final JGraph graph) {
			super ();
			this.graph = graph;
			iter = 0;
			suspended = false;
			happy = true;
			paintOn = true;
		}

		@Override
		public void run () {

			start = System.currentTimeMillis();
			start4 = start;
			
			while (happy) {

				start = System.currentTimeMillis();

				final long nTime = System.nanoTime();
				graph.calculateForces();	// calc forces
				final long nTime2 = System.nanoTime();
				if (positionUpdater != null) {
					synchronized (lock) {
						positionUpdater.updatePositions (graph);	// add the forces
					}
					if (positionUpdater.haltLayout (graph)) {	// suspend if certain strain threshold reached
						suspendMe ();
					}
				}
				final long nTime3 = System.nanoTime();
				
				LOGGER.debug ((nTime2-nTime)/1e6+" milliseconds for calc");
				LOGGER.debug ((nTime3-nTime2)/1e6+" milliseconds for activate");
				
				// Schedule repaint of JGraph every so often
				if ((++iter >= 5 /*&& start - start4 > 100)*/ ) || isSuspended()) {
					start4 = start;
					iter = 0;
					LOGGER.debug (System.nanoTime()+" "+Thread.currentThread());
					if (paintOn) {
						graph.firePropertyChange ("repaintLayoutThread", Boolean.TRUE, Boolean.FALSE);
						SwingUtilities.invokeLater (
							new Runnable () {
								@Override
								public void run() {
									LOGGER.debug ("graph v: "+graph.isVisible()+", d: "+graph.isDisplayable()+", s: "+graph.isShowing()+", point: "+graph.getParent());
									if (graph.isVisible()) {
										graph.repaint();
									}
								}
							}
						);
					}
				}

				try {
					// if suspended, have thread wait until a resume occurs
					if (suspended) {
						synchronized (this) {
							while (suspended) {
								wait ();
							}
						}
					}
				}
				catch (final InterruptedException e) { stopMe ();}
			}
			
			LOGGER.info ("Closing Calculation Thread for "+graph);
		}

			/**
		* Safe suspension of this thread.<P>
		*/
		public void suspendMe () {
			suspended = true;
		}

		/**
		* Safe resumption of this thread.<P>
		*/
		public synchronized void resumeMe () {
			suspended = false;
			notifyAll ();
		}

		/**
		* Safe termination of this thread.<P>
		*/
		public void stopMe () {
			if (isSuspended()) {
				resumeMe ();
			}
			happy = false;
		}

		
		public void setPaintOn (final boolean newPaintOn) {
			paintOn = newPaintOn;
		}
		
		public boolean isSuspended () { return suspended; }
	}
}
