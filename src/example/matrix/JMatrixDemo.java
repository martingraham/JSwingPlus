package example.matrix;

import io.DataPrep;
import io.XMLParse;

import java.awt.Color;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import model.graph.Edge;
import model.graph.GraphModel;
import model.graph.impl.DirectedGraphInstance;
import model.graph.impl.EdgeInstance;
import model.matrix.DefaultMatrixTableModel;
import model.shared.selection.LinkedGraphMatrixSelectionModelBridge;

import util.GraphicsUtil;
import util.Messages;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import example.graph.renderers.edge.AssessmentEdgeRenderer;
import example.graph.renderers.node.StringGraphCellRenderer;
import example.matrix.renderers.AssessmentRenderer;
import example.matrix.renderers.ShapeRenderer;
import example.matrix.renderers.StringHeaderRenderer;

import swingPlus.graph.JGraph;
import swingPlus.matrix.ImageRenderer;
import swingPlus.matrix.JMatrix;
import swingPlus.matrix.JHeaderRenderer;
import swingPlus.matrix.CollectionRenderer;
import swingPlus.shared.MyFrame;
import util.colour.ColorUtilities;
import util.ui.NewMetalTheme;
import util.ui.VerticalLabelUI;


public class JMatrixDemo {

	protected transient GraphModel graph;
	private final static Logger LOGGER = Logger.getLogger (JMatrixDemo.class);
	protected JMatrix table;
	protected JGraph jgraph;
	
