package example.graph.renderers.edge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import example.matrix.IndividualJudgement;

import model.graph.Edge;

import swingPlus.graph.AbstractGraphEdgeRenderer;
import util.colour.ColourArray;
import util.colour.RGBNonLinear;

public class AssessmentEdgeRenderer extends AbstractGraphEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3309916289376155650L;
	
 	static final private ColourArray SPECTRA = new ColourArray (new RGBNonLinear()) ;
 	static final private Stroke STROKE = new BasicStroke (2.0f);
 	
	public AssessmentEdgeRenderer () {
		setBorder (null);
		setBackground (Color.blue.brighter());
		setForeground (Color.black);
		//setFont (Font.decode ("Arial-plain-11"));
	}
	
	
    public Color getSpectra (final double value) {
	     return SPECTRA.getColour ((int)((5.0 - value) * 40.0));
    }

	
	@Override
	public void paintComponent (final Graphics graphics) {
		if (obj != null) {
			translateToOrigin (graphics);
			((Graphics2D)graphics).setStroke (STROKE);
			final IndividualJudgement indJudgement = (IndividualJudgement)((Edge)obj).getEdgeObject();
			//g.setColor (Color.black);
			graphics.setColor (getSpectra (indJudgement.getOverallRating()));
			graphics.drawLine (fromX, fromY, toX, toY);
		}
	}
}
