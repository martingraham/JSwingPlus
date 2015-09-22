package example.parcoord;

import io.LaunchFileDialog;

import javax.swing.JFrame;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import util.Messages;
import util.ui.NewMetalTheme;



public class JParCoordRangeSliderTest2 extends JParCoordRangeSliderTest {

	transient JFrame frame;
	static final private Logger LOGGER = Logger.getLogger (JParCoordRangeSliderTest2.class);
	
	int filterIndex = 0;
	
	public static void main (final String[] args) {
		
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		LOGGER.info ("Logger activated");

		new LaunchFileDialog (JParCoordRangeSliderTest.class);
		//new JParCoordRangeSliderTest2 (args.length > 0 ? args[0] : Messages.getString ("ExampleDataFile"));
	}
	
	public JParCoordRangeSliderTest2 (final String dataFileName) {
		super (dataFileName);
	}
}