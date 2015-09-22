package util.colour;

/**
* Wrapper class that adds description tags to Levkowitz's linear colour scales
* and ennables them to be treated as instances of the same class.<P>
* @author Martin Graham
* @version 1.0
*/

public class ColourScaleObject {

	/**
	* Number of entries in each colour scale.<P>
	*/
	private static int size = 256;
	protected ColorRGB[] rgb;
	/**
	* Descriptor string for a colour scale.<P>
	*/
	protected String scaleDescriptor;


	/**
	* Constructor.
	*/
	public ColourScaleObject () { rgb = new ColorRGB [size]; }

	/**
	* Gets ColorRGB instance for a given index in a colour scale.<P>
	* @param index index
	* @return RGB values at the specified index
	*/
	public ColorRGB getRGB (final int index) { return rgb [index]; }

	public static int getSize () { return size; }
	
	@Override
	public String toString () { return scaleDescriptor; }
}