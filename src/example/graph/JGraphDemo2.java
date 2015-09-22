package example.graph;

import io.AbstractStAXCursorParse;
import io.DataPrep;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;


import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.graph.renderers.edge.MultiTreeGraphEdgeRenderer;
import example.graph.renderers.node.NodeDegreeGraphCellRenderer;
import example.graph.renderers.node.SingleImageGraphCellRenderer;
import example.graph.renderers.node.StringGraphCellRenderer;
import example.graph.renderers.node.StringGraphCellRenderer2;
import example.graph.renderers.node.TaxonBallGraphCellRenderer;
import example.io.MyXMLStAXCursorParse;


import model.graph.Edge;
import model.graph.GraphModel;
import model.graph.impl.DefaultGraphFilter;
import model.graph.impl.SymmetricGraphInstance;

import util.Messages;
import util.colour.ColorUtilities;
import util.ui.NewMetalTheme;

import swingPlus.graph.JGraph;
import swingPlus.shared.MyFrame;



public class JGraphDemo2 {

	private final static Logger LOGGER = Logger.getLogger (JGraphDemo2.class);
	
	protected transient GraphModel graph;

	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		JFrame.setDefaultLookAndFeelDecorated (true);
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		if (args.length > 0) {
			new JGraphDemo2 (args[0]);
		}
	}

	public JGraphDemo2 (final String zipFileName) {	
		
		final List<TreeModel> multiTreeModel = new ArrayList<TreeModel> ();
		final AbstractStAXCursorParse xmlParser = new MyXMLStAXCursorParse (multiTreeModel);
		LOGGER.info ("dataFileName: "+zipFileName);
		try {
			final InputStream iStream = DataPrep.getInstance().getPossiblyZippedInputStream (zipFileName, true);
			xmlParser.parse (iStream);
		} catch (final Exception excep) {
			LOGGER.error ("Error", excep);
			System.exit (0);
		}
		
		graph = new SymmetricGraphInstance ();
		convertMultiTreeToGraph (multiTreeModel, graph);
		
		final BitSet filterSet = new BitSet ();
		filterSet.set (1);
		filterSet.set (5);
		filterSet.set (4);
		
				
		final JGraph jgraph = new JGraph (graph);
		jgraph.setShowEdges (true);
		final MultiTreeGraphEdgeRenderer mtgeRenderer = new MultiTreeGraphEdgeRenderer (multiTreeModel.size());
		jgraph.setDefaultNodeRenderer (example.multitree.TaxonUserObject.class, new StringGraphCellRenderer2 ());
		//jgraph.setDefaultNodeRenderer (example.multitree.TaxonUserObject.class, new SingleImageGraphCellRenderer ("CubeIcon"));
		//jgraph.setDefaultNodeRenderer (example.multitree.TaxonUserObject.class, new NodeDegreeGraphCellRenderer (8.0f));
		jgraph.setDefaultEdgeRenderer (Integer.class, mtgeRenderer);
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
					final Collection<Edge> edges = jgraph.getModel().getEdges (obj);
					for (Edge edge : edges) {
						if (includeEdge (edge)) {
							return true;
						}
					}
					return false;
				}
			}
		);
		//for (EdgeDirection ed : EdgeDirection.values()) {
		//	jgraph.setDefaultEdgeRenderer (ed.getClass(), new DefaultGraphEdgeRenderer ());
		//}
		

		
		SwingUtilities.invokeLater (
				new Runnable () {
					
					final Border selectedBorder = BorderFactory.createLineBorder (Color.black, 2);
					final Border unselectedBorder = BorderFactory.createLineBorder (Color.gray, 2);
					
					@Override
					public void run() {						
						
						final JFrame jf2 = new MyFrame ("JGraph Demo");
						jf2.setSize (1024, 768);
						
						final JPanel treeButtonPanel = new JPanel (new GridLayout (1, multiTreeModel.size()));
						for (int tree = 0; tree < multiTreeModel.size(); tree++) {
							final JButton treeButton = new JButton (Integer.toString (tree));
							treeButtonPanel.add (treeButton);
							colourButton (treeButton, tree);

							final int treeIndex = tree;
							treeButton.addActionListener (
								new ActionListener () {
									@Override
									public void actionPerformed (final ActionEvent event) {
										filterSet.flip (treeIndex);
										colourButton (treeButton, treeIndex);
										
										jgraph.updateFilteredModel ();
										jgraph.restartWorker ();
										LOGGER.debug ("Filterset: "+filterSet);
									}			
								}
							);
						}
						
						//JZoomPane zoomPane = new JZoomPane ();
						jgraph.setOpaque (false);
						//zoomPane.setView (jgraph);
						jf2.getContentPane().add (jgraph); /*jsp*/
						jf2.getContentPane().add (treeButtonPanel, "South");
						jf2.setVisible (true);
						//jsp.setDividerLocation (0.4f);
						//jsp3.setDividerLocation (0.2f);
					}
					
					final void colourButton (final JButton treeButton, final int tree) {
						final Color treeColour = mtgeRenderer.getColourAtIndex (tree);
						final Color treeButtonBackground = filterSet.get (tree) ? treeColour : ColorUtilities.mixColours (Color.lightGray, treeColour, 0.9f);
						treeButton.setBackground (treeButtonBackground);
						treeButton.setBorder (filterSet.get(tree) ? selectedBorder : unselectedBorder);
					}
				}
			);
	}
	

	
	
	protected final void convertMultiTreeToGraph (final List<TreeModel> multiTreeModel, final GraphModel graph) {

		for (int tree = 0; tree < multiTreeModel.size(); tree++) {
			final TreeModel treeModel = multiTreeModel.get (tree);
			final Object root = treeModel.getRoot ();
			if (root instanceof DefaultMutableTreeNode) {
				convertRecurse ((DefaultMutableTreeNode)root, tree, graph);
			}
		}
	}
	
	protected final void convertRecurse (final DefaultMutableTreeNode treeNode, final int treeIndex,
			final GraphModel graph) {
		graph.addNode (treeNode.getUserObject());
		if (treeNode.getParent() != null) {
			graph.addEdge (((DefaultMutableTreeNode)treeNode.getParent()).getUserObject(),
					treeNode.getUserObject(), Integer.valueOf (treeIndex));
		}
		for (int childIndex = treeNode.getChildCount(); --childIndex >= 0;) {
			final TreeNode childNode = treeNode.getChildAt (childIndex);
			if (childNode instanceof DefaultMutableTreeNode) {
				convertRecurse ((DefaultMutableTreeNode)childNode, treeIndex, graph);
			}
		}
	}
}
