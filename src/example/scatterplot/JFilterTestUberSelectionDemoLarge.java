package example.scatterplot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.ListSelectionModel;

import model.shared.SortedTableColumn;
import model.shared.selection.CollectiveTableRowSelectionModel2;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import util.Messages;
import util.swing.TabbedMoveHandler;
import util.ui.NewMetalTheme;


public class JFilterTestUberSelectionDemoLarge {

	transient JFrame frame;
	static final private Logger LOGGER = Logger.getLogger (JFilterTestUberSelectionDemoLarge.class);

	
	public static void main (final String[] args) {
		
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		LOGGER.info ("Logger activated");

		//new LaunchFileDialog (JFilterTestUberSelectionDemoLarge.class);
		new JFilterTestUberSelectionDemoLarge (args.length > 0 ? args[0] : Messages.getString ("ExampleDataFile"));
	}
	
	
	public JFilterTestUberSelectionDemoLarge (final String dataFileName) {
		
		JFrame.setDefaultLookAndFeelDecorated (true);
		
		//ThreadPauseForKey pauser = new ThreadPauseForKey ();
		//pauser.pauseForKeypress();
		
		long nano = System.nanoTime();

		final MakeTableModel mtm = new MakeTableModel ();
		final DefaultTableModel dtm = mtm.buildDataModel (dataFileName);
		
		//MakeTableModel mtm = new MakeTableModel ();
		//DefaultTableModel dtm = mtm.buildDataModel ("connectMSSQL");
		final List<Format> columnTextFormatters = mtm.getColumnTextFormatters();
		
		final RowFilter<TableModel, Integer> oddFilter = new OddRowFilter ();
		final RowFilter<TableModel, Integer> evenFilter = new EvenRowFilter ();
		final RowFilter<TableModel, Integer> noFilter = new NoRowFilter ();
		final List<RowFilter<TableModel, Integer>> filters = new ArrayList<RowFilter<TableModel, Integer>> ();
		filters.add (oddFilter);
		filters.add (noFilter);
		filters.add (evenFilter);

		
		final JTable table = new JParCoord (dtm);
		table.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed (true);
		table.setAutoCreateRowSorter (true);
		table.setColumnSelectionAllowed (true);
		table.setForeground (Color.gray);
		table.setSelectionForeground (Color.blue);
		table.setBorder (BorderFactory.createEmptyBorder (10, 2, 10, 2));
		table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
		
		
		
		//final JTable table2 = new JTable (dtm, new DefaultTableColumnModel ());
		final JTable table2 = new JTable (dtm);
		//((JParCoord)table2).createColumnsFromPreSortedSource (table.getColumnModel());
		table2.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table2.setRowSelectionAllowed (true);
		table2.setAutoCreateRowSorter (true);
		table2.setColumnSelectionAllowed (true);
		table2.setSelectionForeground (Color.blue);
		table2.setBorder (BorderFactory.createEmptyBorder (10, 2, 10, 2));
		table2.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);	
		

		
		//final JTable jscpm = new JScatterPlotMatrix (dtm, new DefaultTableColumnModel ());
		final JTable jscpm = new JScatterPlotMatrix (dtm);
		//((JParCoord)jscpm).createColumnsFromPreSortedSource (table.getColumnModel());
		jscpm.setBackground (new Color (248, 248, 248));
		jscpm.setForeground (table.getForeground());
		jscpm.setSelectionForeground (Color.blue);
		jscpm.setRowSelectionAllowed (false);
		jscpm.setAutoCreateRowSorter (true);

		
		
		final JScatterPlot jscp = new JScatterPlot (jscpm);
		jscp.setxAxis ((SortedTableColumn<?>)jscpm.getColumnModel().getColumn(0));
		jscp.setyAxis ((SortedTableColumn<?>)jscpm.getColumnModel().getColumn(1));
		jscp.setBackground (new Color (248, 248, 248));
		
		jscpm.addMouseListener (
			new MatrixSingleLinkMouseListener (jscp)
			//new MatrixSingleLinkMouseListener ()
		);
		
		
		final JColumnList2 jcl = new JColumnList2 ();
		jcl.addTable (table);
		jcl.getColumnModel().getColumn(1).setWidth (30);
		jcl.addTable (jscpm);
		
		
		final JTable[] tables = {table, table2, jscpm};
		
