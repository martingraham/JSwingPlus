package swingPlus.graph;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;

public class Square extends Rectangle2D.Double {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5182586976850287665L;
	private final static Logger LOGGER = Logger.getLogger (Square.class);
	
	int quadrant;

    public Square () {
        this (0, 0, 0, 0);
    }

    public Square (final double x, final double y, final double side1, final int quadrant) {
        set (x, y, side1, quadrant);
    }

    public void set (final double x1, final double y1, final double side1, final int quadrant1) {
        setRect (x1, y1, side1, side1);
        quadrant = quadrant1;
    }

    public boolean contains (final Point2D.Double point) {
        return contains (point.getX(), point.getY());
    }

    public Square findSubSquareInclude (final Point2D point) {

        final double x1 = point.getX();
        final double y1 = point.getY();
        double xnew = getX();
        double ynew = getY();
        final double halfSide = getWidth() / 2.0;
        int possQuadrant = 0;

        if (x1 > xnew + halfSide) {
           xnew += halfSide;
           possQuadrant += QuadTree.X;
        }
        if (y1 > ynew + halfSide) {
           ynew += halfSide;
           possQuadrant += QuadTree.Y;
        }

        final Square newSq = new Square ();
        newSq.set (xnew, ynew, halfSide, possQuadrant);
        return newSq;
    }

    public int findQuadrant (final Point2D point) {

        final double x1 = point.getX();
        final double y1 = point.getY();
        final double halfSide = getWidth() / 2.0;
        final double xnew = getX();
        final double ynew = getY();
        int possQuadrant = 0;

        if (x1 > xnew + halfSide) {
           possQuadrant += QuadTree.X;
        }
        if (y1 > ynew + halfSide) {
           possQuadrant += QuadTree.Y;
        }

        return possQuadrant;
    }



    public double getSideLength() {
        return getWidth();
    }

    public void setSideLength (final double value) {
        setRect (getX(), getY(), value, value);
    }

    @Override
	public String toString () {
       return ("square: [x: "+getX()+", y: "+getY()+", s: "+getWidth());
    }

    public int getQuadrant() {
        return quadrant;
    }
}