package example.multitree;

import io.AbstractStAXCursorParse;
import io.DataPrep;
import swingPlus.shared.MyFrame;
import ui.RadialTreeUI;
import ui.TreeMapUI;
import util.Messages;
import util.ui.NewMetalTheme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.io.MyXMLStAXCursorParse;

import model.multitree.impl.AbstractCrossTreeSelectionModel;
import model.multitree.impl.DefaultCrossTreeSelectionModel;
import model.multitree.impl.DefaultCrossTreeSelectionModel2;


public class JMultiTreeDemo {

	private final static Logger LOGGER = Logger.getLogger (JMultiTreeDemo.class);
	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {
		
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));

		new JMultiTreeDemo (args.length > 0 ? args[0] : Messages.getString ("example.multitree.exampleMultiTree", "dataFile"));
	}

	
	JMultiTreeDemo (final String dataFileName) {
		
		final List<TreeModel> multiTreeModel = new ArrayList<TreeModel> ();
		final AbstractStAXCursorParse xmlParser = new MyXMLStAXCursorParse (multiTreeModel);
		LOGGER.info ("dataFileName: "+dataFileName);
		try {
			final InputStream iStream = DataPrep.getInstance().getPossiblyZippedInputStream (dataFileName, true);
			xmlParser.parse (iStream);
		} catch (Exception e) {
			LOGGER.error ("Error", e);
			System.exit (0);
		}
		
		SwingUtilities.invokeLater (
			new Runnable () {
					
				@Override
				public void run() {
					makeInterface (multiTreeModel);
				}
			}
		);
	}
	
	
	void makeInterface (final List<TreeModel> multiTreeModel) {
		
		final JFrame jFrame = new MyFrame ("Multiple Tree Demo");
		jFrame.setSize (1024, 768);
		
		final TreeSelectionModel dcsm = new DefaultCrossTreeSelectionModel2 ();
		
		final TreeSelectionListener tsl = new TreeSelectionListener () {
			@Override
			public void valueChanged (final TreeSelectionEvent tsEvent) {
				jFrame.repaint ();
			}
		};
		dcsm.addTreeSelectionListener (tsl);
		
		final DefaultTreeCellRenderer exampleRenderer = new DefaultTreeCellRenderer () {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			final Font font = Font.decode("Gill Sans MT-bold-12");
			final Font font2 = Font.decode("Gill Sans MT-plain-12");
			
		    @Override
			public Component getTreeCellRendererComponent (final JTree tree, final Object value,
					final boolean sel,
					final boolean expanded,
					final boolean leaf, final int row,
					final boolean hasFocus) {
		    	final Component comp = super.getTreeCellRendererComponent (tree, value, sel, expanded, leaf, row, hasFocus);
		    	if (value instanceof DefaultMutableTreeNode) {
		    		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)value;
		    		if (tree.getSelectionModel().isPathSelected (new TreePath (dmtn))) {
		    			comp.setForeground (Color.red);
		    			comp.setFont (font);
		    		} else {
		    			comp.setForeground (Color.black);
		    			comp.setFont (font2);
		    		}
		    	}
		    	return comp;
		    }
		};
		
		final int size = multiTreeModel.size();
		final int xgrid = (int)Math.ceil (Math.sqrt(size) * Math.sqrt (1024.0 / 768.0));
		final int ygrid = (int)Math.ceil ((double)size / (double)xgrid);
		final JPanel jPanel = new JPanel (new GridLayout (xgrid, ygrid));
		jFrame.getContentPane().add (jPanel, BorderLayout.CENTER);
		final Border border = BorderFactory.createBevelBorder (BevelBorder.RAISED);	
		for (TreeModel tModel : multiTreeModel) {
			final JTree treeWidget = new JTree (tModel);
			treeWidget.setFont (Font.decode ("Gill Sans MT-plain-12"));
			treeWidget.setSelectionModel (dcsm);
			//treeWidget.addTreeSelectionListener (treeListener);
			treeWidget.setCellRenderer (exampleRenderer);
			treeWidget.setRootVisible (true);
			treeWidget.setShowsRootHandles (false);
			treeWidget.setLargeModel (true);
			treeWidget.setBorder (border);
			treeWidget.setEditable (true);
			final JScrollPane jsp = new JScrollPane (treeWidget);
			jPanel.add (jsp);
			//BasicTreeUI;
			//DefaultTreeSelectionModel
			final double rand = Math.random();
			if (rand > 0.66) {
				UIManager.put (treeWidget, TreeMapUI.createUI (treeWidget));
				treeWidget.setUI (TreeMapUI.createUI (treeWidget));
				treeWidget.invalidate();
			}
			else if (rand < 0.33) {
				UIManager.put (treeWidget, RadialTreeUI.createUI (treeWidget));
				treeWidget.setUI (RadialTreeUI.createUI (treeWidget));
				treeWidget.invalidate();
			}
		}
		//SwingUtilities.updateComponentTreeUI (frame.getContentPane());

		jFrame.setVisible (true);
	}
}
