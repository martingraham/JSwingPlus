package swingPlus.graph;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ForceAnalyserGraphPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6157267199218183618L;

	public ForceAnalyserGraphPanel (final JGraph jgraph, final ForceAnalyser fAnalyser, final int analyserCompassPoint) {
		super ();
		
		this.setLayout (new BorderLayout ());
		this.add (jgraph, SwingConstants.CENTER);
		this.add (fAnalyser, analyserCompassPoint);
		
		fAnalyser.setPreferredSize (new Dimension (128, 640));
	}
}