	private final static double SAMPLE_THRESHOLD = 0.0;
	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		//final MetalLookAndFeel lf = new MetalLookAndFeel();
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		if (args.length > 0) {
			new JMatrixDemo (args[0]);
		}
	}

	public JMatrixDemo (final String zipFileName) {	
		graph = new DirectedGraphInstance ();
		addData (graph, zipFileName);
		
		JFrame.setDefaultLookAndFeelDecorated (true);
		
		final TableModel mtm = new DefaultMatrixTableModel (graph);
		table = new JMatrix (mtm);
	
		for (int i = 0; i < table.getColumnCount(); i++) {
		    final TableColumn column = table.getColumnModel().getColumn(i);
		    column.setMinWidth (1); 
		    column.setMaxWidth (1000); 
		    column.setPreferredWidth (table.getRowHeight()); 
		}

		table.setDefaultRenderer (IndividualJudgement.class, new AssessmentRenderer ());
		table.setDefaultRenderer (Image.class, new ImageRenderer ());
		table.setDefaultRenderer (RectangularShape.class, new ShapeRenderer ());
		table.setDefaultRenderer (HashSet.class, new CollectionRenderer ());

		table.setGridColor (ColorUtilities.mixColours (table.getBackground(), Color.black, 0.8f));
		//table.setShowGrid (false);
		//table.setIntercellSpacing (new Dimension (0, 0));
		
		final JHeaderRenderer stringHeader = new StringHeaderRenderer ();
		final JHeaderRenderer stringHeader2 = new StringHeaderRenderer ();
		table.getRowHeader().setDefaultRenderer (Object.class, stringHeader);
		table.getRowHeader().setDefaultRenderer (String.class, stringHeader);
		table.getColumnHeader().setDefaultRenderer (Object.class, stringHeader2);
		table.getColumnHeader().setDefaultRenderer (String.class, stringHeader2);
		((JLabel)stringHeader2).setUI (new VerticalLabelUI (false));
		
		stringHeader.setSelectionBackground (table.getRowHeader());
		stringHeader2.setSelectionBackground (table.getColumnHeader());
		table.setDefaultRenderer (String.class, stringHeader);
		
		jgraph = new JGraph (graph);
		jgraph.setShowEdges (false);
		jgraph.setDefaultNodeRenderer (String.class, new StringGraphCellRenderer ());
		jgraph.setDefaultEdgeRenderer (IndividualJudgement.class, new AssessmentEdgeRenderer ());
		jgraph.setAttractiveForceCalculator (new JudgementAttractor ());
		
		//LinkedGraphTableBridgeModel selectionBridge = new LinkedGraphTableBridgeModel ();
		final LinkedGraphMatrixSelectionModelBridge selectionBridge = new LinkedGraphMatrixSelectionModelBridge ();
		selectionBridge.addJTable (table);
		selectionBridge.addJGraph (jgraph);
		
		
		SwingUtilities.invokeLater (
			new Runnable () {
				
				@Override
				public void run() {
					final JFrame jFrame = new MyFrame ("JP Demo");
					jFrame.setName ("Matrix");
					jFrame.setSize (1024, 768);

					table.getColumnHeader().setRowHeight (64);
					final JScrollPane pane = new JScrollPane (table);

					jFrame.getContentPane().add (pane); /*jsp*/
					jFrame.setVisible (true);
					
					
					final JFrame jFrame2 = new MyFrame ("JGraph Demo");
					jFrame2.setName ("NLGraph");
					jFrame2.setSize (1024, 768);
					
					jFrame2.getContentPane().add (jgraph); /*jsp*/
					jFrame2.setVisible (true);
					//jsp.setDividerLocation (0.4f);
					//jsp3.setDividerLocation (0.2f);
				}
			}
		);
	}
	
	
	public void addData (final GraphModel graph, final String zipFileName) {
		addAssessmentData (zipFileName);
	}
	
	
	public final void addAssessmentData (final String zipFileName) {

		InputStream iStream = null;
		try {
			iStream = DataPrep.getInstance().getPossiblyZippedInputStream (zipFileName, false);
		} catch (FileNotFoundException fnfe) {
			LOGGER.error ("Error", fnfe);
		}

		final ZipInputStream zis = (ZipInputStream) iStream;
    	final List<String> evaluationTypes = new ArrayList<String> ();
    	final List<JudgementTypeInterface> judgementTypes = new ArrayList<JudgementTypeInterface> ();
		final Map<String, Object> nodes = new HashMap<String, Object> ();
    	ZipEntry entry;

		int zipIndex = 0;
		Document doc = null;

		try {
		//while (enumer.hasMoreElements() && a < 2500) {
		while ((entry = zis.getNextEntry()) != null) {
			//entry = (ZipEntry) enumer.nextElement();

			if (!entry.isDirectory() && Math.random() > SAMPLE_THRESHOLD) {
				//final String name = entry.getName();

				if (zipIndex % 100 == 0 || entry.toString().equals("eval1000.xml")) {
					LOGGER.info (entry.getSize()+"\t"+entry.toString());
				}

				final String zip = DataPrep.getInstance().readZipEntry (zis, entry);
				/*
				JOptionPane.showMessageDialog (null,
    			zip,
    			"zip entry "+a,
    			JOptionPane.INFORMATION_MESSAGE);
    			*/
				doc = XMLParse.generateDOMFromString (zip);
				final Element topElement = EvaluationDOM.getEvaluationElement (doc);
				final String rootSkillName = EvaluationDOM.getRootSkillName (topElement);
				if (!evaluationTypes.contains (rootSkillName)) {
					evaluationTypes.add (rootSkillName);
					final JudgementTypeInterface newjti = new AbstractJudgementType (topElement);
					judgementTypes.add (newjti);
				}
				
				final int typeIndex = evaluationTypes.indexOf (rootSkillName);
				final JudgementTypeInterface jti = judgementTypes.get (typeIndex);
				final String assessorID = topElement.getAttribute ("AssessorID");
				final String candidateID = topElement.getAttribute ("CandidateID");

				final IndividualJudgement iJudgement = new IndividualJudgement (topElement, jti);
		  		final Edge edge = new EdgeInstance (getNode (assessorID, nodes), getNode (candidateID, nodes), iJudgement);
				graph.addEdge (edge);
		  		zipIndex++;
			}
		}
			}
		catch (final IOException ioe) {
			LOGGER.error (ioe.toString(), ioe);
		}

		try {
			zis.close();
		}
		catch (final IOException ioe) {
			LOGGER.error (ioe.toString(), ioe);
		}
		
		LOGGER.info ("Included "+zipIndex+" assessments");
		addRandomImages (graph);
		addRandomShapes (graph);
		
		//removeRandomNodes (graph);
	}
	
	protected void removeRandomNodes (final GraphModel model) {
		final List<Object> nodeList = new ArrayList<Object> (model.getNodes ());
		model.removeNode (nodeList.get(0));
		model.removeNode (nodeList.get(1));
	}
	
	protected final void addRandomImages (final GraphModel model) {
		final File dir = new File (Messages.getString (GraphicsUtil.GRAPHICPROPS, "ExamplePhotoDir"));
		final File[] files = dir.listFiles (new PhotoFilter ());
		
		if (files != null) {		
			final List<Object> nodeList = new ArrayList<Object> (model.getNodes ());
			
			for (int n = 0; n < Math.min (files.length, 15); n++) {
				BufferedImage bufImage = null;
				LOGGER.info ("reading file "+n+": "+files[n].toString());
				if (files[n] != null) {
					try {
						bufImage = ImageIO.read (files[n]);
					}
					catch (final IOException ioe){
						LOGGER.error (ioe.toString(), ioe);
					}
				}
	
				if (bufImage != null) {
					final Object node1 = nodeList.get ((int)(Math.random() * nodeList.size()));
					final Object node2 = nodeList.get ((int)(Math.random() * nodeList.size()));	
					model.addEdge (node1, node2, bufImage);
				}
			}
		}
	}
	
	
	protected final void addRandomShapes (final GraphModel model) {
		final List<Object> nodeList = new ArrayList<Object> (model.getNodes ());
		
		for (int n = 0; n < 100; n++) {
			final Object node1 = nodeList.get ((int)(Math.random() * nodeList.size()));
			final Object node2 = nodeList.get ((int)(Math.random() * nodeList.size()));
			final Shape shape = Math.random() < 0.5 ? new Rectangle2D.Double (0, 0, 100, 20 + (100 * Math.random()))
						: new Ellipse2D.Double (0, 0, 100, 20 + (100 * Math.random()));
			model.addEdge (node1, node2, shape);
		}
	}
	
	
	protected final Object getNode (final String nodeName, final Map<String, Object> nodes) {
		Object node = nodes.get (nodeName);
  		if (node == null) {
  			node = new String (nodeName);
  			nodes.put (nodeName, node);
  		}
  		return node;
	}
	

	
	
	static class PhotoFilter implements FilenameFilter {

	    //Accept jpg files.
	    @Override
		public boolean accept (final File file, final String name) {
	    	return name.endsWith (".JPG") || name.endsWith(".jpg");
	    }
	}
}
