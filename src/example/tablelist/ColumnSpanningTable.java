package example.tablelist;

import io.parcoord.MakeTableModel;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import example.tablelist.renderers.ColourBarCellRenderer;

import swingPlus.shared.MyFrame;
import ui.AnimatedTableUI;
import ui.MultiCellSpanTableUI;
import util.Messages;
import util.ui.NewMetalTheme;


public class ColumnSpanningTable {

	
	JFrame jframe;
	final static Logger LOGGER = Logger.getLogger (ColumnSpanningTable.class);
	/**
	 * @param args
	 */
	public static void main (final String[] args) {
			
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		LOGGER.info ("Logger activated");

		new ColumnSpanningTable (args.length > 0 ? args[0] : Messages.getString ("ExampleMultiColDataFile"));
	}
	
	public ColumnSpanningTable (final String dataFileName) {
		
		final MakeTableModel mtm = new MakeTableModel ();
		final DefaultTableModel dtm = mtm.buildDataModel (dataFileName);
		//final List<Format> columnTextFormatters = mtm.getColumnTextFormatters();

		//TableUtils.makeAverageRows (dtm);
		
		final JTable jtable3 = new JTable (dtm);
		jtable3.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jtable3.setRowSelectionAllowed (true);
		//jt2.setColumnSelectionAllowed (true);
		jtable3.setRowSorter (new TableRowSorter<DefaultTableModel> (dtm));
		final AnimatedTableUI tlui = new MultiCellSpanTableUI ();
		jtable3.setUI (tlui);
		//jtable3.setDefaultRenderer (String.class, new DefaultTableCellRenderer ());
		
		final TableColumnModel tcm = jtable3.getColumnModel();
		final Color[] columnColours = {Color.red, Color.orange, Color.yellow, Color.green, Color.cyan,
				Color.blue, Color.magenta};

		for (int col = 1; col < tcm.getColumnCount(); col++) {
			tcm.getColumn(col).setCellRenderer (new ColourBarCellRenderer (columnColours [(col - 1) % columnColours.length]));
		}

		
		SwingUtilities.invokeLater (
			new Runnable () {
				@Override
				public void run() {
					jframe = new MyFrame ("Multi-Column Cell Span Table Demo");
					jframe.setSize (1024, 768);
					
					final JScrollPane jscp1 = new JScrollPane ();	
					jscp1.setViewportView (jtable3);

					jframe.getContentPane().add (jscp1);
					jframe.setVisible (true);
					
					//jtable3.setRowHeight (3, 24);
					//jtable3.setRowHeight (6, 72);
				}
			}
		);
		
	}
}
