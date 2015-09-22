package example.table;

import io.parcoord.MakeTableModel;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import util.Messages;
import util.ui.NewMetalTheme;

public class JTableSimpleDemo {

	JFrame jFrame;
	private final static Logger LOGGER = Logger.getLogger (JTableDemo.class);
	
	public static void main (final String[] args) {
		
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		LOGGER.info ("Logger activated");

		new JTableSimpleDemo (args.length > 0 ? args[0] : Messages.getString ("ExampleDataFile"));
	}
	
	JTableSimpleDemo (final String dataFileName) {
		
		JFrame.setDefaultLookAndFeelDecorated (true);

		final MakeTableModel mtm = new MakeTableModel ();
		final DefaultTableModel dtm = mtm.buildDataModel (dataFileName);
		
		final JTable jTable = new JTable ();
		jTable.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jTable.setRowSelectionAllowed (true);
		//jt.setColumnSelectionAllowed (true);
		jTable.setRowSorter (new TableRowSorter<DefaultTableModel> (dtm));
		jTable.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
		jTable.setModel (dtm);
		
		SwingUtilities.invokeLater (
			new Runnable () {
				@Override
				public void run() {
					jFrame = new JFrame ("Simple Table Demo");
					jFrame.setSize (1024, 768);
					jFrame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
					
					final JScrollPane jscp1 = new JScrollPane ();			
					jscp1.setViewportView (jTable);
					
					jFrame.getContentPane().add (jscp1);
					jFrame.setVisible (true);
				}
			}
		);
	}
}
