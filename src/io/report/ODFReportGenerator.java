package io.report;

import io.DataPrep;
import io.TableModelWriter;
import io.parcoord.MakeTableModel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.Messages;
import util.XMLUtil;
import util.screenshot.ScreenshotComponent;

public class ODFReportGenerator extends AbstractReportGenerator {

	static final Logger LOGGER = Logger.getLogger (ODFReportGenerator.class);

	String templatePath;
	
	final static String MANIFEST_IMAGE_ENTRY = Messages.getString ("io.report.odfreport", "ODFManifestImageEntry");
	final static String IMAGE_TEMPLATE = Messages.getString ("io.report.odfreport", "ODFImageEntry");
	
	final static String ODF_FIELD_SPLIT_REGEX_2 = "\\{[\\w\\s]{5,9}\\}";
	final static Pattern ODF_FIELD_SPLIT_PATTERN_2 = Pattern.compile (ODF_FIELD_SPLIT_REGEX_2);

	final static String MANIFEST_ZIP_ENTRY_STRING = "META-INF/manifest.xml";
	final static String CONTENT_ZIP_ENTRY_STRING = "content.xml";
	
	
	public ODFReportGenerator () {
		super ();
		setTableWriter (TableModelWriter.getTableModelWriter ("ODF"));
	}
	
	
	public final static void main (final String[] args) {
		final String fileName = (args.length > 0 ? args[0] : Messages.getString ("ExampleDataFile"));
		final MakeTableModel mtm = new MakeTableModel ();
		final DefaultTableModel dtm = mtm.buildDataModel (fileName);		
		LOGGER.debug ("dtm: "+dtm.getRowCount());

		final ODFReportGenerator rGen = new ODFReportGenerator ();
		rGen.setTemplate ("templates/template.odt");
		rGen.setTableData (dtm, null, null);
		rGen.populateTemplate ();
	}
	
	
	public void setTemplate (final String path) {
		templatePath = path;
	}
	

