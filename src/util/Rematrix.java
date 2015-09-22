package util;

import io.TableModelWriter;
import io.parcoord.MakeTableModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


public class Rematrix {

	
	public static void main (final String args[]) {
		final String fileName = (args.length > 0 ? args[0] : Messages.getString ("ExampleDataFile"));
		final MakeTableModel mtm = new MakeTableModel ();
		final DefaultTableModel dtm = mtm.buildDataModel (fileName);
		final TableModel matrix = Rematrix.rematrix (dtm);
		
		SwingUtilities.invokeLater (
			new Runnable () {
				@Override
				public void run() {
					final JFrame jFrame = new JFrame ("Rematrix data");
					jFrame.setSize (1024, 768);
					jFrame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
					
					
					final JTable jTable = new JTable (dtm);
					final JTable jTable2 = new JTable (matrix);
					jTable2.getTableHeader().setResizingAllowed (true);
					jTable2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					jTable2.setCellSelectionEnabled (true);
					jTable2.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					jTable2.setRowSorter (new TableRowSorter<TableModel> (matrix));
					
					final JButton saveButton = new JButton ("Save");
					saveButton.addActionListener (
						new ActionListener () {
							@Override
							public void actionPerformed (final ActionEvent aEvent) {
								TableModel saveTable = matrix;
								if (jTable2.getSelectedRowCount() > 0 && jTable2.getSelectedColumnCount () > 0) {
									saveTable = makeSubTableFromSelection (jTable2);
								}
								final TableModelWriter csvWriter = new TableModelWriter ("\t", XMLConstants2.UTF8, "0.0");
								csvWriter.setFileName ("data/matrix.txt");
								csvWriter.setTableStructure (saveTable, null, null);
								csvWriter.write();
							}	
						}
					);
					
					final JScrollPane jsp = new JScrollPane (jTable);
					final JScrollPane jsp2 = new JScrollPane (jTable2);
					jFrame.getContentPane().add (jsp, "North");
					jFrame.getContentPane().add (jsp2, "Center");
					jFrame.getContentPane().add (saveButton, "South");

					jFrame.setVisible (true);
					
					for (int n = 0; n < jTable2.getColumnCount(); n++) {
						jTable2.getColumnModel().getColumn(n).setPreferredWidth (64);
						jTable2.getColumnModel().getColumn(n).setWidth (64);
					}
				}
			}
		);
	}
	
	public static TableModel rematrix (final TableModel tableModel) {
		final DefaultTableModel matrix = new DefaultTableModel ();
		final Map<String, Integer> topicIndex = new HashMap<String, Integer> ();
		final Map<String, Integer> companyIndex = new HashMap<String, Integer> ();
		matrix.addColumn ("CompanyName");
		topicIndex.put ("CompanyName", Integer.valueOf(0));
		
		for (int row = 0; row < tableModel.getRowCount(); row++) {
			final String companyName = tableModel.getValueAt(row, 0).toString();
			final String topicName = tableModel.getValueAt(row, 1).toString();
			final String value = tableModel.getValueAt(row, 2).toString();
			if (topicIndex.get (topicName) == null) {
				topicIndex.put (topicName, matrix.getColumnCount());
				matrix.addColumn (topicName);
			}
			if (companyIndex.get (companyName) == null) {
				companyIndex.put (companyName, matrix.getRowCount());
				final Object[] obj = new Object [1];
				obj[0] = companyName;
				matrix.addRow (obj);
			}
			final int mrow = companyIndex.get(companyName).intValue();
			final int mcol = topicIndex.get(topicName).intValue();
			matrix.setValueAt (value, mrow, mcol);
		}
		return matrix;
	}
	
	public static TableModel makeSubTableFromSelection (final JTable table) {
		final DefaultTableModel tableModel = new DefaultTableModel ();
		if (table.getSelectedRowCount() > 0 && table.getSelectedColumnCount () > 0) {
			final int[] sCols = table.getSelectedColumns();
			final int[] sRows = table.getSelectedRows();
			String[] rowData = new String [sRows.length];
			
			for (int col = 0; col < sCols.length; col++) {
				tableModel.addColumn (table.getColumnName(sCols[col]));
			}
			
			for (int row = 0; row < sRows.length; row++) {
				for (int col = 0; col < sCols.length; col++) {			
					final Object obj = table.getValueAt (sRows[row], sCols[col]);
					rowData [col] = (obj == null ? null : obj.toString());
				}
				
				tableModel.addRow (rowData);
			}
		}
		return tableModel;
	}
}
