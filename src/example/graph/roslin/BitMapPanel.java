package example.graph.roslin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.html.HTMLEditorKit;

import model.graph.GraphModel;

import org.apache.log4j.Logger;

public class BitMapPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3891196451291504902L;
	final static Logger LOGGER = Logger.getLogger (BitMapPanel.class);
	
	transient JTable jTable;
	transient JTextPane jtp;
	transient MarkerMap map;
	
	public BitMapPanel () {
			
		super ();
	 
		jTable = new JTable () {
			/**
			 * 
			 */
			private static final long serialVersionUID = 7483405901078343722L;

			
			
			@Override
			public boolean isCellEditable (final int row, final int column) {
				return this.convertColumnIndexToModel (column) != 0;
			}
			
			@Override
			public Class<?> getColumnClass (final int column) {
				final int modelColumnIndex = this.convertColumnIndexToModel (column);
				final Object obj = this.getModel().getValueAt (0, modelColumnIndex);
				return obj.getClass();
			}
			
			@Override
			public void tableChanged (final TableModelEvent event) {
		    	super.tableChanged (event);
		    	LOGGER.debug ("e: "+event.getFirstRow()+"|"+event.getLastRow()+"|"+event.getColumn()+"|"+event.getType()+"|"+event.getSource());
		    	
		    	/**
		    	 * This bit only gets called on UPDATEs, not INSERTs or DELETEs, so doesn't get
		    	 * called when populateModel() does its work.
		    	 */
		    	if (event.getFirstRow() != TableModelEvent.HEADER_ROW 
		    			&& event.getFirstRow() == event.getLastRow()
		    			&& event.getColumn() >= 0
		    			&& event.getType() == TableModelEvent.UPDATE) {
		    		final String markerName = (String) getModel().getValueAt (event.getFirstRow(), 0);
		    		final boolean select = !((Boolean) getModel().getValueAt (event.getFirstRow(), event.getColumn())).booleanValue();
		    		LOGGER.debug ("Set "+markerName+":"+select);
		    		
		    		map.setGloballyActive (event.getFirstRow(), select);
		    		this.repaint();
		    	}
		    }
		};
		
		jTable.setAutoCreateRowSorter (true);
		
		final JScrollPane jsp = new JScrollPane ();
		jsp.setViewportView (jTable);
		final Dimension sPaneSize = jsp.getPreferredSize ();
		sPaneSize.width = 180;
		jsp.setPreferredSize (sPaneSize);
		
		jtp = new JTextPane ();
		jtp.setBorder (BorderFactory.createTitledBorder ("Counts"));
		jtp.setOpaque (false);
		jtp.setEditable (false);
		jtp.setEditorKit (new HTMLEditorKit());
		jtp.setPreferredSize (new Dimension (150, 150));
		
		this.setLayout (new BorderLayout ());
		this.add (jsp, BorderLayout.CENTER);
		this.add (jtp, BorderLayout.SOUTH);
	}
	
	public void populate (final MarkerMap mmap) {
		final BitSet active = mmap.getGloballyActive();
		LOGGER.debug ("Active: "+active);
		map = mmap;
		final BitSetColumnTableModel btm = new BitSetColumnTableModel (0, 2);
		btm.setColumnIdentifiers (new Object[] {"Marker Name", "Active"});	
		
		
		for (int n = 0; n < mmap.getSize(); n++) {
			final Vector<Object> rowData = new Vector<Object> ();
			rowData.add (mmap.getMarkerName(n));
			LOGGER.debug ("nname: "+mmap.getMarkerName(n));
			rowData.add (active.get(n) ? Boolean.TRUE : Boolean.FALSE);
			btm.addRow (rowData);
		}
		
		btm.bitSetColumn (active, 1);
		
		jTable.setModel (btm);
		
		jTable.getColumnModel().getColumn(0).setPreferredWidth (130);
		jTable.getColumnModel().getColumn(1).setPreferredWidth (50);
	}
	
	public void stats (final GraphModel graph) {
		final Collection<Object> nodes = graph.getNodes ();
		final BitSet temp = new BitSet ();
		final BitSet active = map.getGloballyActive();
		
		int unknownTotalMarkers = 0, unknownTotalAnimals = 0;
		int errorTotalMarkers = 0, errorTotalAnimals = 0;
		
		for (Object node : nodes) {
			final Animal animal = (Animal)node;
			
			final SNPData data = animal.getData ();
			if (data != null) {
				final BitSet unknown = data.getUnknownMarkers();
				temp.clear ();
				temp.or (unknown);
				temp.and (active);
				if (!temp.isEmpty()) {
					unknownTotalAnimals++;
					unknownTotalMarkers += temp.cardinality ();
				}
				
				final BitSet errors = data.getPossErrors();
				temp.clear ();
				temp.or (errors);
				temp.and (active);
				if (!temp.isEmpty()) {
					errorTotalAnimals++;
					errorTotalMarkers += temp.cardinality ();
				}
			}
		}
		
		final StringBuilder statBdr = new StringBuilder ("<HTML><font face=\"Gill Sans MT\" size=2>");
		statBdr.append ("<P>").append ("Animals with unknown markers: ").append(unknownTotalAnimals).append("<br/>");
		statBdr.append ("Total unknown markers: ").append(unknownTotalMarkers).append("</P>");

		statBdr.append ("<P>").append ("Animals with error markers: ").append(errorTotalAnimals).append("<br/>");
		statBdr.append ("Total error markers: ").append(errorTotalMarkers).append("</P>");

		statBdr.append ("</font></HTML>");
		jtp.setText(statBdr.toString());
	}
	
	public JTable getTable () {
		return jTable;
	}
	
	static class BitSetColumnTableModel extends DefaultTableModel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5001273771112831127L;
		Map<Integer, BitSet> bitSetMap = new HashMap <Integer, BitSet> ();
		
		BitSetColumnTableModel (final int rows, final int columns) {
			super (rows, columns);
		}
		
		void bitSetColumn (final BitSet bitset, final int column) {
			bitSetMap.put (Integer.valueOf (column), bitset);
		}
		
		@Override
		public Object getValueAt (final int rowIndex, final int columnIndex) {
			final Integer colInt = Integer.valueOf (columnIndex);
			final BitSet bitset = bitSetMap.get (colInt);
			if (bitset != null) {
				return Boolean.valueOf (bitset.get (rowIndex));
			}
			return super.getValueAt (rowIndex, columnIndex);
		}
	}
}
