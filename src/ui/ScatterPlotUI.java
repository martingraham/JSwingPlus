package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.BitSet;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.PanelUI;

import model.scatterplot.DensityPlot;
import model.shared.SortedTableColumn;
import model.shared.selection.TemporarySelectionModel;

import org.apache.log4j.Logger;

import swingPlus.scatterplot.JScatterPlot;
import swingPlus.scatterplot.JScatterPlotMatrix;
import swingPlus.shared.border.DashedBorder;
import ui.ParCoordUI.Combinator;
import util.GraphicsUtil;
import util.Messages;
import util.colour.ColorUtilities;


public class ScatterPlotUI extends PanelUI {

	final private static Class<ScatterPlotUI> CLASS = ScatterPlotUI.class;
	final static Logger LOGGER = Logger.getLogger (CLASS);
	final static String DRAGGING_BOX_TEXT = Messages.getString (CLASS, "draggingBoxText");
	
	JScatterPlot sPlot;
	Insets ins = new Insets (0, 0, 0, 0);
	private final MouseInputAdapter mouseAdapt = new ScatterPlotMouseListener ();
	private final ComponentAdapter compAdapt = new MyComponentListener ();
	private JLabel dragArea;
	Rectangle tempDragArea = new Rectangle ();
	Rectangle visibleRect = new Rectangle ();
	protected BufferedImage[] img = new BufferedImage [1];
	
	final MessageFormat toolTipFormat = new MessageFormat (Messages.getString (CLASS, "tooltipTemplate"));
	final MessageFormat dragToolTipFormat = new MessageFormat (Messages.getString (CLASS, "dragTooltipTemplate"));
	
	Dimension dotSize = new Dimension ();
	

	public static ScatterPlotUI createUI (final JComponent comp) {
	    return new ScatterPlotUI();
	}
	
	@Override
	public void installUI (final JComponent comp) {
        sPlot = (JScatterPlot)comp;
        sPlot.setLayout (null);
        dragArea = new DragLabel ();
        sPlot.add (dragArea);
        installListeners ();
    }

    @Override
	public void uninstallUI (final JComponent comp) {
        sPlot = (JScatterPlot)comp;
        sPlot.remove (dragArea);
        uninstallDefaults (sPlot);
        uninstallListeners ();
        super.uninstallUI (comp);
    }
    
    protected void installDefaults (final JScatterPlot tPanel) {
        //LookAndFeel.installColorsAndFont(BARLEY_PATTERN,
		//			 "Panel.background",
		//			 "Panel.foreground",
		//			 "Panel.font");
        //LookAndFeel.installBorder(BARLEY_PATTERN,"Panel.border");
        //LookAndFeel.installProperty(BARLEY_PATTERN, "opaque", Boolean.TRUE);
    	//mfs.setFontData ();
    }

    protected void uninstallDefaults (final JScatterPlot tPanel) {
        //LookAndFeel.uninstallBorder(tPanel);
    }
    
	protected void installListeners () {
		// Uninstall Handlers
		sPlot.addMouseListener (mouseAdapt);
		sPlot.addMouseMotionListener (mouseAdapt);
		sPlot.addComponentListener (compAdapt);
	}


	protected void uninstallListeners () {
		// Uninstall Handlers
		sPlot.removeMouseListener (mouseAdapt);
		sPlot.removeMouseMotionListener (mouseAdapt);
		sPlot.removeComponentListener (compAdapt);
	}

	
	@Override
	// Overridden for performance reasons. No reason to blank background before paint
	// if we are blitting an image over the top anyways
    public void update (final Graphics graphics, final JComponent comp) {
		//SortedTableColumn stcx = (SortedTableColumn) sPlot.getxAxis();
		//SortedTableColumn stcy = (SortedTableColumn) sPlot.getyAxis();
		//LOGGER.debug ("Scatterplot UI update "+stcx.getModelIndex()+"-"+stcy.getModelIndex());
    	paint (graphics, comp);
    }
    
