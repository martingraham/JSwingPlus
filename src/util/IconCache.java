package util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;



public class IconCache {

	private final static Logger LOGGER = Logger.getLogger (IconCache.class);
	protected static Map <String, Icon> iCache = new HashMap <String, Icon> ();
	
	public static Icon makeIconFromFilename (final String fileName) {
    	return makeIconFromFilename (GraphicsUtil.IMAGEDIR, fileName);
	}

	public static Icon makeIcon (final String descriptor) {	
		return makeIcon (GraphicsUtil.IMAGEDIR, descriptor);
	}
	
	public static Icon makeIcon (final Class<?> klass, final String descriptor) {
		return makeIcon (GraphicsUtil.IMAGEDIR, klass, descriptor);
	}
	
	public static Icon makeIconFromFilename (final String directory, final String fileName) {
		final String fileNamePlusDir = directory + "/" + fileName;
    	LOGGER.debug ("icon filename: "+fileNamePlusDir);
    	return makeIconAbs (fileNamePlusDir, fileNamePlusDir);
	}

	public static Icon makeIcon (final String directory, final String descriptor) {	
		final String propertyString = Messages.getString (GraphicsUtil.GRAPHICPROPS, descriptor);
		final String fileName = directory + "/" + propertyString;
		return makeIconAbs (fileName, propertyString);
	}
	
	public static Icon makeIcon (final String directory, final Class<?> klass, final String descriptor) {
		final String propertyString = Messages.getString (GraphicsUtil.GRAPHICPROPS, klass, descriptor);
		final String fileName = directory + "/" + propertyString;
		return makeIconAbs (fileName, propertyString);
	}
	
	public static Icon colourIconBackground (final ImageIcon imgIcon, final Color col) {
		final BufferedImage bim = new BufferedImage (imgIcon.getIconWidth(), imgIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		final Graphics graphics = bim.createGraphics();
		graphics.setColor (col);
		graphics.fillRect(0, 0, bim.getWidth(), bim.getHeight());
		graphics.drawImage (imgIcon.getImage(), 0, 0, null);
		return new ImageIcon (bim);
	}
	
	public static Icon makeIconAbs (final String absFileName, final String key) {
		Icon icon = iCache.get (absFileName);
		if (icon == null) {
			if (key != null) {
				final BufferedImage bim = GraphicsUtil.loadBufferedImage (absFileName);
				if (bim != null) {
					icon = new ImageIcon (bim);
					if (icon != null) {
						LOGGER.debug (absFileName+"\t cm: "+bim.getColorModel());
					}
				}
			}

			if (icon != null) {
				iCache.put (absFileName, icon);
			}
		}
		
		return icon;   
	}
	

	public static void clearCache () { iCache.clear(); }
}
