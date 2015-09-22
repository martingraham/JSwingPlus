package example.graph;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;


import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.graph.renderers.edge.FatEdgeRenderer;
import example.graph.renderers.node.ImageGraphCellRenderer;



import model.graph.Edge;
import model.graph.GraphModel;
import model.graph.impl.EdgeInstance;
import model.graph.impl.SymmetricGraphInstance;

import util.GraphicsUtil;
import util.Messages;
import util.ui.NewMetalTheme;

import swingPlus.graph.ForceAnalyser;
import swingPlus.graph.JGraph;
import swingPlus.shared.MyFrame;



public class JGraphPhoto {

	private final static Logger LOGGER = Logger.getLogger (JGraphPhoto.class);
	
	protected transient GraphModel graph;
	protected JGraph jgraph;

	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		JFrame.setDefaultLookAndFeelDecorated (true);
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		new JGraphPhoto (args[0], new Dimension (200, 200));
	}

	public JGraphPhoto (final String photoDirectory, final Dimension maxThumbSize) {	
		
		final FileFilter filter = GraphicsUtil.GIF_FILENAME_FILTER;
		final double sample = 0.0;
		
		final File imageCache = new File (System.getProperty("user.home") + File.separatorChar + "imageCache");
		if (!imageCache.exists()) {
			GraphicsUtil.makeThumbnailFileCache (new File (photoDirectory), imageCache, maxThumbSize, filter);
		}
		
		graph = new SymmetricGraphInstance ();
		addNodes (graph, imageCache, filter, sample);
		addEdges (graph);
		
		jgraph = new JGraph (graph);
		jgraph.setShowEdges (true);
		jgraph.setDefaultNodeRenderer (Image.class, new ImageGraphCellRenderer ());
		jgraph.setDefaultNodeRenderer (BufferedImage.class, new ImageGraphCellRenderer ());
		jgraph.setDefaultEdgeRenderer (Boolean.class, new FatEdgeRenderer ());

		//for (EdgeDirection ed : EdgeDirection.values()) {
		//	jgraph.setDefaultEdgeRenderer (ed.getClass(), new DefaultGraphEdgeRenderer ());
		//}
		
		final ForceAnalyser fAnalyser = new ForceAnalyser ();
		fAnalyser.setGraph (jgraph);
		fAnalyser.setPreferredSize (new Dimension (128, 640));
		
		
		SwingUtilities.invokeLater (
				new Runnable () {
					
					@Override
					public void run() {						
						
						jgraph.setOpaque (false);
						
						new MyFrame ("JGraph Demo") {
							private static final long serialVersionUID = -9104111025789014464L;
							{
								setSize (1024, 768);
								getContentPane().add (jgraph);
								getContentPane().add (fAnalyser, BorderLayout.EAST);
								setVisible (true);
							}
						};
					}
				}
			);
	}
	
	
	
	protected void addNodes (final GraphModel graph, final File imageCache, final FileFilter filter, final double sampleRate) {
		final File[] imageFiles = imageCache.listFiles (filter);
		for (File imageFile : imageFiles) {
			if (Math.random() > sampleRate) {
				try {
					final BufferedImage bim = ImageIO.read(imageFile);
					graph.addNode (bim);
					LOGGER.info ("added node "+imageFile.getPath());
				} catch (IOException ioe) {
					LOGGER.error ("Thumbnail loading error.", ioe);
				}
			}
		}
	}
	
	protected void addEdges (final GraphModel graph) {
		final Collection<Object> nodes = graph.getNodes ();
		final List<Object> nodeList = new ArrayList<Object> (nodes);
		final int setSize = nodes.size();
		final Random rnd = new Random ();
	
		for (Object node : nodes) {
			final int links = 1 + (int)(Math.abs (rnd.nextGaussian()));
			do {
				for (int edge = 0; edge < links; edge++) {
					final Object other = nodeList.get (rnd.nextInt (setSize));
					if (other != node) {
						final Edge newEdge = new EdgeInstance (node, other, Boolean.TRUE);
						final Edge recipEdge = new EdgeInstance (other, node, Boolean.TRUE);
						if (!graph.containsEdge (newEdge) && !graph.containsEdge (recipEdge)) {
							graph.addEdge (node, other, Boolean.TRUE);
						}
					}
				}
			} while (graph.getEdges(node) == null || graph.getEdges(node).isEmpty());
		}
	}
}
