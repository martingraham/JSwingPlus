package swingPlus.parcoord.renderers;

import java.awt.Graphics2D;

import swingPlus.parcoord.JParCoord;
import swingPlus.parcoord.RowCoordData;

public class DotRowRenderer extends AbstractRowRenderer {

	/**
	 * Draw a table row as a collection of marks at the appropriate points in each column
	 * @param gDrawable
	 * @param rowCoordData
	 */
	protected int radius = 4;
	
	public void renderRow (final JParCoord jpc, final Graphics2D g, final RowCoordData rowCoordData,
			final int cMin, final int cMax) {
		final int[] xcoords = rowCoordData.getXCoords();
		final int[] ycoords = rowCoordData.getYCoords();
		//final Object[] dataRow = rowCoordData.data;
		
		for (int col = cMin; col <= cMax; col++) {
			final int y = ycoords [col];
			final int x = xcoords [col];
			g.fillOval (x - radius, y - radius, radius * 2, radius * 2);
			//gDrawable.drawLine (x - 8, y, x + 8, y);
		}
	}
	
	public int getColumnSpread () { return 0; }
	
	public int getPixelSpread () { return radius + 1; }
	
	public String getDescription () { return "Dots"; }
}
