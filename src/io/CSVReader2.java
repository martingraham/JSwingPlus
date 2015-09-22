package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class CSVReader2 extends Object {

	private final static Logger LOGGER = Logger.getLogger (CSVReader2.class);
	
     //private String delimiters;
     private final BufferedReader reader;
     private final String quoteChars, delimiterChars;
     private final boolean addEmpties;
     CSVColumnObj columns;
     
     private static int linesRead = 0;
     private static long nanoTotal = 0;
   

     public CSVReader2 (final Reader inputReader, final String delimiterChars, 
    		 final String quoteChars, final boolean addEmpties, final boolean readHeaders,
    		 final boolean doubleRowHeaders) {
        
    	final BufferedReader bufReader = new BufferedReader (inputReader);
    	reader = bufReader;
    	this.quoteChars = quoteChars;
    	this.delimiterChars = delimiterChars.concat("\r\n");
    	this.addEmpties = addEmpties;
    	
    	columns = new CSVColumnObj ();
 
        if (readHeaders) {
           final List<String> headerList = readDataRow ();
           for (int listIndex = 0; listIndex < headerList.size(); listIndex++) {
        	   columns.getColumnHeaderIndices().put (headerList.get (listIndex), Integer.valueOf (listIndex));
           }
           if (doubleRowHeaders) {
        	   final List<String> secondaryHeaderList = readDataRow ();
        	   String firstHeader = "";
        	   for (int listIndex = 0; listIndex < headerList.size(); listIndex++) {
        		   if (headerList.get(listIndex) != null && headerList.get(listIndex).length() > 0) {
        			   firstHeader = headerList.get(listIndex);
        		   }
        		   if (listIndex < secondaryHeaderList.size() && secondaryHeaderList.get(listIndex) != null && secondaryHeaderList.get(listIndex).length() > 0) {
        			   headerList.set (listIndex, firstHeader+" "+secondaryHeaderList.get(listIndex));
        		   }
        		   columns.getColumnHeaderIndices().put (headerList.get(listIndex), Integer.valueOf (listIndex));
        	   }
           }
           columns.getColumnList().addAll (headerList);
        }
     }
     

     public List<String> readDataRow () {

    	//long nano = System.nanoTime();
     	List<String> stringList = null;
     	String str;
     
     	try {
     		str = reader.readLine ();
     		//LOGGER.debug ("s: "+s);
     	} catch (final IOException ioe) {
     		LOGGER.debug (ioe.toString());
     		return stringList;
     	}
     	
     	if (str != null && !str.isEmpty()) {
     		stringList = new ArrayList<String> ();
     		if (str.charAt(0) == '#' || str.charAt(0) == ';') { // comment line
     			dealWithCommentLine (str, stringList);
     		} else {
     			snapIntoList (str, stringList);
     		}
     	}
     	
     	//nanoTotal += System.nanoTime() - nano;
     	//linesRead++;
     	//LOGGER.info ("Avg: "+nanoTotal/linesRead+" ns. per row.");
     	
     	return stringList;
     }
     
     
     void snapIntoList (final String str, final List<String> stringList) {
    	 
      	boolean inQuotes = false;		// Are we currently in a quoted section? 
      	boolean quotesPresent = false;	// Was the last element in quotes?
      	int m = 0;
      	
  		for (int index = 0; index < str.length(); index++) {
 			final char khar = str.charAt (index);
 			if (quoteChars.indexOf (khar) >= 0) {
 				quotesPresent = true;
 				inQuotes ^= true;
 			}
 		
 			else if (!inQuotes && delimiterChars.indexOf (khar) >= 0) {   
 				if (addEmpties || index - m > 0) {
 					final int quoteAdjuster = quotesPresent ? 1 : 0;
 					final String part = str.substring (m + quoteAdjuster, index - quoteAdjuster);
 					stringList.add (part);
 				}
 				m = index + 1;	
 				quotesPresent = false;
 			}    		
 		}
 	
 		if (!inQuotes) {    			
			if (addEmpties || str.length() - m > 0) {
				final int quoteAdjuster = quotesPresent ? 1 : 0;
				final String part = str.substring (m + quoteAdjuster, str.length() - quoteAdjuster);
				//LOGGER.debug ("BARLEY_PATTERN: ["+part+"]");
				stringList.add (part);
			}
 		}
     }
     
     
     void dealWithCommentLine (final String str, final List<String> stringList) {}
     
     public CSVColumnObj getColumnsObj () { return columns; }
}