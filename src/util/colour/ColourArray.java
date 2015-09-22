package util.colour;


import java.awt.Color;

public final class ColourArray {

       Color colArray [];

       public ColourArray (final ColourScaleObject colScale) {
            this (colScale, 1.0f);
       }
       
       
       public ColourArray (final ColourScaleObject colScale, final float alpha) {

           colArray = new Color [colScale.rgb.length];
           ColorRGB tempColour;

		      for (int index = 0; index < colArray.length; index++) {
			      tempColour = colScale.getRGB (index);
			      colArray [index] = new Color (tempColour.getR(), tempColour.getG(), tempColour.getB(), alpha);
		       }
       }
       
       public void grey (final float greyAmount) {
		      for (int index = 0; index < colArray.length; index++) {
			      colArray [index] = ColorUtilities.mixColours (Color.gray, colArray[index], greyAmount);
		       }
       }

       public Color getColour (final int index) { return colArray [index]; }
       
       public int getLength () { return colArray.length; }
}


