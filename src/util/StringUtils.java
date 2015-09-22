package util;

import java.awt.event.KeyEvent;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;


public final class StringUtils {

	private final static Logger LOGGER = Logger.getLogger (StringUtils.class);

    private final static Pattern INT_ISOLATE = Pattern.compile ("\\d+");
    private final static Pattern DATE_ISOLATE = Pattern.compile ("\\d{4}");
	private final static List<SimpleDateFormat> DATE_FORMATS = new ArrayList<SimpleDateFormat> ();
    
    
	static {
		final List<String> dateFormatKeys = Messages.getKeysWithPrefix ("dateFormats", "DateFormat");
		Collections.sort (dateFormatKeys);
		
		for (String dfKey : dateFormatKeys) {
			final String dateFormatString = Messages.getString ("dateFormats", dfKey);
			try {
				final SimpleDateFormat sdf = new SimpleDateFormat (dateFormatString);
				sdf.setLenient (true);
				DATE_FORMATS.add (sdf);
			} catch (IllegalArgumentException iae) {
				LOGGER.info ("Date format pattern "+dateFormatString+" inputStream invalid.");
			}
		}
	}
    
    
    
	private static final StringUtils INSTANCE = new StringUtils ();

	private StringUtils () {}

	public static StringUtils getInstance() { return INSTANCE; }
	

	
	
    public String makeAccString (final Action action) {
    	return (action == null ? null : makeAccString ((KeyStroke)action.getValue(Action.ACCELERATOR_KEY)));
    }
    
    public String makeAccString (final KeyStroke keyStroke) {
    	final StringBuilder strBuild = new StringBuilder (" [");
    	strBuild.append (KeyEvent.getKeyModifiersText(keyStroke.getModifiers()));
    	if (keyStroke.getModifiers() > 0) {
    		strBuild.append('+');
    	}
    	strBuild.append(KeyEvent.getKeyText(keyStroke.getKeyCode()));
    	strBuild.append(']');
    	return strBuild.toString ();
    }
    
	public String colorString (final int intValue) {
		return ("r: "+((intValue >> 16) & 0xff)+", g: "+((intValue >> 8) & 0xff)+", labelBorder: "+((intValue & 0xff)+", a: "+((intValue >> 24) & 0xff)));
	}
	
	
    public String returnRegexPattern (final String str) {

        boolean isRegex = false;

        final StringBuilder strBuild = new StringBuilder ();

        for (int n = 0; n < str.length(); n++) {

            final char chr = str.charAt (n);
            if (!Character.isLetterOrDigit (chr) && !Character.isSpaceChar (chr) && chr != '*' && chr != '?') {
               isRegex = true;
               break;
            }
        }

        if (!isRegex) {
           for (int n = 0; n < str.length(); n++) {

               final char chr = str.charAt (n);
               if (Character.isLetterOrDigit (chr)) {
                  strBuild.append (chr);
               }
               else if (chr == '*') {
                  strBuild.append (".*");
               }
               else if (chr == '?') {
                  strBuild.append (".?");
               }

           }
           return strBuild.toString ();
        }

        return str;
    }

    

    public DateFormat isStringDate (final String str) {
    	DateFormat matchingDateFormat = null;
    	
    	for (int n = 0; n < DATE_FORMATS.size() && matchingDateFormat == null; n++) {
    		final SimpleDateFormat sdf = DATE_FORMATS.get (n);
    		try {
				final Date date = sdf.parse (str);
				if (date != null) {
					matchingDateFormat = sdf;
				}
				LOGGER.debug (str+" --> Date: "+date+" via "+sdf.toLocalizedPattern());
			} catch (ParseException e) {
				LOGGER.debug (str+" doesn't match date format pattern "+sdf.toLocalizedPattern());
			}
    	}

    	 return matchingDateFormat;
    }
    

    public Calendar extractDateFromString (final String str) {
        final GregorianCalendar date = new GregorianCalendar ();
        final Matcher matcher = DATE_ISOLATE.matcher (str);

		if (matcher.find()) {
			final int year = Integer.parseInt (matcher.group());
			date.set (Calendar.YEAR, year);
		}

        return date;
    }



    public int extractNumberFromString (final String str) {

    	boolean found = false;
    	Matcher matcher = null;

    	if (str != null) {
        	matcher = INT_ISOLATE.matcher (str);
        	found = matcher.find ();
        }
		final int number = (found ? Integer.parseInt (matcher.group()) : 1);
		LOGGER.debug ("s: "+str+", number: "+number);
        return number;
    }
}
