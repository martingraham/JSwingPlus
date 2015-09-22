package example.matrix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import model.graph.GraphModel;
import model.graph.impl.DirectedGraphInstance;
import model.graph.impl.SymmetricGraphInstance;
import model.matrix.DefaultMatrixTableModel;

import util.Messages;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.matrix.renderers.BooleanRenderer;
import example.matrix.renderers.StringHeaderRenderer;

import swingPlus.matrix.JMatrix;
import swingPlus.matrix.JHeaderRenderer;
import swingPlus.matrix.CollectionRenderer;
import swingPlus.shared.MyFrame;
import util.colour.ColorUtilities;
import util.ui.NewMetalTheme;
import util.ui.VerticalLabelUI;


public class JMatrixDemo3 {

	private final static Logger LOGGER = Logger.getLogger (JMatrixDemo3.class);
	

	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		//final MetalLookAndFeel lf = new MetalLookAndFeel();
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		new JMatrixDemo3 ();
	}

	public JMatrixDemo3 () {	
		final JMatrix table2, table3;
		
		final GraphModel graph2 = new DirectedGraphInstance ();
		final GraphModel graph3 = new SymmetricGraphInstance ();
		
		final GraphModel[] graphModels = {graph2, graph3};
		for (GraphModel graphModel : graphModels) {
			makeGraph (graphModel);
		}
		
		JFrame.setDefaultLookAndFeelDecorated (true);
	
		
	
		final TableModel mtm2 = new DefaultMatrixTableModel (graph2);
		table2 = new JMatrix (mtm2);
		
		final TableModel mtm3 = new DefaultMatrixTableModel (graph3);
		table3 = new JMatrix (mtm3);
			
		final JMatrix[] matrices = {table2, table3};	
			
		for (JMatrix matrix : matrices) {
			for (int i = 0; i < matrix.getColumnCount(); i++) {
			    final TableColumn column = matrix.getColumnModel().getColumn(i);
			    column.setMinWidth (1); 
			    column.setMaxWidth (1000); 
			    column.setPreferredWidth (matrix.getRowHeight()); 
			}
	

			matrix.setDefaultRenderer (Boolean.class, new BooleanRenderer ());
			matrix.setDefaultRenderer (HashSet.class, new CollectionRenderer ());
	
			matrix.setGridColor (ColorUtilities.mixColours (matrix.getBackground(), Color.black, 0.8f));
			//matrix.setShowGrid (false);
			//matrix.setIntercellSpacing (new Dimension (0, 0));
			
			final JHeaderRenderer stringHeader = new StringHeaderRenderer ();
			final JHeaderRenderer stringHeader2 = new StringHeaderRenderer ();
			matrix.getRowHeader().setDefaultRenderer (Object.class, stringHeader);
			matrix.getRowHeader().setDefaultRenderer (String.class, stringHeader);
			matrix.getColumnHeader().setDefaultRenderer (Object.class, stringHeader2);
			matrix.getColumnHeader().setDefaultRenderer (String.class, stringHeader2);
			((JLabel)stringHeader2).setUI (new VerticalLabelUI (false));
			
			stringHeader.setSelectionBackground (matrix.getRowHeader());
			stringHeader2.setSelectionBackground (matrix.getColumnHeader());
			matrix.setDefaultRenderer (String.class, stringHeader);
		}

		
		SwingUtilities.invokeLater (
			new Runnable () {
				
				@Override
				public void run() {
					final JFrame jFrame = new MyFrame ("JP Demo");
					jFrame.setName ("Matrices");
					jFrame.setSize (1024, 768);
					
					table2.getColumnHeader().setRowHeight (64);
					final JScrollPane pane2 = new JScrollPane (table2);
					
					table3.getColumnHeader().setRowHeight (64);
					final JScrollPane pane3 = new JScrollPane (table3);
					
					final String[] labels = {"Directed", "Symmetric"};
					final JPanel jp = new JPanel (new GridLayout (1, 3));
					for (String label : labels) {
						jp.add (new JLabel (label));
					}
					
					final JScrollPane[] panes = {pane2, pane3};
					final JPanel jp2 = new JPanel (new GridLayout (1, 3));
					for (JScrollPane pane : panes) {
						jp2.add (pane);
					}
					
					jFrame.getContentPane().add (jp2, BorderLayout.CENTER); /*jsp*/
					jFrame.getContentPane().add (jp, BorderLayout.SOUTH); /*jsp*/
					jFrame.setVisible (true);
				}
			}
		);
	}
	
	
	public final void makeGraph (final GraphModel graph) {
		for (int n = 0; n < 10; n++) {
			graph.addNode (Integer.valueOf (n));
		}
		
		final int[][] edgePairs = new int [][] {{2, 3}, {3, 2}, {6, 4}, {6, 5}, {9, 2}, {9, 1}, {0, 8}, {2, 7}, {2, 7}, {7, 2}};
		for (int n = 0; n < edgePairs.length; n++) {
			final int[] edgePair = edgePairs [n];
			graph.addEdge (Integer.valueOf (edgePair[0]), Integer.valueOf (edgePair[1]), Boolean.TRUE);
		}
	}
}
