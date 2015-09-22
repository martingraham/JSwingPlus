package example;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.log4j.Logger;

public class ExampleLaunchData {

	private final static Logger LOGGER = Logger.getLogger (ExampleLaunchData.class);

	String mainClassName;
	String dataFileName;
	String description;
	String longDescription;
	
	public ExampleLaunchData (final String mainClass, final String dataFile, 
			final String desc, final String longDesc) {
		
		mainClassName = mainClass;
		dataFileName = dataFile;
		description = desc;
		longDescription = longDesc;
	}
	

	
	public final String getMainClassName() {
		return mainClassName;
	}

	public final String getDataFileName() {
		return dataFileName;
	}
	
	public final String getDescription() {
		return description;
	}
	
	public final String getLongDescription() {
		return longDescription;
	}
	
	public void launch () {
		try {
			final Class<?> launchClass = Class.forName (getMainClassName ());
			final Method mainMethod = launchClass.getMethod ("main", String[].class);
			final Object[] args = dataFileName.split (" ");
			LOGGER.debug ("Main Method Type Parameters: "+Arrays.toString (mainMethod.getTypeParameters()));
			mainMethod.invoke (null, (Object)args);
		} catch (Exception error) {
			LOGGER.error ("Can't find main method in "+getMainClassName(), error);
		}
	}
}
