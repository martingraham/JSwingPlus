package example.graph;

import io.DataPrep;
import io.TwoColumnGraphReader;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Pattern;


import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.graph.renderers.edge.EdgeCountEdgeRenderer;
import example.graph.renderers.node.NodeDegreeGraphCellRenderer;

import model.graph.EdgeDirection;
import model.graph.GraphModel;
import model.graph.impl.SymmetricGraphInstance;

import util.Messages;
import util.ui.NewMetalTheme;

import swingPlus.graph.ForceAnalyser;
import swingPlus.graph.GraphEdgeRenderer;
import swingPlus.graph.JGraph;
import swingPlus.shared.MyFrame;



public class JGraphLarge {

	static final Logger LOGGER = Logger.getLogger (JGraphLarge.class);
	
	protected transient GraphModel graph;

	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		//final MetalLookAndFeel lf = new MetalLookAndFeel();
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		if (args.length > 0) {
			new JGraphLarge (args[0]);
		}
	}

	// General Relativity and Quantum Cosmology collaboration network
	// http://snap.stanford.edu/data/ca-GrQc.html
	// J. Leskovec, J. Kleinberg and C. Faloutsos. Graph Evolution: Densification and Shrinking Diameters. 
	// ACM Transactions on Knowledge Discovery from Data (ACM TKDD), 1(1), 2007.
	public JGraphLarge (final String zipFileName) {	
		graph = new SymmetricGraphInstance ();
		addData (zipFileName);
		
		JFrame.setDefaultLookAndFeelDecorated (true);
				
		final JGraph jgraph = new JGraph (graph);
		jgraph.setShowEdges (false);
		jgraph.setDefaultNodeRenderer (String.class, new NodeDegreeGraphCellRenderer ());
		
		// DefaultGraphEdgeRenderer  for wiki-vote.txt
		// SimpleEdgeRenderer for arxiv files
		final GraphEdgeRenderer defaultEdgeRenderer = new EdgeCountEdgeRenderer ();
		jgraph.setDefaultEdgeRenderer (EdgeDirection.class, defaultEdgeRenderer);
		for (EdgeDirection ed : EdgeDirection.values()) {
			jgraph.setDefaultEdgeRenderer (ed.getClass(), defaultEdgeRenderer);
		}
		
		final ForceAnalyser fAnalyser = new ForceAnalyser ();
		fAnalyser.setGraph (jgraph);
		fAnalyser.setPreferredSize (new Dimension (128, 640));

		
		SwingUtilities.invokeLater (
				new Runnable () {
					
					@Override
					public void run() {						
						
						final JFrame jf2 = new MyFrame ("JGraph Demo");
						jf2.setSize (1024, 768);
						
						jf2.getContentPane().add (jgraph, BorderLayout.CENTER); /*jsp*/
						jf2.getContentPane().add (fAnalyser, BorderLayout.EAST);
						jf2.setVisible (true);
						
					}
				}
			);
	}
	
	
	
	public final void addData (final String fileName) {

		InputStream iStream = null;
		try {
			iStream = DataPrep.getInstance().getPossiblyZippedInputStream (fileName, false);
		} catch (FileNotFoundException fnfe) {
			LOGGER.debug ("error", fnfe);
		}

		final TwoColumnGraphReader barleyReader = new TwoColumnGraphReader (iStream);
		barleyReader.populate (graph, Pattern.compile ("\t"));
	}
}
