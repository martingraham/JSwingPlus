package example.histogram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.histogram.renderers.CutOff3DBarRenderer;
import example.histogram.renderers.CutOffBarRenderer;
import example.histogram.renderers.GradientBarRenderer;

import swingPlus.histogram.BarRenderer;
import swingPlus.histogram.JHistogram;
import swingPlus.shared.MyFrame;
import util.Messages;
import util.ui.NewMetalTheme;

public class JHistogramDemo {

	final static Logger LOGGER = Logger.getLogger (JHistogramDemo.class);

	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		//final MetalLookAndFeel lf = new MetalLookAndFeel();
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		new JHistogramDemo ();
	}

	public JHistogramDemo () {	
		
		JFrame.setDefaultLookAndFeelDecorated (true);
				
		final JHistogram jhisto = new JHistogram ();
		int[] data = new int [500];
		final Random rnd = new Random ();
		for (int n = 0; n < data.length; n++) {
			data[n] = rnd.nextInt (100);
		}
		jhisto.setData (data);
		
		
		final JHistogram jhisto2 = new JHistogram ();
		for (int n = 0; n < data.length; n++) {
			data[n] = rnd.nextInt (20);
		}
		jhisto2.setData (data);
		
		final JLabel label1 = new JLabel ();
		final JLabel label2 = new JLabel ();
		final Map<JHistogram, JLabel> labelMap = new HashMap<JHistogram, JLabel> ();
		labelMap.put (jhisto, label1);
		labelMap.put (jhisto2, label2);
		
		final JHistogram[] histograms = {jhisto, jhisto2};
		
		for (JHistogram histogram : histograms) {
			histogram.setMajorTickSpacing(10);
			histogram.setPaintTicks (true);
			histogram.setPaintLabels (true);
			histogram.setThumbColour (new Color (255, 255, 0, 128));
			histogram.putClientProperty ("Slider.paintThumbArrowShape", Boolean.TRUE);
			//histogram.setRenderer (new CutOffBarRenderer ());
			
			histogram.addChangeListener (
				new ChangeListener () {
	
					@Override
					public void stateChanged (final ChangeEvent cEvt) {
						final JSlider slider = (JSlider)cEvt.getSource();
						final JLabel label = labelMap.get (slider);
						final int val = slider.getValue();
						final StringBuilder sBuilder = new StringBuilder ();
						sBuilder.append ("Val: "+val+", FloorBin: "+((JHistogram)slider).getFloorBin (val));
						label.setText (sBuilder.toString());
					}
				}
			);
		}
		
		
		final JLabel summaryLabel = new JLabel ();
		final StringBuilder sBuilder = new StringBuilder ();
		sBuilder.append ("Histo1: "+jhisto.getMinDataValue()+" --> "+(jhisto.getMaxDataValuePlusOne() - 1));
		sBuilder.append ("       ");
		sBuilder.append ("Histo2: "+jhisto2.getMinDataValue()+" --> "+(jhisto2.getMaxDataValuePlusOne() - 1));
		summaryLabel.setText (sBuilder.toString());
		
		final JCheckBox axisBox = new JCheckBox ("Vertical", false);
		final JCheckBox invertBox = new JCheckBox ("Invert", false);

		
		axisBox.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					for (JHistogram histogram : histograms) {
						histogram.setOrientation (axisBox.isSelected() ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL);
					}
				}
			}
		);
		
		invertBox.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					for (JHistogram histogram : histograms) {
						histogram.setInverted (invertBox.isSelected());
					}
				}
			}
		);
		
		
		final BarRenderer[] renderers = {new JHistogram.DefaultBarRenderer(),  new CutOffBarRenderer (), new CutOff3DBarRenderer (), new GradientBarRenderer ()};
		final ButtonGroup bgroup = new ButtonGroup();
		final Map<AbstractButton, BarRenderer> buttonRenderMap = new HashMap<AbstractButton, BarRenderer> ();
		final ActionListener renderSelectListener = new ActionListener () {
			@Override 
			public void actionPerformed (final ActionEvent e) {
				BarRenderer renderer = buttonRenderMap.get(e.getSource());
				if (renderer != null) {
					for (JHistogram histogram : histograms) {
						histogram.setRenderer (renderer);
					}
				}
			}	
		};

		for (BarRenderer renderer : renderers) {
			JRadioButton selectButton = new JRadioButton (renderer.toString());
			bgroup.add (selectButton);
			buttonRenderMap.put (selectButton, renderer);
			selectButton.addActionListener (renderSelectListener);
		}
		
		SwingUtilities.invokeLater (
			new Runnable () {
				
				@Override
				public void run() {						
					
					final JFrame jf2 = new MyFrame ("JHistogram Demo");
					jf2.setSize (1024, 512);
					
					jf2.getContentPane().add (jhisto, "Center"); /*jsp*/
					//jf2.getContentPane().add (jhisto2, "North"); /*jsp*/
					
					
					final JPanel optionPanel = new JPanel (new GridLayout (2, 2));
					final JPanel optPanelA = new JPanel ();
					final JPanel optPanelB = new JPanel ();
					final JPanel optPanelC = new JPanel (new GridLayout ((bgroup.getButtonCount() + 1) / 2, 2));
					
					optPanelA.add (axisBox);
					optPanelA.add (invertBox);
					optPanelA.add (label1);
					optPanelA.add (label2);
					
					optPanelB.add (summaryLabel);
					
					final Enumeration<AbstractButton> buttons = bgroup.getElements();
					while (buttons.hasMoreElements()) {
						optPanelC.add (buttons.nextElement());
					}
					
					optionPanel.add (optPanelA);
					optionPanel.add (optPanelB);
					optionPanel.add (optPanelC);
					
					jf2.getContentPane().add (optionPanel, "South"); /*jsp*/
					
					jhisto2.setPreferredSize (new Dimension (jhisto.getWidth(), 320));

					jf2.setVisible (true);
				}
			}
		);
	}
		
}
