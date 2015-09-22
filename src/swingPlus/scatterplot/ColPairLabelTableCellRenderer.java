package swingPlus.scatterplot;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class ColPairLabelTableCellRenderer extends JLabel implements TableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6395361391699200465L;

	@Override
	public Component getTableCellRendererComponent (final JTable table, final Object value,
			final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		
		//this.setBackground (Color.blue);
		
		if (table != null) {
			final TableColumnModel tcm = table.getColumnModel ();
			
			if (tcm != null) {
				final String rowString = (row >= 0 && row < tcm.getColumnCount() 
						? tcm.getColumn(row).getHeaderValue().toString()
						: "");
				final String colString = (column >= 0 && column < tcm.getColumnCount() && row != column  
						? " x "+tcm.getColumn(column).getHeaderValue().toString()
						: "");
				this.setText (rowString + colString);
			}
		}
		return this;
	}

}