	/**
	 * Main painting routine.
	 */
	@Override
	public void paint (final Graphics graphics, final JComponent comp) {

		if (visibleRect.getSize().width <= 0) {
			getActiveBounds ();
		}
		
    	final Dimension cSize = sPlot.getSize();
    	LOGGER.debug ("gclip: "+graphics.getClip()+", vr: "+visibleRect);
    	LOGGER.debug ("c: "+comp.getSize()+", cSize: "+cSize+", img: "+(img[UIConstants.ALL] == null ? "null" : img[UIConstants.ALL].getHeight()+"-"+img[UIConstants.ALL].getWidth()));
    	
       
        final boolean isSVG = GraphicsUtil.isSVGGraphics (graphics);
 
        if (isSVG || sPlot.isReplottingNecessary()) {
           	img[UIConstants.ALL] = GraphicsUtil.initImageWithinTolerance (img[UIConstants.ALL], (Component)comp, cSize, Transparency.OPAQUE, 10);
            final Graphics gContext = GraphicsUtil.primeImageGraphics (graphics, graphics.getClipBounds(), img, comp.getBackground(), UIConstants.ALL);
            if (isSVG) {
            	gContext.translate (ins.left, ins.top);
            }
            
            draw (gContext);
            
            if (isSVG) {
    		    gContext.translate (-ins.left, -ins.top);
    		}
            
            sPlot.setReplottingNecessary (false);
        }
        
        if (!isSVG) {
			final Image plotImage = img[UIConstants.ALL];
			if (plotImage != null) {
				final SortedTableColumn stcx = (SortedTableColumn) sPlot.getxAxis();
				final SortedTableColumn stcy = (SortedTableColumn) sPlot.getyAxis();
				final boolean ascendingXAxis = isAscendingAxis (stcx);
				final boolean ascendingYAxis = isAscendingAxis (stcy);
				
				//g.drawImage (im, ins.left, ins.top, c);
				graphics.drawImage (plotImage, 
			             ascendingXAxis ? 0 : plotImage.getWidth(comp), ascendingYAxis ? 0 : plotImage.getHeight(comp), 
			             ascendingXAxis ? plotImage.getWidth(comp) : 0, ascendingYAxis ? plotImage.getHeight(comp) : 0,
			             0, 0, plotImage.getWidth(comp), plotImage.getHeight(comp),
			             comp);
			}
		}
	}
	
	
    
