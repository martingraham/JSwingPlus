package swingPlus.graph.force.impl;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import swingPlus.graph.JGraph;
import swingPlus.graph.ObjectPlacement;
import swingPlus.graph.QuadTree;
import swingPlus.graph.Square;
import swingPlus.graph.force.RepulsiveForceCalculationInterface;
import ui.GraphUI;
import util.threads.LatchRunnable;
import util.threads.ParallelCollectionProcess;


public class BarnesHut2DForceCalculator implements RepulsiveForceCalculationInterface {

	static final Logger LOGGER = Logger.getLogger (BarnesHut2DForceCalculator.class);
	static final int CORES = Runtime.getRuntime().availableProcessors();	     
	
	protected static int loopCount = 0;
	protected static long timeTotal = 0, qTimeTotal = 0;
 	
	protected double attenuator, eventHorizon, eventHorizonSq;
	protected boolean parallel = true;
	protected QuadTree rootQTree;
	
	
	public BarnesHut2DForceCalculator () {
		this (true);	
	}
		
	public BarnesHut2DForceCalculator (final boolean parallel) {
		this (parallel, 0.8, 3000.0);
	}
	
	public BarnesHut2DForceCalculator (final boolean parallel, final double newAttenuator, final double newEventHorizon) {
		this.parallel = parallel;
		setEventHorizon (newEventHorizon);
		setAttenuator (newAttenuator);
	}
	
	@Override
	public void calculateRepulsiveForces (final JGraph graph) {
		repulse (graph);
	}
	
	
	@Override
	protected void finalize() throws Throwable { 
	    try {
	        cleanup ();
	    } catch (final Exception error) {
	        LOGGER.error ("Finalize exception", error);
	    } 
	    finally {   
	        super.finalize();
	    }
	}  

	
	/**
	 * Shut down executor service for multi-threading
	 */
	@Override
	public void cleanup () {
		LOGGER.info ("Shutting down ExecutorService in "+this.getClass());
		//execService.shutdown();
	}


	
	
	protected void repulse (final JGraph graph) {
        
		final Collection<ObjectPlacement> placements = graph.getObjectPlacementMapping().getAllPlacements();
		final Iterator<ObjectPlacement> iter = placements.iterator();
		
		double minx = Double.POSITIVE_INFINITY;
        double miny = minx;
        double maxx = Double.NEGATIVE_INFINITY;
        double maxy = maxx;
      
        while (iter.hasNext()) {
        	final ObjectPlacement placement = iter.next ();
        	final Point2D.Double location = placement.getLocation();
        	minx = Math.min (minx, location.getX());
        	miny = Math.min (miny, location.getY());
        	maxx = Math.max (maxx, location.getX());
        	maxy = Math.max (maxy, location.getY());
        }

        long nano = System.nanoTime ();
        final Square square = new Square ();
        square.set (minx, miny, Math.max (maxx - minx, maxy - miny), 0);
        rootQTree = new QuadTree (square);

        final Set<Entry<Object, ObjectPlacement>> entries = graph.getObjectPlacementMapping().getAllEntries();
        final Iterator<Entry<Object, ObjectPlacement>> entryIterator = entries.iterator();
        while (entryIterator.hasNext()) {
        	final Entry<Object, ObjectPlacement> entry = entryIterator.next();
			rootQTree.insert (entry.getValue(), entry.getKey());
		}
        nano = System.nanoTime() - nano;
        qTimeTotal += nano;
        loopCount++;
        
        if (LOGGER.isInfoEnabled()) {
        	LOGGER.info ("one quadtree construction run takes "+(nano/1E6));
        	LOGGER.info ("Average qdtree run: "+(qTimeTotal/loopCount)/1E6+" ms.");
        }
        
        //qd.printData();
        nano = System.nanoTime ();
               
        final ParallelNodeForce pnf = new ParallelNodeForce (rootQTree);
        pnf.doParallel (placements, null);
        
        nano = System.nanoTime() - nano;
        timeTotal += nano;
        
        if (LOGGER.isInfoEnabled()) {
        	LOGGER.info ("one force run takes "+(nano/1E6));
        	LOGGER.info ("Average run: "+(timeTotal/loopCount)/1E6+" ms.");
        	LOGGER.info ("for "+placements.size()+" placements on "+CORES+" cores.");     
        	LOGGER.info ("Active threads after: "+Thread.activeCount());
        	LOGGER.info ("------------------------");
        }
	}
	
	
	public void reveal (final Graphics graphics, final GraphUI gui, final Point2D.Double location) {
		rootQTree.draw (graphics, gui, location, 0);
	}


	@Override
	public Object getNearestTo (final JGraph graph, final Point point) {
		final Point2D p2d = new Point2D.Double (point.x, point.y);
		return trackToLeaf (rootQTree, p2d);
	}
	
