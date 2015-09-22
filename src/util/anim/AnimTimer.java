package util.anim;



	public class AnimTimer extends javax.swing.Timer {

        /**
		 * 
		 */
		private static final long serialVersionUID = -1865820104494519581L;
		transient protected AnimActionBase aAction = null;

	    public AnimTimer (final int timeLimit, final AnimActionBase animAction) {
            super (timeLimit, animAction);
            aAction = animAction;
            setRepeats (true);
        }

	    @Override
	    public void start () {
	    	//stop ();
	    	//aAction.cleanUp ();
	    	aAction.init();
	    	super.start();
	    }
	    
        @Override
		public void stop () {
        	//System.err.println (aAction.toString()+" STOPPPPPPPPPPPPPPPPPPPPPPPPPPP running: "+isRunning()+", start called: "+aAction.isStartCalled());

        	if (aAction.isStartCalled() && aAction.getCompleteness() >= 1.0f) {
                // Called if an animation has just finished
        		// i.e. not just a stop/start restart() action
        		aAction.animationFinished();
                this.fireStopPerformed ();
             }
        	
        	if (isRunning ()) {
        		super.stop ();
                aAction.cleanUp ();
        	}
        }
        
        public void addStopWatchListener (final StopWatchListener stopListener) {
        	this.listenerList.add (StopWatchListener.class, stopListener);
        }
        
        public void removeStopWatchListener (final StopWatchListener stopListener) {
        	this.listenerList.remove (StopWatchListener.class, stopListener);
        }

        protected void fireStopPerformed () {
        	// Guaranteed to return a non-null array
            final Object[] listeners = listenerList.getListenerList();

            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length-2; i >= 0; i-=2) {
                if (listeners[i] == StopWatchListener.class) {
                    ((StopWatchListener)listeners[i+1]).timerStopped (this);
                }          
            }
        }
    }