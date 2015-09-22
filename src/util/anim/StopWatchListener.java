package util.anim;

import java.util.EventListener;

import javax.swing.Timer;


public interface StopWatchListener extends EventListener {

	public void timerStopped (final Timer stoppedTimer);
	
}