    protected void draw (final Graphics gContext) {
    	final Graphics2D gContext2D = (Graphics2D)gContext;
    	gContext2D.setColor (sPlot.getForeground());
    	
    	final Dimension size = GraphicsUtil.adjustDimension (sPlot.getSize(), ins);
    	final SortedTableColumn stcx = (SortedTableColumn) sPlot.getxAxis();
    	final SortedTableColumn stcy = (SortedTableColumn) sPlot.getyAxis();
		if (stcx != null && stcy != null) {
			final JTable slaveTable = sPlot.getSlaveTable ();
	    	//ListSelectionModel selectionModel = sPlot.getSelectionModel ();
	    	//TableModel tm = sPlot.getModel();
			
			final int sx = stcx.getDiscreteRange();
			final int sy = stcy.getDiscreteRange();
			int defaultSize = size.getWidth() < 50 ? 1 : 5;
			final int dotw = Math.max (defaultSize, (sx == 0 ? 1 : (int)Math.ceil (size.getWidth() / sx)));
			final int doth = Math.max (defaultSize, (sy == 0 ? 1 : (int)Math.ceil (size.getHeight() / sy)));
			dotSize.setSize (dotw, doth);
	    	
	    	ListSelectionModel uberSelectionModel = null;
	    	Color uberSelectionColour = Color.black;
	    	if (slaveTable instanceof JScatterPlotMatrix) {
	    		uberSelectionModel = ((JScatterPlotMatrix)slaveTable).getUberSelection();
	    		uberSelectionColour = ((JScatterPlotMatrix)slaveTable).getUberSelectedColour();
	    	}
			
			
			DensityPlot dPlot = new DensityPlot (sPlot.getSlaveTable(), stcx, stcy, uberSelectionModel);
			if (dPlot.getEfficiency() > 1.0) {
				long nano = System.nanoTime ();
				dPlot.populate ();
				//if (sPlot.getWidth() > 100) {
				//	System.err.println (dPlot.toString());
				//}
				//LOGGER.debug (dPlot.toString());
				nano = System.nanoTime() - nano;
				LOGGER.debug ("Density plot computation: "+nano/1E6+" ms");
			} else {
				dPlot = null;
			}
	
			final Color[] alphaedColours = {ColorUtilities.addAlpha (slaveTable.getForeground(), sPlot.getAlpha()),
									ColorUtilities.addAlpha (slaveTable.getSelectionForeground(), sPlot.getAlpha()),
									ColorUtilities.addAlpha (((JScatterPlotMatrix)slaveTable).getUberSelectedColour(), sPlot.getAlpha())};
			
			if (dPlot != null) {
				long nano = System.nanoTime ();
				final int[][][] densities = dPlot.getDensityPlot();
				
				for (int row = 0; row < densities[0].length; row++) {
					for (int col = 0; col < densities.length; col++) {
						if (densities[col][row][0] + densities[col][row][1] + densities[col][row][2] > 0) {
							final Color colour = getColorFromDensity (densities[col][row], alphaedColours);
	    					gContext.setColor (colour); 
	    					final int x = ((size.width) * col) / densities.length;
	    					final int y = ((size.height) * row) / densities[0].length;
	    					gContext2D.fillRect (x, y, dotSize.width, dotSize.height);
						}
					}
				}
		    	nano = System.nanoTime() - nano;
				LOGGER.debug ("vs Density plot drawing time: "+nano/1E6+" ms");
			}
			else {
	    		long nano = System.nanoTime ();
	    		//if (size.width > 80) {
	    			//System.err.println ("n sel: "+slaveTable.getSelectionModel());
	    			//System.err.println ("u sel: "+uberSelectionModel);
	    		//}
	    		drawPlot (gContext2D, alphaedColours [0], null, false, true, 0, size, dotSize);
		    	drawPlot (gContext2D, alphaedColours [1], slaveTable.getSelectionModel(), true, true, 1, size, dotSize);
		    	drawPlot (gContext2D, alphaedColours [2], uberSelectionModel, true, false, 2, size, dotSize);

		    	nano = System.nanoTime() - nano;
				LOGGER.debug ("vs Overpainting time: "+nano/1E6+" ms");
			}
		}
    }
    
    
    protected void drawPlot (final Graphics2D gContext2D, final Color plotColour, final ListSelectionModel selectionModel,
    		final boolean drawSelectedOnly, final boolean selectionModelIsViewIndexed, final int layer, 
    		final Dimension size, final Dimension dotSize) {

    	final boolean selectionModelPresent = (selectionModel != null);
    	
    	if ((selectionModelPresent && !selectionModel.isSelectionEmpty()) || !drawSelectedOnly) {
    		final SortedTableColumn stcx = (SortedTableColumn) sPlot.getxAxis();
    		final SortedTableColumn stcy = (SortedTableColumn) sPlot.getyAxis();
			
			//if (sPlot.getWidth() > 100) {
			//	System.err.println ("layer: "+layer);
			//}
			
			if (stcx != null && stcy != null) {
				final int xIndex = stcx.getModelIndex();
				final int yIndex = stcy.getModelIndex();
				final JTable slaveTable = sPlot.getSlaveTable ();
		    	gContext2D.setColor (plotColour);
	
		    	final int minRowIndex = !selectionModelPresent ? 0 : selectionModel.getMinSelectionIndex();
		    	final int maxRowIndex = !selectionModelPresent ? slaveTable.getRowCount() - 1 : 
		    			selectionModel.getMaxSelectionIndex();
		    			//Math.min (selectionModel.getMaxSelectionIndex(), slaveTable.getRowCount() - 1); // + 1;
	    		//if (sPlot.getWidth() > 100 && selectionModel != null) {
	    		//	System.err.println ("msi: "+selectionModel.getMaxSelectionIndex()+", strc: "+slaveTable.getRowCount());
	    		//}
		    	if (maxRowIndex >= minRowIndex && minRowIndex >= 0) {
		    	
		    		//if (sPlot.getWidth() > 100) {
		    		//	System.err.println ("hlayer: "+layer+", model: "+selectionModel);
		    		//}
			    	ListSelectionModel uberSelectionModel = null;
			    	if (slaveTable instanceof JScatterPlotMatrix) {
			    		uberSelectionModel = ((JScatterPlotMatrix)slaveTable).getUberSelection();
			    	}
			    	final ListSelectionModel selectionModel2 = slaveTable.getSelectionModel ();

			    	for (int row = 0; row <= maxRowIndex; row++) {	
			    	//for (int row = minRowIndex; row <= maxRowIndex; row++) {	
			    		final int viewRow = selectionModelIsViewIndexed ? row : slaveTable.convertRowIndexToView (row);
			    		//int modelRow = slaveTable.convertRowIndexToModel (row);
			    		//if (viewRow > 0) {
			    		//if (viewRow < 0) {
			    		//	System.out.println ("viewROw: "+viewRow);
			    		//}
			    		final int modelRow = selectionModelIsViewIndexed ? slaveTable.convertRowIndexToModel (row) : row;
			    		if (modelRow >= 0) {
				    		final int selected = (uberSelectionModel != null && uberSelectionModel.isSelectedIndex(modelRow) ? 2 : 
				    			(selectionModel2.isSelectedIndex(viewRow) ? 1 : 0));
					    		
				    		if (selected == layer) {
					    		//System.out.println ("xindex: "+xIndex+", vrow: "+row);
					    		//System.out.println ("mrow: "+slaveTable.convertRowIndexToModel (row));
				    			final Object xobj = slaveTable.getModel().getValueAt (modelRow, xIndex);
					    		//System.out.println ("yindex: "+yIndex);
				    			final Object yobj = slaveTable.getModel().getValueAt (modelRow, yIndex);
					    		
					    		if (xobj != null && yobj != null) {
					    			final int x = getX (xobj, stcx, size.width);
					    			final int y = getY (yobj, stcy, size.height);
						    		
						    		gContext2D.fillRect (x, y, dotSize.width, dotSize.height);
					    		}
							}
			    		}
					}
		    	}
			}
    	}
    }

