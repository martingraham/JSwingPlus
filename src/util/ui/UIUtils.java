package util.ui;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

public class UIUtils {

	static final Logger LOGGER = Logger.getLogger (UIUtils.class);
	
	
	public static void main (final String[] args) {
		showUIManagerDefaults();
	}
	
    public static void showUIManagerDefaults() {

        final UIDefaults defaults = UIManager.getDefaults();
        LOGGER.info ("Count Item = " + defaults.size());
        final java.util.List<String> properties = new ArrayList<String> ();
        final Set<Map.Entry<Object, Object>> set = defaults.entrySet();
        final Iterator<Map.Entry<Object, Object>> iter = set.iterator();
        while (iter.hasNext()) {
        	final Map.Entry<Object, Object> entry = iter.next();
        	properties.add (entry.getKey().toString());	
        }
        Collections.sort (properties);
        for (final Object o : properties) {
        	LOGGER.info (o.toString() + "\t" + defaults.get(o));
        }
    }
    
    
    public static KeyStroke[] sortKeyStrokesByInputActionName (final InputMap imap) {
    	
    	final class InputMapEntry implements Comparable<InputMapEntry> {
    		transient KeyStroke key;
    		transient Object actionObj;
    		
    		InputMapEntry (final KeyStroke key, final Object obj) {
    			this.key = key;
    			this.actionObj = obj;
    		}
    		
			@Override
			public int compareTo (final InputMapEntry otherIme) {
				return actionObj.toString().compareToIgnoreCase (otherIme.actionObj.toString());
			}
			
			@Override
			public String toString () { 
				return "key: "+key.toString()+" \tobj: "+actionObj.toString();
			}
    	}
    	
    	final List<InputMapEntry> kMapping = new ArrayList<InputMapEntry> ();
    	for (KeyStroke key : imap.allKeys()) {
    		kMapping.add (new InputMapEntry (key, imap.get(key)));
    	}
    	Collections.sort (kMapping);
    	
    	final KeyStroke[] sortedByActionName = new KeyStroke [kMapping.size()];
    	for (int entry = 0; entry < kMapping.size(); entry++) {
    		sortedByActionName [entry] = kMapping.get(entry).key;
    	}
    	return sortedByActionName;
    }
    
    
    
    public static void printActionMap (final ActionMap actionMap) {
    	LOGGER.debug ("-------------------------");
    	LOGGER.debug ("Action Map: "+actionMap);
    	if (actionMap != null && actionMap.allKeys() != null) {
	    	for (Object key : actionMap.allKeys()) {	
	    		LOGGER.debug ("["+key.toString()+" \t"+actionMap.get(key)+"]");
	    	}
    	}
    	LOGGER.debug ("-------------------------");
    }
    
    public boolean shouldIgnore (final MouseEvent mEvent, final JComponent comp) {
    	return (comp == null || !comp.isVisible() 
    			|| mEvent == null || mEvent.isConsumed() || !SwingUtilities.isLeftMouseButton (mEvent));
    }
}
