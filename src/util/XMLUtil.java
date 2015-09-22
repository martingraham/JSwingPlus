package util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class XMLUtil extends Object {


	private final static Logger LOGGER = Logger.getLogger (XMLUtil.class);
	
	private final static String TABS = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";
	
    private final static Pattern REPLACE_SPACE_PATTERN = Pattern.compile(" ");
    private final static Pattern DELETE_NON_XML_CHARS_PATTERN = Pattern.compile ("[^\\w.\\-:]");
    private final static Matcher MATCHER = REPLACE_SPACE_PATTERN.matcher ("");
    private final static Matcher NON_XML_MATCHER = DELETE_NON_XML_CHARS_PATTERN.matcher ("");
    private final static BitSet NAME_CHARS = new BitSet (256);
    private final static BitSet LEGAL_FIRST32 = new BitSet (32);
    static {
    	LEGAL_FIRST32.set (0x09);
    	LEGAL_FIRST32.set (0x0a);
    	LEGAL_FIRST32.set (0x0d);
    	NAME_CHARS.set ('.');
    	NAME_CHARS.set ('-');
    	NAME_CHARS.set (':');
    	NAME_CHARS.set ('_');
    }
    
    static private final BitSet RFC1738_SAFE_CHARS = new BitSet (128);
    static private final String SAFE = "$-_.!*\\(),+";
    static {
    	RFC1738_SAFE_CHARS.set ('a', 'z' + 1);
    	RFC1738_SAFE_CHARS.set ('A', 'Z' + 1);
    	RFC1738_SAFE_CHARS.set ('0', '9' + 1);
    	
    	for (int n = SAFE.length(); --n >= 0;) {
    		RFC1738_SAFE_CHARS.set (SAFE.charAt (n));
    	}
    }
    
    /**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Base64 Alphabet" equivalents as specified 
     * in Table 1 of RFC 2045.
     */
    private static final char INTTOBASE64[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    /**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Alternate Base64 Alphabet" equivalents.
     * This is NOT the real Base64 Alphabet as per in Table 1 of RFC 2045.
     * This alternate alphabet does not use the capital letters.  It is
     * designed for use in environments where "case folding" occurs.
     */
    private static final char INTTOALTBASE64[] = {
        '!', '"', '#', '$', '%', '&', '\'', '(', ')', ',', '-', '.', ':',
        ';', '<', '>', '@', '[', ']', '^',  '`', '_', '{', '|', '}', '~',
        'a', 'b', 'c', 'd', 'e', 'f', 'g',  'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't',  'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6',  '7', '8', '9', '+', '?'
    };

    
    
	private static final XMLUtil INSTANCE = new XMLUtil ();

	private XMLUtil () {
		super ();
	}

	public static XMLUtil getInstance() { return INSTANCE; }

    // 	any Unicode character, excluding the surrogate blocks, FFFE, and FFFF. */
    //	Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] |
    //         [#xE000-#xFFFD] | [#x10000-#x10FFFF]
    
    public String stripIllegalXML (final String input) {
    	
    	final StringBuilder inputBuffer = new StringBuilder (input); 
    	boolean changeMade = false;
    	
    	for (int n = inputBuffer.length(); --n >= 0;) {
			final char current = inputBuffer.charAt (n);
			if ((current < 0x20 && !LEGAL_FIRST32.get(current)) || 
					(current > 0xd7ff && (current < 0xe000 || current == 0xfffe || current == 0xffff))) {
					changeMade = true;
					inputBuffer.deleteCharAt (n);
			}
		}
    	
    	//if (changeMade) {
    	//	System.out.println ("In: "+input+" \tout: "+inputBuffer.toString());
    	//}
    	return changeMade ? new String (inputBuffer) : input;
    }
    

    public String makeXMLName (final String input) {

    	final String legalXML = stripIllegalXML (input);
		final String underScored = legalXML.replace (' ', '_'); // Using names as XML IDs, and XML does not allow spaces in IDs
		final StringBuilder inputBuffer = new StringBuilder (underScored);
		boolean changeMade = false;

		for (int n = inputBuffer.length(); --n >= 0;) {
			final char current = inputBuffer.charAt (n);
			if (!Character.isLetterOrDigit (current) && !NAME_CHARS.get (current)) {
				//inputBuffer.setCharAt (n, '_');
				changeMade = true;
				inputBuffer.deleteCharAt (n);
			}
		}

		return changeMade ? new String (inputBuffer) : underScored;
	}
    


	public String makeXMLNameRegex (final String input) {

        //Matcher matcher = replaceSpacePattern.matcher (input);
        MATCHER.reset (input);
		//Matcher matcher2 = deleteNonXMLCharsPattern.matcher (matcher.replaceAll("_"));
		NON_XML_MATCHER.reset (MATCHER.replaceAll("_"));
		return NON_XML_MATCHER.replaceAll ("");
	}
	
	
	public String encodeRFC1738 (final String unsafe) {
		final StringBuilder strBuild = new StringBuilder ();
		for (int n = 0; n < unsafe.length(); n++) {
			final char khar = unsafe.charAt (n);
			if (RFC1738_SAFE_CHARS.get (khar)) {
				strBuild.append (khar);
			}
			else {
				strBuild.append('%').append(Character.forDigit (khar >> 4, 16)).append(Character.forDigit (khar & 15, 16));
			}
		}
		//System.out.println (unsafe+"\n"+sb.toString());
		return strBuild.toString ();
	}
	
	
	

	
    public String byteArrayToBase64 (final byte[] byteArray, final boolean alternate) {
        final int aLen = byteArray.length;
        final int numFullGroups = aLen/3;
        final int numBytesInPartialGroup = aLen - 3*numFullGroups;
        final int resultLen = 4*((aLen + 2)/3);
        final StringBuffer result = new StringBuffer(resultLen);
        final char[] intToAlpha = (alternate ? INTTOALTBASE64 : INTTOBASE64);

        // Translate all full groups from byte array elements to Base64
        int inCursor = 0;
        for (int i=0; i<numFullGroups; i++) {
            final int byte0 = byteArray[inCursor++] & 0xff;
            final int byte1 = byteArray[inCursor++] & 0xff;
            final int byte2 = byteArray[inCursor++] & 0xff;
            result.append(intToAlpha[byte0 >> 2]);
            result.append(intToAlpha[(byte0 << 4)&0x3f | (byte1 >> 4)]);
            result.append(intToAlpha[(byte1 << 2)&0x3f | (byte2 >> 6)]);
            result.append(intToAlpha[byte2 & 0x3f]);
        }

        // Translate partial group if present
        if (numBytesInPartialGroup != 0) {
            final int byte0 = byteArray[inCursor++] & 0xff;
            result.append(intToAlpha[byte0 >> 2]);
            if (numBytesInPartialGroup == 1) {
                result.append(intToAlpha[(byte0 << 4) & 0x3f]);
                result.append("==");
            } else {
                // assert numBytesInPartialGroup == 2;
                final int byte1 = byteArray[inCursor++] & 0xff;
                result.append(intToAlpha[(byte0 << 4)&0x3f | (byte1 >> 4)]);
                result.append(intToAlpha[(byte1 << 2)&0x3f]);
                result.append('=');
            }
        }
        // assert inCursor == a.length;
        // assert result.length() == resultLen;
        return result.toString();
    }

    
    
	public Document makeDOMFromString (final String xmlString) {
		Document doc = null;
		
		try {
	        final DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
	        final DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
	        final InputSource iSource = new InputSource (new StringReader (xmlString));
	        doc = docBuilder.parse (iSource);
		} catch (ParserConfigurationException pce) {
			LOGGER.error ("Parser Error for xmlString", pce);
		} catch (SAXException saxe) {
			LOGGER.error ("SAX Exception", saxe);
		} catch (IOException ioe) {
			LOGGER.error ("IO Exception for xmlString", ioe);
		}
		
		return doc;
	}
	
	
	public String makeStringFromDOM (final Document document) {

		final StreamResult result = new StreamResult (new StringWriter());
		final DOMSource source = new DOMSource (document);
		try {
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty (OutputKeys.INDENT, "yes");
			transformer.transform (source, result);
		} catch (TransformerException transe) {
			LOGGER.error ("Transformer Exception", transe);
		}

		return result.getWriter().toString();
	}
	
	/**
	 * Create a DocumentFragment object from the contents of the Node node
	 * @param node
	 * @return a DocumentFragment object holding copied of node's children
	 */
	public DocumentFragment createDocumentFragment (final Node node) {
		final DocumentFragment docFragment = node.getOwnerDocument().createDocumentFragment();
		final NodeList nodeChildren = node.getChildNodes();
		
		// Make sure nodes are copied as clones before adding to the document fragment,
		// or the original node gets removed from the child NodeList as we're
		// looping through it.
		// Results in an error where only even-numbered nodes are added to the fragment.
		for (int childIndex = 0; childIndex < nodeChildren.getLength(); childIndex++) {
			final Node child = nodeChildren.item (childIndex);
			docFragment.appendChild (child.cloneNode (true));
		}
		
		return docFragment;
	}
	
	
	public void debugNode (final Node node, final int level) {
		final NodeList children = node.getChildNodes();
		LOGGER.debug (TABS.substring(0, level)+node.getNodeName()+" ["+children.getLength()+" children]");
		for (int i = 0; i < children.getLength(); i++) {
			debugNode (children.item (i), level + 1);
		}
	}
}