	// Work out colour and alpha rather than splat hundreds of
	// shapes of the same colour on top of each other
	// AlphaComposite.SRC_OVER rule = Cd' = Cs*alph + Cd*(1 - alph)
	Color getColorFromDensity (final int[] twoStepDensity, final Color[] alphaedColours) {
		final int unselectedDensity = twoStepDensity [0];
		final int selectedDensity = twoStepDensity [1];
		final int uberSelectedDensity = twoStepDensity [2];
		
		final Color c = ColorUtilities.repeatedlyOverlayAlphaColour (alphaedColours [0], sPlot.getBackground(), unselectedDensity);
		final Color c2 = ColorUtilities.repeatedlyOverlayAlphaColour (alphaedColours [1], c, selectedDensity);
		final Color c3 = ColorUtilities.repeatedlyOverlayAlphaColour (alphaedColours [2], c2, uberSelectedDensity);

		return c3;
     }
    
    
    private void getActiveBounds () {
    	//sPlot.computeVisibleRect (visibleRect);
    	visibleRect = sPlot.getBounds();
    	LOGGER.debug ("vr: "+visibleRect);
    	sPlot.getInsets (ins);
    	GraphicsUtil.adjustRectangle (visibleRect, ins);
    }

    
    boolean isAscendingAxis (final SortedTableColumn<?> stc) {
    	return stc == null || stc.getCurrentOrder().equals (SortOrder.ASCENDING);
    }
    
