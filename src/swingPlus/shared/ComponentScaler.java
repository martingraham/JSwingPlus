package swingPlus.shared;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.apache.log4j.Logger;


import util.GraphicsUtil;

public class ComponentScaler {

	private final static Logger LOGGER = Logger.getLogger (ComponentScaler.class);
	
	protected Point2D scale;
	protected boolean doesScalex, doesScaley;
	
	public ComponentScaler () {
		this (true, true);
	}
	
	public ComponentScaler (final boolean xflag, final boolean yflag) {
		scale = new Point2D.Double (1.0, 1.0);
		doesScalex = xflag;
		doesScaley = yflag;
	}
	

	
	public void scaleGraphics (final Graphics graphics) {
		final Graphics2D g2d = (Graphics2D)graphics;
		final double gscalex = doesScalex ? getScaleX() : 1.0;
		final double gscaley = doesScaley ? getScaleY() : 1.0;
		g2d.scale (gscalex, gscaley);
		LOGGER.debug ("sx: "+gscalex+", sY: "+gscaley);
		
		final Rectangle rect = g2d.getClipBounds();
		LOGGER.debug ("clip labelBorder: "+rect);
		LOGGER.debug (g2d.getTransform());
		//rect.setLocation ((int)(rect.getX() * sx), (int)(rect.getY() * sy));
		//int width = (int)((double)rect.getMaxX() * sx) - rect.x;
		//int height = (int)((double)rect.getMaxY() * sy) - rect.y;
		//rect.setSize (width, height);
		//rect.setSize ((int)(rect.getWidth() * sx), (int)(rect.getHeight() * sy));
		g2d.setClip (rect);
		LOGGER.debug ("clip labelBorder: "+g2d.getClipBounds());
	}
	
	public void unscaleGraphics (final Graphics graphics) {
		final Graphics2D g2d = (Graphics2D)graphics;
		//g2d.setColor (Color.black);
		//final Rectangle rect = g2d.getClipBounds();
		//g2d.drawRect(r.x, r.y, r.width - 1, r.height -1);
		final double gscalex = doesScalex ? getScaleX() : 1.0;
		final double gscaley = doesScaley ? getScaleY() : 1.0;
		g2d.scale (1.0 / gscalex, 1.0 / gscaley);
	}
	
	public void setScale (final Point2D newScale) {
		scale.setLocation (newScale);
	}
	
	public void setScale (final double newScaleX, final double newScaleY) {
		scale.setLocation (newScaleX, newScaleY);
	}
	
	public Point2D getScale () { return new Point2D.Double (getScaleX(), getScaleY()); }
	
	public double getScaleX () { return scale.getX(); }
	
	public double getScaleY () { return scale.getY(); }
	
	public MouseEvent scaleMouseEvent (final MouseEvent mEvent) {
		return GraphicsUtil.scaleMouseEvent (mEvent, doesScalex ? getScaleX() : 1.0, doesScaley ? getScaleY() : 1.0);
	}
}
