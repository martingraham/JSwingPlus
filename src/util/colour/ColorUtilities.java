package util.colour;

import java.awt.Color;

import org.apache.log4j.Logger;

public class ColorUtilities {

  private final static String BLANKHEXSTRING = "#000000";
	
  final static Logger LOGGER = Logger.getLogger (ColorUtilities.class);
  
  
  public static Color decodeWithAlpha (final String colourString) throws NumberFormatException {

	int index = 0;
	if (colourString.startsWith("0x", index) || colourString.startsWith("0X", index)) {
	    index += 2;
	}
	else if (colourString.startsWith("#", index)) {
	    index ++;
	}
	else if (colourString.startsWith("0", index) && colourString.length() > 1 + index) {
	    index ++;
	}
	int colourStringLength = colourString.length() - index;
	
	Color colour = null;
	if ((index == 1 && colourStringLength > 9) || (index == 2 && colourStringLength > 6)) {
		final long longVal = Long.decode(colourString).longValue();
		colour = new Color ((int)((longVal >> 24) & 0xFF), (int)((longVal >> 16) & 0xFF), (int)((longVal >> 8) & 0xFF), (int)(longVal & 0xFF));
	}
	else {
		colour = Color.decode (colourString);
	}
	return colour;
 }

  
 public static String toHTMLHexString (final Color colour) {
        final int col = (colour.getRed() << 16) + (colour.getGreen() << 8) + colour.getBlue();
        final String hex = Integer.toHexString (col);
	    return BLANKHEXSTRING.substring (0, BLANKHEXSTRING.length() - hex.length()) + hex;
 }

 
 public static Color repeatedlyOverlayAlphaColour (final Color col1, final Color background, final int repetitions) {
	 
	 if (repetitions == 0) {
		 return background;
	 }
	 else if (col1.getAlpha() == 255) {
		 return col1;
	 }
	 
	 final float alpha = (float)col1.getAlpha() / 255; 
	 final float invAlpha = 1.0f - alpha;
	 // Overlaying a transparent colour on a background multiple times
	 // involves calculating a colour based on the sum of a geometrical series
	 final float nPower = (float) Math.pow (invAlpha, repetitions);
	 final float sumOfGeometricSeries = Math.abs (nPower - 1.0f) < 0.00001f ?
			 0.0f : ((nPower - 1.0f) / (invAlpha - 1.0f)); 
	 if (alpha < 0.00001f && LOGGER.isDebugEnabled()) {
		 LOGGER.debug ("reps: "+repetitions+", nth power: "+nPower+", sum of g series: "+sumOfGeometricSeries);
	 }
	 
	 float[] col1RGB = col1.getRGBColorComponents (null);
	 final float[] backRGB = background.getRGBColorComponents (null);
 
	 for (int n = col1RGB.length; --n >= 0;) {
		 final float source = col1RGB[n];
		 final float dest = backRGB[n];
		 final float colour = (source * alpha * sumOfGeometricSeries) + (dest * nPower);
		 col1RGB[n] = colour;
	 }

	 return new Color (Math.min (1.0f, col1RGB[0]), Math.min (1.0f, col1RGB[1]), Math.min (1.0f, col1RGB[2]));
}
 
 
 public static Color mixColoursAndAlpha (final Color col1, final Color col2, final float col1weight) {
		final int val = mixColoursInt (col1, col2, col1weight);
		final int newa = Math.round((col2.getAlpha() * (1.0f - col1weight)) + (col1.getAlpha() * col1weight));
		return new Color ((val >> 16) & 0xff, (val >> 8) & 0xff, val & 0xff, newa);
 }

  public static Color mixColours (final Color col1, final Color col2, final float col1weight) {
	  	final int val = mixColoursInt (col1, col2, col1weight);
		return new Color ((val >> 16) & 0xff, (val >> 8) & 0xff, val & 0xff);
  }
  
  public static int mixColoursInt (final Color col1, final Color col2, final float col1weight) {
	  final int newr = Math.round((col2.getRed() * (1.0f - col1weight)) + (col1.getRed() * col1weight));
	  final int newg = Math.round((col2.getGreen() * (1.0f - col1weight)) + (col1.getGreen() * col1weight));
	  final int newb = Math.round((col2.getBlue() * (1.0f - col1weight)) + (col1.getBlue() * col1weight));
	  return 0xff000000 | (newr << 16) | (newg << 8) | (newb << 0);
  }



  public static Color darkenSlightly (final Color col, final float brightnessReduction) {

         float[] colrgb = new float [3];
         colrgb = Color.RGBtoHSB (col.getRed(), col.getGreen(), col.getBlue(), colrgb);
         colrgb [2] = Math.max (0.0f, colrgb[2] - brightnessReduction);

         return addAlpha (Color.getHSBColor (colrgb[0], colrgb[1], colrgb[2]), col.getAlpha());
  }

  
  public static Color desaturate (final Color col) {

      float[] colrgb = new float [3];
      colrgb = Color.RGBtoHSB (col.getRed(), col.getGreen(), col.getBlue(), colrgb);
      colrgb [1] /= 2.0f;

      return Color.getHSBColor (colrgb[0], colrgb[1], colrgb[2]);
}

  public static Color addAlpha (final Color colour, final int alpha) {
  		return new Color ((colour.getRGB() & 0xffffff) | (alpha << 24), true);
  }
}