package io;

import io.parcoord.MakeTableModel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import swingPlus.parcoord.FormatterRenderer;
import swingPlus.parcoord.JColumnList;
import swingPlus.parcoord.JColumnList2;
import swingPlus.scatterplot.JScatterPlotMatrix;
import util.GraphicsUtil;
import util.Messages;
import util.PropertyPrefixBasedAction;


public class LoadTableDataAction extends PropertyPrefixBasedAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6446761719963664424L;
	private JFrame frame;
	
	public LoadTableDataAction (final int keyCode, final String actionPrefix, final JFrame frame) {
		super (keyCode, actionPrefix);
		this.frame = frame;
	}
	
	@Override
	public void actionPerformed (final ActionEvent aEvent) {	
		final JFileChooser jfc = new JFileChooser (".");
		jfc.setAccessory (new HeaderRowPanel ());
		fileChooserSetUp (jfc);
		jfc.showOpenDialog (null);
		
		final boolean doubleHeaders = (((HeaderRowPanel)jfc.getAccessory()).getChoice() == 2);
		final File selectedFile = jfc.getSelectedFile ();
		
		if (selectedFile != null) {
			final String dataFileName = selectedFile.getPath();
			final MakeTableModel mtm = new MakeTableModel ();
			mtm.setDoubleRowHeaders (doubleHeaders);
			final DefaultTableModel dtm = mtm.buildDataModel (dataFileName);
			final List<Format> columnTextFormatters = mtm.getColumnTextFormatters();
			
			final Collection<JComponent> comps = GraphicsUtil.getComponentsBeneath ((JComponent)frame.getContentPane(), JTable.class, true);
			final List<JTable> dataTables = new ArrayList<JTable> ();
			for (JComponent comp : comps) {
				if (comp instanceof JTable) {
					final JTable table = (JTable)comp;
					if (! (table instanceof JColumnList2 || table instanceof JColumnList)) {
						table.setModel (dtm);
						if (! (table instanceof JScatterPlotMatrix)) {
							dataTables.add (table);
						}
					}
				}
			}
			
			if (columnTextFormatters != null && !columnTextFormatters.isEmpty()) {
				for (int modelColumn = 0; modelColumn < columnTextFormatters.size(); modelColumn++) {
					final Format format = columnTextFormatters.get (modelColumn);
					if (format != null) {
						final FormatterRenderer fRender = new FormatterRenderer (format);
						for (JTable dataTable : dataTables) {
							final int viewColumn = dataTable.convertColumnIndexToView (modelColumn);
							if (viewColumn >= 0) {
								dataTable.getColumnModel().getColumn(viewColumn).setCellRenderer (fRender);
							}
						}
					}
				}
			}
			
			extendedAction (dataTables);
		}
	}
	
	public void fileChooserSetUp (final JFileChooser jfc) { /* EMPTY */ }
	
	public void extendedAction (final List<JTable> dataTables) { /* EMPTY */ }
	

	
	static protected class HeaderRowPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -5829432513433432088L;
		JComboBox headerRowChoice;
		
		HeaderRowPanel () {
			
			super ();
			
			this.setLayout (new BorderLayout ());
			final JPanel northPanel = new JPanel (new GridLayout (2, 1));
			
			northPanel.add (new JLabel (Messages.getString ("HeaderRowSizeText")));
			
			final DefaultComboBoxModel dcbm = new DefaultComboBoxModel ();
			dcbm.addElement (Integer.valueOf (0));
			dcbm.addElement (Integer.valueOf (1));
			dcbm.addElement (Integer.valueOf (2));	
			
			headerRowChoice = new JComboBox (dcbm);
			setChoice (1);
			northPanel.add (headerRowChoice);
			
			add (northPanel, "North");
		}
		
		final public int getChoice () {
			return ((Integer)headerRowChoice.getSelectedItem()).intValue();
		}
		
		final public void setChoice (final int value) {
			headerRowChoice.setSelectedItem (Integer.valueOf (value));
		}
	}
}
