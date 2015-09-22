package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

import swingPlus.parcoord.JParCoord;
import swingPlus.scatterplot.ColPairLabelTableCellRenderer;
import swingPlus.scatterplot.JScatterPlot;
import swingPlus.scatterplot.JScatterPlotMatrix;
import util.GraphicsUtil;
import util.Messages;



public class ScatterPlotMatrixUI extends ParCoordUI {

	
	final private static Class<ScatterPlotMatrixUI> CLASSTYPE = ScatterPlotMatrixUI.class;
	final private static Logger logger = Logger.getLogger (CLASSTYPE);
	
	
	JScatterPlotMatrix sPlotMatrix;
	JScatterPlot rubberStamp;
	TableCellRenderer columnXYLabel;
    protected CellRendererPane rendererPane;
	private final MouseInputAdapter mouseAdapt = new ScatterPlotMouseListener ();
	private final ComponentListener resizeAdapt = new ResizeListener ();
	
	private final MessageFormat toolTipFormat = new MessageFormat (Messages.getString (CLASSTYPE, "tooltipTemplate"));

	
	//protected BufferedImage[] img = new BufferedImage [1];
	Point lastColumnRow = new Point (0, 0);	// last column/row (x/y) scatterplot 'cell' drawn in scatterplot matrix
	

	public static ScatterPlotMatrixUI createUI (final JComponent comp) {
	    return new ScatterPlotMatrixUI();
	}
	
	@Override
	public void installUI (final JComponent comp) {
        sPlotMatrix = (JScatterPlotMatrix)comp;
        table = (JTable)comp;
        
    	rubberStamp = new JScatterPlot ();
    	columnXYLabel = new ColPairLabelTableCellRenderer ();
    	
    	final Border border = BorderFactory.createMatteBorder (0, 0, 1, 1, Color.lightGray);
    	rubberStamp.setBorder (border);
    	((JComponent)columnXYLabel).setBorder (border);
     	
    	rendererPane = new CellRendererPane();
        comp.add (rendererPane);
        
        getActiveBounds();
        //super.installUI (sPlotMatrix);
        installDefaults (table);
        installListeners ();
    }

    @Override
	public void uninstallUI (final JComponent comp) {
        comp.remove (rendererPane);
        uninstallDefaults ((JTable)comp);
        uninstallListeners ();
        //super.uninstallUI (c);
    }
    
    protected void installDefaults (final JTable tComp) {
        //LookAndFeel.installColorsAndFont(BARLEY_PATTERN,
		//			 "Panel.background",
		//			 "Panel.foreground",
		//			 "Panel.font");
        //LookAndFeel.installBorder(BARLEY_PATTERN,"Panel.border");
        //LookAndFeel.installProperty(BARLEY_PATTERN, "opaque", Boolean.TRUE);
    	//mfs.setFontData ();
    }

    protected void uninstallDefaults (final JTable tComp) {
        //LookAndFeel.uninstallBorder(tPanel);
    }
    
	@Override
	protected void installListeners () {
		// Uninstall Handlers
		table.addMouseListener (mouseAdapt);
		table.addMouseMotionListener (mouseAdapt);
		table.addComponentListener (resizeAdapt);
	}


	@Override
	protected void uninstallListeners () {
		// Uninstall Handlers
		table.removeMouseListener (mouseAdapt);
		table.removeMouseMotionListener (mouseAdapt);
		table.removeComponentListener (resizeAdapt);
	}

	
	public void resetIncrementalDrawing () {
		lastColumnRow.setLocation (0, 0);
	}
	
	@Override
	// Overridden for performance reasons. No reason to blank background before paint
	// if we are blitting an image over the top anyways
    public void update (final Graphics graphics, final JComponent comp) {
		logger.debug ("Scatterplot Matrix UI update ");
		//	g.clearRect(0, 0, c.getWidth(), c.getHeight());
    	paint (graphics, comp);
    }
    
