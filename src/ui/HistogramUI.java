package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSliderUI;

import org.apache.log4j.Logger;

import swingPlus.histogram.BarRenderer;
import swingPlus.histogram.JHistogram;


public class HistogramUI extends BasicSliderUI {

	final static Logger LOGGER = Logger.getLogger (HistogramUI.class);
	
	protected JHistogram histogram;
	
	public HistogramUI (final JHistogram histogram) {
		super (histogram);
		this.histogram = histogram;
	}
	
	public static HistogramUI createUI (final JComponent comp) {
		return new HistogramUI((JHistogram)comp);
	}

	
	@Override
    public Dimension getMinimumSize (final JComponent comp)  {
		final Dimension minSize = super.getMinimumSize (comp);
		if ( slider.getOrientation() == JSlider.VERTICAL ) {
			minSize.width -= trackRect.width;
			minSize.width += 20;
		} else {
			minSize.height -= trackRect.height;
			minSize.height += 20;
		}

        return minSize;
    }
	
	
    // Making the thumbSize big makes trackRect big, which makes space to draw data
    @Override
	protected Dimension getThumbSize() {
        final Dimension size = new Dimension();
        calculateTickRect ();
        if ( slider.getOrientation() == JSlider.VERTICAL ) {
		    size.width = contentRect.width - getWidthOfWidestLabel() - tickRect.width;
		    size.height = 11;
		} else {
		    size.width = 11;
		    size.height = contentRect.height - getHeightOfTallestLabel() - tickRect.height;
		}
	
		return size;
    }
	
