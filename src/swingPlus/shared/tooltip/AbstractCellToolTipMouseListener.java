package swingPlus.shared.tooltip;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;



public class AbstractCellToolTipMouseListener extends MouseAdapter {
	
	@Override
	public void mouseExited (final MouseEvent mEvent) {
		if (mEvent.getComponent() instanceof JComponent) {
			final JComponent jComp = (JComponent)mEvent.getComponent();
			jComp.setToolTipText ("");
		}		
	}
}
