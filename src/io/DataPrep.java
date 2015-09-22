package io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import util.XMLConstants2;


/**
* Class that holds static methods for performing basic I/O such as reading in text files.
* Can now autodetect zip files and wrap them in appropriate input streams
* @author Martin Graham
* @version 1.100827
*/

public final class DataPrep {

	static final Logger LOGGER = Logger.getLogger (DataPrep.class);
	
	private static final DataPrep DP_INSTANCE = new DataPrep ();
	
	// 4 bytes that indicate the start of a zip file (bytes in little endian order)
	public static final int ZIP_HEADER_WORD = 0x04034b50;
	



	private DataPrep () {}

	public static DataPrep getInstance() { return DP_INSTANCE; }


    /**
     * Gets an InputStream from the local scope JAR file for the given filename .<P>
     * @param fileName file name
     * @return InputStream pointing to file
     */
    public String fileToString (final String fileName) {

    	final StringBuilder strBuilder = new StringBuilder ();
    	try {
        	final BufferedReader bReader = getBufferedReader (fileName);
        	String strLine;
	        //Read File Line By Line
	        while ((strLine = bReader.readLine()) != null)   {
	        	strBuilder.append (strLine);
	        }
    	} catch (final Exception e){//Catch exception if any
            LOGGER.error ("Error: " + e.getMessage());
    	}

    	return strBuilder.toString ();
    }
    
    
    
    public String inputStreamToString (final InputStream iStream) {

    	final StringBuilder strBuilder = new StringBuilder ();
    	try {
        	final BufferedReader bReader = getBufferedReader (iStream);
        	String strLine;
	        //Read File Line By Line
	        while ((strLine = bReader.readLine()) != null)   {
	        	strBuilder.append (strLine);
	        }
    	} catch (final Exception e){//Catch exception if any
            LOGGER.error ("Error: " + e.getMessage());
    	}

    	return strBuilder.toString ();
    }
	
	
    public void printFile (final BufferedReader bReader, final File file) {

    	try {
        	final PrintWriter pWriter = makeBufferedPrintWriter (file, XMLConstants2.UTF8, false);
        	String strLine;
	        //Read File Line By Line
	        while ((strLine = bReader.readLine()) != null)   {
	          // Print the content on the console
	          pWriter.println (strLine);
	        }
    	} catch (final Exception e){//Catch exception if any
            LOGGER.error ("Error: " + e.getMessage());
    	}

    }

    public BufferedReader getBufferedReader (final String fileName) {
     	final InputStream iStream = getInputStream (getRelativeURL (fileName));
    	LOGGER.info ("Filename: "+fileName+"\tis: "+iStream.toString());
    	return getBufferedReader (iStream);
    }


    public BufferedReader getBufferedReader (final InputStream iStream) {
		return new BufferedReader (new InputStreamReader (iStream));
    }

    /**
     * Main routine to construct an input stream on various types of resource
     * First looks for an absolute url (http://), then for a local file, 
     * then tries constructing a relative url path in the local file system.
     * If none of these work it throws a FileNotFoundException
     * @param fileName - name of url / file to make an InputStream for
     * @return InputStream object, null if Stream cannot be constructed (should throw error if this inputStream the case)
     * @throws FileNotFoundException
     */
    public InputStream getInputStream (final String fileName) throws FileNotFoundException {
    	InputStream iStream = null;
    	
    	if (fileName.startsWith ("http://")) {
			try {
				iStream = getInputStream (new URL (fileName));
			} catch (MalformedURLException malURLError) {
				LOGGER.error (malURLError.toString(), malURLError);
			}
    	}
		
		if (iStream == null) {
			final File file = new File (fileName);
			LOGGER.debug ("F name: "+file.getAbsolutePath().toString());
	
			if (file.exists()) {
				iStream = getInputStream (file);
			}
			else {
				final URL url = getRelativeURL (file.getPath());
				if (url != null) {
					LOGGER.debug ("URL fromFile: "+url.toString());
					iStream = getInputStream (url);
				}
			}
		}
    	
		if (iStream == null) {
			throw new FileNotFoundException ("Cannot find: "+fileName);
		}
		return iStream;
    }
    



