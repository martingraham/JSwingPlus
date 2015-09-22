package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class RegexReader extends BufferedReader {

	private final static Logger LOGGER = Logger.getLogger (RegexReader.class);
	static final Pattern DEFAULT_PATTERN = Pattern.compile ("\\s");
	
    Pattern pattern;
	
    
	public RegexReader (final InputStream iStream) {
		this (new InputStreamReader (iStream), DEFAULT_PATTERN);
	}
    
	public RegexReader (final InputStream iStream, final String regex) {
		this (new InputStreamReader (iStream), Pattern.compile (regex));
	}
	
	public RegexReader (final InputStream iStream, final Pattern regexPattern) {
		this (new InputStreamReader (iStream), regexPattern);
	}
	
	
	public RegexReader (final Reader reader) {
		this (reader, DEFAULT_PATTERN);
	}
	
	public RegexReader (final Reader reader, final Pattern regexPattern) {
		super (reader);
		setRegex (regexPattern);
	}
	
	
	public final void setRegex (final Pattern regexPattern) {
		pattern = regexPattern;
	}
	
	public Pattern getRegex () {
		return pattern;
	}

	
	
	public String[] readAndSplitLine () throws IOException {
		return readAndSplitLine (pattern);
	}
	
	public String[] readAndSplitLine (final Pattern pat) throws IOException {
		final String strLine = readLine ();
	    return (strLine == null ? null : pat.split (strLine, -1));
	}
}
