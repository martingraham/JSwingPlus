package io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
* Parses nexus text file through various methods into arguments that can be passed to {@link BaseForest Forest} methods.
* @author Martin Graham
* @version application
*/


public final class XMLParse {


	private final static String INDENT = "\n\t\t\t\t\t\t\t\t\t";
	private final static Logger LOGGER = Logger.getLogger (XMLParse.class);



	public static Document inputXML (final String XMLFileName, final boolean validating, final boolean namespaceAware) {

		Document document = null;

		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware (namespaceAware);
		factory.setValidating (validating);
		factory.setIgnoringElementContentWhitespace (true);
		factory.setCoalescing (true);

		/*
		try {
		    URL u = DataPrep.getURLFromJAR (XMLFileName);
			File f = new File (u.getFile());
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse (f);
		}
             */

         // THIS WORKS FOR LOCAL FILE AND JAR FILE ACCESS
		try {
		    //final URI uriBase = DataPrep.getInstance().getBaseJARURI (XMLFileName);
		    final InputStream inputStream = DataPrep.getInstance().getPossiblyZippedInputStream (XMLFileName, true);
		    LOGGER.info ("InputStream: "+inputStream.toString());
		    LOGGER.info ("XML parser: "+factory.toString());
			final DocumentBuilder builder = factory.newDocumentBuilder();
			//document = builder.parse (inputStream, uriBase.toString());
			document = builder.parse (inputStream);
		}

		/*
		try {
			File f = new File (XMLFileName);
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse (f);
		}
        */


		catch (final SAXException sxe) {
			// Error generated during parsing)
			Exception embedExcep = sxe;
			if (sxe.getException() != null) {
				embedExcep = sxe.getException();
			}
			LOGGER.error (embedExcep.toString(), embedExcep);
		}

		catch (final ParserConfigurationException pce) {
			// Parser with specified options can't be built
			LOGGER.error (pce.toString(), pce);
		}

		catch (final IOException ioe) {
			// I/O error
			LOGGER.error ("Cannot find in local directory. Will try for JAR file access. ", ioe);
		}



		// See if XML file inputStream in jar if not found locally
		if (document == null) {
			try {
				final URL url = DataPrep.getInstance().getRelativeURL (XMLFileName);
				LOGGER.info ("-----------------------");
				LOGGER.info (url.toString());
				LOGGER.info ("Path: "+url.getPath());
				LOGGER.info ("Ref: "+url.getFile());
				LOGGER.info ("------------------------");
				
				//InputStream inputStream = DataPrep.getInputStreamFromJAR (XMLFileName);
				final DocumentBuilder builder = factory.newDocumentBuilder();
				LOGGER.info ("factory parser: "+factory.toString());

				/*
				File f = new File (u.getFile());
				LOGGER.info ("------------------------");
				LOGGER.info ("Filename: "+f.getName());
				LOGGER.info ("Filepath: "+f.getPath());
				LOGGER.info ("AbsFilePath: "+f.getAbsolutePath());
				LOGGER.info ("------------------------");
				*/

				//document = builder.parse (f.getPath());
				document = builder.parse (url.toString());
				//document = builder.parse (new File (u.getFile()) );
			}

			catch (final SAXException sxe) {
				LOGGER.error (sxe.toString(), sxe);
			}

			catch (final ParserConfigurationException pce) {
				// Parser with specified options can't be built
				LOGGER.error (pce.toString(), pce);
			}

			catch (final IOException ioe) {
				// I/O error
				LOGGER.error (ioe.toString(), ioe);
			}

 		}

		return document;
	}



    public static String getPCDataFromElement (final Element elem) {

        // For some reason, some cdata sections get chopped into multiple text nodes
        // this routine copes with gathering them together again

        final NodeList nodeList = elem.getChildNodes();
        final StringBuilder sBuilder = new StringBuilder ();

        for (int n = 0; n < nodeList.getLength(); n++) {
            LOGGER.debug ("Datum "+n+" : "+nodeList.item(n).getTextContent());
            sBuilder.append (nodeList.item(n).getTextContent());
        }

        LOGGER.debug ("toString2: "+elem.getFirstChild().getNodeValue());
        LOGGER.debug ("toString2: "+elem.getFirstChild().getTextContent());
        LOGGER.debug ("Data: "+((CharacterData)elem.getFirstChild()).getData());
		return sBuilder.toString();
	}


	public static Document generateDOMFromString (final String XMLFile) {

		Document document = null;

		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware (false);
		factory.setValidating (false);
		factory.setIgnoringElementContentWhitespace (true);

         // THIS WORKS FOR LOCAL FILE AND JAR FILE ACCESS
		try {
		    //URI uriBase = DataPrep.getBaseJARURI (XMLFileName);
			final DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse (new InputSource (new StringReader (XMLFile)));
			//document = builder.parse (new InputSource (new StringReader (XMLFile)), uriBase.toString());
		}


		catch (final SAXException sxe) {
			// Error generated during parsing)
			Exception embedExc = sxe;
			if (sxe.getException() != null) {
				embedExc = sxe.getException();
			}
			LOGGER.error (embedExc.toString(), embedExc);
		}

		catch (final ParserConfigurationException pce) {
			// Parser with specified options can't be built
			LOGGER.error (pce.toString(), pce);
		}

		catch (final IOException ioe) {
			// I/O error
			LOGGER.error ("Cannot find in local directory. Will try for JAR file access. ", ioe);
		}

		return document;
	}




	public static Document xmlTransform (final Document doc, final String styleSheetName) {

		try {
			final InputStream styleSheet = DataPrep.getInstance().getInputStream (styleSheetName);
			final StreamSource stylesource = new StreamSource (styleSheet);

			final TransformerFactory tFactory = TransformerFactory.newInstance();
			final Transformer transformer = tFactory.newTransformer (stylesource);

			final DOMSource source = new DOMSource (doc);

			/*
            File f = new File ("XMLOutput.xml");
			StreamResult sr = new StreamResult (f); // Output.
            transformer.transform (source, sr);
			*/

            final DOMResult domResult = new DOMResult();
            transformer.transform (source, domResult);

            final Document doc2 = (Document)domResult.getNode();
            return doc2;
		}
		catch (final TransformerConfigurationException tce) {
			LOGGER.error ("** Transformer Factory error: ", tce);
			return null;

        } catch (final TransformerException te) {
        	LOGGER.error ("** Transformer error: ", (te.getException() == null ? te : te.getException()));
        	return null;

        } catch (final FileNotFoundException fnfe) {
        	LOGGER.error ("File not found", fnfe);
        	return null;
        }
	}
}