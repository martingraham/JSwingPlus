package example.graph.roslin;

import io.DataPrep;

import example.graph.roslin.io.GenotypeRoslinReader;
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
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.graph.renderers.edge.RoslinEdgeRenderer2;
import example.graph.renderers.node.RoslinGraphCellRenderer4;


import model.graph.EdgeDirection;
import model.graph.GraphModel;
import model.graph.impl.SymmetricGraphInstance;

import util.Messages;
import util.ui.NewMetalTheme;

import swingPlus.graph.JGraph;
import swingPlus.graph.SelectionPopupMenu;
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



public class JGraphDemo5 {

	private final static Logger LOGGER = Logger.getLogger (JGraphDemo5.class);
	
	protected transient GraphModel graph;

	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		//final MetalLookAndFeel lf = new MetalLookAndFeel();
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		if (args.length > 0) {
			new JGraphDemo5 (args);
		}
	}

	public JGraphDemo5 (final String[] filenames) {	
		
		final String pedigreeFilename = filenames [0];
		final String genotypeFilename = filenames [1];
		
		graph = new SymmetricGraphInstance ();
		final MarkerMap mmap = new MarkerMap ();
		addPedigreeData (pedigreeFilename);
		addGenotypeData (genotypeFilename, mmap);
		final PedigreeErrorCheck pec = new PedigreeErrorCheck (graph);
		pec.check ();
		
		final int horizOrVert = SwingConstants.VERTICAL;
		
		JFrame.setDefaultLookAndFeelDecorated (true);
		final AttractiveForceCalculationInterface linkAttractor = new SimpleEdgeAttractor ();
		final RepulsiveForceCalculationInterface nodeRepulse = new BarnesHut2DForceCalculator ();
		final PositionUpdater nodeLinkUpdater = new ForceBasedPositionUpdater ();
		
		final AttractiveForceCalculationInterface pedSort = new MedianSortHoriz (horizOrVert);
		((MedianSortHoriz)pedSort).setLayerSep (300);
		((MedianSortHoriz)pedSort).setMinObjSep (5);
		final RepulsiveForceCalculationInterface nullRepulse = new NullRepulsiveForceCalculator ();
		final PositionUpdater pedigreeUpdater = new OneOffPositionUpdater ();
		
		final JGraph jgraph = new JGraph (graph, nullRepulse, pedSort, pedigreeUpdater);
		jgraph.setShowEdges (true);
		jgraph.setDefaultNodeRenderer (Animal.class, new RoslinGraphCellRenderer4 (horizOrVert));

		jgraph.setDefaultEdgeRenderer (EdgeDirection.class, new RoslinEdgeRenderer2 ());
		for (EdgeDirection ed : EdgeDirection.values()) {
			jgraph.setDefaultEdgeRenderer (ed.getClass(), new RoslinEdgeRenderer2 ());
		}
		
		jgraph.setShowEdges (false);
		
		final RoslinFilter filter = new RoslinFilter (mmap);
		jgraph.setGraphFilter (filter);
		
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
		
		final JButton jfb = new JButton ("Re-Layout");
		jfb.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent event) {
					jgraph.restartWorker();
					//jgraph.repaint ();
				}		
			}
		);
		
		final JButton jbut = new JButton ("Toggle Layout");
		jbut.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent event) {
					if (jgraph.getAttractiveForceCalculator() == linkAttractor) {
						jgraph.setAttractiveForceCalculator (pedSort);
						jgraph.setRepulsiveForceCalculator (nullRepulse);
						jgraph.setPositionUpdater (pedigreeUpdater);
					} else {
						jgraph.setAttractiveForceCalculator (linkAttractor);
						jgraph.setRepulsiveForceCalculator (nodeRepulse);
						jgraph.setPositionUpdater (nodeLinkUpdater);
					}
					jgraph.restartWorker ();
				}		
			}
		);
		
		SwingUtilities.invokeLater (
			new Runnable () {
				
				@Override
				public void run() {						
					
					final JFrame jf2 = new MyFrame ("JPedigree 1.0");
					jf2.setSize (1024, 768);
					
					jf2.getContentPane().add (jgraph); /*jsp*/
					jf2.setVisible (true);
					
					final JPanel optionPanel = new JPanel ();
					optionPanel.add (jcb);
					optionPanel.add (jfb);
					//optionPanel.add (jbut);
					jf2.getContentPane().add (optionPanel, BorderLayout.SOUTH);
					
					SelectionPopupMenu.getInstance().addDataSpecificAction (new MarkerAnimalFilterAction ());

					final BitMapPanel bmapPanel = new BitMapPanel ();
					bmapPanel.populate (mmap);
					bmapPanel.getTable().getModel().addTableModelListener (
						new TableModelListener () {
							@Override
							public void tableChanged (final TableModelEvent event) {
						    	if (event.getFirstRow() != TableModelEvent.HEADER_ROW 
						    			&& event.getFirstRow() == event.getLastRow()
						    			&& event.getColumn() >= 0
						    			&& event.getType() == TableModelEvent.UPDATE) {

						    		// Done in a runnable to make sure these don't happen
						    		// until the Swing Thread has finished running through
						    		// all the tableChanged methods in the table's varied
						    		// listeners.
						    		SwingUtilities.invokeLater (
						    			new Runnable () {
						    				public void run () {
						    					bmapPanel.stats (graph);
						    					jgraph.updateFilteredModel ();
						    					jgraph.repaint ();
						    				}
						    			}
						    		);
						    		
						    	}
							}		
						}
					);
					
					final JTabbedPane jtp = new JTabbedPane ();		
					jtp.addTab ("Marker Filter", bmapPanel);
					
					jf2.getContentPane().add (jtp, BorderLayout.EAST);
					
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
	
	
	
	public final void addGenotypeData (final String fileName, final MarkerMap mmap) {

		InputStream iStream = null;
		try {
			iStream = DataPrep.getInstance().getPossiblyZippedInputStream (fileName, false);
		} catch (FileNotFoundException fnfe) {
			LOGGER.debug ("error", fnfe);
		}

		final GenotypeRoslinReader grr = new GenotypeRoslinReader (iStream);
		grr.populate (graph, mmap);
	}
}
