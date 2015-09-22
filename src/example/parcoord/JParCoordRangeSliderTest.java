package example.parcoord;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.LaunchSaveFileDialog;
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
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.ListSelectionModel;

import model.shared.SortedTableColumn;
import model.shared.selection.CollectiveTableRowSelectionModel2;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import util.IconCache;
import util.Messages;
import util.colour.ColorUtilities;
import util.swing.TabbedMoveHandler;
import util.ui.NewMetalTheme;



public class JParCoordRangeSliderTest {

	transient JFrame frame;
	static final private Logger LOGGER = Logger.getLogger (JParCoordRangeSliderTest.class);
	
	int filterIndex = 0;
	
	public static void main (final String[] args) {
		
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		LOGGER.info ("Logger activated");

		//new LaunchFileDialog (JParCoordRangeSliderTest.class);
		new JParCoordRangeSliderTest (args.length > 0 ? args[0] : Messages.getString ("ExampleDataFile"));
	}
	
	
	public JParCoordRangeSliderTest (final String dataFileName) {
		
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
		filters.add (noFilter);
		filters.add (evenFilter);
		filters.add (oddFilter);

		
		final JTable table = new JParCoord (dtm);
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
			trs.setRowFilter (filters.get (filterIndex));
		}
		table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
		
		
		
		//final JTable table2 = new JTable (dtm, new DefaultTableColumnModel ());
		final JTable table2 = new JTable (dtm);
		//((JParCoord)table2).createColumnsFromPreSortedSource (table.getColumnModel());
		table2.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table2.setRowSelectionAllowed (true);
		table2.setAutoCreateRowSorter (true);
		table2.setColumnSelectionAllowed (true);
		table2.setSelectionForeground (Color.orange);
		table2.setBorder (BorderFactory.createEmptyBorder (10, 2, 10, 2));
		if (table.getRowSorter() instanceof TableRowSorter) {
			final TableRowSorter<? extends TableModel> trs = (TableRowSorter<? extends TableModel>)table2.getRowSorter();
			trs.setRowFilter (noFilter);
		}
		table2.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);	
		

		
		
		final JTable jscpm = new JScatterPlotMatrix (dtm, new DefaultTableColumnModel ());
		((JParCoord)jscpm).createColumnsFromPreSortedSource (table.getColumnModel());
		jscpm.setBackground (new Color (248, 248, 248));
		jscpm.setForeground (table.getForeground());
		jscpm.setSelectionForeground (Color.blue);
		jscpm.setRowSelectionAllowed (false);
		jscpm.setAutoCreateRowSorter (true);
		if (jscpm.getRowSorter() instanceof TableRowSorter) {
			final TableRowSorter<? extends TableModel> trs = (TableRowSorter<? extends TableModel>)jscpm.getRowSorter();
			trs.setRowFilter (filters.get (filterIndex));
		}

		
		
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
		
		
		final JTable[] tables = {table, table2, jscpm};
		final CollectiveTableRowSelectionModel2 ctrsm = new CollectiveTableRowSelectionModel2 ();
		
		final Color orangeRed = ColorUtilities.mixColours (Color.orange, Color.red, 0.5f);
		for (JTable indTable : tables) {
			if (indTable instanceof JParCoord) {
				ctrsm.addJTable (indTable);
				((JParCoord)indTable).setUberSelectedColour (orangeRed);
				if (indTable == table2) {
					((JParCoord)indTable).setDrawUnselectedItems (false);
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
				@Override
				public void actionPerformed (final ActionEvent event) {
					filterIndex = (filterIndex + 1) % (filters.size());
					final JTable[] tables = {table, jscpm};
					for (JTable table : tables) {
						if (table.getRowSorter() instanceof TableRowSorter<?>) {
							final TableRowSorter<? extends TableModel> trs = (TableRowSorter<? extends TableModel>)table.getRowSorter();
							trs.setRowFilter (filters.get (filterIndex));
						}
					}
				}
			}
		);
		
		final PolylineTypeBox ptb = new PolylineTypeBox ();
		ptb.addToJPCList ((JParCoord)table);
		
		final JButton labelSepTestButton = new JButton ("Test Label Separation Change");
		labelSepTestButton.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent event) {
					filterIndex = (filterIndex + 1) % (filters.size());
					final JTable[] tables = {table, jscpm};
					for (JTable indTable : tables) {
						if (indTable instanceof JParCoord) {
							((JParCoord)indTable).setSuggestedLabelSeparation ((int)(Math.random() * 200));
						}
					}
				}
			}
		);
		
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
					jtp4.addTab ("Raw Data", new JScrollPane (table2) /*jp*/ /*jscp4*/);
					
					
					final TabbedMoveHandler dragHandler = new TabbedMoveHandler (frame);
					final JTabbedPane[] tabArray = {jtp1, jtp2, jtp3, jtp4};
					for (JTabbedPane jtp : tabArray) {
						dragHandler.addToTabbedPane (jtp);
					}
					
					final JSplitPane jsp2 = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, jtp1, jtp2);
					final JSplitPane jsp3 = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, jtp3, jtp4);
					final JSplitPane jsp = new JSplitPane (JSplitPane.VERTICAL_SPLIT, jsp2, jsp3);

					final JComponent[] extrasArray = {filterTestButton, new JTableLabel (table), ptb, labelSepTestButton};
					final JPanel additionalInfoPanel = new JPanel (new GridLayout (extrasArray.length, 0));	
					for (JComponent extra : extrasArray) {
						additionalInfoPanel.add (extra);
					}
					
					final JToolBar jtb = (JToolBar) frame.getContentPane().getComponent (0);
					jtb.addSeparator ();
					final JSeparator jsep = new JSeparator (SwingConstants.VERTICAL);
					jsep.setMaximumSize (new Dimension (4, 24));
					jsep.setPreferredSize (new Dimension (4, 24));
					jtb.add (jsep);
					jtb.addSeparator ();
					jtb.add (makeSaveButton ((JParCoord)table));
					
					frame.getContentPane().add (jsp);
					frame.getContentPane().add (additionalInfoPanel, BorderLayout.SOUTH);
					
					frame.setVisible (true);
				}
			}
		);
	}
	
	
	public JButton makeSaveButton (final JParCoord jpc) {
		final Icon saveIcon = IconCache.makeIcon ("SaveFileIcon");
		final JButton saveButton = new JButton (saveIcon);
		saveButton.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					new LaunchSaveFileDialog (Arrays.asList (new JParCoord[] {jpc}));
				}
			}
		);
		
		return saveButton;
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
			final int rowIndex = ((Integer)entry.getIdentifier()).intValue();
			return (rowIndex != 1);
		}
	}
}
