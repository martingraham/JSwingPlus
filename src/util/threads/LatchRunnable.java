package util.threads;

import java.util.concurrent.CountDownLatch;

public abstract class LatchRunnable implements Runnable {

	CountDownLatch cLatch;
	
	public LatchRunnable (final CountDownLatch latch) {
		this.cLatch = latch;
	}
	
	@Override
	public void run() {
        if (cLatch != null) {
        	cLatch.countDown();
        }
	}
	
	abstract public Object getResult ();
}
