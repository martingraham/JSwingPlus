package example.graph;

import io.DataPrep;
import io.TwoColumnGraphReader;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Pattern;


import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import model.graph.EdgeDirection;
import model.graph.GraphModel;
import model.graph.impl.SymmetricGraphInstance;

import util.IconCache;
import util.Messages;
import util.ui.NewMetalTheme;

import swingPlus.graph.DefaultGraphCellRenderer;
import swingPlus.graph.DefaultGraphEdgeRenderer;
import swingPlus.graph.JGraph;
import swingPlus.shared.MyFrame;



public class JGraphDemo {

	final static Logger LOGGER = Logger.getLogger (JGraphDemo.class);
	
	protected transient GraphModel graph;

	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		//final MetalLookAndFeel lf = new MetalLookAndFeel();
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		if (args.length > 0) {
			new JGraphDemo (args[0]);
		}
	}

	public JGraphDemo (final String zipFileName) {	
		graph = new SymmetricGraphInstance ();
		addBarleyData (zipFileName);
		
		JFrame.setDefaultLookAndFeelDecorated (true);
				
		final JGraph jgraph = new JGraph (graph);
		jgraph.setShowEdges (true);
		jgraph.setDefaultNodeRenderer (String.class, new DefaultGraphCellRenderer (IconCache.makeIcon ("BarleyIcon")));
		jgraph.setDefaultEdgeRenderer (EdgeDirection.class, new DefaultGraphEdgeRenderer ());
		for (EdgeDirection ed : EdgeDirection.values()) {
			jgraph.setDefaultEdgeRenderer (ed.getClass(), new DefaultGraphEdgeRenderer ());
		}
		
		SwingUtilities.invokeLater (
				new Runnable () {
					
					@Override
					public void run() {						
						
						final JFrame jf2 = new MyFrame ("JGraph Demo");
						jf2.setSize (1024, 768);
						
						jf2.getContentPane().add (jgraph); /*jsp*/
						jf2.setVisible (true);
						//jsp.setDividerLocation (0.4f);
						//jsp3.setDividerLocation (0.2f);
					}
				}
			);
	}
	
	
	
	public final void addBarleyData (final String fileName) {

		InputStream iStream = null;
		try {
			iStream = DataPrep.getInstance().getPossiblyZippedInputStream (fileName, false);
		} catch (FileNotFoundException fnfe) {
			LOGGER.debug ("error", fnfe);
		}

		final TwoColumnGraphReader barleyReader = new TwoColumnGraphReader (iStream);
		barleyReader.populate (graph, Pattern.compile (" parent "));
	}
}