	/**
	 * Populates an ODF text document template.
	 * This routine reads in XML as strings and then parses the strings to DOM objects.
	 * This is because reading entries from zip files needs more than just a ZipInputStream
	 * object to do correctly (needs ZipEntry object as well for correct length)
	 */
	public void populateTemplate () {
		
		if (templatePath != null && saveFilePath != null/* && tableWriter.isDataSet()*/) {

			// Get contents.xml entry in zip file as DOM object
			final String contentsXml = DataPrep.getInstance().getZipEntryContents (templatePath, CONTENT_ZIP_ENTRY_STRING);				
			final Document contentsDOM = XMLUtil.getInstance().makeDOMFromString (contentsXml);
			
			try {
				// Add JSwing defined ODF styles to contents DOM
				
				// Get url of xml file holding additional JSwing defined ODF styles
				// Since this isn't in the zip file, we can get the DOM in the more normal way.
				final InputStream inputStream = this.getClass().getResourceAsStream ("odfTemplateStyles.xml");
				final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				final Document stylesDOM = docBuilder.parse (inputStream);
				inputStream.close ();

				// Get the top level node of the JSwing ODF styles document
				// (should be 'office:automatic-styles') and add its contents to the
				// first node of the same type in the contents DOM
				final Node topStyleNode = stylesDOM.getDocumentElement();
				LOGGER.debug ("topStyleNodeName: "+topStyleNode.getNodeName());
				final NodeList styleSections = contentsDOM.getElementsByTagName (topStyleNode.getNodeName());
				final Node styleSection = (styleSections.getLength() > 0) ? styleSections.item(0) : null;
				if (styleSection != null) {
					final DocumentFragment stylesFrag = XMLUtil.getInstance().createDocumentFragment (topStyleNode);
					styleSection.appendChild (contentsDOM.adoptNode (stylesFrag.cloneNode (true)));
				}
			} catch (final IOException ioe) {
				LOGGER.error ("Error in reading JSwing ODF styles", ioe);
			} catch (ParserConfigurationException pce) {
				LOGGER.error ("Error making DocumnetBuilder instance from Factory", pce);
			} catch (SAXException saxe) {
				LOGGER.error ("Error parsing JSwing ODF styles with DocumentBuilder", saxe);
			}
			
			// Replace simple {fields} in text nodes with a regex substitution
			final Map<String, String> propMap = new HashMap<String, String> ();
			propMap.put ("{customer}", properties.get("Customer Name").toString());
			propMap.put ("{report no}", properties.get("Report No.").toString());
			propMap.put ("{title}", properties.get("Report Title").toString());	
			replaceFields (contentsDOM, propMap);
			
			
			try {
				final ZipOutputStream append = new ZipOutputStream (new FileOutputStream (saveFilePath)); 
				final ZipFile zipFile = new ZipFile (templatePath);
				
				final Set<String> exceptTheseEntryNames = new HashSet<String> ();
				exceptTheseEntryNames.add (CONTENT_ZIP_ENTRY_STRING);
				exceptTheseEntryNames.add (MANIFEST_ZIP_ENTRY_STRING);
				
				DataPrep.getInstance().copyZipEntries (zipFile, append, exceptTheseEntryNames);
				
				
				final StringWriter sWriter = new StringWriter ();
				final PrintWriter pWriter = new PrintWriter (sWriter);
				tableWriter.writeEmbedded (pWriter);
				try {
					sWriter.close();
				} catch (IOException ioe) {
					LOGGER.error ("String Writer closing exception", ioe);
				}
				final String tables = sWriter.toString();
				final Document tableDocumentFragment = XMLUtil.getInstance().makeDOMFromString (tables);
				
				final Date currentDate = new Date ();
				final SimpleDateFormat sdf = new SimpleDateFormat ("yyMMdd_HHmmss");
				final String dateTime = sdf.format (currentDate);
				
				
				// Image processing
				
				// Grab and output screen images as entries in the zip file
				final ScreenshotComponent screenshotter = new ScreenshotComponent ();
				final List<String> imageNames = new ArrayList<String> ();
				if (componentsToAppend != null) {
					for (int compIndex = 0; compIndex < componentsToAppend.size(); compIndex++) {
						final JComponent jcomp = componentsToAppend.get (compIndex);
						final String imageName = "Pictures/Screenshot"+jcomp.getName()+"_"+dateTime+".png";
						imageNames.add (imageName);
						final ZipEntry newEntry = new ZipEntry (imageName); 
						append.putNextEntry (newEntry);
						screenshotter.captureAndOutputComponent (jcomp, append);
					}
				}
				
				
				// Record these image entries in the manifest file zip entry
				final String manifest = DataPrep.getInstance().getZipEntryContents (templatePath, MANIFEST_ZIP_ENTRY_STRING);			
				final Document manifestDOM = XMLUtil.getInstance().makeDOMFromString (manifest);
				final NodeList listOfPersons = manifestDOM.getElementsByTagName ("manifest:manifest");
				final Node manifestRoot = (listOfPersons.getLength() == 1) ? listOfPersons.item(0) : null;
				
				for (int index = 0; index < imageNames.size(); index++) {
					final Element imageManifestEntry = manifestDOM.createElement ("manifest:file-entry");
					imageManifestEntry.setAttribute ("manifest:full-path", imageNames.get (index));
					imageManifestEntry.setAttribute ("manifest:media-type", "image/png");
					manifestRoot.appendChild (imageManifestEntry);
				}
				
				// Add the new manifest entry to the zip file
				final String newManifest = XMLUtil.getInstance().makeStringFromDOM (manifestDOM);
				final ZipEntry manifestEntry = new ZipEntry (MANIFEST_ZIP_ENTRY_STRING); 
				append.putNextEntry (manifestEntry); 
				append.write (newManifest.getBytes()); 
				
				
				// Make relevant tags from templates to hold images in ODT format
				final StringBuilder sBuilder = new StringBuilder ("<wellformed>");
				final Format format = new MessageFormat (IMAGE_TEMPLATE); 

				for (int index = 0; index < imageNames.size(); index++) {
					final String imageName = imageNames.get (index);
					final JComponent jcomp = componentsToAppend.get (index);
					final int width = jcomp.getWidth();
					final int height = jcomp.getHeight();
					
					double widthIn = width / 72.0;
					double heightIn = height / 72.0;
					final double scale = widthIn / 6.5;
					if (scale > 1.0) {
						widthIn /= scale;
						heightIn /= scale;
					}
					
					final Object[] holders = {imageName, index, index, widthIn+"in", heightIn+"in", jcomp.getName()};
					//Object[] holders = {imageName, "gname"+index, "gid"+(index), widthPC+'%', heightPC+'%', jcomp.getName()};
					final String imageTagput = format.format (holders);
					sBuilder.append (imageTagput);
				}
				sBuilder.append ("</wellformed>");
				
				// Turn this string into a XML node to add to the content DOM
				final String imageBlock = sBuilder.toString();
				final Document imageDocNode = XMLUtil.getInstance().makeDOMFromString (imageBlock);			
				final DocumentFragment imageFragment = XMLUtil.getInstance().createDocumentFragment (imageDocNode.getFirstChild());
				
				// Replace {fields} in text with nodes, splitting up parents of text correctly
				final Map<String, Node> fieldNodeMap = new HashMap<String, Node> ();
				fieldNodeMap.put ("{tables}", tableDocumentFragment.getDocumentElement());
				fieldNodeMap.put ("{images}", imageFragment);
				replaceFieldsWithNodes (contentsDOM, fieldNodeMap);
				
				
				final String newContentXml = XMLUtil.getInstance().makeStringFromDOM (contentsDOM);
				LOGGER.debug (newContentXml);
				final ZipEntry newEntry = new ZipEntry (CONTENT_ZIP_ENTRY_STRING); 
				append.putNextEntry (newEntry); 
				append.write (newContentXml.getBytes()); 
				//append.closeEntry(); 

				zipFile.close();
				append.close();
			} catch (FileNotFoundException fnfe) {
				LOGGER.error (fnfe);
			} catch (IOException ioe) {
				LOGGER.error (ioe);
			} 
		}
	}
	
	
	void replaceFields (final Document dom, final Map<String, String> fieldMap) {
	    try {
	    	final XPath xpath = XPathFactory.newInstance().newXPath();  
	    	final XPathExpression expr = xpath.compile("//text()[contains(., \"{\")]");
	    	final Object result = expr.evaluate (dom, XPathConstants.NODESET);
	    	final NodeList nodes = (NodeList) result;
	        
	        for (int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++) {
	        	final Node node = nodes.item (nodeIndex);
	        	final String value = node.getNodeValue ();
	        	final Matcher matcher = ODF_FIELD_SPLIT_PATTERN_2.matcher (value);
	        	final StringBuilder strBuilder = new StringBuilder ();
	        	int lastMatch = 0;
	        	
	        	while (matcher.find()) {
					final String field = value.substring (matcher.start(), matcher.end());
					final String replacementText = fieldMap.get (field);
					if (replacementText != null) {
						strBuilder.append (value.substring (lastMatch, matcher.start()));
						strBuilder.append (replacementText);
						lastMatch = matcher.end();
					}
	        	}
	        	
	        	if (lastMatch > 0) {
	        		strBuilder.append (value.substring (lastMatch, value.length()));
	        		node.setNodeValue (strBuilder.toString());
	        	}
	        	LOGGER.debug (value+" --> "+node.getNodeValue());
	        }
		} catch (XPathExpressionException xpee) {
			LOGGER.error ("XPath Error when grabbing all text nodes", xpee);
		}
	}
	
	
	void replaceFieldsWithNodes (final Document dom, final Map<String, Node> fieldMap) {
	    try {
	    	final XPath xpath = XPathFactory.newInstance().newXPath();  
	    	final XPathExpression expr = xpath.compile("//text()[contains(., \"{\")]");
	    	final Object result = expr.evaluate (dom, XPathConstants.NODESET);
	    	final NodeList nodes = (NodeList) result;
	        
	        for (int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++) {
	        	final Node node = nodes.item (nodeIndex);
	        	final String value = node.getNodeValue ();
	        	final Matcher matcher = ODF_FIELD_SPLIT_PATTERN_2.matcher (value);
	        	int lastMatch = 0;
	        	final DocumentFragment dFrag = dom.createDocumentFragment();
	        	
	        	while (matcher.find()) {
					final String field = value.substring (matcher.start(), matcher.end());
					final Node replacementNode = fieldMap.get (field);
					if (replacementNode != null) {
						dFrag.appendChild (createSplitTextNode (node, lastMatch, matcher.start()));
						dFrag.appendChild (dom.adoptNode (replacementNode.cloneNode (true)));
						lastMatch = matcher.end();
					}
	        	}
	        	
	        	if (lastMatch > 0) {
	        		dFrag.appendChild (createSplitTextNode (node, lastMatch, value.length()));
					final Node originalParentNode = node.getParentNode(); // node that holds the text() node i.e. <P>text</P>
					final Node originalGrandParentNode = originalParentNode.getParentNode(); // node that holds the parent node i.e. <div><P>text</P></div>
					originalGrandParentNode.insertBefore (dFrag, originalParentNode);
					originalGrandParentNode.removeChild (originalParentNode);
	        	}
	        }
		} catch (XPathExpressionException xpee) {
			LOGGER.error ("XPath Error when replacing fields with nodes", xpee);
		}
	}
	
	/**
	 * Makes a copy of whatever element surrounds the text contained in node and splits
	 * part of the text into it.
	 * Consider a text node "text" in a parent node, say a paragraph element.
	 * This would return a cloned paragraph element, with a child text node, containing part
	 * of the original text string.
	 * i.e. "text", 2, 4 --> returns <P>ex</P>
	 * @param node - text node
	 * @param startTextIndex
	 * @param endTextIndex
	 * @return new Node containing a piece of the original text
	 */
	Node createSplitTextNode (final Node node, final int startTextIndex, final int endTextIndex) {
		final Node extraTextNode = node.cloneNode (false);
		final String value = node.getNodeValue ();
		extraTextNode.setNodeValue (value.substring (startTextIndex, endTextIndex));
		final Node extraNode = node.getParentNode().cloneNode (false);
		extraNode.appendChild (extraTextNode);
		return extraNode;
	}
}
