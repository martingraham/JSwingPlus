package util.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class Executioner {


	
	static final private Executioner INSTANCE = new Executioner ();
	
	static public Executioner getInstance () { return INSTANCE; }
	
	
	
	ExecutorService execService;
	
	private Executioner () {  
		execService = new ThreadPoolExecutor (0, Integer.MAX_VALUE,
                10L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
		//execService = Executors.newCachedThreadPool ();
		
		Runtime.getRuntime().addShutdownHook (
			new Thread () {
				public void run () {
					execService.shutdown ();
				}
			}
		);
	}
	
	public ExecutorService getExecutorService () { return execService; }
	
}
