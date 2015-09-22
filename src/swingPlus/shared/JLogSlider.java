package swingPlus.shared;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.border.BevelBorder;

import org.apache.log4j.Logger;

/**
 * JSlider extended class that generates exponentially scaled values from
 * the values in the slider.
 * @author cs22
 *
 */
public class JLogSlider extends JSlider implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5364044341092471439L;
	private final static Logger LOGGER = Logger.getLogger (JLogSlider.class);
	
	
	
	double base, lin, scaledMin;
	
    public JLogSlider() {
        this (HORIZONTAL, -3, 3);
    }

    public JLogSlider (final int orientation) {
        this (orientation, -3, 3);
    }

    public JLogSlider (final int minExponent, final int maxExponent) {
        this (HORIZONTAL, minExponent, maxExponent);
    }
    
    public JLogSlider (final int orientation, final int minExponent, final int maxExponent) {
    	this (orientation, minExponent, maxExponent, Math.E);
    }
    
    public JLogSlider (final int orientation, final int minExponent, final int maxExponent,
    		final double base) {
	   	super (orientation);
	   	//final int constrainedexponent = Math.min (Math.max (minExponent, initialExponent), maxExponent);
	   	
	   	this.base = base;
	   	lin = 10.0;
	   	scaledMin = minExponent;
	   	
	   	final double range = maxExponent - minExponent;
	   	final int initialPoint = (int)(-minExponent * lin); 
		this.getModel().setRangeProperties (initialPoint, 0, 0, (int)(range * lin), false);
   	
		setBorder (BorderFactory.createBevelBorder (BevelBorder.RAISED));
		setOpaque (false);
		//setBackground (new Color (192, 192, 192));
		setMajorTickSpacing ((int)lin);
		
		setInverted (true);
		
		final Hashtable<Integer, JComponent> symbolTable = new Hashtable <Integer, JComponent> ();
		symbolTable.put (Integer.valueOf (getMinimum()), new JLabel ("-" /*IconCache.makeIcon("ZoomInIcon")*/));
		symbolTable.put (Integer.valueOf (initialPoint), new JLabel ("="));
		symbolTable.put (Integer.valueOf (getMaximum()), new JLabel ("+" /*IconCache.makeIcon("ZoomOutIcon")*/));
		setLabelTable (symbolTable);
		
		setPaintLabels (true);
	}
    
    @Override
	public Hashtable<Integer, JComponent> createStandardLabels (final int increment, final int start) {
    	final Hashtable<Integer, JComponent> symbolTable = new Hashtable <Integer, JComponent> ();
    	
    	for (int n = start; n <= getMaximum(); n += increment) {
    		final double scaledVal = Math.pow (base, (n / lin) + scaledMin);
    		symbolTable.put (n, new JLabel (Double.toString (scaledVal)));
    	}
    	return symbolTable;
    }
    
    @Override
	public Hashtable<Integer, JComponent> createStandardLabels (final int increment) {
    	return createStandardLabels (increment, getMinimum());
    }
    
    
    public double getScaleFromValue () {
    	return getScaleFromValue ((double)getValue());
    }
    
    
    public double getScaleFromValue (final double value) {
    	return Math.pow (base, (value / lin) + scaledMin);
    }
    
    
    public int getValueFromScale (final double scale) {
		final double log = Math.log (scale) / Math.log (base);
		final double val = (log - scaledMin) * lin;
		final int intVal = (int) Math.round (val);
		return intVal;
    }
    
    
    /**
     * As a slider model is a range of integer values not all continuous
     * values can be represented exactly on the scale. This method returns
     * the nearest double that can be represented as an exact point on the slider.
     * If we don't calculate this what we get is 'jumps' when we feed one scale value into the slider
     * and a slightly different scale results.
     * 
     * @param targetValue - original double
     * @return nearest double to targetValue that can be represented as an exact point on the slider
     */
    public double roundedValue (final double targetValue) {
		final int intVal = getValueFromScale (targetValue);
		return getScaleFromValue ((double)intVal);
    }

    
    /**
     * Changes to scale from other sources are fed back here.
     * The slider then adjusts it's position to fit this value.
     */
	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		final String property = evt.getPropertyName();
        if ("scale".equals (property)) {
        	setValueFromScale (((Double)evt.getNewValue()).doubleValue());
        }
	}
	
	
    public void setValueFromScale (final double scale) {
    	if (!this.getModel().getValueIsAdjusting()) {
    		final int intVal = getValueFromScale (scale);
	    	setValue (intVal);
    	}
    }
}
