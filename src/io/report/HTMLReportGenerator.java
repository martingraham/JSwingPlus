package io.report;

import io.DataPrep;
import io.TableModelWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JComponent;

import org.apache.log4j.Logger;

import util.Messages;
import util.XMLConstants2;
import util.screenshot.ScreenshotComponent;

public class HTMLReportGenerator extends AbstractReportGenerator {

	static final Logger LOGGER = Logger.getLogger (HTMLReportGenerator.class);
	public final static String DEFAULT_TEMPLATE_STRING = Messages.getString ("io.report.defaultReport", "Template URL");
	public final static URL DEFAULT_TEMPLATE = HTMLReportGenerator.class.getResource (DEFAULT_TEMPLATE_STRING);

	URL[] screenshots;
	URL templateURL;
	File htmlFile;
	

	String generatedHTML = null;
	
	
	public HTMLReportGenerator () {
		super ();
		setTableWriter (TableModelWriter.getTableModelWriter ("HTML"));
	}
	
	
	public final static void main (final String[] args) {
		final HTMLReportGenerator rGen = new HTMLReportGenerator ();
		rGen.setTemplate (null);
		rGen.populateTemplate ();
	}
	
	
	public void setTemplate (final String urlString) {
		templateURL = null;
		
		if (urlString != null) {
			if (urlString.startsWith ("http://")) {
				try {
					templateURL = new URL (urlString);
				} catch (MalformedURLException mue) {
					LOGGER.error ("Cannot parse URL: "+urlString, mue);
				}
			}
			else {
				templateURL = HTMLReportGenerator.class.getResource (urlString);
			}
		}
		
		if (templateURL == null) {
			templateURL = DEFAULT_TEMPLATE;
		}
	}
	
	
	
	public void populateTemplate () {
		if (templateURL != null && tableWriter.isDataSet()) {
			final InputStream iStream = DataPrep.getInstance().getInputStream (templateURL);
			final String template = DataPrep.getInstance().inputStreamToString (iStream);
			
			final Date currentDate = new Date ();
			final Format pattern = new MessageFormat (template);
			final Format formats[] = ((MessageFormat)pattern).getFormats();
			Object[] holders = new Object [formats.length];
			
			final File folder = new File (properties.getProperty ("Report No.") + properties.getProperty ("Customer Name"));
			folder.mkdir ();
			
			holders[0] = properties.get ("Report Title");
			holders[1] = currentDate;
			holders[2] = properties.get ("Customer Name");
			holders[3] = properties.get ("Report No.");
			//holders[4] = "http://www.dcs.napier.ac.uk/~marting/ivdemos/EdNapLogoSmall.png";
			
			final ScreenshotComponent screenshotter = new ScreenshotComponent ();
			final SimpleDateFormat sdf = new SimpleDateFormat ("yyMMdd_HHmmss");
			final String dateTime = sdf.format (currentDate);
			final StringBuilder sBuilder = new StringBuilder ();
			for (int compIndex = 0; compIndex < componentsToAppend.size(); compIndex++) {
				final JComponent comp = componentsToAppend.get (compIndex);
				if (comp.getName() == null) {
					comp.setName ("View "+compIndex);
				}
				final File imageFile = screenshotter.captureAndOutputComponent (comp, dateTime, folder);
				sBuilder.append("<h4>").append(comp.getName()).append("</h4><br/><img src=\"").append(imageFile.getName()).append("\"></br>");
			}
			holders[4] = sBuilder.toString();
			
			final StringWriter sWriter = new StringWriter ();
			final PrintWriter pWriter = new PrintWriter (sWriter);
			tableWriter.writeEmbedded (pWriter);
			try {
				sWriter.close();
			} catch (IOException ioe) {
				LOGGER.error ("String Writer closing exception", ioe);
			}
			holders[5] = sWriter.toString();
			
			generatedHTML = pattern.format (holders);
			
			
			try {
				htmlFile = new File (folder, folder.getName() + ".html");
				final PrintWriter pWriter2 = DataPrep.getInstance().makeBufferedPrintWriter (
						htmlFile, XMLConstants2.UTF8, false);
				pWriter2.println (generatedHTML);
				pWriter2.flush ();
				pWriter2.close ();
			} catch (IOException ioe) {
				LOGGER.error ("Write error.", ioe);
			}
		}
	}
	
	public File getHTMLFile () { return htmlFile; }
}
