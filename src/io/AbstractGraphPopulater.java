package io;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import model.graph.GraphModel;


public abstract class AbstractGraphPopulater {

	final static Logger LOGGER = Logger.getLogger (AbstractGraphPopulater.class);

	protected InputStream inputStream;
	protected RegexReader regexReader;

	public AbstractGraphPopulater (final InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public void populate (final GraphModel graph, final Pattern splitter) {
		// Allow regexReader to be set with setReader() beforehand (i.e. if multi-section parsing is needed)
		if (regexReader == null) {
			regexReader = new RegexReader (inputStream, splitter);
		} else {
			regexReader.setRegex (splitter);
		}
		
		String[] parts;
        //Read File Line By Line and split
        try {
			while ((parts = regexReader.readAndSplitLine()) != null && (!haltParse (parts))) {
				doStuff (graph, parts);
			}
		} catch (IOException ioe) {
			LOGGER.error ("Error", ioe);
		}
	}
	
	public void setReader (final RegexReader newReader) {
		regexReader = newReader;
	}
	
	abstract protected void doStuff (final GraphModel graph, String[] parts);
	
	protected boolean haltParse (final String[] parts) {
		return false;
	}
}
