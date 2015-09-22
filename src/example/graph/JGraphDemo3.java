package example.graph;


import java.awt.Color;
import java.util.Collection;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.graph.renderers.edge.ColourEdgeRenderer;
import example.graph.renderers.node.SingleImageGraphCellRenderer;


import model.graph.GraphModel;
import model.graph.impl.SymmetricGraphInstance;

import util.Messages;
import util.ui.NewMetalTheme;

import swingPlus.graph.JGraph;
import swingPlus.shared.MyFrame;



public class JGraphDemo3 {

	private final static Logger LOGGER = Logger.getLogger (JGraphDemo3.class);
	
	protected transient GraphModel graph;

	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		JFrame.setDefaultLookAndFeelDecorated (true);
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		new JGraphDemo3 ();
	}

	public JGraphDemo3 () {	
		
		graph = new SymmetricGraphInstance ();
		addNodes (graph, 5000);
		addEdges (graph);
		
		final JGraph jgraph = new JGraph (graph);
		jgraph.setShowEdges (true);
		jgraph.setDefaultNodeRenderer (Integer.class, new SingleImageGraphCellRenderer ("PersonIcon2"));
		jgraph.setDefaultEdgeRenderer (Color.class, new ColourEdgeRenderer ());

		//for (EdgeDirection ed : EdgeDirection.values()) {
		//	jgraph.setDefaultEdgeRenderer (ed.getClass(), new DefaultGraphEdgeRenderer ());
		//}
		

		
		SwingUtilities.invokeLater (
				new Runnable () {
					
					@Override
					public void run() {						
						
						jgraph.setOpaque (false);
						
						new MyFrame ("JGraph Demo") {{
							setSize (1024, 768);
							getContentPane().add (jgraph);
							setVisible (true);
						}};
					}
				}
			);
	}
	
	
	
	protected final void addNodes (final GraphModel graph, final int nodeSize) {
		for (int n = 0; n < nodeSize; n++) {
			graph.addNode (Integer.valueOf (n));
		}
	}
	
	protected final void addEdges (final GraphModel graph) {
		final Color[] colours = {Color.blue, Color.green, Color.red};
		final Collection<Object> nodes = graph.getNodes ();
		final Random rnd = new Random ();
		
		Object lastNode = null;
		for (Object node : nodes) {
			if (lastNode != null) {
				graph.addEdge (lastNode, node, colours [rnd.nextInt (3)]);
			}
			lastNode = node;
		}
	}
}
