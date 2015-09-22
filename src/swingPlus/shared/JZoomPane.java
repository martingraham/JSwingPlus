package swingPlus.shared;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JSlider;
import javax.swing.JViewport;

import org.apache.log4j.Logger;

import util.GraphicsUtil;

public class JZoomPane extends JViewport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -471258802006232720L;
	static final private Logger LOGGER = Logger.getLogger (JZoomPane.class);
	
	final JLogSlider slider;
	
	public JZoomPane () {
		super ();
		slider = new JLogSlider (JSlider.VERTICAL, -3, 3);
		slider.setLocation (320, 20);
		slider.setSize (30, 128);
		
		final MouseAdapter mAdapter = new ZoomMouseAdapter ();
		this.addMouseListener (mAdapter);
		this.addMouseMotionListener (mAdapter);
		this.addMouseWheelListener (mAdapter);
	}
	
	@Override
	public void paint (final Graphics graphics) {
		super.paint (graphics);
		final Graphics compGraphics = graphics.create (slider.getX(), slider.getY(),
				slider.getWidth(), slider.getHeight());
		slider.paint (compGraphics);
		//System.err.println ("slider: "+slider);
	}
	
	class ZoomMouseAdapter extends MouseAdapter {
		
		@Override
		public void mouseClicked (final MouseEvent mEvent) {
			if (slider.getBounds().contains (mEvent.getPoint())) {
				final MouseEvent me2 = GraphicsUtil.convertMouseEvent (mEvent, slider, slider.getX(), slider.getY());
				slider.dispatchEvent (me2);
			}
		}
		
		@Override
		public void mousePressed (final MouseEvent mEvent) {
			if (slider.getBounds().contains (mEvent.getPoint())) {
				final MouseEvent me2 = GraphicsUtil.convertMouseEvent (mEvent, slider, slider.getX(), slider.getY());
				slider.dispatchEvent (me2);
			}
		}
		
		@Override
		public void mouseDragged (final MouseEvent mEvent) {
			if (slider.getBounds().contains (mEvent.getPoint())) {
				final MouseEvent me2 = GraphicsUtil.convertMouseEvent (mEvent, slider, slider.getX(), slider.getY());
				LOGGER.debug ("recalibrated mouse dragged event: "+me2);
				slider.dispatchEvent (me2);
			}
		}
		
		@Override
		public void mouseReleased (final MouseEvent mEvent) {
			if (slider.getBounds().contains (mEvent.getPoint())) {
				final MouseEvent me2 = GraphicsUtil.convertMouseEvent (mEvent, slider, slider.getX(), slider.getY());
				slider.dispatchEvent (me2);
			}
		}
	}
}
