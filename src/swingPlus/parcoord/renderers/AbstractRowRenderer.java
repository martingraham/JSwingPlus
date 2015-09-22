package swingPlus.parcoord.renderers;

import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import swingPlus.parcoord.JParCoord;
import swingPlus.parcoord.RowCoordData;

public abstract class AbstractRowRenderer implements RowRenderer {

	protected final Path2D path;
	
	public AbstractRowRenderer () {
		path = new Path2D.Float ();
	}
	
	public void renderRow (final JParCoord jpc, final Graphics2D g, final RowCoordData rowCoord,
			final int cMin, final int cMax) {
		g.draw (path);
	}
}
