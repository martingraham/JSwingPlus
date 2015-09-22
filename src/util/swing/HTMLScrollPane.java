package util.swing;

import io.DataPrep;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.html.HTMLDocument;

import org.apache.log4j.Logger;



public class HTMLScrollPane extends JScrollPane implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4505753360758413407L;

	private final static Logger LOGGER = Logger.getLogger (HTMLScrollPane.class);
	
	protected JEditorPane htmlEditorPane;
	protected PropertyChangeListener pcl;
	protected String unformattedHTML;
	protected String[] arguments;
	
	protected boolean isLoading;
	
	public HTMLScrollPane () {
		super ();
		
		htmlEditorPane = new JEditorPane ();
		htmlEditorPane.setEditable (false);
		htmlEditorPane.setContentType ("text/html");
		setViewportView (htmlEditorPane);
		
	    pcl = new PropertyChangeListener () {
	        public void propertyChange (final PropertyChangeEvent pce) {
	        	final String name = pce.getPropertyName();
	    	    LOGGER.debug ("property: "+name+ " : " + pce.getNewValue());
	    	   
	        	if ("page".equals (name)) {   		
	        	    htmlEditorPane.setCaretPosition (0);
	        	    
	        	    if (unformattedHTML == null) {
	        	    	unformattedHTML = htmlEditorPane.getText();
	        	    	isLoading = false;
	        	    	
		        	    if (arguments != null) {
		        	    	LOGGER.debug ("Safe format after page load");
		        	    	safeFormat ();
		        	    }
	        	    }
	        	}
	        }
		};
	    htmlEditorPane.addPropertyChangeListener (pcl);
		
		setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		isLoading = false;
	}


	
	public void setHTMLURL (final URL htmlURL) {
	    if (htmlURL != null) {
	    	try {
	    		isLoading = true;
	    		unformattedHTML = null;
				htmlEditorPane.setPage (htmlURL);

			} catch (final IOException ioe) {
				LOGGER.error (ioe.toString(), ioe);
			}
	    }
	    
	    htmlEditorPane.setCaretPosition (0);
	}
	
	
	public void setHTMLURL (final URL baseURL, final String relativeFilename) {
		if (relativeFilename != null) {
			try {
				final URL htmlURL = new URL (baseURL, relativeFilename);
				LOGGER.debug ("html url: "+htmlURL);
				setHTMLURL (htmlURL);
				final HTMLDocument htmlDoc = (HTMLDocument)htmlEditorPane.getDocument();	// So now we can do this
				htmlDoc.setBase (baseURL);
				LOGGER.debug ("baseURL for document now "+htmlDoc.getBase());
			} catch (final MalformedURLException mue) {
				LOGGER.error ("Error", mue);
			}
		}
	}
	
	
	public void setHTMLURL (final String relDirectory, final String relativeFilename) {
		final URL baseURL = DataPrep.getInstance().getBaseURL (relDirectory);
		
	    if (relativeFilename != null) {
	    	setHTMLURL (baseURL, relativeFilename);
	    }
	}
	
	
	public void format (final String[] newArguments) {
		arguments = newArguments;
		LOGGER.debug ("format isLoading: "+isLoading);
		if (!isLoading) {
			safeFormat ();
		}
	}
	
	
	void safeFormat () {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("unformatted doc:\n"+unformattedHTML);
		}
		final MessageFormat htmlFormat = new MessageFormat (unformattedHTML);
		final String formattedHTML = htmlFormat.format (arguments);
		htmlEditorPane.setText (formattedHTML);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("formatted doc:\n"+formattedHTML);
		}
	}
	
	
	public JEditorPane getEditorPane () { return htmlEditorPane; }



	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		// EMPTY
	}
}
