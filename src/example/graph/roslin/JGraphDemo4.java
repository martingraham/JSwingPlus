package example.graph.roslin;

import io.DataPrep;

import example.graph.roslin.io.PedigreeRoslinEdgeReader;
import example.graph.roslin.io.PedigreeRoslinNodeReader;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Pattern;


import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.graph.renderers.edge.RoslinEdgeRenderer2;
import example.graph.renderers.node.RoslinGraphCellRenderer2;


import model.graph.EdgeDirection;
import model.graph.GraphModel;
import model.graph.impl.SymmetricGraphInstance;

import util.Messages;
import util.ui.NewMetalTheme;

import swingPlus.graph.JGraph;
import swingPlus.graph.force.AttractiveForceCalculationInterface;
import swingPlus.graph.force.PositionUpdater;
import swingPlus.graph.force.RepulsiveForceCalculationInterface;
import swingPlus.graph.force.impl.BarnesHut2DForceCalculator;
import swingPlus.graph.force.impl.ForceBasedPositionUpdater;
import swingPlus.graph.force.impl.MedianSortHoriz;
import swingPlus.graph.force.impl.NullRepulsiveForceCalculator;
import swingPlus.graph.force.impl.OneOffPositionUpdater;
import swingPlus.graph.force.impl.SimpleEdgeAttractor;
import swingPlus.shared.MyFrame;



public class JGraphDemo4 {

	private final static Logger LOGGER = Logger.getLogger (JGraphDemo4.class);
	
