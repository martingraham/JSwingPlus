package util.anim;

public class SineTransform extends RangeTransformer {

	
	private final double start = 0.0, s2 = 0.2, s3 = 0.8, end = 1.0;
	
	@Override
	void normalisedTransform (final double value, final double[] transformValues) {
		
		if (transformCount () == 3) {
		    double val = value;
		    if (val < s2) {
		    	val = start;
		    }
		    else if (val > s3) {
		    	val = end;
		    }
		    else {
		    	val -= s2;
		    	val *= (end / (s3 - s2));
		    	val *= Math.PI;
		    	val -= Math.PI / 2.0;
		    	val = Math.sin (val);
		    	val += 1.0;
		    	val /= 2.0;
		    }
		    transformValues [1] = val;	
			
			val = value * (end / s2);
			
			if (val > end) {
				val = start;
			} else {
				val *= Math.PI;
	        	val -= Math.PI * 2.0;
	        	val = Math.cos (val);
	        	val += 1.0;
	        	val /= 2.0; 	
        	}
        	transformValues [0] = val;
        	
			val = (value - s3) * (end / s2);
			if (val < start) {
				val = start;
			} else {
				val *= Math.PI;
				val -= Math.PI / 2.0;
				val = Math.sin (val);
				val += 1.0;
				val /= 2.0;
        	}
        	transformValues [2] = val;
		} 
	}

	@Override
	int transformCount () { return transforms.length; }
}
