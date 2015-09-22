package swingPlus.parcoord.renderers;

import java.awt.Graphics2D;

import swingPlus.parcoord.JParCoord;
import swingPlus.parcoord.RowCoordData;

public interface RowRenderer {

	public void renderRow (final JParCoord jpc, final Graphics2D g, final RowCoordData rowCoord,
			final int cMin, final int cMax);
	
	// No of columns either side this renderer is affected by when drawing value at one column
	// 0 = dots, 1 = lines, 2  = curves
	public int getColumnSpread ();
	
	// No of pixels either side of the column center line a renderer can go
	// lines/curves are zero, but dots can take up a few pixels
	public int getPixelSpread ();
	
	// Human readable description for user interface components
	public String getDescription ();
}
