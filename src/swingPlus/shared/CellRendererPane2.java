package swingPlus.shared;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;

public class CellRendererPane2 extends CellRendererPane {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4788830891126910539L;


	@Override
	public void paintComponent (final Graphics graphics, final Component comp, final Container container, 
			final int x, final int y, final int width, final int height, 
			final boolean shouldValidate) {
		
		if (comp == null) {
		    if (container != null) {
		    final Color oldColor = graphics.getColor();
			graphics.setColor(container.getBackground());
			graphics.fillRect(x, y, width, height);
			graphics.setColor(oldColor);
		    }
		    return;
		}

		if (comp.getParent() != this) {
		    this.add(comp);
		}

		comp.setBounds(x, y, width, height);

		if(shouldValidate) {
		    comp.validate();
		}

		boolean wasDoubleBuffered = false;
		if ((comp instanceof JComponent) && ((JComponent)comp).isDoubleBuffered()) {
		    wasDoubleBuffered = true;
		    ((JComponent)comp).setDoubleBuffered(false);
		}

		final Graphics subGraphics = graphics.create (x, y, width, height);
		try {
		    comp.paint(subGraphics);
		}
		finally {
		    subGraphics.dispose();
		}

		if (wasDoubleBuffered && (comp instanceof JComponent)) {
		    ((JComponent)comp).setDoubleBuffered(true);
		}

		//c.setBounds(-w, -h, 0, 0);	// This can be done by calling code at end of cell drawing
	}
}
