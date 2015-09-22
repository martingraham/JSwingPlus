package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import util.threads.Executioner;
import util.threads.LatchRunnable;


public class MathUtils {

	private final static Logger LOGGER = Logger.getLogger (MathUtils.class);
	
	public static void main (final String args[]) {
		final int[] numbers = {0, 1, 2, 3, 12, 25, 36, 100, 137, 10000, 10080, 65536, 317017,
				1000000, 1000000, 1000000, 1000000, 1000000, 
				1000001, 1000001, 1000001, 1000001, 1000001, 
				21621600, 367567200,
				Integer.MAX_VALUE - 1, Integer.MAX_VALUE};
		/*
		LOGGER.info ("Integer numbers/n");
		for (int n = 0; n < numbers.length; n++) {
			long nano = System.nanoTime();
			final List<Integer> factors = INSTANCE.factorise (numbers [n]);
			nano = System.nanoTime() - nano;
			LOGGER.info ("Factorised "+numbers[n]+" to "+factors.size()+" factors in "+(nano/1E6)+" ms.");
			LOGGER.info (factors.toString());
		}
		*/

		
		final long[] lnumbers2 = {0, 1, 2, 3, 12, 25, (long)1E6 + 1, (long)1E6 + 2, 
					(long)1E8 + 1, (long)1E8 + 2, (long)1E10 + 1, (long)1E10 + 2,
					(long)1E11, (long)1E12, (long)1E14, (long)1E14 + 1,
					(long)1E15, (long)1E15 + 1, (long)1E16, (long)1E16 + 1, Long.MAX_VALUE};
		
		LOGGER.info ("\n\nLong numbers in long range, parallel implementation/n");
		for (int n = 0; n < lnumbers2.length; n++) {
			long nano = System.nanoTime();
			final List<Long> factors = INSTANCE.factorise (lnumbers2 [n]);
			nano = System.nanoTime() - nano;
			LOGGER.info ("Factorised "+lnumbers2[n]+" to "+factors.size()+" factors in "+(nano/1E6)+" ms.");
			LOGGER.info (factors.toString());
		}
	}
	

	private static final MathUtils INSTANCE = new MathUtils ();

	private MathUtils () {}

	public static MathUtils getInstance() { return INSTANCE; }
	
	
	
	
	public List<Long> factorise (final long value) {
		final List<Long> lowFactors = new ArrayList<Long> ();
		final int cores = Runtime.getRuntime().availableProcessors();	   
		
		if (value > 0) {
			final long limit = (long) Math.sqrt (value);
			final List<Long> highFactors = new ArrayList<Long> ();
			lowFactors.add (Long.valueOf (1));
			highFactors.add (Long.valueOf (value));
			//LOGGER.debug ("factor sqrt: "+limit);
			
			final boolean oddNumber = ((value & 1) == 1);
			final int startNumber = oddNumber ? 3 : 2;
			
			if (cores > 1 && limit > 100000) {
		        final CountDownLatch doneSignal = new CountDownLatch (cores);

		        final int maxSearchSpace = (int)Math.ceil ((double)(limit - startNumber) / cores);
		        long tally = startNumber;
		        final List<List<Long>> lowFactorLists = new ArrayList<List<Long>> ();
		        final List<List<Long>> highFactorLists = new ArrayList<List<Long>> ();
		        
		        for (int core = 0; core < cores; core++) {
		        	lowFactorLists.add (new ArrayList<Long> ());
		        	highFactorLists.add (new ArrayList<Long> ());
		        	final long highTally = Math.min (limit, tally + maxSearchSpace);
		        	if (oddNumber && (tally % 2 == 0)) {
		        		tally++;
		        	}
		        	Executioner.getInstance().getExecutorService().execute (
		        		new FactoriseRunnable (value, tally, highTally, oddNumber, 
		        			lowFactorLists.get(core), highFactorLists.get(core), doneSignal));
		        	tally = highTally + 1;
		        }
		        
		        try {
		        	doneSignal.await(); // wait for all to finish
		        } catch (InterruptedException interruptError) {
		        	LOGGER.error (interruptError);
		        }
		        
		        for (int core = 0; core < cores; core++) {
		        	lowFactors.addAll (lowFactorLists.get (core));
		        	highFactors.addAll (highFactorLists.get (core));
		        }
	        }
	        else {
	        	factoriseChunk (value, startNumber, limit, oddNumber, lowFactors, highFactors);
	        }
				
			
			// remove double occurrence of square root factor if x is a perfect square
			if (lowFactors.get (lowFactors.size() - 1).equals (highFactors.get (highFactors.size() - 1))) {
				highFactors.remove (highFactors.size() - 1);
			}
			
			Collections.reverse (highFactors);
			lowFactors.addAll (highFactors);
		}
		
		return lowFactors;
	}
	

	
	public void factoriseChunk (final long value, final long startNumber, final long limit, final boolean oddNumber,
			final List<Long> lowFactors, final List<Long> highFactors) {
		final int step = oddNumber ? 2 : 1;
		
		if (oddNumber) {
			for (long divisor = startNumber; divisor <= limit; divisor += step) {
				final long dividend = value / divisor;
				if (dividend * divisor == value) {
					lowFactors.add (Long.valueOf (divisor));
					highFactors.add (Long.valueOf (dividend));
				}
			}
		} else {
			for (long divisor = startNumber; divisor <= limit; divisor++) {
				final long dividend = value / divisor;
				if (dividend * divisor == value) {
					lowFactors.add (Long.valueOf (divisor));
					highFactors.add (Long.valueOf (dividend));
				}
			}
		}
	}
	
	
	
