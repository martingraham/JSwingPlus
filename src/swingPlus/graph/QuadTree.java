package swingPlus.graph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

import org.apache.log4j.Logger;

import ui.GraphUI;

public final class QuadTree {

	final static Logger LOGGER = Logger.getLogger (QuadTree.class);
	
    private ObjectPlacement centroid;
    private Object obj;
    private boolean leaf;
    private final Square square;
    private int bodyTotal;
    private QuadTree[] div;
    public final static int X = 1, Y = 2;


    public QuadTree (final Square sqr) {
        this.square = sqr;
        bodyTotal = 0;
        centroid = null;
        obj = null;
        leaf = true;
    }

    public void insert (final ObjectPlacement objPlace, final Object obj) {

    	final boolean debugEnabled = LOGGER.isDebugEnabled();
    	
        if (centroid == null) {
           centroid = objPlace;
           this.obj = obj;
           bodyTotal = 1;
        }

        else {
             Square newSquare;
             int val;
             
             if (leaf) {
                div = new QuadTree [4];
                if (debugEnabled) {
                	LOGGER.debug ("a centroid: "+centroid.getLocation()+"\t t: "+objPlace.getLocation());
                }
                newSquare = square.findSubSquareInclude (centroid.getLocation());
                val = newSquare.getQuadrant();
                div [val] = new QuadTree (newSquare);
                if (debugEnabled) {
                	LOGGER.debug ("move current centroid "+centroid.toString()+" to subsector "+val);
                }
                div [val].insert (centroid, obj);

                final BasicObjectPlacement bop = new BasicObjectPlacement ();
                bop.setLocation (new Point2D.Double (centroid.getLocation().getX(), centroid.getLocation().getY()));
                centroid = bop;
                this.obj = null;
                
                // Have to stop points with same location recursing to infinity
                if (objPlace.getLocation().equals (centroid.getLocation())) {
                	LOGGER.debug ("exact overlay "+objPlace.getLocation()+", "+centroid.getLocation());
                	objPlace.getLocation().setLocation (square.x + (Math.random() * square.width), square.y + (Math.random() * square.height));
                }
                leaf = false;
             }

             val = square.findQuadrant (objPlace.getLocation());
             if (div [val] == null) {
                newSquare = square.findSubSquareInclude (objPlace.getLocation());
                div [val] = new QuadTree (newSquare);
             }
             
             if (debugEnabled) {
            	 LOGGER.debug ("labelBorder centroid: "+centroid.getLocation()+"\t t: "+objPlace.getLocation());
            	 LOGGER.debug ("place centroid "+objPlace+" in subsector "+val);
             }
            
             div[val].insert (objPlace, obj);

             // Average the centroid positioning
             double x = centroid.getLocation().getX() * bodyTotal;
             double y = centroid.getLocation().getY() * bodyTotal;
             bodyTotal++;
             x = (x + objPlace.getLocation().getX()) / bodyTotal;
             y = (y + objPlace.getLocation().getY()) / bodyTotal;
             centroid.getLocation().setLocation (x, y);
             if (debugEnabled) {
            	 LOGGER.debug ("group centroid: "+centroid);
             }
        }
        if (debugEnabled) {
        	LOGGER.debug ("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        }
    }
  
    public ObjectPlacement getCentroid () {
    	return centroid;
    }
    
    public Object getObject () {
    	return obj;
    }

	public boolean isLeaf() {
		return leaf;
	}
	
	public QuadTree[] getSubTrees () {
		return div;
	}
	
	public Square getSquare() { return square; }
   
	public int getBodyTotal () { return bodyTotal; }
	
	public void printData () {
		LOGGER.info ("QuadTree: "+countNodes());
	}

	
	
	int countNodes () {
		int count = 1;
		final QuadTree[] subTrees = this.getSubTrees();
		if (subTrees != null) {
			for (QuadTree subTree : subTrees) {
				if (subTree != null) {
					if (!subTree.isLeaf()) {
						count += subTree.countNodes ();
					} else {
						count++;
					}
				}
			}
		}
		return count;
	}
	
	
	
    public void draw (final Graphics graphics, final GraphUI gui, 
    		final Point2D.Double examplePoint, final int depth) {
        
        
        final double cx = centroid.getLocation().getX();
        final double dx = (examplePoint.getX() - cx);
        
        final double cy = centroid.getLocation().getY();
        final double dy = (examplePoint.getY() - cy);
        
        final int x1 = gui.logic2ViewCoordX (cx);
        final int y1 = gui.logic2ViewCoordY (cy);
        
        if (depth == 0) {
        	final int x2 = gui.logic2ViewCoordX (examplePoint.getX());
            final int y2 = gui.logic2ViewCoordY (examplePoint.getY());
            graphics.setColor (Color.blue);
            graphics.fillOval (x2 - 5, y2 - 5, 10,  10);
        }
        
        
        final double dsq = (dx * dx) + (dy * dy);
        final boolean close = dsq * 0.25 > square.getSideLength() * square.getSideLength();
        
        graphics.setColor (close ? Color.magenta : Color.gray); 
        final int xx = gui.logic2ViewCoordX (square.getX());
        final int yy = gui.logic2ViewCoordY (square.getY());
        final int x2 = gui.logic2ViewCoordX (square.getMaxX()) - 1;
        //int y2 = gui.logic2ViewCoordY (square.getMaxY());
        final int r = x2 - xx;
        
        if (close || this.isLeaf()) {
        	graphics.drawRect (xx, yy, r, r);
	        graphics.drawString (Integer.toString (bodyTotal), xx + 2, yy + 12);
	        graphics.drawLine (xx, yy, x1, y1);
	        graphics.drawOval (x1 - (r / 20), y1 - (r / 20), r / 10,  r / 10);
        }

        if (!close && div != null) {
           for (int n = div.length; --n >= 0;) {
               if (div[n] != null) { div[n].draw (graphics, gui, examplePoint, depth + 1); }
           }
        }
    }
}