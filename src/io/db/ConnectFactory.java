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

import java.util.Properties;

import org.apache.log4j.Logger;


public class ConnectFactory {
	
	final static Logger LOGGER = Logger.getLogger (ConnectFactory.class);

	
	static public Connect getConnect (final Properties connectionProperties) {
		
		final String className = connectionProperties.getProperty ("dsClass");
		Connect connect = null;
		if ("com.mysql.jdbc.jdbc2.optional.MysqlDataSource".equals(className)) {
			connect = new ConnectMySQL (connectionProperties);
		}
		else if ("com.microsoft.sqlserver.jdbc.SQLServerDataSource".equals(className)) {
			connect = new ConnectMSSQL (connectionProperties);
		}
		
		return connect;
	}
}
