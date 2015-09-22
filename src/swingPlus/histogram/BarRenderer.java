package swingPlus.histogram;

import java.awt.Graphics;

public interface BarRenderer {

	public void drawBar (final JHistogram histo, final Graphics graphics,
			final int x, final int y, final int width, final int height,
			final int dataValue, final double dataValueCount);
}
