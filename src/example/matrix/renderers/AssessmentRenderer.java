package example.matrix.renderers;

import swingPlus.matrix.AbstractEdgeRenderer;
import util.GraphicsUtil;
import util.Messages;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import example.matrix.IndividualJudgement;
import example.matrix.JudgementTypeInterface;

import util.colour.ColourArray;
import util.colour.RGBNonLinear;


public class AssessmentRenderer extends AbstractEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 309750583683062153L;
	
 	static final private ColourArray SPECTRA = new ColourArray (new RGBNonLinear()) ;
	public final static Color DISCRETECOLOUR [] = {SPECTRA.getColour (200), SPECTRA.getColour (160),
            SPECTRA.getColour (120), SPECTRA.getColour (80), SPECTRA.getColour (40), SPECTRA.getColour (0)};

	private final static Font DETAILFONT = Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "MatrixUI.cellFont"));
	
    public AssessmentRenderer () {
    	super ();
	    // Don't paint behind the component
	    setOpaque (true);
	    setBackground (Color.red); // garish colour just so we can be certain no background rendering inputStream happening
	    prefSize.setSize (100, 150);
    }
    
    
    
    public Color getSpectra (final double value) {
	     return SPECTRA.getColour ((int)((5.0 - value) * 40.0));
   }
    
    
    @Override
	public void paintComponent (final Graphics gContext) {

    	if (edge != null) {
	    	final IndividualJudgement iJudgement = (IndividualJudgement) edge.getEdgeObject ();
			final JudgementTypeInterface jti = iJudgement.getJudgementType();
			//final int attributeTotal = jti.getAttributeTotal();
	
			double value1 = 0.0;
			int depthLevel = 0;
			List<List<Integer>> cumuDepthLists = null; 
			List<Integer> cumuDepthList = null;
	
			final Rectangle drawSpace = this.getBounds();
			if (drawSpace.height > 1) {
				cumuDepthLists = jti.getCumulativeDepthLists();
				int depth = cumuDepthLists.size();
				while (depth > 0 && depthLevel == 0) {
					depth--;
					cumuDepthList = cumuDepthLists.get (depth);
					if (cumuDepthList.size() * 2 <= drawSpace.height) {
						depthLevel = depth;
					}
				}
			}
	
			if (drawSpace.width < 10 || depthLevel == 0 /*|| !drawDetail*/) {
	
				gContext.setColor (getSpectra (iJudgement.getOverallRating()));
				gContext.fillRect (0, 0, Math.max (drawSpace.width, 2), Math.max (drawSpace.height, 2));
				if (drawSpace.width > 2 && drawSpace.height > 2 /*&& !filtered*/) {
					//gContext.setColor (DisplayPanel2.REFLECTGUIDECOLOUR);
					gContext.setColor (BLACKGRADE);
					((Graphics2D)gContext).drawLine (0, 0 + drawSpace.height - 1, 0 + drawSpace.width - 1, 0 + drawSpace.height - 1);
					((Graphics2D)gContext).drawLine (0 + drawSpace.width - 1, 0, 0 + drawSpace.width - 1, 0 + drawSpace.height - 1);
				}
			}
			else {
	        	gContext.setFont (DETAILFONT);
	        	final FontMetrics fMetrics = gContext.getFontMetrics ();
				List<Integer> depthList = jti.getDepthList (depthLevel);
				final double subAttHeight = ((double)(drawSpace.height - 1.0) / (double)depthList.size());
				final int singleWidthIndent = Math.max (5, drawSpace.width / (depthLevel + 2));
	
				for (int n = depthLevel + 1; --n >= 0;) {
	
					depthList = jti.getDepthList (n);
					final int offset = singleWidthIndent * n;
					final int widthIndent = depthLevel == n ? drawSpace.width - offset : singleWidthIndent;
	
					double nodey = 0.0;
					final int nodex = offset;
	
					for (int m = 0; m < depthList.size(); m++) {
	
	        			final int attIndex = ((Integer) depthList.get(m)).intValue();
						value1 = iJudgement.getAttributeValue (attIndex);
						final int desc = jti.totalDescendantNodes (attIndex, depthLevel);
						final double height = desc * subAttHeight;
						final double lineBot = nodey + height - 1;
	
						//setColor (jti.getAttributeFlag (attIndex) ? getSpectra (value1) : DisplayPanel2.ASSESSMENT_FILTERED);
						gContext.setColor (getSpectra (value1));
	
						gContext.fillRect (nodex, (int)Math.ceil (nodey), widthIndent, (int)Math.ceil (height));
	
						if (height > 10 && widthIndent > 10) {
							gContext.setColor (BLACKGRADE2);
							final String attName = jti.getAttributeName (attIndex);
							final int length = fMetrics.stringWidth (attName);
							String subString;
							if (length >= widthIndent) {
								final int substringLength = GraphicsUtil.stringCharsFitLength2 (widthIndent, attName, length, fMetrics, false);
								subString = attName.substring (0, substringLength);
							} else {
								subString = attName;
							}
							final double stringy = lineBot - (Math.max (0, (height - 10.0)) / 2.0) - 1.0;
							gContext.drawString (subString, nodex, (int)stringy);
						}
	
						nodey += height;
					}
	
					nodey = 0.0;
	
					for (int m = 0; m < depthList.size(); m++) {
	
	        			final int attIndex = ((Integer) depthList.get(m)).intValue();
						final int desc = jti.totalDescendantNodes (attIndex, depthLevel);
						final double height = desc * subAttHeight;
						final double lineBot = nodey + height - 1;
	
						//setColor (jti.getAttributeFlag (attIndex) ? blackGrade : DisplayPanel2.ASSESSMENT_FILTERED);
						gContext.setColor (BLACKGRADE);
	
						((Graphics2D)gContext).drawLine (nodex, (int)Math.ceil (lineBot), nodex + widthIndent - 1, (int)Math.ceil (lineBot));
						((Graphics2D)gContext).drawLine (nodex + widthIndent - 1, (int)Math.ceil (nodey), nodex + widthIndent - 1, (int)Math.ceil (lineBot));
	
						nodey += height;
					}
	
				}
			}
    	}
    }
    
    @Override
    public Dimension getPreferredSize () { return prefSize; }
}
