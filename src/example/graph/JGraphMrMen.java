package example.graph;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.graph.renderers.node.MrManCellRenderer;


import model.graph.GraphModel;

import swingPlus.graph.force.impl.BarnesHut2DForceCalculator;
import swingPlus.graph.force.impl.SimpleEdgeAttractor;
import util.Messages;
import util.ui.NewMetalTheme;




public class JGraphMrMen extends JGraphPhoto {

	private final static Logger LOGGER = Logger.getLogger (JGraphMrMen.class);

	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		JFrame.setDefaultLookAndFeelDecorated (true);
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		new JGraphMrMen (args[0]);
	}

	public JGraphMrMen (final String photoDirectory) {	
		
		super (photoDirectory, new Dimension (100, 100));
		((BarnesHut2DForceCalculator)jgraph.getRepulsiveForceCalculator()).setAttenuator (0.8);
		((SimpleEdgeAttractor)jgraph.getAttractiveForceCalculator()).setLinkLength (320);

		jgraph.setDefaultNodeRenderer (MrMan.class, new MrManCellRenderer ());
	}
	
	
	
	protected void addNodes (final GraphModel graph, final File imageCache, final FileFilter filter, final double sampleRate) {
		final String[] mrmen = {"bounce", "brave", "bump", "busy", "chatterbox", "cheerful", "clever", "clumsy", "cool",
				"daydream", "dizzy", "forgetful", "funny", "fussy", "good", "greedy", "grumble", "grumpy", "happy", "impossible",
				"jelly", "lazy", "mean", "messy", "mischief", "muddle", "noisy", "nonsense", "nosey", "perfect", "quiet", "rude",
				"rush", "silly", "skinny", "slow", "small", "sneeze", "snow", "strong", "tall", "tickle", "topsy-turvy",
				"uppity", "worry", "wrong"
		};
		
		final String web1 = "http://www.themistermen.co.uk/images/mrmen_uk/";
		final String imgsuffix = ".gif";

		for (String man : mrmen) {
			
			final String gifman = man + imgsuffix;
			File imageFile = new File (imageCache.getPath() + File.separator + gifman);
			if (!imageFile.exists()) {
				try {
					URL url = new URL (web1 + gifman);
					LOGGER.debug (url);
					final BufferedImage bim = ImageIO.read (url);
					if (bim != null) {
						ImageIO.write (bim, "gif", imageFile);
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (Math.random() > sampleRate) {
				try {
					final BufferedImage bim = ImageIO.read(imageFile);
					String name = imageFile.getName();
					if (name.startsWith ("THUMB")) {
						name = name.substring(5);
					}
					name = name.substring (0, name.indexOf('.'));
					name = "MR. "+name.toUpperCase();
					final MrMan mrman = new MrMan (bim, name);
					graph.addNode (mrman);
					LOGGER.info ("added node "+imageFile.getPath());
				} catch (IOException ioe) {
					LOGGER.error ("Thumbnail loading error.", ioe);
				}
			}
		}
	}
	
	
	/*
	protected void addNodes (final GraphModel graph, final File imageCache, final FileFilter filter, final double sampleRate) {

		final File[] imageFiles = imageCache.listFiles (filter);
		for (File imageFile : imageFiles) {
			if (Math.random() > sampleRate) {
				try {
					final BufferedImage bim = ImageIO.read(imageFile);
					String name = imageFile.getName();
					if (name.startsWith ("THUMB")) {
						name = name.substring(5);
					}
					name = name.substring (0, name.indexOf('.'));
					name = "MR. "+name.toUpperCase();
					final MrMan mrman = new MrMan (bim, name);
					graph.addNode (mrman);
					LOGGER.info ("added node "+imageFile.getPath());
				} catch (IOException ioe) {
					LOGGER.error ("Thumbnail loading error.", ioe);
				}
			}
		}
	}
	*/
	
	
	public class MrMan {
		
		protected BufferedImage image;
		protected String name;
		
		MrMan (final BufferedImage image, final String name) {
			setImage (image);
			setName (name);
		}
		
		public BufferedImage getImage() {
			return image;
		}
		
		public final void setImage (final BufferedImage image) {
			this.image = image;
		}
		
		public String getName() {
			return name;
		}
		
		public final void setName (final String name) {
			this.name = name;
		}
		
		public String toString () {
			return getName ();
		}
	}
}