    Comparable getXObj (final int x) {
    	return getObj ((double)(x - ins.left) / ((double)visibleRect.width), sPlot.getxAxis());
    }
    
    Comparable getYObj (final int y) {
    	return getObj ((double)(y - ins.top) / (double)visibleRect.height, sPlot.getyAxis());
    }
    
    Comparable getObj (double ratio, final SortedTableColumn<?> axis) {
    	final double range = (double)axis.getDiscreteRange ();
		if (range > 0) {
			ratio = 0.5 - ratio;
			ratio *= ((range) / (range - 1));
			ratio = 0.5 - ratio;
		}

		if (!isAscendingAxis (axis)) {
			ratio = 1.0 - ratio;
		}

		return axis.getValue (ratio);
    }
    

    
    int getX (final Object xObj, final SortedTableColumn<?> xAxis, final int width) {
    	final int x = xAxis.getY (xObj, width - 1 - dotSize.width);
    	//if (!xAxis.getCurrentOrder().equals (SortOrder.ASCENDING)) {
		//	x = (width - dotSize.width)- x;
		//}
    	return x + ins.left;
    }
    
    int getY (final Object yObj, final SortedTableColumn<?> yAxis, final int height) {
    	final int y = yAxis.getY (yObj, height - 1 - dotSize.height);
    	//if (!yAxis.getCurrentOrder().equals (SortOrder.ASCENDING)) {
		//	y = (height - dotSize.height) - y;
		//}
    	return y + ins.top;
    }
    
	class ScatterPlotMouseListener extends MouseInputAdapter {
		
		int sx = 0, sy = 0;
		Comparable curXObj, curYObj;
		Comparable sxObj, syObj;
		TemporarySelectionModel tsm = new TemporarySelectionModel ();
		BitSet bs = new BitSet ();
		BitSet bs2 = new BitSet ();
		Object[] formatValues = new Object [6];
		
		void fillFormatValues (final Object xObj, final Object yObj, final Object dragXObj, final Object dragYObj) {
    		formatValues [0] = xObj;
    		formatValues [1] = sPlot.getxAxis().getHeaderValue().toString();
    		formatValues [2] = yObj;
    		formatValues [3] = sPlot.getyAxis().getHeaderValue().toString();
    		formatValues [4] = dragXObj;
    		formatValues [5] = dragYObj;
		}
		
		boolean modelPresent () {
			return sPlot.getxAxis() != null && sPlot.getyAxis() != null;
		}
		
		@Override
		public void mouseMoved (final MouseEvent mEvent) {
			if (modelPresent()) {
				final Comparable xObj = getXObj (mEvent.getX());
				final Comparable yObj = getYObj (mEvent.getY());
				
				if (xObj != curXObj || yObj != curYObj) {
					curXObj = xObj;
					curYObj = yObj;
					fillFormatValues (xObj, yObj, null, null);
		    		sPlot.setToolTipText (toolTipFormat.format (formatValues));
				}
			}
		}
		
