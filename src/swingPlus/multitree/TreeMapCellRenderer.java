package swingPlus.multitree;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;

import ui.TreeMapUI;
import util.colour.ColorUtilities;

public class TreeMapCellRenderer extends AbstractTreeCellRenderer2 {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8473654526208091946L;
	
	boolean drawnKids = false;
	int depth = 0;
	int minHeight = 0;
	Color[] selBackground = new Color [10];

	
	@Override
	public Component getTreeCellRendererComponent (final JTree tree, final Object value,
			final boolean selected, final boolean expanded, final boolean leaf, final int row,
			final boolean hasFocus) {
		
    	Color selBackgroundInit = UIManager.getDefaults().getColor ("Tree.selectionBackground");
    	for (int n = 0; n < selBackground.length; n++) {
    		selBackground [n] = ColorUtilities.darkenSlightly (selBackgroundInit, 0.05f);
    		selBackgroundInit = selBackground [n];
    	}
		super.getTreeCellRendererComponent (tree, value, selected, expanded, leaf, row, hasFocus);
		return this;
	}
	
	
	public void set (final int depth, final boolean drawnKids, final int minHeight) {
		this.depth = depth;
		this.drawnKids = drawnKids;
		this.minHeight = minHeight;
	}
	
	
	public void draw (final Graphics gContext) {
		   
		final double x = this.getX();
		final double y = this.getY();
		final double width = this.getWidth ();
		final double origHeight = this.getHeight ();
		   
	    if (((TreeMapUI)tree.getUI()).getLastVisibleRect().intersects (x, y, width, origHeight + minHeight)) {
	    	
	    	//final ColourScheme cs = sm.getCurrentColourScheme();
	    	final Object userObject = node.getUserObject();
	
	        final int height = drawnKids ? minHeight : (int)origHeight + minHeight;
	        final int ty = (int)(y - minHeight);
	        final int ymid = (ty + (height >> 1) + (height & 1));
	        //final int selected = mfni.getSelected();
			//final int highAccent = Math.max (selected, mfnl.getConceptSelected ());
			//final boolean brushAccent = mfni.isBrushed() | mfnl.isConceptBrushed ();
			//final TreePanelRep tpr = tmapPanel.getDataRep();
			final boolean root = (tree.getModel().getRoot() == node);
	
	        //final int total = mfnl.getTotalDescendents();
	        final boolean leaf = node.isLeaf();
	        //gContext.setColor (brushAccent ?
	        //        (highAccent > 0 ? sm.getLinkLightColours (highAccent - 1) :  cs.getUnselectedBrushed()) :
	        //        (highAccent > 0 ? sm.getLinkColours (highAccent - 1) : cs.getUnselected()) );
	        //if (root && highAccent == 0) {
	        //	gContext.setColor (gContext.getColor().brighter());
	        //}
	        
	        final Color depthColour = selBackground [Math.min (selBackground.length - 1, depth)];
	        gContext.setColor (selected ? Color.red : depthColour);
	        
		    if (!drawnKids) {
				gContext.fill3DRect ((int)x, ty, (int)width + 1, height + 1, true);		
			} else {
				gContext.fillRect((int)x, ty, (int)width, height);
				//gContext.setColor (sm.getCurrentColourScheme().getBackground2());
				gContext.draw3DRect ((int)x, (int)y, (int)width, (int)origHeight, true);
			}
	        	
	        if (!leaf) {
	            //gContext.setColor (brushAccent ? cs.getUnselectedBrushed() : cs.getUnselected()); // colour
	            int runningTotal = 0;
				final int includedSelections = 0;
	            final double maxSize =  Math.min (4.0, (width - 1) / (includedSelections > 0 ? includedSelections : 1));
	            double extraWidth = 0.0;
	
	            /*
		        for (int n = tmapPanel.getForestView().getForestModel().getSelectIndex() + 1; --n >= 1;) {
	                final int chosen = mfnl.getChosenDescendents (n);
	                if (chosen > 0) {
	                   final double prop = (double)chosen / (double)total;
	                   double propWidth = Math.max (maxSize, prop * width);
	                   final double propStart = extraWidth + (((double)runningTotal / (double)total) * (width - 1 - extraWidth));
	                   extraWidth += Math.max (0, maxSize - (prop * width));
	
	                   propWidth = Math.max (1, propWidth);
	                   gContext.setColor (brushAccent ? sm.getLinkLightColours (n-1) : sm.getLinkDarkColours (n-1));
			           if (root) {
	                      gContext.setColor (gContext.getColor().brighter());
	                   }
	                   gContext.fillRect ((int)x + (int)propStart, ymid, (int)propWidth, (height >> 1));
	 
			           runningTotal += chosen;
	                }
	            }
	            */
			}
	        
	        if (height >= minHeight) {
	        	writeNodeName (gContext, tree, x, width, ty, height, 0, false, node);
	        }
	    }
	}
		
   private void writeNodeName (final Graphics graphics, final JTree tmapPanel, 
    		final double x, final double width, final double y, final double height, 
    		final int highAccent, final boolean brushAccent, final DefaultMutableTreeNode node) {

        final Object userObject = node.getUserObject();
       // final SpectrumModel sm = tmapPanel.getTreeMapView().spectrumViewGetModel();
        //final ColourScheme cs = sm.getCurrentColourScheme();

        //final TMapFontSizer tmfs = (TMapFontSizer)TreeMapUI.getFontSizer();
        final Graphics2D g2d = (Graphics2D)graphics;
	    graphics.setFont (tree.getFont());
        final FontMetrics fontMetrics = graphics.getFontMetrics();
        String str = null;
        if (tmapPanel.getModel().getRoot() == node) {
            str = tmapPanel.getName() + userObject.toString();
        } else {
            str = userObject.toString();
        }
 
		final int sWidth = fontMetrics.stringWidth (str);
		final Rectangle visRect = ((TreeMapUI)tmapPanel.getUI()).getLastVisibleRect();
		final double minx = Math.max (x, visRect.getX());
		final double maxx = Math.min (x + width, visRect.getMaxX());
		final int visWidth = (int)(maxx - minx);

		if (visWidth > 4) {
			final int texty = (int)y + ((fontMetrics.getAscent() - fontMetrics.getDescent() + 1 + (int)height) >> 1);
			graphics.setColor (Color.gray);
		    if (sWidth > visWidth) {
		    	final AffineTransform affTransform = g2d.getTransform();
		    	final double scale = (double)visWidth / (double)sWidth;
		    	//LOGGER.debug ("scale: "+scale);
		    	g2d.scale (scale, scale);
		    	graphics.drawString (str, (int)((1.0 / scale) * minx), (int)((1.0 / scale) * texty));
				g2d.setTransform (affTransform);
			}
			else {
				graphics.drawString (str, (int)minx + ((visWidth - sWidth) >> 1), texty);
			}
		}
	}
}
