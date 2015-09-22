package io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.apache.log4j.Logger;


public class CSVColumnStripper {

	private static final Logger LOGGER = Logger.getLogger (CSVColumnStripper.class);
	
	/**
	 * @param args
	 */
	public static void main (String[] args) {
		// TODO Auto-generated method stub
		if (args.length > 0) {
			final String dataFileName = args[0];
			
			InputStream is = null;
			try {
				is = DataPrep.getInstance().getInputStream (dataFileName);
			} catch (FileNotFoundException e) {
				LOGGER.error (e.toString());
				e.printStackTrace();
			}
			final BufferedReader fr = DataPrep.getInstance().getBufferedReader(is);
			
			final CSVReader2 cr = new CSVReader2 (fr, "\t,", "\"", true, false, false);
			final CSVSimpleWriter csw = new CSVColRowFilteredWriter (args[0]+".new", null, ',', '\"', "\r\n", false, 28)
			{
			    public boolean isRowWriteable (final List<String> parts) {
			    	return linesWritten < 300 * 1000;
			    }
			};
			final BitSet bs = new BitSet();
			int[] cols = {1, 4, 7, 12, 13, 20, 26};
			for (int n = 0; n < cols.length; n++) {
				bs.set(cols[n]);
			}
			csw.setFilteringObject (bs);
			
			int linesRead = 0;
			List<String> dataRow = null;
			
		    while (!((dataRow = cr.readDataRow()) == null)) { 
		    	linesRead++;
		    	csw.writeLine (dataRow);
		    	if (linesRead % 10000 == 0) {
		    		LOGGER.info ("Read "+linesRead+" lines.");
		    	}
		    }
		    
		    
		    csw.close();
		    try {
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
