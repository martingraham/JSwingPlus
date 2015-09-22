package util.anim;

public abstract class RangeTransformer  {

	final double[] transforms = new double [3];
		
	final public double [] transform (final double value) {
		
		final double bvalue = Math.min (1.0, Math.max (0.0, value));
	    normalisedTransform (bvalue, transforms);
	    
	    for (int n = 0; n < transforms.length; n++) {
	    	transforms[n] = Math.min (1.0, Math.max (0.0, transforms[n]));
	    }
	    
	    return transforms;
	}

	abstract void normalisedTransform (double value, double[] transformValues);
	
	abstract int transformCount ();
	
	double getTransform (final int index) {
		return (index >= 0 && index < transformCount()) ? transforms [index] : 0.0;
	}
}
