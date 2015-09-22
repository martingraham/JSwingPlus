package io;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

import data.OrdinalDatum;

/**
 * Class that parses comment lines in csv files to discover whether they
 * contains a pre-defined ordering for the entries in a column
 * @author cs22
 *
 */
public class CSVReaderOrdinalLines extends CSVReader2 implements OrdinalDataSource {

	private static final Logger LOGGER = Logger.getLogger (CSVReaderOrdinalLines.class);
	
	protected Map<String, Map<String, OrdinalDatum>> ordinalsByColumns;
	
	public CSVReaderOrdinalLines (final Reader inputReader, final String delimiterChars, final String quoteChars,
			final boolean addEmpties, final boolean readHeaders, final boolean doubleRowHeaders) {
		super (inputReader, delimiterChars, quoteChars, addEmpties, readHeaders, doubleRowHeaders);
		ordinalsByColumns = new HashMap <String, Map<String, OrdinalDatum>> ();
	}
	
    @Override
	void dealWithCommentLine (final String str, final List<String> stringList) {

   		snapIntoList (str, stringList);
   	 	
   	 	if (stringList.size() > 1) {
   	 		final String columnName = stringList.get(0).substring(1); // remove comment char
   	 		LOGGER.debug ("al: "+columnName);
   	 		LOGGER.debug ("obc: "+ordinalsByColumns);
   	 		
	   	 	Map<String, OrdinalDatum> ordinalColumn = ordinalsByColumns.get (columnName);
	   	 	if (ordinalColumn == null) {
	   	 		ordinalColumn = new HashMap <String, OrdinalDatum> ();
	   	 		ordinalsByColumns.put (columnName, ordinalColumn);
	   	 	}
   	 		for (int listIndex = 1; listIndex < stringList.size(); listIndex++) {
	   	 		final String ordinalString = stringList.get (listIndex);
	   	 		final int ordinalValue = listIndex;
	   	 		final OrdinalDatum ordDatum = new OrdinalDatum (ordinalString, ordinalValue);
	   	 		ordinalColumn.put (ordinalString, ordDatum);
	   	 	}
   	 	}
   	 	stringList.clear ();
    }

    @Override
    public Map<String, Map<String, OrdinalDatum>> getOrdinalData () { return ordinalsByColumns; }

    @Override
    public Map<String, OrdinalDatum> getOrdinalData (final String columnName) {
    	return ordinalsByColumns.get (columnName);
    }
}
