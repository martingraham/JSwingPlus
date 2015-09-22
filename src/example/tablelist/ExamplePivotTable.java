package example.tablelist;

import io.parcoord.MakeTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import model.pivottable.AbstractPivotDataAggregator;
import model.pivottable.Aggregators;
import model.pivottable.CountDifferenceAggregator;
import model.pivottable.PairwiseProbabilityMap;
import model.pivottable.SimplePivotTableModel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.tablelist.renderers.BarCellRenderer;
import example.tablelist.renderers.HeatSpotCellRenderer;
import example.tablelist.renderers.HeatSpotCellRenderer2;
import example.tablelist.renderers.LabelSubTableCellRenderer;

import swingPlus.pivottable.CellContentsDisplayer;
import swingPlus.pivottable.JPivotTable;
import swingPlus.shared.JTableST;
import swingPlus.shared.MyFrame;
import ui.AnimatedTableUI;
import util.Messages;
import util.ui.NewMetalTheme;


public class ExamplePivotTable {

	
	JFrame jframe;
	final static Logger LOGGER = Logger.getLogger (ExamplePivotTable.class);
	/**
	 * @param args
	 */
	public static void main (final String[] args) {
			
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		LOGGER.info ("Logger activated");

		new ExamplePivotTable (args.length > 2 ? args : new String[] {Messages.getString ("ExampleDataFile")});
	}
	
