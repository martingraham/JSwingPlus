package example.multiview;

import io.db.Connect;
import io.json.JSONStructureMaker;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import model.graph.Edge;
import model.graph.GraphFilter;
import model.graph.GraphModel;
import model.matrix.DefaultMatrixTableModel;
import model.matrix.MatrixTableModel;
import model.shared.selection.LinkedGraphMatrixSelectionModelBridge;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import swingPlus.graph.GraphCellRenderer;
import swingPlus.graph.JGraph;
import swingPlus.graph.force.impl.BarnesHut2DForceCalculator;
import swingPlus.graph.force.impl.EdgeWeightedAttractor;
import swingPlus.matrix.JHeaderRenderer;
import swingPlus.matrix.JMatrix;
import swingPlus.parcoord.JParCoord;
import swingPlus.shared.MyFrame;
import util.Messages;
import util.ui.NewMetalTheme;
import util.ui.VerticalLabelUI;
import example.graph.renderers.node.NodeDegreeGraphCellRenderer;
import example.multiview.renderers.edge.EdgeCountFatEdgeRenderer;
import example.multiview.renderers.matrix.JSONObjHeaderRenderer;
import example.multiview.renderers.matrix.NumberShadeRenderer;
import example.multiview.renderers.node.JSONNodeTypeGraphRenderer;
import example.multiview.renderers.node.JSONTooltipGraphCellRenderer;


public class JMultiViewJSON {


