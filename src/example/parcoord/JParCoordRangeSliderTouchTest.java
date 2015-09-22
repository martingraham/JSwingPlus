package example.parcoord;

import java.util.List;

import swingPlus.parcoord.JParCoord;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ui.ParCoordMultiplexColumnTouchUI;
import util.GraphicsUtil;
import util.Messages;
import util.ui.NewMetalTheme;



public class JParCoordRangeSliderTouchTest extends JParCoordRangeSliderTest {

	static final private Logger LOGGER = Logger.getLogger (JParCoordRangeSliderTouchTest.class);
	
	
	public static void main (final String[] args) {
		
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		LOGGER.info ("Logger activated");

		//new LaunchFileDialog (JParCoordRangeSliderTouchTest.class);
		new JParCoordRangeSliderTouchTest (args.length > 0 ? args[0] : Messages.getString ("ExampleDataFile"));
	}
	
	
	public JParCoordRangeSliderTouchTest (final String dataFileName) {
		
		super (dataFileName);
		
		SwingUtilities.invokeLater (
			new Runnable () {
				@Override
				public void run() {
					final List<JComponent> parCoords = GraphicsUtil.getComponentsBeneath ((JComponent)(frame.getContentPane()), JParCoord.class, false);
					for (JComponent comp : parCoords) {
						LOGGER.debug ("JParcoord: "+comp);
						((JParCoord)comp).setUI (new ParCoordMultiplexColumnTouchUI ());
						LOGGER.debug ("JParcoord: "+((JParCoord)comp).getUI());
					}
				}
			}
		);
	}

}