    public InputStream getInputStream (final File file) {

		URL url = null;
		try {
			url = file.toURI().toURL();
		} catch (MalformedURLException error) {
			LOGGER.error (error.toString(), error);
			return null;
		}
		return getInputStream (url);
	}


	public InputStream getInputStream (final URL url) {

		InputStream iStream;

		try	{
			iStream = url.openStream ();
      	}
    	catch (final IOException ioError)      {
    		LOGGER.error ("Error opening up Input Stream", ioError);
      		iStream = null;
    	}

    	return new BufferedInputStream (iStream);
	}

	
	
	public URL getBaseURL (final String dir) {
		return this.getClass().getResource (dir);
	}
	
	/**
	 * Gets the directory a jar or source code is running in.
	 * Good for relative references.
	 * @return
	 */
	public URI getHoldingDirectory () {
		final File file = new File (".");
		final File absFile = new File (file.getAbsolutePath());
		final File parentFile = absFile.getParentFile ();
		//file.delete ();
		//absFile.delete();
		return parentFile.toURI();
	}

	// public static URL getRelativeURL (String fileName)
	/**
	* Routine that obtains an URL address for a file either from a Jar file or from the local directory
	* Appears to work on application AND applet, happily enough.<P>
	* @param fileName filename of target file
	* @return URL address of target file
	*/
	public URL getRelativeURL (final String fileName) {

		URL url = null;

		// Try for resource via class-based resource loader, for paths beginning with /
		try {
			url = DataPrep.class.getResource (fileName);
			LOGGER.debug ("Class based loader gives: "+url);
		}
		catch (final Exception error)      {
			LOGGER.debug ("Error in DataPrep.getRelativeURL method. ", error);
			url = null;
		}

		// Otherwise try thread-based class loader
		if (url == null) {	// If can't get, try relative or absolute path to current JAR/directory base address
			// In a jar, the following command will return the directory the jar
			// is executing in iff the jar's manifest file's classpath contains '.'
			// In a file structure it will return the directory the application is running in e.g. blah/blah/build/
			final ClassLoader cLoader = Thread.currentThread().getContextClassLoader();
			url = cLoader.getResource (fileName); 
			LOGGER.debug ("Thread based class loader gives: "+url);
		}
		
		// Absolute file or ../ prefixed file?
		if (url == null) {
			try {
				URI uri = new URI (fileName);
				if (!uri.isAbsolute()) { // works for ../
					final ClassLoader cLoader = Thread.currentThread().getContextClassLoader();
					final URL baseURL = cLoader.getResource (".");
					uri = baseURL.toURI().resolve (uri);
					LOGGER.info ("Resolved URI: "+uri);
				}
				url = uri.toURL();
			} catch (MalformedURLException malURLError) {
				LOGGER.error (malURLError.toString(), malURLError);
			} catch (URISyntaxException uriSynError) {
				LOGGER.error (uriSynError.toString(), uriSynError);
			} catch (IllegalArgumentException illArgError) {
				LOGGER.error ("Illegal Argument Exception caught.", illArgError);
			}
			LOGGER.debug ("Absolute file loader gives: "+url);
		}

		return url;
	}
	
	
	/**
	 * Test an InputStream for zipness and return as a ZipInputStream if so
	 * @param fileName - String representing filename to input
	 * @return InputStream - either original argument or ZipInputStream subclass
	 */
	public InputStream getPossiblyZippedInputStream (final String fileName, final boolean readFirstEntry) throws FileNotFoundException {
		return getPossiblyZippedInputStream (getInputStream (fileName), readFirstEntry);
	}
	