	protected transient GraphModel graph;

	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		//final MetalLookAndFeel lf = new MetalLookAndFeel();
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		if (args.length > 0) {
			new JGraphDemo4 (args[0]);
		}
	}

	public JGraphDemo4 (final String zipFileName) {	
		graph = new SymmetricGraphInstance ();
		addPedigreeData (zipFileName);
		
		JFrame.setDefaultLookAndFeelDecorated (true);
		final AttractiveForceCalculationInterface linkAttractor = new SimpleEdgeAttractor ();
		final RepulsiveForceCalculationInterface nodeRepulse = new BarnesHut2DForceCalculator ();
		final PositionUpdater nodeLinkUpdater = new ForceBasedPositionUpdater ();
		
		final AttractiveForceCalculationInterface pedSort = new MedianSortHoriz ();
		final RepulsiveForceCalculationInterface nullRepulse = new NullRepulsiveForceCalculator ();
		final PositionUpdater pedigreeUpdater = new OneOffPositionUpdater ();
		
		final JGraph jgraph = new JGraph (graph, nullRepulse, pedSort, pedigreeUpdater);
		jgraph.setShowEdges (true);
		jgraph.setDefaultNodeRenderer (Animal.class, new RoslinGraphCellRenderer2 ());

		jgraph.setDefaultEdgeRenderer (EdgeDirection.class, new RoslinEdgeRenderer2 ());
		for (EdgeDirection ed : EdgeDirection.values()) {
			jgraph.setDefaultEdgeRenderer (ed.getClass(), new RoslinEdgeRenderer2 ());
		}
		
		jgraph.setShowEdges (false);
		
		
		final JCheckBox jcb = new JCheckBox ("Show All Edges", jgraph.isShowEdges());
		jcb.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent event) {
					jgraph.setShowEdges (jcb.isSelected());
					jgraph.repaint ();
				}		
			}
		);
		
		
		final JSlider llengthSlider = new JSlider (20, 1000);
		llengthSlider.addChangeListener(
			new ChangeListener () {
				@Override
				public void stateChanged (final ChangeEvent cEvent) {
					((SimpleEdgeAttractor)linkAttractor).setLinkLength (llengthSlider.getValue());
				}
			}
		);
		
		final JSlider lstiffSlider = new JSlider (20, 1000);
		lstiffSlider.addChangeListener(
			new ChangeListener () {
				@Override
				public void stateChanged (final ChangeEvent cEvent) {
					((SimpleEdgeAttractor)linkAttractor).setStiffness (lstiffSlider.getValue());
				}
			}
		);
		
		
		final JSlider repulseSlider = new JSlider (1, 50, 10);
		repulseSlider.addChangeListener(
			new ChangeListener () {
				@Override
				public void stateChanged (final ChangeEvent cEvent) {
					((BarnesHut2DForceCalculator)nodeRepulse).setAttenuator (repulseSlider.getValue());
				}
			}
		);
		
		final JButton jbut = new JButton ("Toggle Layout");
		jbut.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent event) {
					final boolean toDAG = jgraph.getAttractiveForceCalculator() == linkAttractor;
					if (jgraph.getAttractiveForceCalculator() == linkAttractor) {
						jgraph.setAttractiveForceCalculator (pedSort);
						jgraph.setRepulsiveForceCalculator (nullRepulse);
						jgraph.setPositionUpdater (pedigreeUpdater);
					} else {
						jgraph.setAttractiveForceCalculator (linkAttractor);
						jgraph.setRepulsiveForceCalculator (nodeRepulse);
						jgraph.setPositionUpdater (nodeLinkUpdater);
					}
					llengthSlider.setEnabled (!toDAG);
					lstiffSlider.setEnabled (!toDAG);
					repulseSlider.setEnabled (!toDAG);
					jgraph.restartWorker ();
				}		
			}
		);
		
		

		
		((SimpleEdgeAttractor)linkAttractor).setLinkLength (llengthSlider.getValue());
		((SimpleEdgeAttractor)linkAttractor).setStiffness (lstiffSlider.getValue());
		((BarnesHut2DForceCalculator)nodeRepulse).setAttenuator (repulseSlider.getValue());
		llengthSlider.setEnabled (false);
		lstiffSlider.setEnabled (false);
		repulseSlider.setEnabled (false);
		
		SwingUtilities.invokeLater (
				new Runnable () {
					
					@Override
					public void run() {						
						
						final JFrame jf2 = new MyFrame ("JGraph Demo");
						jf2.setSize (1024, 768);
						
						jf2.getContentPane().add (jgraph); /*jsp*/
						jf2.setVisible (true);
						
						final JPanel optionPanel = new JPanel ();
						optionPanel.add (jcb);
						optionPanel.add (jbut);
						optionPanel.add (new JSeparator (SwingConstants.VERTICAL));
						optionPanel.add (new JLabel ("Link Length:"));
						optionPanel.add (llengthSlider);
						optionPanel.add (new JSeparator (SwingConstants.VERTICAL));
						optionPanel.add (new JLabel ("Link Stiffness:"));
						optionPanel.add (lstiffSlider);
						
						optionPanel.add (new JLabel ("Repulse Strength:"));
						optionPanel.add (repulseSlider);
						jf2.getContentPane().add (optionPanel, BorderLayout.SOUTH);
						//jsp.setDividerLocation (0.4f);
						//jsp3.setDividerLocation (0.2f);
					}
				}
			);
	}
	
	
	
	public final void addPedigreeData (final String fileName) {

		InputStream iStream = null;
		try {
			iStream = DataPrep.getInstance().getPossiblyZippedInputStream (fileName, false);
		} catch (FileNotFoundException fnfe) {
			LOGGER.debug ("error", fnfe);
		}

		final PedigreeRoslinNodeReader pnr = new PedigreeRoslinNodeReader (iStream);
		pnr.populate (graph, Pattern.compile ("\t"));
		
		try {
			iStream = DataPrep.getInstance().getPossiblyZippedInputStream (fileName, false);
		} catch (FileNotFoundException fnfe) {
			LOGGER.debug ("error", fnfe);
		}
		
		final PedigreeRoslinEdgeReader per = new PedigreeRoslinEdgeReader (iStream);
		per.setNodeMap (pnr.getNodeMap());
		per.populate (graph, Pattern.compile ("\t"));
		
		LOGGER.info ("Edge count: "+graph.getEdgeCount());
		LOGGER.info ("Node count: "+graph.getNodeCount());
	}
}