	@Override
    public void paint (final Graphics graphics, final JComponent comp)   {
        recalculateIfInsetsChanged();
		recalculateIfOrientationChanged();
		final Rectangle clip = graphics.getClipBounds();
	
		if ( !clip.intersects(trackRect) && slider.getPaintTrack())
		    calculateGeometry();
	
		if ( slider.getPaintTrack() && clip.intersects( trackRect ) ) {
		    paintTrack( graphics );
		}
		
		paintData (graphics);
		
	    if ( slider.getPaintTicks() && clip.intersects( tickRect ) ) {
	        paintTicks( graphics );
	    }
	    if ( slider.getPaintLabels() && clip.intersects( labelRect ) ) {
	        paintLabels( graphics );
	    }
		if ( slider.hasFocus() && clip.intersects( focusRect ) ) {
		    paintFocus( graphics );      
		}
		if ( clip.intersects( thumbRect ) ) {
		    paintThumb( graphics );
		}
    }
        

    
    @Override
    public void paintThumb (final Graphics graphics)  {        
    	final Rectangle knobBounds = thumbRect;
    	final int width = knobBounds.width;
    	final int height = knobBounds.height;  
        graphics.translate (knobBounds.x, knobBounds.y);

        if ( slider.isEnabled() ) {
            graphics.setColor (histogram.getThumbColour());
        }
        else {
            graphics.setColor (histogram.getThumbColour().darker());
        }

        final Boolean paintThumbArrowShape =
		    (Boolean)slider.getClientProperty("Slider.paintThumbArrowShape");
		
		if ((!slider.getPaintTicks() && paintThumbArrowShape == null) ||
		    (paintThumbArrowShape == null || paintThumbArrowShape.equals (Boolean.FALSE))) {
	
		    // "plain" version
            graphics.fillRect(0, 0, width, height);

            graphics.setColor(Color.black);
            graphics.drawLine(0, height-1, width-1, height-1);    
            graphics.drawLine(width-1, 0, width-1, height-1);    

            graphics.setColor(getHighlightColor());
            graphics.drawLine(0, 0, 0, height-2);
            graphics.drawLine(1, 0, width-2, 0);

            graphics.setColor(getShadowColor());
            graphics.drawLine(1, height-2, width-2, height-2);
            graphics.drawLine(width-2, 1, width-2, height-3);
        }
        else if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
        	final int cw = width / 2;
        	final int thumbHeight = Math.min (50, height-1-cw);
        	final int thumbTop = (height-1-cw) - thumbHeight;
            graphics.fillRect(1, thumbTop, width-3, thumbHeight);
            
            final Polygon poly = new Polygon();
            poly.addPoint(1, height-cw);
            poly.addPoint(cw-1, height-1);
            poly.addPoint(width-2, height-1-cw);
            graphics.fillPolygon(poly);       

            graphics.setColor(getHighlightColor());
            graphics.drawLine(0, thumbTop, width-2, thumbTop);
            graphics.drawLine(0, thumbTop, 0, thumbTop + thumbHeight);
            graphics.drawLine(0, height-cw, cw-1, height-1); 

            graphics.setColor(Color.black);
            graphics.drawLine(width-1, thumbTop, width-1, thumbTop + thumbHeight - 1);    
            graphics.drawLine(width-1, height-1-cw, width-1-cw, height-1);       

            graphics.setColor(getShadowColor());
            graphics.drawLine(width-2, thumbTop + 1, width-2, thumbTop + thumbHeight + 1);    
            graphics.drawLine(width-2, height-1-cw, width-1-cw, height-2);       
        }
        else {  // vertical
        	final int cw = height / 2;
        	final int thumbWidth = Math.min (50, width-1-cw);
        	final int thumbLeft = (width-1-cw) - thumbWidth;
            if (slider.getComponentOrientation().isLeftToRight()) {
            	graphics.fillRect(thumbLeft, 1, thumbWidth, height-3);
            	
            	final Polygon poly = new Polygon();
	            poly.addPoint(width-cw-1, 0);
	            poly.addPoint(width-1, cw);
	            poly.addPoint(width-1-cw, height-2);
	            graphics.fillPolygon(poly);
	
	            graphics.setColor(getHighlightColor());
	            graphics.drawLine(thumbLeft, 0, thumbLeft, height - 2);      // left
	            graphics.drawLine(thumbLeft, 0, thumbLeft + thumbWidth, 0);                 // top
	            graphics.drawLine(width-cw-1, 0, width-1, cw);              // top slant
	
	            graphics.setColor(Color.black);
	            graphics.drawLine(thumbLeft, height-1, thumbLeft + thumbWidth - 1, height-1);             // bottom
	            graphics.drawLine(thumbLeft + thumbWidth, height-1, thumbLeft + thumbWidth + cw, height-1-cw);        // bottom slant
	
	            graphics.setColor(getShadowColor());
	            graphics.drawLine(thumbLeft + 1, height-2, thumbLeft + thumbWidth - 1,  height-2 );         // bottom
                graphics.drawLine(thumbLeft + thumbWidth, height-2, width-2, height-cw-1 );     // bottom slant
            }
            else {
            	graphics.fillRect (5, 1, thumbWidth, height-3);
            	
            	final Polygon poly = new Polygon();
            	poly.addPoint(cw, 0);
            	poly.addPoint(0, cw);
            	poly.addPoint(cw, height-2);
            	graphics.fillPolygon(poly);

            	graphics.setColor(getHighlightColor());
            	graphics.drawLine(cw-1, 0, width-2, 0);             // top
            	graphics.drawLine(0, cw, cw, 0);                // top slant

            	graphics.setColor(Color.black);
            	graphics.drawLine(0, height-1-cw, cw, height-1 );         // bottom slant
            	graphics.drawLine(cw, height-1, width-1, height-1);           // bottom

            	graphics.setColor(getShadowColor());
            	graphics.drawLine(cw, height-2, width-2,  height-2 );         // bottom
            	graphics.drawLine(width-1, 1, width-1,  height-2 );          // right
		    }
        }

