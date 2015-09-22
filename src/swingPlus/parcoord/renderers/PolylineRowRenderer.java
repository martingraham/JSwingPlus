package swingPlus.parcoord.renderers;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import swingPlus.parcoord.JParCoord;
import swingPlus.parcoord.RowCoordData;


public class PolylineRowRenderer extends AbstractRowRenderer {

	final Line2D line2d;
	
	public PolylineRowRenderer () {
		super ();
		line2d = new Line2D.Float ();
	}
	
	
	/**
	 * Draw a table row as a zig-zag line crossing each column at the appropriate point
	 * @param gDrawable
	 * @param row2d
	 * @param rowCoordData
	 */
	public void renderRow (final JParCoord jpc, final Graphics2D g, final RowCoordData rowCoordData,
			final int cMin, final int cMax) {
		path.reset ();
		
		final int[] xcoords = rowCoordData.getXCoords();
		final int[] ycoords = rowCoordData.getYCoords();
		
		int starty, endy, startx, endx;
		starty = ycoords [cMin];
		startx = xcoords [cMin];
		
		
		for (int col = cMin + 1; col <= cMax; col++) {
			endy = ycoords [col];
			endx = xcoords [col];
			line2d.setLine (startx, starty, endx, endy);
			//final boolean nullSkip = (Math.abs (ind - h) > 1);
			
			path.append (line2d, true);

			starty = endy;
			startx = endx;
		}
		
		
		super.renderRow (jpc, g, rowCoordData, cMin, cMax);
	}
	
	public int getColumnSpread () { return 1; }
	
	public int getPixelSpread () { return 0; }
	
	public String getDescription () { return "Polylines"; }
}
