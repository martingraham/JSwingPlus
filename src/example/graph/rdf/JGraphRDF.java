package example.graph.rdf;


import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import example.graph.renderers.node.RDFGraphCellRenderer;

import model.graph.GraphModel;

import util.Messages;
import util.ui.NewMetalTheme;

import swingPlus.graph.DefaultGraphEdgeRenderer;
import swingPlus.graph.JGraph;
import swingPlus.shared.MyFrame;



public class JGraphRDF {

	private final static Logger LOGGER = Logger.getLogger (JGraphRDF.class);
	
	protected transient GraphModel graph;

	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		JFrame.setDefaultLookAndFeelDecorated (true);
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		if (args.length > 0) {
			new JGraphRDF (args[0]);
		}
	}

	public JGraphRDF (final String dataFileName) {	
				
		graph = new JenaRDFGraphWrapper (loadAndProbeRDF (dataFileName));
					
		final JGraph jgraph = new JGraph (graph);
		jgraph.setShowEdges (true);
		jgraph.setDefaultNodeRenderer (com.hp.hpl.jena.rdf.model.impl.ResourceImpl.class, new RDFGraphCellRenderer ());
		jgraph.setDefaultNodeRenderer (com.hp.hpl.jena.rdf.model.impl.PropertyImpl.class, new RDFGraphCellRenderer ());
		jgraph.setDefaultEdgeRenderer (com.hp.hpl.jena.rdf.model.impl.ResourceImpl.class, new DefaultGraphEdgeRenderer ());
		jgraph.setDefaultEdgeRenderer (com.hp.hpl.jena.rdf.model.impl.LiteralImpl.class, new DefaultGraphEdgeRenderer ());

		/*
		jgraph.setGraphFilter (
			new DefaultGraphFilter () {
				@Override
				public boolean includeEdge (final Edge edge) {
					return edge == null ? false : filterSet.get (((Integer)edge.getEdgeObject()).intValue());
				}	
				
				@Override
				public boolean includeNode (final Object obj) {
					if (obj == null) {
						return false;
					}
					final Set<Edge> edges = jgraph.getModel().getEdges (obj);
					for (Edge edge : edges) {
						if (includeEdge (edge)) {
							return true;
						}
					}
					return false;
				}
			}
		);
		*/
		//for (EdgeDirection ed : EdgeDirection.values()) {
		//	jgraph.setDefaultEdgeRenderer (ed.getClass(), new DefaultGraphEdgeRenderer ());
		//}
		

		
		SwingUtilities.invokeLater (
				new Runnable () {
					
					@Override
					public void run() {						
						
						final JFrame jf2 = new MyFrame ("JGraph Demo");
						jf2.setSize (1024, 768);
						
						//JZoomPane zoomPane = new JZoomPane ();
						jgraph.setOpaque (false);
						//zoomPane.setView (jgraph);
						jf2.getContentPane().add (jgraph); /*jsp*/
						jf2.setVisible (true);
						//jsp.setDividerLocation (0.4f);
						//jsp3.setDividerLocation (0.2f);
					}
				}
			);
	}
	
	
	protected final Model loadAndProbeRDF (final String inputFileName) {
		final Model model = ModelFactory.createDefaultModel ();
		final InputStream iStream = FileManager.get().open (inputFileName);
		if (iStream == null) {
		    LOGGER.error ("file not found");
		    throw new IllegalArgumentException("File: " + inputFileName + " not found");

		}

		// read the RDF/XML file
		model.read (iStream, null);

		/*
		LOGGER.debug ("Nodes");
		NodeIterator nIter = model.listObjects();
		while (nIter.hasNext()) {
			LOGGER.debug (nIter.next());
		}
		
		LOGGER.debug ("\n\n\nStatements");
		StmtIterator sIter = model.listStatements();
		while (sIter.hasNext()) {
			Statement stmt = sIter.next ();
			try {
				LOGGER.debug (stmt.getResource().getClass()+" "+stmt.getPredicate().getClass()+" "+stmt.getObject().getClass());
			} catch (Exception error) {
				LOGGER.error ("error", error);
			}
			LOGGER.debug (stmt);
		}
		*/
		
		return model;
	}
}