        graphics.translate (-knobBounds.x, -knobBounds.y);
    }
    
    
    public void paintData (final Graphics graphics) {
    	final SortedMap<Integer, Number> bin = histogram.getBinnedData();
		final Set<Map.Entry<Integer, Number>> binEntries = bin.entrySet();
		final int minVal = histogram.getMinDataValue();
    	//LOGGER.debug (Arrays.toString (data));
    	final BarRenderer renderer = histogram.getRenderer ();
    	
    	if (histogram.getOrientation() == JSlider.HORIZONTAL ) {
    		final int dataRange = histogram.getMaximum() - histogram.getMinimum();
    		final double widthPerDatum =  (double)trackRect.width / (double)dataRange;
    		final double heightPerDatum = (double)trackRect.height / histogram.getMaxBinCount();

    		for (Map.Entry<Integer, Number> binEntry : binEntries) {
    			final int value = binEntry.getKey().intValue();
    			final double count = binEntry.getValue().doubleValue();

    			final int valFromMin = value - minVal;
    			final int goodWidth = (int)(widthPerDatum * (valFromMin + 1)) - (int)(widthPerDatum * valFromMin);
    			final int rectx = (drawInverted()
    					? (int)trackRect.getX() + (trackRect.width - (int)(widthPerDatum * (valFromMin + 1)))
    					: (int)trackRect.getX() + (int)(widthPerDatum * valFromMin)
    			);
    			final int minHeight = Math.max (1, (int)(heightPerDatum * count));
    			renderer.drawBar (histogram, graphics, rectx, (int)trackRect.getMaxY() - minHeight,
    					(int)Math.max (goodWidth, 1.0), minHeight, value, count);
    		}
    	} 
    	
    	else if (histogram.getOrientation() == JSlider.VERTICAL) {
    		//final int dataRange = data [data.length - 1] - data [0] + 1;
    		final int dataRange = histogram.getMaximum() - histogram.getMinimum();
    		final double widthPerDatum =  (double)trackRect.width / histogram.getMaxBinCount();
    		final double heightPerDatum = (double)trackRect.height / (double)dataRange;
    	
    		for (Map.Entry<Integer, Number> binEntry : binEntries) {
    			final int value = binEntry.getKey().intValue();
    			final double count = binEntry.getValue().doubleValue();
    			
    			final int valFromMin = value - minVal;
    			final int goodHeight = (int)(heightPerDatum * (valFromMin + 1)) - (int)(heightPerDatum * valFromMin);
    			// Invert the invert for drawing data vertically. 
    			// Slider seems to default to orient axis bottom to top (i.e. increase val with decreasing y)
    			final int recty = (!drawInverted()
    					? (int)trackRect.getY() + (trackRect.height - (int)(heightPerDatum * (valFromMin + 1)))
    					: (int)trackRect.getY() + (int)(heightPerDatum * valFromMin)
    			);
    			final int minWidth = Math.max (1, (int)(widthPerDatum * count));
    			renderer.drawBar (histogram, graphics, (int)trackRect.getMaxX() - minWidth, recty,
    					minWidth, (int)Math.max (goodHeight, 1.0), value, count);
    		}
    	} 
    }
    
    
    // Used exclusively by setThumbLocation()
    private static Rectangle unionRect = new Rectangle();
    
    /**
     * Adjusted to redraw whole of bar that the thumb is currently over, not just the thumb's width
     */
    public void setThumbLocation (final int x, final int y)  {
    	
    	unionRect.setBounds (thumbRect);
        thumbRect.setLocation (x, y);
        
        final int dataRange = histogram.getMaximum() - histogram.getMinimum();
        
        if (histogram.getOrientation() == JSlider.HORIZONTAL) {
        	SwingUtilities.computeUnion (thumbRect.x, thumbRect.y, getVarThumbScalar (), thumbRect.height, unionRect);
			final int barWidth =  (int)(dataRange > 0 ? Math.ceil ((double)trackRect.width / (double)dataRange) : 0);
	        
	        // If bar thickness is more than thumb thickness
	        //if (barWidth > getVarThumbScalar ()) {
	        	// Change unionRect's left boundary to match the leftmost boundary of the leftmost bar that unionRect intersects
	        	final int diffX = unionRect.x - trackRect.x;
	        	final int barDeltaX = diffX % barWidth;
		        unionRect.x -= barDeltaX + barWidth;
		        unionRect.x = Math.max (unionRect.x, 0);
		        
		        unionRect.width += barDeltaX + barWidth; // Compensate for change in x so rightmost boundary is at same place
		        
		        // Then change unionRect's right boundary to match the rightmost boundary of the rightmost bar that unionRect intersects
		        final int diffX2 = ((int)unionRect.getMaxX() - trackRect.x);
		        final int barDeltaX2 = (barWidth - (diffX2 % barWidth)) + 2;
		        unionRect.width += barDeltaX2 + barWidth;
		        unionRect.width = Math.max (unionRect.width, trackRect.width);
		        unionRect.height++;
		        
		        // Thus all bars intersected by unionRect will be drawn in full
	        //}
        }
        else if (histogram.getOrientation() == JSlider.VERTICAL) {
        	SwingUtilities.computeUnion (thumbRect.x, thumbRect.y, thumbRect.width, getVarThumbScalar (), unionRect);
			final int barHeight =  (int)(dataRange > 0 ? Math.ceil ((double)trackRect.height / (double)dataRange) : 0);
	        
	        // If bar thickness is more than thumb thickness
	        //if (barHeight > getVarThumbScalar ()) {
	        	// Change unionRect's top boundary to match the topmost boundary of the topmost bar that unionRect intersects
	        	final int diffY = unionRect.y - trackRect.y;
	        	final int barDeltaY = diffY % barHeight;
		        unionRect.y -= barDeltaY + barHeight;
		        unionRect.y = Math.max (unionRect.y, 0);
		        
		        unionRect.height += barDeltaY + barHeight; // Compensate for change in y so bottommost boundary is at same place
		        
		        // Then change unionRect's bottom boundary to match the bottommost boundary of the bottommost bar that unionRect intersects
		        final int diffY2 = ((int)unionRect.getMaxY() - trackRect.y);
		        final int barDeltaY2 = barHeight - (diffY2 % barHeight) + 2;
		        unionRect.height += barDeltaY2 + barHeight;
		        unionRect.height = Math.max (unionRect.height, trackRect.height);
		        unionRect.width++;
		        
		        // Thus all bars intersected by unionRect will be drawn in full
	        //}
        }
        
        slider.repaint (unionRect.x, unionRect.y, unionRect.width, unionRect.height);
    }
    
    
    /**
     * 
     * @return thumb rectangle width along slider axis 
     */
    protected int getVarThumbScalar () {
    	return (histogram.getOrientation() == JSlider.VERTICAL ? thumbRect.height : thumbRect.width);
    }
    
    
    protected int xPositionForValue (final int value)    {
        final int min = slider.getMinimum();
        final int max = slider.getMaximum();
        final int trackLength = trackRect.width;
        final double valueRange = (double)max - (double)min;
        final double pixelsPerValue = (double)trackLength / valueRange;
        final int trackLeft = trackRect.x;
        final int trackRight = trackRect.x + (trackRect.width - 1);
        int xPosition;

        if ( !drawInverted() ) {
            xPosition = trackLeft;
            xPosition += Math.round( pixelsPerValue * ((double)value - min) );
        }
        else {
            xPosition = trackRight;
            xPosition -= Math.round( pixelsPerValue * ((double)value - min) );
        }
        
        // Extra line needed
        xPosition += drawInverted() ? -(pixelsPerValue / 2) : (pixelsPerValue / 2);
        xPosition = Math.max( trackLeft, xPosition );
        xPosition = Math.min( trackRight, xPosition );

        return xPosition;
    }

    protected int yPositionForValue (final int value)  {
        return yPositionForValue(value, trackRect.y, trackRect.height);
    }

    /**
     * Returns the y location for the specified value.  No checking is
     * done on the arguments.  In particular if <code>trackHeight</code> is
     * negative undefined results may occur.
     *
     * @param value the slider value to get the location for
     * @param trackY y-origin of the track
     * @param trackHeight the height of the track
     * @since 1.6
     */
    protected int yPositionForValue (final int value, final int trackY, final int trackHeight) {
    	final int min = slider.getMinimum();
    	final int max = slider.getMaximum();
    	final double valueRange = (double)max - (double)min;
    	final double pixelsPerValue = (double)trackHeight / (double)valueRange;
    	final int trackBottom = trackY + (trackHeight - 1);
        int yPosition;

        if ( !drawInverted() ) {
            yPosition = trackY;
            yPosition += Math.round( pixelsPerValue * ((double)max - value ) );
        }
        else {
            yPosition = trackY;
            yPosition += Math.round( pixelsPerValue * ((double)value - min) );
        }

        // Extra line needed
        yPosition += drawInverted() ? (pixelsPerValue / 2) : -(pixelsPerValue / 2);
        yPosition = Math.max( trackY, yPosition );
        yPosition = Math.min( trackBottom, yPosition );

        return yPosition;
    }

    /**
     * Returns a value give a y position.  If yPos is past the track at the top or the
     * bottom it will set the value to the min or max of the slider, depending if the
     * slider is inverted or not.
     */
    public int valueForYPosition (final int yPos) {
        int value;
		final int minValue = slider.getMinimum();
		final int maxValue = slider.getMaximum();
		final int trackLength = trackRect.height;
		final int trackTop = trackRect.y;
		final int trackBottom = trackRect.y + (trackRect.height - 1);
		
		if ( yPos <= trackTop ) {
		    value = drawInverted() ? minValue : maxValue;
		}
		else if ( yPos >= trackBottom ) {
		    value = drawInverted() ? maxValue : minValue;
		}
		else {
			final int min = slider.getMinimum();
			final int max = slider.getMaximum();
			final double pixelsPerValue = (double)trackLength / ((double)max - (double)min);
		    int distanceFromTrackTop = yPos - trackTop;
		    // Extra line needed
		    distanceFromTrackTop += drawInverted() ? -(pixelsPerValue / 2) : (pixelsPerValue / 2);

		    final double valueRange = (double)maxValue - (double)minValue;
		    final double valuePerPixel = valueRange / (double)trackLength;
		    final int valueFromTrackTop = (int)Math.round( distanceFromTrackTop * valuePerPixel );
	
		    value = drawInverted() ? minValue + valueFromTrackTop : maxValue - valueFromTrackTop;
		}
		
		return value;
    }
    
    
    public Dimension valueRangeForYPosition (final int yPos) {
    	final Dimension dim = new Dimension ();
    	dim.width = valueForYPosition (yPos);
    	
		final int trackLength = trackRect.height;
		final int min = slider.getMinimum();
		final int max = slider.getMaximum();
		final double pixelsPerValue = (double)trackLength / ((double)max - (double)min);
		
		if (pixelsPerValue >= 1.0) {
			dim.height = dim.width;
		} else {
			final int newYPos = yPos + (drawInverted() ? 1 : -1); // y default is to be inverted
			dim.height = valueForYPosition (newYPos) - 1;
			dim.height = Math.max (dim.height, dim.width);
		}
		
		return dim;
    }
  
    /**
     * Returns a value give an x position.  If xPos is past the track at the left or the
     * right it will set the value to the min or max of the slider, depending if the
     * slider is inverted or not.
     */
    public int valueForXPosition (final int xPos) {
        int value;
		final int minValue = slider.getMinimum();
		final int maxValue = slider.getMaximum();
		final int trackLength = trackRect.width;
		final int trackLeft = trackRect.x; 
		final int trackRight = trackRect.x + (trackRect.width - 1);

		
		if ( xPos <= trackLeft ) {
		    value = drawInverted() ? maxValue : minValue;
		}
		else if ( xPos >= trackRight ) {
		    value = drawInverted() ? minValue : maxValue;
		}
		else {
			final int min = slider.getMinimum();
			final int max = slider.getMaximum();
			final double pixelsPerValue = (double)trackLength / ((double)max - (double)min);
		    int distanceFromTrackLeft = xPos - trackLeft;
		    // Extra line needed
		    distanceFromTrackLeft += drawInverted() ? (pixelsPerValue / 2) : -(pixelsPerValue / 2);
		    
		    final double valueRange = (double)maxValue - (double)minValue;
		    final double valuePerPixel = valueRange / (double)trackLength;
		    final int valueFromTrackLeft = (int)Math.round( distanceFromTrackLeft * valuePerPixel );
		    
		    value = drawInverted() ? Math.round (maxValue - valueFromTrackLeft) :
		      Math.round (minValue + valueFromTrackLeft);
		}
		
		return value;
    }
    
    
    
    public Dimension valueRangeForXPosition (final int xPos) {
    	final Dimension dim = new Dimension ();
    	dim.width = valueForXPosition (xPos);
    	
		final int trackLength = trackRect.width;
		final int min = slider.getMinimum();
		final int max = slider.getMaximum();
		final double pixelsPerValue = (double)trackLength / ((double)max - (double)min);
		
		if (pixelsPerValue >= 1.0) {
			dim.height = dim.width;
		} else {
			final int newXPos = xPos + (drawInverted() ? -1 : 1);
			dim.height = valueForXPosition (newXPos) - 1;
			dim.height = Math.max (dim.height, dim.width);
		}
		
		return dim;
    }
}
