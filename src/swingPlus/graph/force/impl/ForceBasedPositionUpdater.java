package swingPlus.graph.force.impl;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import swingPlus.graph.JGraph;
import swingPlus.graph.ObjectPlacement;
import swingPlus.graph.ObjectPlacementMapping;
import swingPlus.graph.force.PositionUpdater;

public class ForceBasedPositionUpdater implements PositionUpdater {

	private final static Logger LOGGER = Logger.getLogger (ForceBasedPositionUpdater.class);
	double lastKE = Double.MAX_VALUE;
	double totalKE = 0.0;
	
	@Override
	public boolean haltLayout (final JGraph graph) {
		final double tension = Math.sqrt (totalKE);
		final int nodeCount = graph.getFilteredModel().getNodeCount();
		final double tensionPerNode = tension / nodeCount;
		
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info ("Total tension: "+tension);
			LOGGER.info ("Node count: "+nodeCount);
			LOGGER.info ("KE per node: "+tensionPerNode);
		}
		return lastKE < totalKE && tensionPerNode < 0.0001;
			//Math.sqrt (totalKE) < (graph.getFilteredModel().getEdgeCount() / (double)330);
	}

	
	/*
	 * Once forces are calculated then use them to move objects
	 * Returns total force squared (strain squared) on object placement 
	 */
	@Override
	public void updatePositions (final JGraph graph) {
    	final Collection<ObjectPlacement> ops = graph.getObjectPlacementMapping().getAllPlacements();
    	final Iterator<ObjectPlacement> iter = ops.iterator();
    	lastKE = totalKE;
    	totalKE = 0.0;
    	
    	while (iter.hasNext()) {
    		final ObjectPlacement objPlacement = iter.next();
 			objPlacement.putForce ();
			totalKE += (objPlacement.getVelocityX() * objPlacement.getVelocityX()) + (objPlacement.getVelocityY() * objPlacement.getVelocityY());
			if (Double.isNaN (totalKE) && LOGGER.isDebugEnabled()) {
				LOGGER.debug ("velocityX: "+objPlacement.getVelocityX()+", velocityY: "+objPlacement.getVelocityY()+", op: "+objPlacement);
			}
    	}
    	
    	centreVelocities (graph.getObjectPlacementMapping());
	}
	
	
	/*
	 * Stops system scuttling away off-screen
	 */
	void centreVelocities (final ObjectPlacementMapping placementMapping) {
		
    	final Collection<ObjectPlacement> ops = placementMapping.getAllPlacements();
    	Iterator<ObjectPlacement> iter = ops.iterator();
		
        double univx = 0.0, univy = 0.0;
        final int nCount = Math.max (1, ops.size());
        
    	while (iter.hasNext()) {
    		final ObjectPlacement objPlacement = iter.next();
    		univx += objPlacement.getVelocityX();
			univy += objPlacement.getVelocityY();
		}
    			
		final double deltavx = univx / nCount, deltavy = univy / nCount;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("dvx: "+deltavx+", dvy: "+deltavy);
		}
		
    	iter = ops.iterator();
    	while (iter.hasNext()) {
    		final ObjectPlacement objPlacment = iter.next();
    		final Point2D.Double location = objPlacment.getLocation();
    		objPlacment.getLocation().setLocation (location.x - deltavx, location.y - deltavy);
    	}
	}

}