	public ExamplePivotTable (final String[] args) {
		
		final MakeTableModel mtm = new MakeTableModel ();
		final DefaultTableModel dtm = mtm.buildDataModel (args[0]);
		final List<Format> columnTextFormatters = mtm.getColumnTextFormatters();
		
		final MakeTableModel mtm2 = new MakeTableModel ();
		mtm2.setMakeAllStringType (true);
		final DefaultTableModel labelledValues = mtm2.buildDataModel (args[1]);
		// Map<columnName, Map<columnValue, descriptiveLabel>>
		final ColumnLabelMap labelMap = new ColumnLabelMap ();
		labelMap.parseModel (labelledValues);
		
		final MakeTableModel mtm3 = new MakeTableModel ();
		final List<Class<?>> columnTypes = Arrays.asList (new Class<?>[] {String.class, String.class, String.class, String.class, Double.class});
		mtm3.setEnforcedColumnTypes (columnTypes);
		final DefaultTableModel probabilities = mtm3.buildDataModel (args[2]);
		final PairwiseProbabilityMap probabilityMap = new PairwiseProbabilityMap ();
		probabilityMap.parseModel (probabilities);
		Aggregators.getInstance().addAggregator (new CountDifferenceAggregator (probabilityMap));
		
		
		final JTable jtable2 = new JTableST (dtm);
		jtable2.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jtable2.setRowSelectionAllowed (true);
		jtable2.setRowSorter (new TableRowSorter<DefaultTableModel> (dtm));

		
		final int[] origAxes = {0, 3, 4};
		
		final JTable jtable3 = new JPivotTable (makeSimplePivotTableModel (dtm, origAxes, Aggregators.getInstance().getList().get(0)));
		jtable3.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jtable3.setRowSelectionAllowed (true);
		jtable3.setColumnSelectionAllowed (true);
		jtable3.setShowGrid (false);
		jtable3.setIntercellSpacing (new Dimension (0, 0));
		jtable3.setRowSorter (new TableRowSorter<TableModel> (jtable3.getModel()));
		//jtable3.setAutoResizeMode (JTable.AUTO_RESIZE_LAST_COLUMN);
		
		final AnimatedTableUI atui = new AnimatedTableUI ();
		jtable3.setUI (atui);
		//jtable3.getColumnModel().setColumnMargin (8);
		//jtable3.setRowMargin (8);
		
		final HeatSpotCellRenderer hscr = new HeatSpotCellRenderer ();
		final HeatSpotCellRenderer2 hscr2 = new HeatSpotCellRenderer2 ();
		final BarCellRenderer bcr = new BarCellRenderer ();
		final TableCellRenderer[] rendererArray = {hscr, hscr2, bcr};
		for (TableCellRenderer renderer : rendererArray) {
			final PropertyChangeListener pcl = (PropertyChangeListener)renderer;
			((SimplePivotTableModel)jtable3.getModel()).addPropertyChangeListener (pcl);
		}
		
		jtable3.setDefaultRenderer (Integer.class, hscr);
		jtable3.setDefaultRenderer (Double.class, hscr);

		
		final TableCellRenderer labelSubRenderer = new LabelSubTableCellRenderer ((SimplePivotTableModel)jtable3.getModel(), labelMap.getMap());
		
		final JTable jRowHeader = ((JPivotTable)jtable3).getRowHeader();
		jRowHeader.setDefaultRenderer (Integer.class, labelSubRenderer);
		jRowHeader.setDefaultRenderer (String.class, labelSubRenderer);
		jRowHeader.setDefaultRenderer (Double.class, labelSubRenderer);
		final AnimatedTableUI atui2 = new AnimatedTableUI ();
		jRowHeader.setUI (atui2);
		
		final JTable jColumnHeader = ((JPivotTable)jtable3).getColumnHeader();
		jColumnHeader.setDefaultRenderer (Integer.class, labelSubRenderer);
		jColumnHeader.setDefaultRenderer (String.class, labelSubRenderer);
		jColumnHeader.setDefaultRenderer (Double.class, labelSubRenderer);
		final AnimatedTableUI atui3 = new AnimatedTableUI ();
		jColumnHeader.setUI (atui3);
		
		//jtable3.getTableHeader().setDefaultRenderer (labelSubRenderer);
		//jtable3.getTableHeader().addMouseMotionListener (new DefaultTableCellToolTipListener ());
		
		
		final List<String> originalColumnList = new ArrayList<String> ();
		for (int col = 0; col < dtm.getColumnCount(); col++) {
			originalColumnList.add (dtm.getColumnName (col));
		}
		
		final List<JComboBox> selLists = new ArrayList<JComboBox> ();
		for (int comboBoxIndex = 0; comboBoxIndex < 3; comboBoxIndex++) {
			final JComboBox comboBox = new JComboBox (originalColumnList.toArray());
			comboBox.setSelectedIndex (origAxes [comboBoxIndex]);
			selLists.add (comboBox);
		}

		final ActionListener comboListener = new ActionListener () {
			@Override
			public void actionPerformed (final ActionEvent aEvent) {
				final JComboBox comboBox = (JComboBox)aEvent.getSource();
				final int comboBoxIndex = selLists.indexOf (comboBox);
				final int selectedIndex = comboBox.getSelectedIndex();
		        origAxes [comboBoxIndex] = selectedIndex;
		        //System.err.println (origAxes[0]+", "+origAxes[1]+", "+origAxes[2]);
		        
		        final SimplePivotTableModel pivotModel = (SimplePivotTableModel)jtable3.getModel();
		        pivotModel.setColumnXIndex (origAxes [0]);
		        pivotModel.setColumnYIndex (origAxes [1]);
		        pivotModel.setColumnDataIndex (origAxes [2]);
			}
		};
		
		for (int comboBoxIndex = 0; comboBoxIndex < selLists.size(); comboBoxIndex++) {
			selLists.get(comboBoxIndex).addActionListener (comboListener);
		}
		
		
		final JComboBox comboBox = new JComboBox (Aggregators.getInstance().getList().toArray());
		comboBox.setSelectedIndex (0);
		selLists.add (comboBox);
		final ActionListener aggregatorComboListener = new ActionListener () {
			@Override
			public void actionPerformed (final ActionEvent aEvent) {
				final JComboBox comboBox = (JComboBox)aEvent.getSource();
				final int selectedIndex = comboBox.getSelectedIndex();
				final SimplePivotTableModel pivotModel = (SimplePivotTableModel)jtable3.getModel();
		        pivotModel.setAggregator (Aggregators.getInstance().getList().get (selectedIndex));
			}
		};
		comboBox.addActionListener (aggregatorComboListener);
		
		
		final JComboBox cellRendererBox = new JComboBox (rendererArray);
		cellRendererBox.addActionListener (
			new ActionListener () {

				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					final JComboBox comboBox = (JComboBox)aEvent.getSource();
					final int selectedIndex = comboBox.getSelectedIndex();
					final TableCellRenderer tcr = (TableCellRenderer) comboBox.getItemAt (selectedIndex);
					jtable3.setDefaultRenderer (Integer.class, tcr);
					jtable3.setDefaultRenderer (Double.class, tcr);	
					jtable3.repaint ();
				}
			}
		);
		
		
		SwingUtilities.invokeLater (
			new Runnable () {
				@Override
				public void run() {
					jframe = new MyFrame ("Animated PivotTable Demo");
					jframe.setSize (1024, 768);
					
					final JScrollPane jscp1 = new JScrollPane ();
					final JScrollPane jscp2 = new JScrollPane ();
					
					final JTabbedPane jtp = new JTabbedPane ();
					final CellContentsDisplayer ccd = new CellContentsDisplayer ((JPivotTable)jtable3, jtp);
					jtable3.getSelectionModel().addListSelectionListener (ccd);
					jtable3.getColumnModel().getSelectionModel().addListSelectionListener (ccd);
					
					jscp1.setViewportView (jtable3);
					jtp.addTab ("PivotTable", jscp1);

					jscp2.setViewportView (jtable2);
					
					final JSplitPane jsp = new JSplitPane (JSplitPane.VERTICAL_SPLIT, jtp, jscp2);
							
					
					final JPanel comboBoxPanel = new JPanel ();
					comboBoxPanel.setLayout (new GridLayout (selLists.size() + 1, 2, 5, 1));
					final String[] comboLabels = {"Columns", "Rows", "Data", "Aggregate"};
					for (int n = 0; n < selLists.size(); n++) {
						comboBoxPanel.add (new JLabel (comboLabels[n], SwingConstants.RIGHT));
						comboBoxPanel.add (selLists.get(n));
					}
					comboBoxPanel.add (new JLabel ("Cell Renderer", SwingConstants.RIGHT));
					comboBoxPanel.add (cellRendererBox);
					
					final JPanel comboContainer = new JPanel (new BorderLayout ());
					comboContainer.add (comboBoxPanel, "North");
					
					jframe.getContentPane().add (jsp);
					
					jframe.getContentPane().add (comboContainer, "East");
					jframe.setVisible (true);
					
					jtable3.setRowHeight (3, 24);
					//jtable3.setRowHeight (6, 72);
					
					jsp.setDividerLocation (0.4f);
					jtable3.setRowHeight(34);
				}
			}
		);
	}
	
	
	final TableModel makeSimplePivotTableModel (final TableModel origDataModel, final int[] axes, 
			final AbstractPivotDataAggregator aggregator) {
		final SimplePivotTableModel pivotModel = new SimplePivotTableModel (origDataModel);
		pivotModel.setColumnXIndex (axes[0]);
		pivotModel.setColumnYIndex (axes[1]);
		pivotModel.setColumnDataIndex (axes[2]);
		pivotModel.setAggregator (aggregator);
		return pivotModel;
	}
}
