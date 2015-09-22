package io;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.log4j.Logger;

import util.XMLConstants2;


public class CSVSimpleWriter extends Object {

	private final static Logger LOGGER = Logger.getLogger (CSVSimpleWriter.class);
    public static final String DEFAULT_ENCODING = XMLConstants2.UTF8;
    
	 protected  PrintWriter pWriter;
     protected final char quoteChar, delimiterChar;
     protected final String lineEnd;
     protected Object filterObj;
     protected int linesWritten = 0;

     public CSVSimpleWriter (final String fileName, final String encoding, final char delimiterChar, final char quoteChar, final String lineEnd, final boolean zip) {     
    	 
    	pWriter = null;
    	this.quoteChar = quoteChar;
    	this.delimiterChar = delimiterChar;
    	this.lineEnd = lineEnd;
    	 
    	try {
	    	final File file = new File (fileName);
			pWriter = DataPrep.getInstance().makeBufferedPrintWriter (file, encoding == null ? DEFAULT_ENCODING : encoding, zip);
			LOGGER.info ("PWriter: "+pWriter);
		} catch (IOException e) {
			LOGGER.error (e.toString(), e);
		}
    	
     }
     
     
     public void writeLine (final List<String> parts) {
    	 if (pWriter != null && isRowWriteable (parts)) {
    		 filterColumns (parts);
    		 final StringBuilder sBuilder = new StringBuilder ();
	
    		 for (int col = 0; col < parts.size(); col++) {
    			 sBuilder.append (parts.get(col));
    			 sBuilder.append (col < parts.size() - 1 ? delimiterChar : lineEnd);
    		 }

    		 pWriter.write (sBuilder.toString());
    		 linesWritten++;
    	 }
     }
     
     
     public void close () {
    	 if (pWriter != null) {
    		 pWriter.close(); 
    	 }
    	 
    	 LOGGER.info ("Lines written: "+linesWritten);
     }
     
     
     public void filterColumns (final List<String> parts) {
    	 // EMPTY METHOD
     }
     
     public boolean isRowWriteable (final List<String> parts) {
    	 return true;
     }
     
     public void setFilteringObject (final Object obj) {
    	 filterObj = obj;
     }
}