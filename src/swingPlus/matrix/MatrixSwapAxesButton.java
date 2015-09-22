package swingPlus.matrix;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.table.TableModel;

import util.IconCache;
import util.Messages;

import model.matrix.DefaultMatrixTableModel;

public class MatrixSwapAxesButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3029908394151413304L;
	private static final Icon ICON = IconCache.makeIcon ("SwapAxesIcon");
	
	private JMatrix matrix;
	
	public MatrixSwapAxesButton (final JMatrix jmat) {
		super (ICON);
		this.setToolTipText (Messages.getString ("swapButtonTooltipText"));
		
		matrix = jmat;
		
		addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					final TableModel tableModel = matrix.getModel();
					if (tableModel instanceof DefaultMatrixTableModel) {
						((DefaultMatrixTableModel)tableModel).flipAxes();
					}
					matrix.flipColumnModelWithRows ();
				}		
			}
		);
	}
}