	static final Logger LOGGER = Logger.getLogger (JMultiViewJSON.class);
	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		//final MetalLookAndFeel lf = new MetalLookAndFeel();
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		if (args.length > 0) {
			new JMultiViewJSON (args[0]);
		}
	}


	public JMultiViewJSON (final String fileName) {	
		
		GraphModel graph = null;	
		TableModel tableModel = null;
		MatrixTableModel matrixModel = null;
		Map<JsonNode, String> nodeTypeMap = null;
		
		try {
			final ObjectMapper objMapper = new ObjectMapper ();
			final JsonNode rootNode = objMapper.readValue (new File (fileName), JsonNode.class);
			LOGGER.info ("rootnode: "+rootNode);
			
			final JSONStructureMaker structureMaker = new JSONStructureMaker (rootNode);
			//graph = structureMaker.makeGraph (new String[] {"people"}, new String[] {"publications", "grants"});
			//graph = structureMaker.makeGraph (new String[] {"grants"}, new String[] {"publications", "people"});
			//graph = structureMaker.makeGraph (new String[] {"publications", "people", "grants"}, new String[] {"people"});	
			graph = structureMaker.makeGraph (new String[] {"publications", "people"}, new String[] {"people"});	
			
			//tableModel = structureMaker.makeTable ("publications");
			tableModel = structureMaker.makeTable ("people");
			
			
			matrixModel = new DefaultMatrixTableModel (graph);
			
			
			nodeTypeMap = structureMaker.makeNodeTypeMap (new String[] {"publications", "people", "grants"});
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		final JGraph jgraph = new JGraph (graph);
		final EdgeWeightedAttractor edgeWeighter = new EdgeWeightedAttractor ();
		jgraph.setAttractiveForceCalculator (edgeWeighter);
		jgraph.setShowEdges (true);
		
		final GraphCellRenderer jsonGraphRenderer = new JSONNodeTypeGraphRenderer (nodeTypeMap);
		jgraph.setDefaultNodeRenderer (String.class, new NodeDegreeGraphCellRenderer ());
		jgraph.setDefaultNodeRenderer (JsonNode.class, jsonGraphRenderer);
		jgraph.setDefaultNodeRenderer (ObjectNode.class, jsonGraphRenderer);
		jgraph.setDefaultEdgeRenderer (Integer.class, new EdgeCountFatEdgeRenderer ());
		jgraph.setDefaultNodeToolTipRenderer (ObjectNode.class, new JSONTooltipGraphCellRenderer ());
		
		
		
		final JMatrix jmatrix = new JMatrix ((TableModel) matrixModel);
		final JHeaderRenderer stringHeader = new JSONObjHeaderRenderer ();
		final JHeaderRenderer stringHeader2 = new JSONObjHeaderRenderer ();
		jmatrix.getRowHeader().setDefaultRenderer (Object.class, stringHeader);
		jmatrix.getRowHeader().setDefaultRenderer (String.class, stringHeader);
		jmatrix.getColumnHeader().setDefaultRenderer (Object.class, stringHeader2);
		jmatrix.getColumnHeader().setDefaultRenderer (String.class, stringHeader2);
		((JLabel)stringHeader2).setUI (new VerticalLabelUI (false));
		
		stringHeader.setSelectionBackground (jmatrix.getRowHeader());
		stringHeader2.setSelectionBackground (jmatrix.getColumnHeader());
		//jmatrix.setDefaultRenderer (HashSet.class, stringHeader);
		jmatrix.setDefaultRenderer (String.class, stringHeader);
		jmatrix.setDefaultRenderer (Integer.class, new NumberShadeRenderer ());
		
		
		
		
		final JTable table = new JParCoord (tableModel);
		table.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed (true);
		table.setAutoCreateRowSorter (true);
		table.setColumnSelectionAllowed (true);
		table.setForeground (Color.lightGray);
		table.setSelectionForeground (Color.orange);
		if (table instanceof JParCoord) {
			((JParCoord)table).setBrushForegroundColour (Color.gray);
			((JParCoord)table).setBrushSelectionColour (Color.red);
			((JParCoord)table).setSelectedStroke (new BasicStroke (2.0f));
			//((JParCoord)table).setBrushing (true);
		}
		table.setGridColor (Color.gray);
		table.setShowVerticalLines (false);
		table.setBorder (BorderFactory.createEmptyBorder (24, 2, 24, 2));
		if (table.getRowSorter() instanceof TableRowSorter) {
			final TableRowSorter<? extends TableModel> trs = (TableRowSorter<? extends TableModel>)table.getRowSorter();
		}
		table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);

		
		jgraph.setPreferredSize (new Dimension (768, 640));
		table.setPreferredSize (new Dimension (768, 384));
		table.setMinimumSize (new Dimension (256, 128));
		

		
		final LinkedGraphMatrixSelectionModelBridge selectionBridge = new LinkedGraphMatrixSelectionModelBridge ();
		selectionBridge.addJGraph (jgraph);
		selectionBridge.addJTable (table);
		selectionBridge.addJTable (jmatrix);
		
		
		SwingUtilities.invokeLater (
			new Runnable () {
				
				@Override
				public void run() {						
					
					final JFrame jf2 = new MyFrame ("JGraph Demo");
					jf2.setSize (1024, 768);
					
					
					
					final JPanel optionPanel = new JPanel ();
					optionPanel.setLayout (new BoxLayout (optionPanel, BoxLayout.Y_AXIS));
					
					final JSlider llengthSlider = new JSlider (20, 1000, (int)edgeWeighter.getLinkLength());
					llengthSlider.addChangeListener(
						new ChangeListener () {
							@Override
							public void stateChanged (final ChangeEvent cEvent) {
								edgeWeighter.setLinkLength (llengthSlider.getValue());
							}
						}
					);
					
					final JSlider lstiffSlider = new JSlider (20, 1000, edgeWeighter.getStiffness());
					lstiffSlider.addChangeListener(
						new ChangeListener () {
							@Override
							public void stateChanged (final ChangeEvent cEvent) {
								edgeWeighter.setStiffness (lstiffSlider.getValue());
							}
						}
					);
					
					
					final JSlider repulseSlider = new JSlider (1, 50, 10);
					repulseSlider.addChangeListener(
						new ChangeListener () {
							@Override
							public void stateChanged (final ChangeEvent cEvent) {
								((BarnesHut2DForceCalculator)jgraph.getRepulsiveForceCalculator()).setAttenuator (3.0 / repulseSlider.getValue());
							}
						}
					);
					
					
					final JCheckBox showSingletons = new JCheckBox ("Show singletons", true);
					showSingletons.addActionListener (
						new ActionListener () {
							@Override
							public void actionPerformed (final ActionEvent e) {
								final Object source = e.getSource();
								if (source instanceof JCheckBox) {
									final boolean selected = ((JCheckBox)source).isSelected();
									
									final GraphFilter singletonFilter = new GraphFilter () {

										@Override
										public boolean includeNode (final Object obj) {
											return jgraph.getModel().getEdges(obj).size() > 0 || selected;
										}

										@Override
										public boolean includeEdge (final Edge edge) {
											return true;
										}
									};
									
									jgraph.setGraphFilter (singletonFilter);
								}

							}
						}
					);
					
					final JButton clearSelections = new JButton ("Clear Selections");
					clearSelections.addActionListener (
						new ActionListener () {
							@Override
							public void actionPerformed (ActionEvent e) {
								jgraph.getSelectionModel().clearSelection ();
							}
						}
					);
					
					
					final JButton graphFreezer = new JButton ("Freeze Graph");
					graphFreezer.addActionListener (
						new ActionListener () {
							@Override
							public void actionPerformed (ActionEvent e) {
								jgraph.pauseWorker();
							}
						}
					);

					
					optionPanel.add (new JLabel ("Link Length:"));
					optionPanel.add (llengthSlider);
					optionPanel.add (new JLabel ("Link Stiffness:"));
					optionPanel.add (lstiffSlider);
					optionPanel.add (new JLabel ("Repulse Strength:"));
					optionPanel.add (repulseSlider);
					optionPanel.add (showSingletons);
					optionPanel.add (clearSelections);
					optionPanel.add (graphFreezer);
					
					JScrollPane tableScrollPane = new JScrollPane (table);
					JScrollPane matrixScrollPane = new JScrollPane (jmatrix);
					JTabbedPane jtp = new JTabbedPane ();
					jtp.addTab ("Node-Link", jgraph);
					jtp.addTab ("Matrix", matrixScrollPane);

					jf2.getContentPane().add (optionPanel, BorderLayout.EAST);
					jf2.getContentPane().add (jtp, BorderLayout.CENTER);
					jf2.getContentPane().add (tableScrollPane, BorderLayout.SOUTH);
					
					jf2.setVisible (true);
				}
			}
		);
		
	}
}
