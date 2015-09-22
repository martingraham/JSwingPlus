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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;




public class ConnectMSSQL extends AbstractConnect {
	

	final static Logger LOGGER = Logger.getLogger (ConnectMSSQL.class);

	
	public ConnectMSSQL(final Properties connectionProperties) {
		super (connectionProperties);
	}


	public final DataSource makeConnection (final Properties connectionProperties) {
		
		Class klass = null;
		DataSource dataSource = null;
		
		try {
			final String className = connectionProperties.getProperty ("dsClass");
			klass = Class.forName (className);
			dataSource = (DataSource) klass.newInstance();
			LOGGER.info ("ds: "+dataSource);
			
			Class[] partypes = new Class [1];
			Object[] argList = new Object [1];
			Method meth = null;
			
			partypes[0] = String.class;
            meth = klass.getMethod ("setServerName", partypes);
            argList [0] = connectionProperties.getProperty ("serverName");
            meth.invoke (dataSource, argList);

            partypes[0] = Integer.TYPE;
            meth = klass.getMethod ("setPortNumber", partypes);
            argList [0] = Integer.parseInt (connectionProperties.getProperty ("portNumber"));
            meth.invoke (dataSource, argList);
            
            partypes[0] = String.class;
            meth = klass.getMethod ("setDatabaseName", partypes);
            argList [0] = connectionProperties.getProperty ("databaseName");
            meth.invoke (dataSource, argList);		
		} catch (ClassNotFoundException cnfe) {
			LOGGER.error (ERROR, cnfe);
		} catch (InstantiationException inste) {
			LOGGER.error (ERROR, inste);
		} catch (IllegalAccessException iae) {
			LOGGER.error (ERROR, iae);
		} catch (SecurityException sece) {
			LOGGER.error (ERROR, sece);
		} catch (NoSuchMethodException nsme) {
			LOGGER.error (ERROR, nsme);
		} catch (IllegalArgumentException iae) {
			LOGGER.error (ERROR, iae);
		} catch (InvocationTargetException ite) {
			LOGGER.error (ERROR, ite);
		}
		
		return dataSource;
	}
}
