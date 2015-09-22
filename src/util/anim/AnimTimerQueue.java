package util.anim;


import javax.swing.Timer;

import org.apache.log4j.Logger;

import util.collections.CircularQueue;

/**
 * 
 * @author cs22
 * Class that maintains a queue of AnimTimer objects.
 * Every time a AnimTimer is added to the queue it adds a listener to
 * the AnimTimer through the StopWatchListener interface.
 * Once stop() is called on the AnimTimer the timer at the head of the queue is
 * popped off and started, and so on, until the queue empties.
 * This allows animation steps to be chained together.
 */
public class AnimTimerQueue extends CircularQueue<AnimTimer> implements StopWatchListener {

	private final static Logger LOGGER = Logger.getLogger (AnimTimerQueue.class);
	
	
	AnimTimer lastOffer;
	
	public AnimTimerQueue () {
        this (10);
    }

    public AnimTimerQueue (final int size) {
    	super (size);
    }
    
    @Override
    public boolean offer (final AnimTimer element) {
        final boolean wasEmpty = isEmpty ();
        LOGGER.debug ("offer B4 Q: "+this+" "+this.hashCode());
        LOGGER.debug ("lo: "+lastOffer+", element: "+element);
        
        if (lastOffer == element) {
        	return false;
        }
        
        lastOffer = element;
        
    	final boolean success = super.offer (element);
    	
    	
        if (success) {
        	if (wasEmpty) {
        		element.start ();
        	}
        	element.addStopWatchListener (this);
        	
        	LOGGER.debug ("offer after Q: "+this);
        }
        return success;
    }

	@Override
	public void timerStopped (final Timer stoppedTimer) {
		LOGGER.debug ("stopped Q: "+this);
		
		if (stoppedTimer instanceof AnimTimer) {
			((AnimTimer)stoppedTimer).removeStopWatchListener (this);
		}
		
		if (! this.isEmpty()) {
			this.remove();
			
			if (! this.isEmpty()) {
				final AnimTimer nextTimer = this.peek();
				LOGGER.debug ("stopped to Q: "+this);
				nextTimer.start ();
			} else {
				lastOffer = null;
			}
		}
	}
}
