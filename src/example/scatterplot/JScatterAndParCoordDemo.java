package example.scatterplot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.text.Format;
import java.util.List;

import io.LaunchFileDialog;
import io.parcoord.MakeTableModel;

import swingPlus.parcoord.FormatterRenderer;
import swingPlus.parcoord.JColumnList2;
import swingPlus.parcoord.JParCoord;
import swingPlus.parcoord.JTableLabel;
import swingPlus.parcoord.PolylineTypeBox;
import swingPlus.scatterplot.JScatterPlot;
import swingPlus.scatterplot.JScatterPlotMatrix;
import swingPlus.scatterplot.JScatterPlotPanel;
import swingPlus.scatterplot.MatrixSingleLinkMouseListener;
import swingPlus.shared.MyFrame;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.ListSelectionModel;

import model.shared.SortedTableColumn;
import model.shared.selection.LinkedTableRowSelectionModel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import util.Messages;
import util.swing.TabbedMoveHandler;
import util.ui.NewMetalTheme;


public class JScatterAndParCoordDemo {

	transient JFrame frame;
	static final private Logger LOGGER = Logger.getLogger (JScatterAndParCoordDemo.class);
	
	int filterIndex = 0;
	
	public static void main (final String[] args) {
		
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		initialiseLogging (args);
		
		new LaunchFileDialog (JScatterAndParCoordDemo.class);
	}
	
	
	public JScatterAndParCoordDemo (final String dataFileName) {
		
		JFrame.setDefaultLookAndFeelDecorated (true);
		
		long nano = System.nanoTime();

		final MakeTableModel mtm = new MakeTableModel ();
		final DefaultTableModel dtm = mtm.buildDataModel (dataFileName);
		
		//MakeTableModel mtm = new MakeTableModel ();
		//DefaultTableModel dtm = mtm.buildDataModel ("connectMSSQL");
		final List<Format> columnTextFormatters = mtm.getColumnTextFormatters();
	

		
		final JTable table = new JParCoord (dtm);
		table.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed (true);
		table.setAutoCreateRowSorter (true);
		table.setColumnSelectionAllowed (true);
		table.setForeground (new Color (160, 160, 160));
		table.setSelectionForeground (Color.blue);
		table.setBorder (BorderFactory.createEmptyBorder (10, 2, 10, 2));
		table.setGridColor (Color.darkGray);
		//jt.setRowSorter (new TableRowSorter<DefaultTableModel> (dtm));
		table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
	

		
		final JTable jscpm = new JScatterPlotMatrix (dtm, new DefaultTableColumnModel ());
		((JParCoord)jscpm).createColumnsFromPreSortedSource (table.getColumnModel());
		jscpm.setBackground (new Color (248, 248, 248));
		jscpm.setForeground (table.getForeground());
		jscpm.setSelectionForeground (Color.blue);
		jscpm.setRowSelectionAllowed (false);
		jscpm.setAutoCreateRowSorter (true);

		final JScatterPlot jscp = new JScatterPlot (jscpm);
		jscp.setxAxis ((SortedTableColumn<?>)table.getColumnModel().getColumn(0));
		jscp.setyAxis ((SortedTableColumn<?>)table.getColumnModel().getColumn(1));
		jscp.setBackground (new Color (248, 248, 248));
		
		jscpm.addMouseListener (
			new MatrixSingleLinkMouseListener (jscp)
			//new MatrixSingleLinkMouseListener ()
		);
		
		
		
		final JColumnList2 jcl = new JColumnList2 ();
		jcl.addTable (table);
		jcl.getColumnModel().getColumn(1).setWidth (30);
		jcl.addTable (jscpm);
		
		final LinkedTableRowSelectionModel ltrsm = new LinkedTableRowSelectionModel ();
		ltrsm.addJTable (table);
		ltrsm.addJTable (jscpm);
		
		if (columnTextFormatters != null && !columnTextFormatters.isEmpty()) {
			final JTable[] tables = {table, jscpm};
			
			for (int modelColumn = 0; modelColumn < columnTextFormatters.size(); modelColumn++) {
				final Format format = columnTextFormatters.get (modelColumn);
				
				if (format != null) {
					final FormatterRenderer fRender = new FormatterRenderer (format);
					
					for (JTable jtable : tables) {
						final int viewColumn = jtable.convertColumnIndexToView (modelColumn);
						if (viewColumn >= 0) {
							jtable.getColumnModel().getColumn(viewColumn).setCellRenderer (fRender);
						}
					}
				}
			}
		}
		
		/*
		final CollectiveTableRowSelectionModel2 ctrsm = new CollectiveTableRowSelectionModel2 ();	
		if (table instanceof JParCoord) {
			ctrsm.addJTable (table);
			((JParCoord)table).setUberSelection (ctrsm.getUberSelectionModel());
		}
		if (jscpm instanceof JParCoord) {
			ctrsm.addJTable (jscpm);
			((JParCoord)jscpm).setUberSelection (ctrsm.getUberSelectionModel());
		}
		*/
		
		
		
		nano = System.nanoTime() - nano;
		LOGGER.debug ("Initialisation of data models and vis components: "+(nano/1E6)+" ms.");
		
		

		
		final PolylineTypeBox ptb = new PolylineTypeBox ();
		ptb.addToJPCList ((JParCoord)table);
		
		SwingUtilities.invokeLater (
			new Runnable () {
				@Override
				public void run() {
					frame = new MyFrame ("Filter Change Test");
					frame.setSize (1024, 768);
					
					final JTabbedPane jtp1 = new JTabbedPane ();
					final JTabbedPane jtp2 = new JTabbedPane ();
					final JTabbedPane jtp3 = new JTabbedPane ();
					final JTabbedPane jtp4 = new JTabbedPane ();
					
					final JScrollPane jscp1 = new JScrollPane ();
					final JScrollPane jscp2 = new JScrollPane ();
					final JScrollPane jscp3 = new JScrollPane ();
					final JScrollPane jscp4 = new JScrollPane ();
					
					jscp1.setViewportView (jscpm);
					//jscp2.setViewportView (jt2);
					//jscp2.setViewportView (new JScatterPlotPanel (jscp));
					jscp3.setViewportView (jcl);
					jscp4.setViewportView (table);
					
					//JPanel jp = new JPanel ();
					//jp.setLayout(new BorderLayout ());
					//jp.add (table, "Center");
					//jp.add (table.getTableHeader(), "North");
					
					
					jtp1.addTab ("Scatterplot Matrix", jscp1);
					jtp2.addTab ("Scatterplot", new JScatterPlotPanel (jscp));
					jtp3.addTab ("Column Chooser", jscp3);
					jtp4.addTab ("Parallel Coordinates", jscp4 /*jp*/ /*jscp4*/);
					
					
					final TabbedMoveHandler dragHandler = new TabbedMoveHandler (frame);
					final JTabbedPane[] tabArray = {jtp1, jtp2, jtp3, jtp4};
					for (JTabbedPane jtp : tabArray) {
						dragHandler.addToTabbedPane (jtp);
					}
					
					final JSplitPane jsp2 = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, jtp1, jtp2);
					final JSplitPane jsp3 = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, jtp3, jtp4);
					final JSplitPane jsp = new JSplitPane (JSplitPane.VERTICAL_SPLIT, jsp2, jsp3);

			
					final JPanel additionalInfoPanel = new JPanel (new GridLayout (2, 0));	
					additionalInfoPanel.add (new JTableLabel (table));
					additionalInfoPanel.add (ptb);
					
					frame.getContentPane().add (jsp);
					frame.getContentPane().add (additionalInfoPanel, BorderLayout.SOUTH);
					frame.setVisible (true);
				}
			}
		);
	}
	
	
	 
	static void initialiseLogging (final String args[]) {
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		LOGGER.info ("Logger activated");
	}
	

	

	
	static class OddRowFilter extends RowFilter<TableModel, Integer> {
		@Override
		public boolean include (final Entry<? extends TableModel, ? extends Integer> entry) {
			final int rowIndex = ((Integer)entry.getIdentifier()).intValue();
			return (rowIndex & 1) == 0;
		}
	}
	
	static class EvenRowFilter extends RowFilter<TableModel, Integer> {
		@Override
		public boolean include (final Entry<? extends TableModel, ? extends Integer> entry) {
			final int rowIndex = ((Integer)entry.getIdentifier()).intValue();
			return (rowIndex & 1) == 1;
		}
	}
	
	static class NoRowFilter extends RowFilter<TableModel, Integer> {
		@Override
		public boolean include (final Entry<? extends TableModel, ? extends Integer> entry) {
			return true;
		}
	}
}
