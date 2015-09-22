package example.graph;

import io.DataPrep;
import io.graphnet.NetFileReader;

import java.awt.BorderLayout;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.graph.renderers.edge.EdgeCountEdgeRenderer;
import example.graph.renderers.node.ThesaurusGraphCellRenderer;

import model.graph.EdgeDirection;
import model.graph.GraphModel;
import model.graph.impl.SymmetricGraphInstance;

import util.Messages;
import util.ui.NewMetalTheme;

import swingPlus.graph.GraphEdgeRenderer;
import swingPlus.graph.JGraph;
import swingPlus.shared.MyFrame;



public class JGraphDemoNetFile {

	static final Logger LOGGER = Logger.getLogger (JGraphDemoNetFile.class);
	
	protected transient GraphModel graph;

	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		//final MetalLookAndFeel lf = new MetalLookAndFeel();
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		if (args.length > 0) {
			new JGraphDemoNetFile (args[0]);
		}
	}

	public JGraphDemoNetFile (final String zipFileName) {	
		graph = new SymmetricGraphInstance ();
		addData (zipFileName);
		
		JFrame.setDefaultLookAndFeelDecorated (true);
				
		final JGraph jgraph = new JGraph (graph);
		jgraph.setShowEdges (true);
		jgraph.setDefaultNodeRenderer (String.class, new ThesaurusGraphCellRenderer ());
		
		final GraphEdgeRenderer defaultEdgeRenderer = new EdgeCountEdgeRenderer ();
		jgraph.setDefaultEdgeRenderer (EdgeDirection.class, defaultEdgeRenderer);
		for (EdgeDirection ed : EdgeDirection.values()) {
			jgraph.setDefaultEdgeRenderer (ed.getClass(), defaultEdgeRenderer);
		}
		
		//final ForceAnalyser fAnalyser = new ForceAnalyser ();
		//fAnalyser.setGraph (jgraph);
		//fAnalyser.setPreferredSize (new Dimension (128, 640));

		
		SwingUtilities.invokeLater (
				new Runnable () {
					
					@Override
					public void run() {						
						
						final JFrame jf2 = new MyFrame ("JGraph Net File Demo");
						jf2.setSize (1024, 768);
						
						jf2.getContentPane().add (jgraph, BorderLayout.CENTER); /*jsp*/
						//jf2.getContentPane().add (fAnalyser, BorderLayout.EAST);
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

		final NetFileReader netFileReader = new NetFileReader (iStream);
		netFileReader.populate (graph);
		
		if (iStream != null) {
			try {
				iStream.close ();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
