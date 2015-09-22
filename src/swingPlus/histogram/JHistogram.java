package swingPlus.histogram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import ui.HistogramUI;

public class JHistogram extends JSlider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 166052425109330124L;
	final static Logger LOGGER = Logger.getLogger (JHistogram.class);
	private final static String UICLASSID = "HistogramUI";
	static {
		UIManager.put (UICLASSID, "ui.HistogramUI");
	}
	
	protected NavigableMap<Integer, Number> bin; // data
	protected double maxBinCount;	// Largest bin size in the data
	
	protected Color thumbColour = new Color (192, 192, 192, 128);
	protected boolean autoPowerTenScale;
	
	protected BarRenderer renderer;
	
	
    public JHistogram () {
        this (HORIZONTAL);
    }

    public JHistogram (final int orientation) {
        this (orientation, new int[] {0, 100}, 50);
    }


    public JHistogram (final int orientation, final int[] data, final int value)  {      
	    this (orientation, binData (data), value);
    }
    
    
    public JHistogram (final int orientation, 
    		final NavigableMap<Integer, Number> newBin, final int value)  {
        super (orientation);
        
        setAutoPowerTenScale (true);
        setData (newBin);
	    setValue (value);
	    
	    setRenderer (new DefaultBarRenderer ());
	    this.setPaintTrack (false);
	    this.setToolTipText ("");
    }
    

    @Override
	public HistogramUI getUI() {
        return (HistogramUI)ui;
    }

    @Override
    public String getUIClassID() {
        return UICLASSID;
    }
    
    
    public void setData (final int[] data) {
    	setData (binData (data));
    }
    
    
    public void setData (final NavigableMap<Integer, Number> newBin) {
    	bin = newBin;
    	calcMaxBinCount ();
    	
        if (getModel() == null) {
        	setModel (new DefaultBoundedRangeModel (0, 0, getMinDataValue(), getMaxDataValuePlusOne()));
        } else {
        	getModel().setMinimum (getMinDataValue ());
        	getModel().setMaximum (getMaxDataValuePlusOne ());
        }
        
        if (isAutoPowerTenScale ()) {
	        final int range = (getModel().getMaximum() - getModel().getMinimum()) / 2;
	        // Makes tickspacing the power of ten beneath the range of the values to be shown
	        // Division by 2 made so that i.e. 180 is shown in multiples of 10, rather than just 0 and 100
	        // whereas 
	        final int exponent = (int)Math.floor (Math.log10 (range));
	        final int majTickSpacing = (int)Math.pow (10, exponent);
	    	this.setMajorTickSpacing (majTickSpacing);
        }
        
    	if (this.getMajorTickSpacing() > 0) {
    		this.setLabelTable (this.createStandardLabels (this.getMajorTickSpacing()));
    	}
    }

    
    public int getMinDataValue () {
    	return (bin == null || bin.isEmpty() ? 0 : bin.firstKey());
    }
    
    public int getMaxDataValuePlusOne () {
    	return (bin == null || bin.isEmpty() ? 1 : bin.lastKey() + 1);
    }
    
  
    
    public NavigableMap<Integer, Number> getBinnedData () {
    	return bin;
    }
    
    
    // Works out maximum value in bins, used for scaling the histogram when drawing
    protected void calcMaxBinCount () {
    	final Collection<Number> binValues = bin.values();
    	double max = 0.0f;
    	for (Number value : binValues) {
    		final double valueDouble = value.doubleValue();
    		if (valueDouble > max) {
    			max = valueDouble;
    		}
    	}
    	maxBinCount = max;
    }
    
    
    public double getMaxBinCount () { return maxBinCount; }
   
	
    // Methods that get bin counts at given points and bin totals of given ranges
    
	public double getBinCount (final int value) {
		final Number binCount = bin.get (Integer.valueOf (value));
		return binCount == null ? 0.0 : binCount.doubleValue();
	}
	
	public double getBinCount (final Integer intObj) {
		final Number binCount = bin.get (intObj);
		return binCount == null ? 0.0 : binCount.doubleValue();
	}
	
	
	public double getBinCount (final Dimension range) {
		Integer start = Integer.valueOf (range.width);
		double binCount = 0.0;
		while (start != null && start.intValue() <= range.height) {
			binCount += getBinCount (start);
			start = this.getBinnedData().higherKey (start);
		}
		return binCount;
	}
    
    // Returns the lower or equal bin key to the value passed in.
    // Used to discover (with an old value) if changing value crosses any data points
    // e.g. if this function returns 7 for the value 8, and also 7 for the value 9, we know
    // the change in value hasn't included/excluded any bins in the data
    public int getFloorBin (final int val) {
    	final Integer floorKey = bin.floorKey (Integer.valueOf (val));
    	return (floorKey == null ? Integer.MIN_VALUE : floorKey.intValue());
    }
    

	public Color getThumbColour () {
		return thumbColour;
	}

	public void setThumbColour (final Color thumbColour) {
		this.thumbColour = thumbColour;
	}

	final public boolean isAutoPowerTenScale() {
		return autoPowerTenScale;
	}

	final public void setAutoPowerTenScale (final boolean autoPowerTenScale) {
		this.autoPowerTenScale = autoPowerTenScale;
	}
	
	/**
	 * Narrows a range down to the bins closest to and inside the boundaries of that range
	 * @param range - Dimension of range where height (to) should be >= width (from)
	 * @return Dimension - narrowed range to fit what occurs in the actual bin data
	 */
	public Dimension narrowRangeToBins (final Dimension range) {
		final Integer fromVal = this.getBinnedData().ceilingKey (Integer.valueOf (range.width));
		final Integer toVal = this.getBinnedData().floorKey (Integer.valueOf (range.height));
		if (fromVal == null && toVal == null) { // no data in bins (or when range.width > range.height, range.width > maxkey, range.height < minkey)
			return new Dimension (0, 0);
		}
		// if a null value occurs substitute with the other value
		return new Dimension (fromVal == null ? toVal.intValue() : fromVal.intValue(), 
				toVal == null ? fromVal.intValue() : toVal.intValue());
	}

	
	public Dimension getValueFromPos (final MouseEvent mEvent) {	
		return (this.getOrientation() == SwingConstants.HORIZONTAL
			? getUI().valueRangeForXPosition (mEvent.getX())
			: getUI().valueRangeForYPosition (mEvent.getY())
		);
	}
	
	
	@Override
	public String getToolTipText (final MouseEvent mEvent) {
		final Dimension range = getValueFromPos (mEvent);
		final Dimension binRange = narrowRangeToBins (range);
		final double binSize = getBinCount (binRange);
		final boolean isUnitBin = (binRange.height == binRange.width);
		return binSize == 0.0 ? "" : (binRange.width+ (isUnitBin ? "" : " to "+binRange.height)+" x "+binSize);
		//return binSize == 0.0 ? "" : (range.width+" to "+range.height+" x "+binSize);
	}
	
	
	final public void setRenderer (final BarRenderer newRenderer) {
		if (newRenderer != renderer && newRenderer != null) {
			renderer = newRenderer;
			repaint ();
		}
	}
	
	final public BarRenderer getRenderer () {
		return renderer;
	}

	
	
	public static class DefaultBarRenderer implements BarRenderer {
		
		@Override
		public void drawBar (final JHistogram histogram, final Graphics graphics, final int x, final int y,
				final int width, final int height, final int dataValue, final double dataValueCount) {
			
			graphics.setColor (dataValue == histogram.getValue() ? Color.red : Color.cyan);
			if (dataValue >= histogram.getValue()) {
				graphics.fill3DRect (x, y, width, height, true);
			} else {
				graphics.drawRect (x, y, width - 1, height - 1);
			}
			graphics.setColor (dataValue >= histogram.getValue() ? Color.black : Color.gray);
			if (width > 20) {
				graphics.drawString (Double.toString (dataValueCount), x, y + height - 12);
			} else {
				// Make text vertical for narrow bars
				final Graphics2D g2D = (Graphics2D) graphics;
		        final AffineTransform oldTransform = g2D.getTransform();
		        final int texty = y + height - 12;
		        g2D.translate (x, texty);
			    g2D.rotate (Math.PI / 2); 
		    	g2D.translate (-x, -texty);
		    	g2D.drawString (Double.toString (dataValueCount), x - 4, texty);
		    	g2D.setTransform (oldTransform);
			}
		}
		
		@Override
		public String toString () {
			return "Default Renderer";
		}
	}
	
	
	
	// Static method that turns an array of ints into a binned TreeMap
    static protected NavigableMap<Integer, Number> binData (final int data[]) {

    	final NavigableMap<Integer, Number> newBin = new TreeMap <Integer, Number> ();
    	Arrays.sort (data);
    	
		for (int n = 0; n < data.length; n++) {
			int lastDatum = data [n];
			int count = 0;
			int lookahead;
			for (lookahead = n; lookahead < data.length && lastDatum == data[lookahead]; lookahead++) {
				lastDatum = data [lookahead];
				count++;
			}
			n = lookahead - 1;

			newBin.put (Integer.valueOf (lastDatum), Integer.valueOf (count));
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("bin: "+newBin);
		}
		
		return newBin;
    }
}
