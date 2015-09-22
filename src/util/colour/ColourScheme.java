package util.colour;


import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

import util.Messages;


public class ColourScheme {

	private static final Logger LOGGER = Logger.getLogger (ColourScheme.class);
	
	ColourScaleObject cso;
	
	Color background1, background2, background3;
	Color unselected, unselectedBrushed;
	Color textSelected, textUnselected, textSelectedBrushed, textUnselectedBrushed;
	Color backgroundText;
	Color borderSelected, borderUnselected;
	
	public ColourScheme () {
		this (1);
	}
	
	public ColourScheme (final int index) {
		
		final String indexer = "colourScheme" + index + '.';
		
		try {
			final Field[] fields = this.getClass().getDeclaredFields();
			for (final Field field : fields) {
				final String fieldName = field.getName();
				final String str = Messages.getString ("colourScheme", indexer + fieldName);
				if (str != null && !str.isEmpty() && str.charAt(0) != '!') {
					final Color col = Color.decode (str);
					field.set (this, col);
				}
			}			
			
			try {
				final String str = Messages.getString("colourScheme", indexer + "specClass");
				if (str != null) {
					final Class<?> spectraClass = Class.forName (str);
					final Constructor<?> cons = spectraClass.getConstructor ((Class[])null);	
					final Object classInstance = cons.newInstance ();
					if (classInstance instanceof ColourScaleObject) {
						cso = (ColourScaleObject) classInstance;
					}
				}
			}
			catch (final ClassNotFoundException excep) {
				LOGGER.error ("Error", excep);
			} catch (final NoSuchMethodException excep) {
				LOGGER.error ("Error", excep);
			} catch (final InstantiationException excep) {
				LOGGER.error ("Error", excep);
			} catch (final InvocationTargetException excep) {
				LOGGER.error ("Error", excep);
			} 
		} catch (final IllegalArgumentException excep) {
			LOGGER.error ("Error", excep);
		} catch (final SecurityException excep) {
			LOGGER.error ("Error", excep);
		} catch (final IllegalAccessException excep) {
			LOGGER.error ("Error", excep);
		} 
		
		if (background2 == null) {
			background2 = ColorUtilities.darkenSlightly (background1, 0.06f);
		}
		if (background3 == null) {
			background3 = ColorUtilities.darkenSlightly (background2, 0.06f);
		}	
		if (backgroundText == null) {
			backgroundText = ColorUtilities.darkenSlightly (background3, 0.25f);
		}
	}
	
	public final ColourScaleObject getCso() {
		return cso;
	}
	public final Color getBackground1() {
		return background1;
	}
	public final Color getBackground2() {
		return background2;
	}
	public final Color getBackground3() {
		return background3;
	}
	public final Color getUnselected() {
		return unselected;
	}
	public final Color getUnselectedBrushed() {
		return unselectedBrushed;
	}
	public final Color getTextSelected() {
		return textSelected;
	}
	public final Color getTextUnselected() {
		return textUnselected;
	}
	public final Color getTextSelectedBrushed() {
		return textSelectedBrushed;
	}
	public final Color getTextUnselectedBrushed() {
		return textUnselectedBrushed;
	}
	public final Color getBackgroundText() {
		return backgroundText;
	}

	public final Color getBorderSelected() {
		return borderSelected;
	}

	public final Color getBorderUnselected() {
		return borderUnselected;
	}
	
	@Override
	public String toString () { return cso.toString(); }
	
}
