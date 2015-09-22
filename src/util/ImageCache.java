package util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;



public class ImageCache {

	private final static Logger LOGGER = Logger.getLogger (ImageCache.class);
	
	private static Map <Object, Image> iCache = new HashMap <Object, Image> ();
	
	public static Image makeImageFromFilename (final String fileName) {
		final String fileNamePlusDir = GraphicsUtil.IMAGEDIR + "/" + fileName;
    	LOGGER.debug ("icon filename: "+fileNamePlusDir);
    	return makeImageAbs (fileNamePlusDir, fileNamePlusDir);
	}

	public static Image makeImage (final String descriptor) {	
		final String propertyString = Messages.getString (GraphicsUtil.GRAPHICPROPS, descriptor);
		final String fileName = GraphicsUtil.IMAGEDIR + "/" + propertyString;
		return makeImageAbs (fileName, propertyString);
	}
	
	public static Image makeImage (final Class<?> klass, final String descriptor) {
		final String propertyString = Messages.getString (GraphicsUtil.GRAPHICPROPS, klass, descriptor);
		final String fileName = GraphicsUtil.IMAGEDIR + "/" + propertyString;
		return makeImageAbs (fileName, propertyString);
	}
	
	
	public static Image makeImageAbs (final String absFileName, final String key) {
		Image img = iCache.get (absFileName);
		if (img == null) {
			if (key != null) {
				final BufferedImage bim = GraphicsUtil.loadBufferedImage (absFileName);
				img = bim;
			}

			if (img != null) {
				iCache.put (absFileName, img);
			}
		}
		
		return img;   
	}
	
	
	public static Image makeImageAbs (final URL url) {
		Image img = iCache.get (url);
		if (img == null) {
			try {
				final BufferedImage bim = ImageIO.read (url);
				img = bim;
			} catch (IOException ioe) {
				LOGGER.error ("IOException", ioe);
			}

			if (img != null) {
				iCache.put (url, img);
			}
		}
		
		return img;   
	}

	public static void clearCache () { iCache.clear(); }
}
