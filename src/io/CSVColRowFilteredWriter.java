package io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.apache.log4j.Logger;

public class CSVColRowFilteredWriter extends CSVSimpleWriter {

	/**
	 * Class for parsing Games Analytics data
	 
	 * 1. Combines rows with less than X columns into one row as the original formatting 
	 *    sometimes knocked the last column onto a newline in the original file
	 *    ***actually had to knock that on the head as the last column entry was counted as a null if the delimiter wasn't knocked
	 *    onto the next newline as well, so I've just ignored rows with say 3 or less columns as these are chopped off from the previous row.
	 *    
	 * 2. filterColumns method only keeps columns in a row according to values in a BitSet.
	 * 3. Can be set up as an anonymous class with an isRowWriteable method to limit number of rows
	 */
	
	private final static Logger LOGGER = Logger.getLogger (CSVColRowFilteredWriter.class);
	protected int expectedColumnCount;
	protected List<String> partial;

	public CSVColRowFilteredWriter (final String fileName, final String encoding, final char delimiterChar, 
			final char quoteChar, final String lineEnd, final boolean zip, final int expectedColumnCount) {     
    	super (fileName, encoding, delimiterChar, quoteChar, lineEnd, zip);
    	this.expectedColumnCount = expectedColumnCount;
    	partial = new ArrayList<String> ();
     }
	
	
	public void writeLine (final List<String> parts) {
		if (parts.size() >= 4) {
			/*
		   	 if (pWriter != null && isRowWriteable (parts)) {
		   		 if (!partial.isEmpty()) {
		   			 parts.addAll (0, partial);
		   			LOGGER.info ("merged row: "+Arrays.toString(parts.toArray()));
		   		 }
		   		 
		   		 if (parts.size() < expectedColumnCount) {
		   			LOGGER.info (linesWritten+": "+Arrays.toString(parts.toArray()));
		   			 partial.clear();
		   			 partial.addAll (parts);
		   		 }
		   		 else {
		   		 */
		   			 super.writeLine (parts);
		   			// partial.clear();
		   		 //}
		   // }
		}
    }
	
	
    public void filterColumns (final List<String> parts) {
	   	 if (filterObj != null) {
	   		 if (filterObj instanceof BitSet) {
	   			 final BitSet columnFilter = (BitSet)filterObj;
	   			 
	   		     int shift = 0;
		        for (int n = 0; n < parts.size(); n++) {
		        	
		            if (shift > 0 && columnFilter.get(n)) {
		            	parts.set (n - shift, parts.get(n));
			        }

		            if (! columnFilter.get(n)) {
		            	shift++;
		            }
		        }
		        
		        final int listEnd = parts.size();
		        for (int n = listEnd; --n >= listEnd - shift;) {
		            parts.remove (n);
		        }
	   		 }
	   	 }
    }
}
