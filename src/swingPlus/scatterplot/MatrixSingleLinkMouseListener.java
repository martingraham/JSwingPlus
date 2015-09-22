package swingPlus.scatterplot;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import model.shared.SortedTableColumn;

/**
 * 
 * @author cs22
 * Mouse Class that links together a scatterplot matrix and a separate scatterplot component
 * Clicking on a scatterplot in the matrix will project it to the individual scatterplot
 * which should have more screen space to reveal detail
 */
public class MatrixSingleLinkMouseListener extends MouseAdapter {

	JScatterPlot jsp;
	
	
	public MatrixSingleLinkMouseListener () {
		this (null);
	}

	public MatrixSingleLinkMouseListener (final JScatterPlot jsp) {
		super ();
		setJsp (jsp);
	}
	
	
	public final JScatterPlot getJsp() {
		return jsp;
	}

	public final void setJsp (final JScatterPlot jsp) {
		this.jsp = jsp;
	}
	
	@Override
	public void mouseClicked (final MouseEvent mEvent) {

		if (mEvent.getComponent() instanceof JScatterPlotMatrix) {
			final JScatterPlotMatrix jspm = (JScatterPlotMatrix)mEvent.getComponent();
			
			if (getJsp() == null) {
				launchInternalFrame (jspm);
			}
			
			if (jspm.getModel() != null && jspm.getModel().equals (getJsp().getSlaveTable().getModel())) {
				final Dimension pairing = jspm.getPlotUnderPoint (mEvent.getX(), mEvent.getY());
				
				if (pairing.width != pairing.height && pairing.width >= 0 && pairing.height >= 0) {
					final SortedTableColumn newXAxis = (SortedTableColumn) jspm.getColumnModel().getColumn (pairing.width);
					final SortedTableColumn newYAxis = (SortedTableColumn) jspm.getColumnModel().getColumn (pairing.height);
						
					if (((jsp.getxAxis() == null) || !jsp.getxAxis().equals(newXAxis))
							|| ((jsp.getyAxis() == null) || !jsp.getyAxis().equals(newYAxis))) {
						jsp.setxAxis (newXAxis);
						jsp.setyAxis (newYAxis);
						jsp.repaint ();
					}
				}
			}
		}
	}
	
	void launchInternalFrame (final JScatterPlotMatrix jspm) {
		final JFrame jFrame = new JFrame ("Scatterplot");
		jFrame.setSize (450, 450);
		jFrame.setAlwaysOnTop (true);
		
		final JScatterPlot newJsp = new JScatterPlot ();
		newJsp.setAlpha (128);
		newJsp.setBackground (jspm.getBackground());
		newJsp.setSlaveTable (jspm);
		
		final JScatterPlotPanel jspPanel = new JScatterPlotPanel (newJsp);
		jFrame.getContentPane().add (jspPanel);
		
		setJsp (newJsp);
		
		jFrame.addWindowListener (
			new WindowAdapter () {
				@Override
				public void windowClosing (final WindowEvent event) {
					setJsp (null);
					jspPanel.setJsp (null);
				    jFrame.setVisible (false);
				    jFrame.dispose ();
				}
			}
		);
		
		jFrame.setVisible (true);
	}
}
