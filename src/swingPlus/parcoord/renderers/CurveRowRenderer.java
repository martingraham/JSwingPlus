package swingPlus.parcoord.renderers;

import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;

import swingPlus.parcoord.JParCoord;
import swingPlus.parcoord.RowCoordData;


public class CurveRowRenderer extends AbstractRowRenderer {

    protected final static double TENSION = 0.18;
	
	final CubicCurve2D ccurve;
	
	public CurveRowRenderer () {
		super ();
		ccurve = new CubicCurve2D.Float ();
	}
	
	
	public void renderRow (final JParCoord jpc, final Graphics2D g, final RowCoordData rowCoordData,
			final int cMin, final int cMax) {
		path.reset ();
		
		final int[] xcoords = rowCoordData.getXCoords();
		final int[] ycoords = rowCoordData.getYCoords();
		
		final boolean firstCol = (cMin == 0);
		int prevpx, prevpy, startpx, startpy, endpx, endpy, nextpx, nextpy;
		double tangstartpx, tangstartpy, tangendpx, tangendpy;
		
		prevpx = firstCol ? -20 : xcoords [cMin - 1];
		prevpy = firstCol ? ycoords [cMin] : ycoords [cMin - 1];
		
		startpx = xcoords [cMin];
		startpy = ycoords [cMin];
		
		endpx = xcoords [cMin + 1];
		endpy = ycoords [cMin + 1];

		//LOGGER.debug (continuousPath ? "Continuous" : "Non Continuous");
		
		for (int col = cMin + 2; col <= cMax + 1; col++) {
			
			nextpx = (col >= xcoords.length ? jpc.getWidth() : xcoords [col]);
			nextpy = (col >= ycoords.length ? endpy : ycoords [col]);

			tangendpx = endpx - (TENSION * (nextpx - startpx));
			tangendpy = endpy - (TENSION * (nextpy - startpy));

			tangstartpx = startpx + (TENSION * (endpx - prevpx));
			tangstartpy = startpy + (TENSION * (endpy - prevpy));

			ccurve.setCurve (
				startpx, startpy,
		        tangstartpx, tangstartpy,
		        tangendpx, tangendpy,
		        endpx, endpy);		
				
			path.append (ccurve, true);
			
			prevpx = startpx;
			prevpy = startpy;
			startpx = endpx;
			startpy = endpy;
			endpx = nextpx;
			endpy = nextpy;
		}
		
		super.renderRow (jpc, g, rowCoordData, cMin, cMax);
	}
	
	public int getColumnSpread () { return 2; }
	
	public int getPixelSpread () { return 0; }
	
	public String getDescription () { return "Curves"; }
}
