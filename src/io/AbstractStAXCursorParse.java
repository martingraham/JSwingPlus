package io;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

import util.Messages;
import util.swing.InitialisationProgressBar;



public abstract class AbstractStAXCursorParse {


	private final static Logger LOGGER = Logger.getLogger (AbstractStAXCursorParse.class);
	
    protected Stack< Map<String, String>> attributeStack;
    private final Stack<String> elementTypeStack;
    protected InitialisationProgressBar ipb;
    protected Object populateMe;
    protected String text;
    protected int count;



    public AbstractStAXCursorParse () {
        super ();
        attributeStack = new Stack< Map<String, String>> ();
        elementTypeStack = new Stack<String> ();
    	ipb = new InitialisationProgressBar (Messages.getString ("StaxParseProgressBarHeader"));
    }

    public AbstractStAXCursorParse (final Object populateMe) {
        this ();
        this.populateMe = populateMe;
    }



    public void parse (final InputStream inStream) throws Exception {

        count = 0;
		final int eventType = 0;
        XMLInputFactory xmlif = null ;

        try {
            xmlif = XMLInputFactory.newInstance();
            xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,Boolean.TRUE);
            xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,Boolean.FALSE);
            //set the IS_COALESCING property to true , if application desires to
            //get whole text data as one event.
            xmlif.setProperty(XMLInputFactory.IS_COALESCING , Boolean.TRUE);
        } catch (final Exception ex) {
            LOGGER.error (ex.toString(), ex);
        }

        LOGGER.debug ("FACTORY: " + xmlif + "\n");
        ipb.setHeaderLabel (inStream.toString());
        
        final long starttime = System.currentTimeMillis() ;
        if (xmlif != null) {
	        try{
	            //pass the file name.. all relative entity refernces will be resolved against this as
	            //base URI.
	            //XMLStreamReader xmlr = xmlif.createXMLStreamReader(filename, new FileInputStream(filename));
				//final XMLStreamReader xmlr = xmlif.createXMLStreamReader (inputStream, XMLConstants2.ENCODING);
	            final XMLStreamReader xmlr = xmlif.createXMLStreamReader (inStream);
				//when XMLStreamReader inputStream created, it inputStream positioned at START_DOCUMENT event.
	            //printEventType(eventType);
	            //printStartDocument(xmlr);
	            //check if there are more =events in the input stream
	            for (int event = xmlr.next(); event != XMLStreamConstants.END_DOCUMENT; event = xmlr.next(), count++) {
	
	                //eventType = xmlr.next ();
	                //printEventType(eventType);
	
	                //these functions prints the information about the particular event by calling relevant function
	
	                processStartElement (xmlr);
	                processEndElement (xmlr);
	                processText (xmlr);
	            }
	
	            xmlr.close();
	            inStream.close();
	
	        } catch (final XMLStreamException ex){
	        	LOGGER.debug (ex.getMessage()+"\n"+"Text: ["+text+"]\neventType2: "+eventType);
	            if (ex.getNestedException() != null) {
	            	LOGGER.error (ex.getNestedException().toString(), ex.getNestedException());
				}
	            ipb.kill ();
	        } catch (final Exception ex){
	        	LOGGER.error (ex.toString(), ex);
	            ipb.kill ();
	        }
        }
        
        final long endtime = System.currentTimeMillis();
        LOGGER.info ("Parsing Time = " + (endtime - starttime) );

        ipb.kill (); // Kill off initialisation progress bar
    }


    protected void processStartElement (final XMLStreamReader xmlr) {
    	if (xmlr.isStartElement()) {
            final String qName = xmlr.getLocalName();
            elementTypeStack.push (qName);
            
            Map<String, String> attrStore = null;
            final int attCount = xmlr.getAttributeCount() ;

            if (attCount > 0){
                attrStore = new HashMap<String, String> (8);

                for(int i = 0 ; i < attCount ; i++) {
                    attrStore.put (xmlr.getAttributeLocalName(i), xmlr.getAttributeValue(i));
                }
            }
            LOGGER.debug ("<" + qName+"> att: "+count+", attrStore: "+attrStore);
            attributeStack.push (attrStore);
    		
    		specificProcessStartElement (qName, attrStore);
    	}
    }

    protected void processEndElement (final XMLStreamReader xmlr) {
    	 if (xmlr.isEndElement()) {
             final Map<String, String> attrStore = attributeStack.pop();
             final String tagName = elementTypeStack.pop ();
             specificProcessEndElement (tagName, attrStore);
    	 }
    }

    abstract protected void specificProcessStartElement (final String tagName, final Map<String, String> attrStore);
    
    abstract protected void specificProcessEndElement (final String tagName, final Map<String, String> attrStore);
    
    protected void processText (final XMLStreamReader xmlr){
        if (xmlr.hasText()){
            text = xmlr.getText();
        }
    }
}