	Object trackToLeaf (final QuadTree qTree, final Point2D point) {
		final int quadrant = qTree.getSquare().findQuadrant (point);
		final QuadTree newqt = qTree.getSubTrees() [quadrant];
		if (newqt == null) {
			return qTree.getObject ();
		}
		if (newqt.isLeaf()) {
			return newqt.getObject ();
		}
		return trackToLeaf (newqt, point);
	}
	
	
	/**
	 * 
	 * @param r
	 * @return the smallest available <code>QuadTree</code> that encloses <code>Rectangle</code> r
	 */
	public QuadTree findEnclosingQuadTree (final Rectangle rect) {
		return findEnclosingQuadTree (rootQTree, rect);
	}
	

	QuadTree findEnclosingQuadTree (final QuadTree qTree, final Rectangle rect) {
		QuadTree enclosingSubQT = null;
		for (int n = 0; n < qTree.getSubTrees().length && enclosingSubQT == null; n++) {
			final QuadTree subQT = qTree.getSubTrees()[n];
			if (subQT != null && subQT.getSquare().contains (rect)) {
				enclosingSubQT = subQT;
			}
		}
		
		return (enclosingSubQT == null) ? qTree : findEnclosingQuadTree (enclosingSubQT, rect);
	}
	
	
	
	
	public final double getAttenuator() {
		return attenuator;
	}

	public final void setAttenuator (final double newAttenuator) {
		this.attenuator = newAttenuator;
	}
	
	public final double getEventHorizon () {
		return eventHorizon;
	}
	
	public final void setEventHorizon (final double newEventHorizon) {
		eventHorizon = newEventHorizon;
		eventHorizonSq = eventHorizon * eventHorizon;
	}




	class ParallelNodeForce extends ParallelCollectionProcess {

		QuadTree quadTree;
		
		public ParallelNodeForce (final QuadTree qTree) {
			super ();
			this.quadTree = qTree;
		}

		@Override
		public LatchRunnable makeSubListProcess (final List<?> subList, final CountDownLatch cLatch) {
			return new CalcForce (quadTree, subList, cLatch);
		}
		
		@Override
		public void addPartialResult (final Object mergedResult, final Object partialResult) {
			// Empty
		}
	}
	
	
	class CalcForce extends LatchRunnable {
		Collection<ObjectPlacement> placements;
		QuadTree quadTree;
		
		CalcForce (final QuadTree qTree, final Collection<?> placements, final CountDownLatch latch) {
			super (latch);
			this.placements = (Collection<ObjectPlacement>)placements;
			quadTree = qTree;
		}
		
		public void run () {
			final Iterator<ObjectPlacement> iter = placements.iterator();
	        while (iter.hasNext()) {
	        	calcForce (quadTree, iter.next());
	        }
	        super.run ();
		}
		
		
	    public void calcForce (final QuadTree qTree, final ObjectPlacement objPlace) {
    	
	    	final ObjectPlacement centroid = qTree.getCentroid();
	        if (centroid != objPlace) {

	           if (qTree.isLeaf()) {
	        	   calcRepulsiveForce (centroid, objPlace, 1);
	           } else {
	             final Point2D.Double cenLoc = centroid.getLocation();
	             final Point2D.Double objLoc = objPlace.getLocation();
	             final double dx = cenLoc.getX() - objLoc.getX();
				 final double dy = cenLoc.getY() - objLoc.getY();
	             final double dsq = (dx * dx) + (dy * dy);
	             //System.err.println ("side: "+side+", dsq: "+dsq);
	             if (dsq < eventHorizonSq) {
	            	 final double side = qTree.getSquare().getSideLength();
		             if (0.25 * dsq > (side * side)) {	// dsq is d squared, 0.25 is 0.5 squared
		            	 // calcRepulsiveForce (centroid, objPlace, qTree.getBodyTotal());
		            	 final double dsqMod = Math.max (dsq, 0.001) / qTree.getBodyTotal() * attenuator;
		            	 objPlace.incForce (-dx / dsqMod, -dy / dsqMod);
		             }
		             else {
		            	 final QuadTree[] subTrees = qTree.getSubTrees();
		                 for (QuadTree subTree : subTrees) {
		                     if (subTree != null) {
		                         calcForce (subTree, objPlace);
		                     }
		                 }
		             }
	             }
	           }
	        }
	    }
		
	   
		public void calcRepulsiveForce (final ObjectPlacement op1, final ObjectPlacement op2, final int bodyMultiplier) {

			final double dsq, dx, dy, afx, afy;
			
			dx = op1.getLocation().getX() - op2.getLocation().getX();
			dy = op1.getLocation().getY() - op2.getLocation().getY();
			dsq = Math.max ((dx * dx) + (dy * dy), 0.001) / bodyMultiplier * attenuator;
			//if (dsq > GRAV_THRESHOLD) return;
			afx = dx / dsq;
			afy = dy / dsq;
			//fx += afx;
			//fy += afy;
			op2.incForce (-afx, -afy);
			//op2.incForceX (-afx);
			//op2.incForceY (-afy);
		}
		
		
		public Object getResult () { return null; }
	}
}