	class FactoriseRunnable extends LatchRunnable {

		long value, startNumber, limit;
		boolean oddNumber;
		List<Long> lowFactors, highFactors;
		
		FactoriseRunnable (final long value, final long startNumber, final long limit, final boolean oddNumber,
			final List<Long> lowFactors, final List<Long> highFactors, final CountDownLatch latch) {
			super (latch);
			this.value = value;
			this.startNumber = startNumber;
			this.limit = limit;
			this.oddNumber = oddNumber;
			this.lowFactors = lowFactors;
			this.highFactors = highFactors;
		}
		
		@Override
		public void run() {
			factoriseChunk (value, startNumber, limit, oddNumber, lowFactors, highFactors);
	        super.run ();
		}
		
		public Object getResult () { return null; }
	}
	
	
	
	
	public List<Integer> factorise (final int value) {
		final List<Integer> lowFactors = new ArrayList<Integer> ();
		
		if (value > 0) {
			final int limit = (int) Math.sqrt (value);
			final List<Integer> highFactors = new ArrayList<Integer> ();
			lowFactors.add (Integer.valueOf (1));
			highFactors.add (Integer.valueOf (value));
			LOGGER.debug ("factor sqrt: "+limit);
			
			final boolean oddNumber = ((value & 1) == 1);
			final int startNumber = oddNumber ? 3 : 2;
			final int step = oddNumber ? 2 : 1;
			
			if (oddNumber) {
				for (int divisor = startNumber; divisor <= limit; divisor += step) {
					final int dividend = value / divisor;
					if (dividend * divisor == value) {
						lowFactors.add (Integer.valueOf (divisor));
						highFactors.add (Integer.valueOf (dividend));
					}
				}
			} else {
				for (int divisor = startNumber; divisor <= limit; divisor++) {
					final int dividend = value / divisor;
					if (dividend * divisor == value) {
						lowFactors.add (Integer.valueOf (divisor));
						highFactors.add (Integer.valueOf (dividend));
					}
				}
			}
			
			// remove double occurrence of square root factor if x is a perfect square
			if (lowFactors.get (lowFactors.size() - 1).equals (highFactors.get (highFactors.size() - 1))) {
				highFactors.remove (highFactors.size() - 1);
			}
			
			Collections.reverse (highFactors);
			lowFactors.addAll (highFactors);
		}
		
		return lowFactors;
	}
	
    private <T extends Comparable<? super T>> int partition (final List<T> compObjs, final int left, final int right, final int pivotIndex) {
	    final T pivotValue = compObjs.get (pivotIndex);
	    compObjs.set (pivotIndex, compObjs.get (right));
	    compObjs.set (right, pivotValue);
	    int storeIndex = left;
	    for (int i = left; i < right; i++) {
	        if (pivotValue.compareTo (compObjs.get(i)) > 0) {
	        	final T temp = compObjs.get (storeIndex);
	        	compObjs.set (storeIndex, compObjs.get (i));
	        	compObjs.set (i, temp);
	            storeIndex++;
	        }
	    }
        final T temp = compObjs.get (storeIndex);
        compObjs.set (storeIndex, compObjs.get (right));
        compObjs.set (right, temp);
	    return storeIndex;
    }

    
    public <T extends Comparable<? super T>> T quickSelect (final List<T> compObjs, final int kSelect) {
    	
    	int left = 0, right = compObjs.size() - 1;
    	int pivotNewIndex;
    	//System.out.println ("-----------");
    	
    	while (true) {
	        final int randIndex = (int)(Math.random() * (right + 1 - left)) + left;
	        pivotNewIndex = partition (compObjs, left, right, randIndex);
	        
	        //System.out.println (r+", "+pivotNewIndex+", A; "+A);

	        if (kSelect == pivotNewIndex) {
	        	return compObjs.get (kSelect);
	        }
	        else if (kSelect < pivotNewIndex) {
	        	right = pivotNewIndex - 1;
	        }
	        else {
	        	left = pivotNewIndex + 1;
	        }
    	}
    }
    
    
	public <T extends Number & Comparable<? super T>> float getMedian (final List<T> numbers) {
		
		float median = 0.0f;
		
		if (numbers != null) {
			final int count = numbers.size();
			
			if (count > 0) {	
				if (count > 1) {
					Collections.sort (numbers);
					//System.out.println (numbers.toString());
				}
				median = ((count & 1) == 1) ? 
					numbers.get((count - 1) >> 1).floatValue() : 
					((numbers.get(count >> 1).floatValue() + numbers.get((count >> 1) - 1).floatValue())) / 2.0f;
				
			}
		}
		
		return median;
	}
	
	
	public <T extends Number & Comparable<? super T>> float getMean (final List<T> numbers) {
		
		float mean = 0.0f;
		
		if (numbers != null) {
			final int count = numbers.size();
			
			if (count > 0) {	
				float total = 0.0f;
				for (final T f : numbers) {
					total += f.floatValue();
				}
				mean = total / count;
			}
		}
		
		return mean;
	}
}