		@Override
		public void mousePressed (final MouseEvent mEvent) {
			if (modelPresent()) {
				sx = mEvent.getX();
				sy = mEvent.getY();
				sxObj = getXObj (sx);
				syObj = getYObj (sy);
				dragArea.setText (DRAGGING_BOX_TEXT);
				tsm.clear ();
				tsm.copy (sPlot.getSlaveTable().getSelectionModel());
			}
		}
		
		
		@Override
		public void mouseDragged (final MouseEvent mEvent) {
			if (modelPresent()) {
				dragArea.setVisible (true);
				final int fx = Math.min (Math.max (0, mEvent.getX()), sPlot.getWidth());
				final int fy = Math.min (Math.max (0, mEvent.getY()), sPlot.getHeight());
				tempDragArea.setFrameFromDiagonal (sx, sy, fx, fy);
				if (! dragArea.getBounds().equals (tempDragArea)) {
					dragArea.setBounds (tempDragArea);
					final Object dxObj = getXObj (fx);
					final Object dyObj = getYObj (fy);
					fillFormatValues (sxObj, syObj, dxObj, dyObj);
					sPlot.setToolTipText (dragToolTipFormat.format (formatValues));
					ToolTipManager.sharedInstance().mouseMoved (mEvent);
					sPlot.repaint(0, 0, sPlot.getWidth() - 1, sPlot.getHeight());
				}
			}
		}
		
		
		@Override
		public void mouseReleased (final MouseEvent mEvent) {
			if (modelPresent()) {
				dragArea.setVisible (false);	
				
				final Comparable fxObj = getXObj (mEvent.getX());
				final Comparable fyObj = getYObj (mEvent.getY());
				
				final Dimension dimx = new Dimension ();
				sPlot.getxAxis().getRange (dimx, sxObj, sxObj, fxObj, fxObj);
	
				final Dimension dimy = new Dimension ();
				sPlot.getyAxis().getRange (dimy, syObj, syObj, fyObj, fyObj);
	
				LOGGER.debug ("dimx: "+dimx+", dimy: "+dimy);
				LOGGER.debug ("syobj: "+syObj+", fyObj: "+fyObj);
				LOGGER.debug ("sxobj: "+sxObj+", fxObj: "+fxObj);
				final ListSelectionModel sModel = sPlot.getSlaveTable().getSelectionModel();
				sModel.setValueIsAdjusting (true);
				Combinator comb = null;
				if (mEvent.isAltDown ()) {
					comb = Combinator.FILTER;
				}
				else if (mEvent.isControlDown()) {
					comb = Combinator.ADD;
				}
				
				if (comb == null) {
					sModel.clearSelection();
					comb = Combinator.ADD;
				}			
						
				LOGGER.debug ("x: "+sxObj+" to "+fxObj+", y: "+syObj+" to "+fyObj);
				LOGGER.debug ("x: "+dimx.width+" to "+dimx.height+", y: "+dimy.width+" to "+dimy.height);
				
				tsm.clear ();
				bs.clear ();
				bs2.clear ();
				tsm.selectRange2 (dimx.width, dimx.height, bs, sPlot.getSlaveTable().getRowSorter(), sPlot.getxAxis(), Combinator.ADD);
				LOGGER.debug ("bs: "+bs);
				tsm.selectRange2 (dimy.width, dimy.height, bs2, sPlot.getSlaveTable().getRowSorter(), sPlot.getyAxis(), Combinator.ADD);
				LOGGER.debug ("bs2: "+bs2);
				bs.and (bs2);
				LOGGER.debug ("bs: "+bs+", nsb: "+bs.nextSetBit(0)+", length: "+bs.length());
				//tsm.copy (sModel);
				// lsm now contains square 'selection'
				tsm.modify (sModel, bs, sPlot.getSlaveTable().getRowSorter(), sPlot.getxAxis() /* or yaxis it doesn't matter */, comb);
				//add or filter to existing selection
				sPlot.repaint();
				sModel.setValueIsAdjusting (false);
			}
		}
	}
	
	
    class MyComponentListener extends ComponentAdapter {
    	@Override
    	public void componentResized (final ComponentEvent cEvent) {
    		getActiveBounds ();
    		ScatterPlotUI.this.sPlot.resizeAndRepaint ();
    	}
    }
    
	
	static class DragLabel extends JLabel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3495157457429524510L;
		
		public DragLabel () {
			super ();
			setHorizontalAlignment (SwingConstants.CENTER);
			setOpaque (true);
			final Color bg = Color.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "ParCoordUI.selectedColour"));
		    setBackground (new Color (bg.getRed(), bg.getGreen(), bg.getBlue(), 128));
		    setForeground (Color.white);
			setBorder (new DashedBorder (Color.white, 
				new BasicStroke (2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.5f, new float[] {8.0f, 8.0f}, 0.0f)) );
		}
	}
}