	/**
	 * Main painting routine.
	 */
	@Override
	public void paint (final Graphics graphics, final JComponent comp) {
    	
    	final Rectangle visRect = comp.getVisibleRect ();
    	final Rectangle clip = graphics.getClipBounds ();
    	logger.debug ("clip: "+clip+", vr: "+visRect);
    	logger.debug ("g: "+graphics+", g.hint: "+((Graphics2D)graphics).getRenderingHint (GraphicsUtil.DRAWTOFILE));
    	
    	final boolean nonScreenOutput = GraphicsUtil.isSVGGraphics (graphics) | GraphicsUtil.isNonScreenImage ((Graphics2D)graphics);

    	final Dimension cSize = visRect.getSize();
		img[UIConstants.ALL] = GraphicsUtil.initImage (img[UIConstants.ALL], (Component)comp, cSize, Transparency.OPAQUE);

        
        if (nonScreenOutput) {
        	//LOGGER.debug ("SCATTERPLOT MATRIX SVG");
            if (lastColumnRow.x == 0 && lastColumnRow.y == 0) {
            	GraphicsUtil.primeImageGraphics (graphics, graphics.getClipBounds(), img, comp.getBackground(), UIConstants.ALL);
            }
            resetIncrementalDrawing ();
        	draw (graphics, clip, false);
        } else if (img[UIConstants.ALL] != null) {
        	boolean redrawAll = ((JParCoord)table).isRedrawAll();	
        	boolean redrawSelections = ((JParCoord)table).isRedrawSelections();
        	if (redrawAll) {
                if (lastColumnRow.x == 0 && lastColumnRow.y == 0) {
                	GraphicsUtil.primeImageGraphics (graphics, graphics.getClipBounds(), img, comp.getBackground(), UIConstants.ALL);
                }
        	}
        	if (redrawAll || redrawSelections) {
        		draw (img[UIConstants.ALL].getGraphics(), clip, true);	
        	}
			graphics.drawImage (img[UIConstants.ALL], 0, 0, comp);
        }
	}
	
    
    protected void draw (final Graphics gContext, final Rectangle clip, final boolean stoppable) {
    	final long millis = System.currentTimeMillis();
    	
    	gContext.setColor (table.getForeground());
    	final int columnCount = table.getColumnCount();
    	final double ystep = table.getHeight() / (double)columnCount;
    	final Rectangle cellRect = new Rectangle (0, 0, 0, 0);
    	//rendererPane.setBounds (0, 0, sPlotMatrix.getWidth(), sPlotMatrix.getHeight());
    	float px = 0.0f, py = 0.0f, nextpx = 0.0f, nextpy = 0.0f;
     	boolean quit = false;
     	
     	logger.debug ("incoming lastX: "+lastColumnRow.x+", lastY: "+lastColumnRow.y+", colCount: "+columnCount);
    	
     	for (int x = 0; x < lastColumnRow.x; x++) {
     		px += table.getColumnModel().getColumn(x).getWidth();
     	}
     	nextpx = px;
     	
    	for (int x = lastColumnRow.x; x < columnCount && !quit; x++) {
    		nextpx += table.getColumnModel().getColumn (x).getWidth();
    		py = (float)(ystep * lastColumnRow.y);
    		nextpy = py;
    		
    		if (! (px > clip.getMaxX() || nextpx < clip.getX())) {
	    		for (int y = lastColumnRow.y; y <= x && !quit; y++) {	
	    			nextpy += ystep;
	    			
	    			if (x != y || sPlotMatrix.isRedrawAll()) { // dont redraw text labels if only selection needs redrawn
		    			final TableCellRenderer tcr = (x == y) ? columnXYLabel : rubberStamp;
		    			tcr.getTableCellRendererComponent (table, null, false, false, y, x);
		        		cellRect.setBounds (Math.round (px), Math.round (py), 
		        				Math.round (nextpx) - Math.round (px), 
		        				Math.round (nextpy) - Math.round (py));
		        		//LOGGER.debug ("cellRect: "+cellRect);
		        		//LOGGER.debug ("rendererPane: "+rendererPane.getBounds());
		        		if (cellRect.intersects (clip)) {
		        			logger.debug ("x: "+x+", y: "+y);
				        	rendererPane.paintComponent (gContext, (Component)tcr, table, cellRect);
		        		}
	    			}
	    			
	        		py = nextpy;
	        		
	        		if (System.currentTimeMillis() - millis > 100 && stoppable) {
	        			lastColumnRow.setLocation (x, y + 1);
	        			quit = true;
	        		}
	    		}
	    		if (!quit) {
	    			lastColumnRow.y = 0;
	    		}
    		}
    		px = nextpx;
    	}
    	
    	if (!quit) {
    		lastColumnRow.setLocation (columnCount, columnCount);
    	}
    	
    	rendererPane.removeAll ();
    	logger.debug ("lastX: "+lastColumnRow.x+", lastY: "+lastColumnRow.y);
    	if (lastColumnRow.x != columnCount && lastColumnRow.y != columnCount) {
    		table.repaint ();
    	} else {
    		resetIncrementalDrawing ();
    		sPlotMatrix.setRedrawAll (false);
    		sPlotMatrix.setRedrawSelections (false);
    	}
    }
    
    
    public Dimension getPlotUnderPoint (final int x, final int y) {
    	final int columnCount = table.getColumnCount();
    	//double xstep = sPlotMatrix.getWidth() / (double)columnCount;
    	final double ystep = table.getHeight() / (double)columnCount;
    	
    	final int tcx = table.getColumnModel().getColumnIndexAtX (x);
    	//int tcx = (int) ((double)x / xstep);
    	final int tcy = (int) ((double)y / ystep);
    	return new Dimension (tcx, tcy);
    }
    
    
    
	class ScatterPlotMouseListener extends MouseInputAdapter {
		int colX = 0, colY = 0;
		Object[] formatValues = new Object [2];
		
		@Override
		public void mouseMoved (final MouseEvent event) {
			if (isTableActive()) {
				final Dimension dim = getPlotUnderPoint (event.getX(), event.getY());
		    	
		    	if (dim.width != colX || dim.height != colY) {
		    		colX = dim.width;
		    		colY = dim.height;
		    		
		    		final String xCol = table.getColumnName (colX);
		    		final String yCol = table.getColumnName (colY);
		    		
		    		formatValues [0] = xCol;
		    		formatValues [1] = yCol;
	
		    		table.setToolTipText (toolTipFormat.format (formatValues));
		    	}
			}
		}
	}
	
	
    class ResizeListener extends ComponentAdapter {
    	@Override
    	public void componentResized (ComponentEvent cEvent) {
    		logger.debug ("height: "+cEvent.getComponent().getHeight());
    		sPlotMatrix.setRedrawAll (true);
    		resetIncrementalDrawing ();
    		sPlotMatrix.repaint() ;
    	}
    }
}