		final CollectiveTableRowSelectionModel2 ctrsm = new CollectiveTableRowSelectionModel2 ();
		for (JTable jtable : tables) {
			if (jtable instanceof JParCoord) {
				ctrsm.addJTable (jtable);
				if (jtable == table2) {
					((JParCoord)jtable).setDrawUnselectedItems (false);
					((JParCoord)jtable).setUberSelectedColour (Color.cyan);
				}
			}
		}


		if (columnTextFormatters != null && !columnTextFormatters.isEmpty()) {
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
		
		
		nano = System.nanoTime() - nano;
		LOGGER.debug ("Initialisation of data models and vis components: "+(nano/1E6)+" ms.");
		
		
		final JButton filterTestButton = new JButton ("Filter Odd/Even");
		filterTestButton.addActionListener (
			new ActionListener () {
				
				int filterIndex = 0;
				@Override
				public void actionPerformed (final ActionEvent event) {
					filterIndex = (filterIndex + 1) % (filters.size());
					final JTable[] tables = {table, jscpm};
					
					for (JTable indTable : tables) {
						if (indTable.getRowSorter() instanceof DefaultRowSorter<?,?>) {
							final DefaultRowSorter<? extends TableModel,? extends Integer> drs = (DefaultRowSorter<? extends TableModel,? extends Integer>)indTable.getRowSorter();
							drs.setRowFilter (filters.get (filterIndex));
						}
					}
					
					final int nextFilterIndex = (filterIndex + 1) % (filters.size());
					filterTestButton.setText ("Current Filter: "+filters.get(filterIndex).toString()
							+" (Next Filter: "+filters.get(nextFilterIndex).toString()+")");
				}
			}
		);
		filterTestButton.doClick ();
		
		final PolylineTypeBox ptb = new PolylineTypeBox ();
		for (JTable indTable : tables) {
			if (indTable instanceof JParCoord) {
				ptb.addToJPCList ((JParCoord)indTable);
			}
		}
		
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
					//final JScrollPane jscp2 = new JScrollPane ();
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
					jtp4.addTab ("Raw Data", new JScrollPane (table2) /*jp*/ /*jscp4*/);
					
					
					final TabbedMoveHandler dragHandler = new TabbedMoveHandler (frame);
					final JTabbedPane[] tabArray = {jtp1, jtp2, jtp3, jtp4};
					for (JTabbedPane jtp : tabArray) {
						dragHandler.addToTabbedPane (jtp);
					}
					
					final JSplitPane jsp2 = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, jtp1, jtp2);
					final JSplitPane jsp3 = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, jtp3, jtp4);
					final JSplitPane jsp = new JSplitPane (JSplitPane.VERTICAL_SPLIT, jsp2, jsp3);

			
					final JPanel additionalInfoPanel = new JPanel (new GridLayout (3, 0));	
					additionalInfoPanel.add (filterTestButton);
					additionalInfoPanel.add (new JTableLabel (table));
					additionalInfoPanel.add (ptb);
					
					frame.getContentPane().add (jsp);
					frame.getContentPane().add (additionalInfoPanel, BorderLayout.SOUTH);
					frame.setVisible (true);
				}
			}
		);
	}
	
	

	

	
	static class OddRowFilter extends RowFilter<TableModel, Integer> {
		@Override
		public boolean include (final Entry<? extends TableModel, ? extends Integer> entry) {
			final int rowIndex = ((Integer)entry.getIdentifier()).intValue();
			return (rowIndex & 1) == 0;
		}
		
		@Override
		public String toString () { return "Odd Row Filter"; }
	}
	
	
	static class EvenRowFilter extends RowFilter<TableModel, Integer> {
		@Override
		public boolean include (final Entry<? extends TableModel, ? extends Integer> entry) {
			final int rowIndex = ((Integer)entry.getIdentifier()).intValue();
			return (rowIndex & 1) == 1;
		}
		
		@Override
		public String toString () { return "Even Row Filter"; }
	}
	
	
	static class NoRowFilter extends RowFilter<TableModel, Integer> {
		@Override
		public boolean include (final Entry<? extends TableModel, ? extends Integer> entry) {
			final int rowIndex = ((Integer)entry.getIdentifier()).intValue();
			return (rowIndex != 1);
		}
		
		@Override
		public String toString () { return "No First Row"; }
	}
}
