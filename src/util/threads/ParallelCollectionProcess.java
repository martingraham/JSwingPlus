package util.threads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;


import org.apache.log4j.Logger;

/**
 * Abstract class that chops up a collection of objects into sublists
 * and operates on these sublists independently.
 * if your computer has multiple cores this should be done in parallel.
 * Caveat is of course make sure none of the objects depend on what could
 * happen to other objects being processed at the same time.
 * @author cs22
 *
 */
public abstract class ParallelCollectionProcess {

	static final Logger LOGGER = Logger.getLogger (ParallelCollectionProcess.class);	

	
	public void doParallel (final Collection<?> collection, final Object mergedResult) {
		
		final int cores = Runtime.getRuntime().availableProcessors();
		final int coresToUse = Math.min (cores, collection.size());
		final List<?> objList = (collection instanceof List<?>) ? (List<?>)collection :
			new ArrayList (collection);
		final int listSize = objList.size();
		LOGGER.debug ("Collection Size: "+listSize+" vs cores: "+cores);
		
		
		if (coresToUse > 1) {			 
			final CountDownLatch doneSignal = new CountDownLatch (coresToUse);
			final int maxListSize = (int)Math.ceil ((double)listSize / coresToUse);
			int tally = 0;
			final List<LatchRunnable> listOfLists = new ArrayList<LatchRunnable> ();
	        for (int core = 0; core < coresToUse; core++) {
	        	final int highTally = Math.min (listSize, tally + maxListSize);
	        	final List<?> subList = objList.subList (tally, highTally);
	        	listOfLists.add (makeSubListProcess (subList, doneSignal));
	        	Executioner.getInstance().getExecutorService().execute (listOfLists.get (core));
	        	tally = highTally;
	        }
	        
	        LOGGER.debug ("Active threads during: "+Thread.activeCount());
	        try {
	        	doneSignal.await(); // wait for all to finish
	        } catch (InterruptedException interruptError) {
	        	LOGGER.error (interruptError);
	        }
	        
	        for (LatchRunnable lRunnable : listOfLists) {
	        	addPartialResult (mergedResult, lRunnable.getResult());
	        }
		} else {
			final LatchRunnable lRunnable = makeSubListProcess (objList, null);
			lRunnable.run ();
			addPartialResult (mergedResult, lRunnable.getResult()); // except it's the full result
		}
	}
	
	abstract public LatchRunnable makeSubListProcess (final List<?> subList, final CountDownLatch cLatch);

	/**
	 * generally just refers to adding a processed sublist onto a final finished list
	 * @param mergedResult
	 * @param partialResult
	 */
	abstract public void addPartialResult (final Object mergedResult, final Object partialResult);
}
