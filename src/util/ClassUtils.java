package util;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;


public final class ClassUtils {

	private final static Logger LOGGER = Logger.getLogger (ClassUtils.class);
 
	
	private final static ClassUtils INSTANCE = new ClassUtils ();

	private ClassUtils () {}

	public static ClassUtils getInstance() { return INSTANCE; }
	
	
	
    public Class<?> getCommonSuperclass (final Set<Class<?>> classSet) {
    	
    	Class<?> superClass = null;
    	
    	if (!classSet.isEmpty()) {

	    	final Iterator<Class<?>> classIterator = classSet.iterator();
	    	while (classIterator.hasNext()) {
	    		final Class<?> nextClass = classIterator.next ();
	    		if (superClass == null) {
	    			superClass = nextClass;
	    		}
	    		else if (nextClass.isAssignableFrom (superClass)) {
	    			superClass = nextClass;
	    		}
	    		else if (superClass.isAssignableFrom (nextClass)) {
	    			// do nothing
	    		}
	    		else {
	    			final Set<Class<?>> parentClassSet = new HashSet<Class<?>> ();
	    			Class<?> parentClass = superClass;
	    			while (parentClass != Object.class && parentClass != null) {
	    				parentClass = parentClass.getSuperclass();
	    				parentClassSet.add (parentClass);
	    			}
	    			
	    			Class<?> nextParentClass = nextClass;
	    			boolean match = false;
	    			while (!match) {
	    				nextParentClass = nextParentClass.getSuperclass ();
	    				match = parentClassSet.contains (nextParentClass);
	    			}
	    			
	    			superClass = nextParentClass;
	    		}
	    	}
    	}
    	
    	if (superClass == null) {
    		superClass = Object.class;
    	}
    	
    	LOGGER.debug ("For classes: "+classSet.toString()+", "+superClass.getName()+" is the common superclass.");
    	return superClass;
    }
}
