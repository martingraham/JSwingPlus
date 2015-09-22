package util.anim;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

	public abstract class AnimActionBase extends AbstractAction {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8869505433429043152L;

		public final static int FADE_OUT = 0, MOVE = 1, FADE_IN = 2;

		// Flag that represents whether start() was called on the AnimTimer that performs this action
		// start() in a timer should be configured to call init()
		// stop() in a timer should be configured to call cleanUp()
		private boolean startCalled = false;
		
    	private double completeness = 0.0;
    	private int[] percentTransition;
    	private double step;
    	private RangeTransformer rangeTransform;

    	public AnimActionBase (final double stepVal, final RangeTransformer newrt) {
    		super ();
    		setTransformer (newrt);
    		percentTransition = new int [newrt.transformCount()];
    		step = stepVal;
    	}

        final public void actionPerformed (final ActionEvent evt) {

            //System.out.println ("evt source: "+evt.getSource());
            //System.out.println ("completeness: "+completeness+", s: "+step+", evt; "+evt.hashCode());
	        setCompleteness (completeness + step);

        	doWork (evt);

            if (completeness >= 1.0) {
            	final javax.swing.Timer sourceTimer = (javax.swing.Timer) evt.getSource();
                sourceTimer.stop ();
            }
        }

        
        // abstract methods
        abstract public void doWork (ActionEvent evt);
        
        abstract public void animationFinished ();

        
        
		public void init () {
			startCalled = true;
			setCompleteness (0.0);
			//System.out.println ("reset to zero");
		}

		public void cleanUp () {
			//System.out.println ("in clean up");
			startCalled = false;
			setCompleteness (1.0);
		}

		
		public boolean isStartCalled () { return startCalled; }
		
		
        protected void setCompleteness (final double complete) {
        	completeness = Math.max (0.0, Math.min (complete, 1.0));
        	final double[] val = rangeTransform.transform (completeness);
        	for (int n = 0; n < val.length; n++) {
        		percentTransition [n] = (int)(val[n] * 100.0);
        	}
        	//System.out.println ("completeness: "+completeness+", percentTransition: "+percentTransition);
        }
        
        public double getCompleteness () { return completeness; }

        public int getPercentTransition (final int index) { return percentTransition [index]; }

        final public void setTransformer (final RangeTransformer newrt) { rangeTransform = newrt; }
    }