package io.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public final class FormatResultSet {

	static final Logger LOGGER = Logger.getLogger (FormatResultSet.class);
	static final private FormatResultSet INSTANCE = new FormatResultSet ();
	
	public static FormatResultSet getInstance () {
		return INSTANCE;
	}
	
	private FormatResultSet () {}
	
	public void printResultSet (final ResultSet resultSet) {
		try {
			final StringBuilder sbuild = new StringBuilder ();
			
			final ResultSetMetaData rsmd = resultSet.getMetaData();
			final int cols = rsmd.getColumnCount();
			
			for (int col = 1; col <= cols; col++) {
				sbuild.append ("\t"+rsmd.getColumnLabel(col));
			}
			LOGGER.info (sbuild.toString());
			
			sbuild.setLength (0);
			for (int col = 1; col <= cols; col++) {
				sbuild.append ("\t----------");
			}
			LOGGER.info (sbuild.toString());
			
			sbuild.setLength (0);
			while (resultSet.next()) {
				for (int col = 1; col <= cols; col++) {
					sbuild.append ("\t"+resultSet.getObject(col));
				}
				sbuild.append ("\n");
				//LOGGER.info (sbuild.toString());
			}
			LOGGER.info (sbuild.toString());
		}
		catch (SQLException sqle) {
			LOGGER.error (sqle);
			sqle.printStackTrace();
		}
	}
}
