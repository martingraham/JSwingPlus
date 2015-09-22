package io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import util.Messages;

public class RegexDelimitedParser {

	private final static Logger LOGGER = Logger.getLogger (RegexDelimitedParser.class);
	
    protected final RegexReader regexReader;
    CSVColumnObj columns;
    
  
    public static void main (final String[] args) {
		InputStream inStream = null;
		try {
			inStream = DataPrep.getInstance().getInputStream (args[0]);
		} catch (FileNotFoundException e) {
			LOGGER.error (e.toString(), e);
		}

		final RegexReader regexReader = new RegexReader (inStream, Messages.getString ("regex", "CSV_REGEX"));
		if (regexReader != null) {
			//RegexDelimitedParser rReader = new RegexDelimitedParser (fr, true); 
			String[] dataRow;
			

		    try {
				while (!((dataRow = regexReader.readAndSplitLine()) == null)) {   
					for (Object obj : dataRow) {
						LOGGER.debug ("["+obj+"] ");
					}
				}
			} catch (IOException e) {
				LOGGER.error (e);
			}
		}

    	
    }
    

    public RegexDelimitedParser (final RegexReader in, final boolean readHeaders) {
       
   		regexReader = in;
   	
   		columns = new CSVColumnObj ();

       if (readHeaders) {
    	   try {
    		   	final String[] al = regexReader.readAndSplitLine();
    		   	if (al != null) {
	          		for (int n = 0; n < al.length; n++) {
	          			columns.getColumnHeaderIndices().put (al [n], Integer.valueOf (n));
	          		}
	          		columns.getColumnList().addAll (Arrays.asList (al));
    		   	}
			} catch (IOException e) {
				LOGGER.error (e);
			}
       }
    }
    
    
    
    void dealWithCommentLine (List<String> strings) {}
    
    public CSVColumnObj getColumnsObj () { return columns; }
}