	/**
	 * Test an InputStream for zipness and return as a ZipInputStream if so
	 * @param inputStream - InputStream to test for zipness
	 * @return InputStream - either original argument or ZipInputStream subclass
	 */
	public InputStream getPossiblyZippedInputStream (InputStream iStream, final boolean readFirstEntry) {
		if (iStream != null) {
			LOGGER.debug ("inputStream: "+iStream.toString()+"\nmark supported: "+iStream.markSupported());
			if (DataPrep.getInstance().isZip (iStream)) {
				try {
					final ZipInputStream zis = new ZipInputStream (iStream);
					if (readFirstEntry) {
						zis.getNextEntry ();
					}
					iStream = zis;
				}
				catch (final IOException ioe) {
					LOGGER.error ("IOE Exception accessing Zip Entry: "+ioe.toString());
				}
			}
		}
		return iStream;
	}
	

    
    
    /**
     * Tests if this inputStream a stream to a zip file by reading the first 4 bytes and
     * asking if they're a standard zip header
     * @param inputStream - InputStream to test
     * @return true if a zip file, false if not
     */
    public boolean isZip (final InputStream iStream) {
    	
    	final BufferedInputStream bis = new BufferedInputStream (iStream);
    	boolean zip = false;
    	final byte[] headerWordBytes = new byte [4];

    	try {
    		iStream.mark (128);
			bis.read (headerWordBytes);
			int wordVal = 0;
			// Zip file words are stored little endian (i.e. least significant bytes first)
			for (int n = 0; n < headerWordBytes.length; n++) {
				wordVal += (headerWordBytes [n] << (n * 8));
			}
			zip = (wordVal == ZIP_HEADER_WORD);
			iStream.reset();
		} catch (IOException ioe) {
			LOGGER.error (ioe.getMessage(), ioe);
		}
		LOGGER.debug ("zip: "+zip);
		return zip;
    }
    
    
    /**
     * Method to read an entry in a zip file as a string, copes with size == -1
     * @param zis - ZipInputStream
     * @param entry - ZipEntry in ZipInputStream
     * @return String containing the contents of the file represented by the ZipEntry
     */
	public String readZipEntry (final ZipInputStream zis, final ZipEntry entry) {

		final int size = (int)entry.getSize();
		final byte[] data = new byte [Math.max (8192, size)];
		final StringBuilder sBuilder = new StringBuilder ();
		
    	try {
    		int read = 0;
    		
    		do {
    			int offset = 0;
    			
	    		do {
	    		    read = zis.read (data, offset, data.length - offset);
	    		    offset += read;
	    		}
	    		while (read != -1 && offset < data.length);
	    		sBuilder.append (new String (data, 0, offset + (read == -1 ? 1 : 0)));
    	    }
    		while (read != -1);
			zis.closeEntry ();
		}
		catch (final IOException ioe) {
			LOGGER.error ("Error in making input stream from zip entry.", ioe);
			return null;
		}

	    return sBuilder.toString();
	}

	
	/**
	 * Wrapper around {@link #readZipEntry (ZipInputStream, ZipEntry)} that still returns a zip entry contents as a String.
	 * It uses Strings to state the zipFilepath and entryName
	 * rather than specific {@link ZipInputStream} and {@link ZipEntry} objects as readZipEntry requires.
	 * @param zipFilepath - the zip file to open
	 * @param entryName - the entry within the zip file to return
	 * @return a String object holding the contents of the entry 
	 */
	public String getZipEntryContents (final String zipFilepath, final String entryName) {
		String zipEntryContents = null;
		try {
			final InputStream iStream = getInputStream (zipFilepath);
			final ZipInputStream zipIn = (ZipInputStream)getPossiblyZippedInputStream (iStream, false);
	    	ZipEntry entry;
			while ((entry = zipIn.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					final String name = entry.getName();
	
					if (entryName != null && entryName.equals (name)) {
						zipEntryContents = readZipEntry (zipIn, entry);
						LOGGER.debug ("ZipEntry Contents for ["+entryName+"] in file "+zipFilepath+"\n"+zipEntryContents);
					}
				}
			}
		}
		catch (final IOException ioe) {
			LOGGER.error (ioe.toString(), ioe);
		}
		
		return zipEntryContents;
	}
	
	
	/**
	 * copies input stream to output stream. Closes input stream when done.
	 * @param inputStream
	 * @param outputStream
	 * @throws IOException
	 */
	public void copyInputStream (final InputStream inputStream, final OutputStream outputStream) throws IOException {
		final byte[] buffer = new byte [1024];
	    int len;

	    while((len = inputStream.read (buffer)) >= 0)
	      outputStream.write (buffer, 0, len);

	    inputStream.close();
	    //out.close();
	}
	
	
	
	
	public void copyZipEntries (final ZipFile zInFile, final ZipOutputStream zOut, final Collection<String> exceptTheseEntryNames) {
		try {
			final Enumeration<? extends ZipEntry> entries = zInFile.entries();
			// first, copy contents from existing war 
			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement ();
				final String name = entry.getName();
				
				LOGGER.debug ("copy: " + name); 
				
				if (exceptTheseEntryNames == null || !exceptTheseEntryNames.contains (name)) { 
					zOut.putNextEntry (new ZipEntry (entry.getName())); 
					if (!entry.isDirectory()) { 
						copyInputStream (zInFile.getInputStream (entry), zOut); 
					}
	            } 
	
	           
	        } 
			zOut.closeEntry(); 
		} catch (IOException ioe) {
			LOGGER.error ("Error", ioe);
		}
	}
	
	
	



    public PrintWriter makeBufferedPrintWriter (final File file, final String encoding, final boolean zip) throws IOException {

    	OutputStream out = new FileOutputStream (file);
    	if (zip) {
    		final ZipOutputStream zout = new ZipOutputStream (out);
            zout.putNextEntry (new ZipEntry (file.getAbsolutePath()));
            out = zout;
    	}
    	return new PrintWriter (new BufferedWriter (new OutputStreamWriter (out, encoding)));
    }
    
    
    
    public PrintWriter makeBufferedPrintWriter (OutputStream oStream, final String encoding, final boolean zip) throws IOException {

    	if (zip) {
    		final ZipOutputStream zout = new ZipOutputStream (oStream);
            zout.putNextEntry (new ZipEntry ("zipped file"));
            oStream = zout;
    	}
    	return new PrintWriter (new BufferedWriter (new OutputStreamWriter (oStream, encoding)));
    }
    
    
    public void makeZip (final String zipFileName, final File startDirectory, final FileFilter filter) {
    	try {
    		final FileOutputStream fos = new FileOutputStream (zipFileName+".zip");
    		final ZipOutputStream zos = new ZipOutputStream (fos);
			LOGGER.info ("Making zipFile "+zipFileName+".zip");
			addToZipFile (zos, startDirectory, filter);
		    zos.flush(); 
		    zos.close(); 
		    LOGGER.info ("-- Finished --");
		} catch (IOException ioe) {
			LOGGER.error (ioe);
		}
    }
    
    void addToZipFile (final ZipOutputStream zos, final File file, final FileFilter fileFilter) {
    	if (file.isDirectory()) {
    		final File[] dirFiles = file.listFiles (fileFilter);
    		for (File dirFile : dirFiles) {
    			addToZipFile (zos, dirFile, fileFilter);
    		}
    	} else {
			try {
	    		final byte[] buf = new byte[1024]; 
	    		int len; 
			    final FileInputStream fin = new FileInputStream (file);
			    zos.putNextEntry (new ZipEntry (file.getPath().toString())); 
			    while ((len = fin.read(buf)) > 0) { 
			    	zos.write (buf, 0, len); 
			    }
			    //fin.
			    LOGGER.info ("[zip] Adding "+file.getPath());
			} catch (FileNotFoundException fnfe) {
				LOGGER.error (fnfe);
			} catch (IOException ioe) {
				LOGGER.error (ioe);
			} 
    	}
    }
}