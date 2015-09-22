package swingPlus.shared.tooltip;

import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.table.JTableHeader;

import org.apache.log4j.Logger;

import swingPlus.shared.ScaledTable;




public class DefaultTableCellToolTipListener extends AbstractCellToolTipMouseListener {

	final static Logger LOGGER = Logger.getLogger (DefaultTableCellToolTipListener.class);
	
	int row = -1, column = -1;
	
	@Override
	public void mouseMoved (final MouseEvent mEvent) {
		int newRow = -1, newColumn = -1;
		final JComponent jComp = (JComponent)mEvent.getComponent();
		
		if (jComp instanceof JTable) {
			final JTable jTable = (JTable)jComp;
			newRow = jTable.rowAtPoint (mEvent.getPoint());
			newColumn = jTable.columnAtPoint (mEvent.getPoint());
		}
		else if (jComp instanceof JTableHeader) {
			final JTableHeader jth = (JTableHeader)jComp;
			newRow = 0;
			newColumn = jth.columnAtPoint (mEvent.getPoint());
		}
		
		if (newRow != row || newColumn != column && newRow >= 0 && newColumn >= 0) {
			row = newRow;
			column = newColumn;
			Object obj = null;
			
			if (row != -1 && column != -1) {
	        	if (jComp instanceof ScaledTable) {
	        		final ScaledTable scaledTable = (ScaledTable) jComp;
	        		obj = scaledTable.getValueAt (row, column);
	        	}
	        	else if (jComp instanceof JTableHeader) {
	        		final JTableHeader jth = (JTableHeader) jComp;
	        		final JTable jTable = jth.getTable();
	        		obj = jTable.getModel().getValueAt (0, jTable.convertRowIndexToModel (row));
	        	}
	        	
	        	if (obj != null) {
	        		LOGGER.debug ("obj: "+obj+", "+obj.getClass());
	        		final JToolTip jtt = jComp.createToolTip();
	        		if (jtt instanceof AbstractRendererToolTip) {
		        		final AbstractRendererToolTip artt = (AbstractRendererToolTip)jtt;
		        		if (artt != null) {
				        	if ((obj instanceof Collection && ((Collection)obj).isEmpty())) {
				        		jComp.setToolTipText ("");
				        		artt.setToolTipObject (null, row, column);
				        	} else {
				        		jComp.setToolTipText (obj.toString());
				        		artt.setToolTipObject (obj, row, column);
				        	}
		        		}
	        		} else {
		        		final String str = jComp.getToolTipText (mEvent);
		    			jComp.setToolTipText (str);
	        		}
	        	} else {
	        		final String str = jComp.getToolTipText (mEvent);
	    			jComp.setToolTipText (str);
	        	}
			}
		}
	}
	
	@Override
	public void mouseExited (final MouseEvent mEvent) {
		if (mEvent.getComponent() instanceof JComponent) {
			super.mouseExited (mEvent);
			row = -1;
			column = -1;
		}		
	}
}
