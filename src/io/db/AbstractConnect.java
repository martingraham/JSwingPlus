package io.db;
//=====================================================================
//
//  File:    connectDS.java      
//  Summary: This Microsoft SQL Server 2005 JDBC Driver sample application
//	     demonstrates how to connect to a SQL Server database by 
//	     using a data source object. It also demonstrates how to 
//	     retrieve data from a SQL Server database by using a stored 
//	     procedure.
//  Date:    April 2006	     
//
//---------------------------------------------------------------------
//
//  This file inputStream part of the Microsoft SQL Server JDBC Driver Code Samples.
//  Copyright (C) Microsoft Corporation.  All rights reserved.
//
//  This source code inputStream intended only as a supplement to Microsoft
//  Development Tools and/or on-line documentation.  See these other
//  materials for detailed information regarding Microsoft code samples.
//
//  THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF 
//  ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
//  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
//  PARTICULAR PURPOSE.
//
//===================================================================== 

import java.sql.*;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import util.swing.JPasswordBox;



public abstract class AbstractConnect implements Connect {


	final static Logger LOGGER = Logger.getLogger (AbstractConnect.class);
	
	protected Connection con;

	
	public AbstractConnect (final Properties connectionProperties) {
		
		try {
			// Establish the connection. 
			//org.apache.derby.impl.jdbc.EmbedConnection
			
			final DataSource dataSource = makeConnection (connectionProperties);
			/*
			ClientDataSource40 ds = new org.apache.derby.jdbc.ClientDataSource40();
			//ds.setIntegratedSecurity(true);
			ds.setServerName (Messages.getString (CONNECT_PROPS, "serverName"));
			ds.setPortNumber (Integer.parseInt (Messages.getString (CONNECT_PROPS, "portNumber"))); 
			ds.setDatabaseName (Messages.getString (CONNECT_PROPS, "databaseName"));
			*/
			
			
			String password = connectionProperties.getProperty ("password", "");
			String username = connectionProperties.getProperty ("user", "");
			
			if ("".equals (password)) {
				final JPasswordBox jpb = new JPasswordBox (username);
				jpb.getDialog().setSize (240, 92);
				jpb.getDialog().setVisible (true);
				// Pause this thread till unlocked by 'ok' being pressed (turns visible off)
				
				final char[] pchars = jpb.getPassword();
				username = jpb.getUsername();
				password = new String (pchars);
				//System.out.println ("User entered password: "+password);
			}
			
			con = dataSource.getConnection (username, password);
		}
		// Handle any errors that may have occurred.
    	catch (final Exception excep) {
    		LOGGER.error (ERROR, excep);
    	}
	}
	
	public Connection getConnection () { return con; }
	
	public void close () { 
		if (con != null) {
			try { 
				con.close();
			}
			catch (final SQLException sqle) {
				LOGGER.error (ERROR, sqle);
			}
		}
	}
	
	public abstract DataSource makeConnection (final Properties connectionProperties);
}
