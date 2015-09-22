package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import swingPlus.matrix.JMatrix;
import swingPlus.shared.CellRendererPane2;
import swingPlus.shared.JLogSlider;
import util.collections.ArrayListUtil;
import util.colour.ColorUtilities;

import model.graph.Edge;
import model.matrix.MatrixTableModel;


public class MatrixUI extends BasicTableUI {

	private final static Logger LOGGER = Logger.getLogger (MatrixUI.class);
	private static final Double ZERO = Double.valueOf (0.0);
	
	//private final Comparator<Edge> columnSorter = new ViewColumnSorter ();
	private final EdgeOppositeNodeSorter nodeBasedEdgeSorter = new EdgeOppositeNodeSorter ();
	MouseAdapter mouseOverListener = new MouseOverListener ();
	ComponentAdapter keepScaleWidgetInCornerListener = new MatrixComponentListener ();
	JLogSlider scaleSlider;
	double scale = 1.0;
	ChangeListener sliderListener = new ScaleListener ();
	Rectangle visibleRect = new Rectangle ();
	
	
	public static ComponentUI createUI (final JComponent comp) {
		return new MatrixUI();
	}
	
	@Override
    public void installUI (final JComponent comp) {
    	super.installUI (comp);
    	rendererPane = new CellRendererPane2 ();
    	table.addMouseListener (mouseOverListener);
    	table.addMouseWheelListener (mouseOverListener);
    	table.addComponentListener (keepScaleWidgetInCornerListener);
    }
	
	@Override
    public void uninstallUI (final JComponent comp) {
        super.uninstallUI (comp);
        table.removeMouseListener (mouseOverListener);
        table.removeMouseWheelListener (mouseOverListener);
        table.removeComponentListener (keepScaleWidgetInCornerListener);
    }
	
	@Override
	protected void installDefaults () {
		super.installDefaults ();
		setSlider (new JLogSlider (JSlider.VERTICAL, -3, 3));
	}
	
	protected void uninstallDefaults (final JComponent tPanel) {
        super.uninstallDefaults ();
        removeSlider ();
    }
	
	
	
	public void setSlider (final JLogSlider newSlider) {
		if (scaleSlider != null) {
			newSlider.setMinimum (scaleSlider.getMinimum ());
			newSlider.setMaximum (scaleSlider.getMaximum ());
			newSlider.setValue (scaleSlider.getValue ());
			removeSlider ();
		}
		
		scaleSlider = newSlider;
		
		if (scaleSlider != null) {
			scaleSlider.addChangeListener (sliderListener);
			if (scaleSlider.getParent() != null) {
				scaleSlider.getParent().remove (scaleSlider);
			}
			table.add (scaleSlider);
			table.addPropertyChangeListener (scaleSlider);
			scaleSlider.setLocation (20, 20);
			scaleSlider.setSize (30, 128);
		}
	}
	
	void removeSlider () {
		scaleSlider.removeChangeListener (sliderListener);
		table.removePropertyChangeListener (scaleSlider);
		table.remove (scaleSlider);
	}
	   /** Paint a representation of the <code>table</code> instance
     * that was set in installUI().
     */
    @Override
	public void paint (final Graphics graphics, final JComponent comp) {
		
		long nano = System.nanoTime();
		
		final Rectangle clip = graphics.getClipBounds();
        //clip.setSize (d)
        LOGGER.debug ("clip: "+clip);
        final Rectangle bounds = table.getBounds();
        LOGGER.debug ("bounds: "+bounds);
        
        // account for the fact that the graphics has already been translated
        // into the table's bounds
        bounds.x = bounds.y = 0;

        if (table.getRowCount() <= 0 || table.getColumnCount() <= 0 ||
                // this check prevents us from painting the entire table
                // when the clip doesn't intersect our bounds at all
                !bounds.intersects(clip)) {

            paintDropLines(graphics);
            return;
		}

        final boolean ltr = table.getComponentOrientation().isLeftToRight();

        Point upperLeft = clip.getLocation();
        if (!ltr) {
            upperLeft.x++;
        }

        final Point lowerRight = new Point(clip.x + clip.width - (ltr ? 1 : 0),
                                     clip.y + clip.height);

        int rMin = table.rowAtPoint(upperLeft);
        int rMax = table.rowAtPoint(lowerRight);
        // This should never happen (as long as our bounds intersect the clip,
        // which inputStream why we bail above if that inputStream the case).
        if (rMin == -1) {
        	rMin = 0;
        }
        // If the table does not have enough rows to fill the view we'll get -1.
        // (We could also get -1 if our bounds don't intersect the clip,
        // which inputStream why we bail above if that inputStream the case).
        // Replace this with the index of the last row.
        if (rMax == -1) {
        	rMax = table.getRowCount()-1;
        }

        int cMin = table.columnAtPoint(ltr ? upperLeft : lowerRight); 
        int cMax = table.columnAtPoint(ltr ? lowerRight : upperLeft);        
        // This should never happen.
        if (cMin == -1) {
        	cMin = 0;
        }
        // If the table does not have enough columns to fill the view we'll get -1.
        // Replace this with the index of the last column.
        if (cMax == -1) {
        	cMax = table.getColumnCount()-1;
        }

        // Paint the grid.
        paintGrid (graphics, rMin, rMax, cMin, cMax);

        // Paint the cells.
        paintCells (graphics, rMin, rMax, cMin, cMax);

        paintDropLines(graphics);
        
		nano = System.nanoTime() - nano;
		LOGGER.debug ("Painted matrix in "+(nano/1E6)+" ms.");
    }
    
    
    
    private void paintDropLines (final Graphics graphics) {
    	final JTable.DropLocation loc = table.getDropLocation();
        if (loc == null) {
            return;
        }

        final Color color = UIManager.getColor ("Table.dropLineColor");
        final Color shortColor = UIManager.getColor ("Table.dropLineShortColor");
        if (color == null && shortColor == null) {
            return;
        }

        Rectangle rect;

        rect = getHDropLineRect(loc);
        if (rect != null) {
        	final int x = rect.x;
        	
            if (color != null) {
                extendRect(rect, true);
                graphics.setColor (color);
                graphics.fillRect (rect.x, rect.y, rect.width, rect.height);
            }
            if (!loc.isInsertColumn() && shortColor != null) {
            	final int width = rect.width;
                graphics.setColor (shortColor);
                graphics.fillRect (x, rect.y, width, rect.height);
            }
        }

        rect = getVDropLineRect (loc);
        if (rect != null) {
        	final int y = rect.y;
        	
            if (color != null) {
                extendRect (rect, false);
                graphics.setColor (color);
                graphics.fillRect  (rect.x, rect.y, rect.width, rect.height);
            }
            if (!loc.isInsertRow() && shortColor != null) {
            	final int height = rect.height;
                graphics.setColor (shortColor);
                graphics.fillRect (rect.x, y, rect.width, height);
            }
        }
    }

    private Rectangle getHDropLineRect (final JTable.DropLocation loc) {
        if (!loc.isInsertRow()) {
            return null;
        }

        int row = loc.getRow();
        int col = loc.getColumn();
        if (col >= table.getColumnCount()) {
            col--;
        }

        Rectangle rect = table.getCellRect(row, col, true);

        if (row >= table.getRowCount()) {
            row--;
            final Rectangle prevRect = table.getCellRect(row, col, true);
            rect.y = prevRect.y + prevRect.height;
        }

        if (rect.y == 0) {
            rect.y = -1;
        } else {
            rect.y -= 2;
        }

        rect.height = 3;

        return rect;
    }

    private Rectangle getVDropLineRect (final JTable.DropLocation loc) {
        if (!loc.isInsertColumn()) {
            return null;
        }

        final boolean ltr = table.getComponentOrientation().isLeftToRight();
        int col = loc.getColumn();
        Rectangle rect = table.getCellRect(loc.getRow(), col, true);

        if (col >= table.getColumnCount()) {
            col--;
            rect = table.getCellRect(loc.getRow(), col, true);
            if (ltr) {
                rect.x = rect.x + rect.width;
            }
        } else if (!ltr) {
            rect.x = rect.x + rect.width;
        }

        if (rect.x == 0) {
            rect.x = -1;
        } else {
            rect.x -= 2;
        }
        
        rect.width = 3;

        return rect;
    }
    
	
    private Rectangle extendRect (final Rectangle rect, final boolean horizontal) {
        if (rect == null) {
            return rect;
        }

        if (horizontal) {
            rect.x = 0;
            rect.width = table.getWidth();
        } else {
            rect.y = 0;

            if (table.getRowCount() == 0) {
            	rect.height = table.getHeight();
            } else {
            	final Rectangle lastRect = table.getCellRect(table.getRowCount() - 1, 0, true);
                rect.height = lastRect.y + lastRect.height;
            }
        }

        return rect;
    }
	
    
    /*
     * Paints the grid lines within <I>aRect</I>, using the grid
     * color set with <I>setGridColor</I>. Paints vertical lines
     * if <code>getShowVerticalLines()</code> returns true and paints
     * horizontal lines if <code>getShowHorizontalLines()</code>
     * returns true.
     */
    private void paintGrid (final Graphics graphics, final int rMin, final int rMax,
    		final int cMin, final int cMax) {
 
    	final Rectangle minCell = table.getCellRect (rMin, cMin, true);
    	final Rectangle maxCell = table.getCellRect (rMax, cMax, true);
    	final Rectangle damagedArea = minCell.union (maxCell);
        
    	final Graphics2D g2D = (Graphics2D)graphics;
    	final AffineTransform affTransform = g2D.getTransform();
    	final double scalex = affTransform.getScaleX();
    	final double scaley = affTransform.getScaleY();
        LOGGER.debug ("scale x: "+scalex);
        
        final Color fadeColor = ColorUtilities.mixColours (table.getGridColor(), table.getBackground(), Math.min (1.0f, table.getRowHeight() / 10.0f));
    	graphics.setColor (fadeColor);

        
        if (table.getShowHorizontalLines()) {
        	int lastDrawn = -100;
        	final int tableWidth = damagedArea.x + damagedArea.width;
        	int y = damagedArea.y;
        	for (int row = rMin; row <= rMax; row++) {
        		y += table.getRowHeight(row);
        		if ((double)(y - lastDrawn) > (8.0 / scaley)) {
        			graphics.drawLine (damagedArea.x, y - 1, tableWidth - 1, y - 1);
        			lastDrawn = y;
        		}
        	}
        }
        
        if (table.getShowVerticalLines()) {
        	int lastDrawn = -100;
        	final TableColumnModel columnModel = table.getColumnModel();
        	final int tableHeight = damagedArea.y + damagedArea.height;
        	int x;
        	if (table.getComponentOrientation().isLeftToRight()) {
        		x = damagedArea.x;
        		for (int column = cMin; column <= cMax; column++) {
        			final int width = columnModel.getColumn(column).getWidth();
        			x += width;
            		if ((double)(x - lastDrawn) > (8.0 / scalex)) {
            			graphics.drawLine (x - 1, 0, x - 1, tableHeight - 1);
            			lastDrawn = x;
            		}	
        		}
        	} else {
        		x = damagedArea.x;
        		for (int column = cMax; column >= cMin; column--) {
        			final int width = columnModel.getColumn(column).getWidth();
        			x += width;
               		if (x - lastDrawn > 4.0 / scalex) {
            			graphics.drawLine(x - 1, 0, x - 1, tableHeight - 1);
            			lastDrawn = x;
            		}	
        		}
        	}
		}
    }
    
    
    private void paintCells (final Graphics graphics, final int rMin, final int rMax, 
    		final int cMin, final int cMax) {
    	final JTableHeader header = table.getTableHeader();
    	final TableColumn draggedColumn = (header == null) ? null : header.getDraggedColumn();

    	final TableColumnModel columnModel = table.getColumnModel();
    	final int columnMargin = columnModel.getColumnMargin();
    	
    	//columnModelToViewIndex = buildColumnIndexTable ();

        Rectangle cellRect;

	    for (int row = rMin; row <= rMax; row++) {
	    	cellRect = table.getCellRect(row, cMin, false);
	    	
	    	if (cMax - cMin > 4) {
	    		paintRow (graphics, draggedColumn, cellRect, row, cMin, cMax);
	    	}
	    	else {
	    		paintRowStandard (graphics, columnModel, cellRect, row, cMin, cMax, columnMargin, draggedColumn);
	    	}
	    }

        // Paint the dragged column if we are dragging.
        if (draggedColumn != null) {
        	LOGGER.debug ("cmin: "+cMin+", cmax: "+cMax+", di: "+table.convertColumnIndexToView (draggedColumn.getModelIndex()));
        	paintDraggedArea(graphics, rMin, rMax, draggedColumn, header.getDraggedDistance());
        }

    	// Remove any renderers that may be left in the rendererPane.
    	rendererPane.removeAll();
    }
    
    
    protected void paintRowStandard (final Graphics graphics, final TableColumnModel columnModel, 
    		Rectangle cellRect, final int row, final int cMin, final int cMax, 
    		final int columnMargin, final TableColumn draggedColumn) {
        
    	final boolean leftToRight = table.getComponentOrientation().isLeftToRight();
    	
    	for (int column = cMin; column <= cMax; column++) {
    		final TableColumn aColumn = columnModel.getColumn(column);
    		final int columnWidth = aColumn.getWidth();
            cellRect.width = columnWidth - columnMargin;
            if (!leftToRight) {
                cellRect.x -= (column == cMin ? 0 : columnWidth);
            }
            if (aColumn != draggedColumn) {
                paintCell(graphics, cellRect, row, column);
            }
            if (leftToRight) {
            	cellRect.x += columnWidth;
            }
    	}
    }
    
    
    private void paintRow (final Graphics graphics, final TableColumn draggedColumn, 
    		Rectangle cellRect, final int row, final int cMin, final int cMax) {
    	
    	final boolean leftToRight = table.getComponentOrientation().isLeftToRight();
    	final MatrixTableModel mtm = (MatrixTableModel) table.getModel();
    	final int modelRowIndex = table.convertRowIndexToModel (row);
    	final Object rowObject = mtm.getRowObject (modelRowIndex);
    	final Set<Edge> edges = mtm.getRowData (rowObject);
    	final TableColumnModel columnModel = table.getColumnModel();
    	final int columnMargin = columnModel.getColumnMargin();
    	
    	if (edges == null || edges.size() < 5) {
    		paintRowStandard (graphics, columnModel, cellRect, row, cMin, cMax, columnMargin, draggedColumn);
    	} else {
    		final List<Edge> edgeList = new ArrayList<Edge> (edges);
    		nodeBasedEdgeSorter.setObject (rowObject);
    		
    		for (int n = edgeList.size(); --n >= 0;) {
    			final Edge edge = edgeList.get (n);
    			//final int viewCol = table.convertColumnIndexToView (mtm.getColumnIndex (edge.getNode2()));
    			final int viewCol = table.convertColumnIndexToView (mtm.getColumnIndex (nodeBasedEdgeSorter.getOppNode (edge)));
    			if (viewCol < cMin || viewCol > cMax /*|| edge.getNode1() != rowObject*/) {
    				edgeList.set (n, null);
    			}
    		}
			//edgeList = (List<Edge>)ArrayListUtil.removeNulls2 (edgeList);
			ArrayListUtil.removeNulls (edgeList);
    		
    		if (!edgeList.isEmpty()) {
	    		//Collections.sort (edgeList, columnSorter);
	    		Collections.sort (edgeList, nodeBasedEdgeSorter);
	    		int column = cMin;
	    		TableColumn lastDrawnColumn = null;
	    		
	    		for (Edge edge : edgeList) {
	    			//final int viewCol = table.convertColumnIndexToView (mtm.getColumnIndex (edge.getNode2()));
	    			final int viewCol = table.convertColumnIndexToView (mtm.getColumnIndex (nodeBasedEdgeSorter.getOppNode (edge)));
	    			TableColumn aColumn;
	    			int columnWidth;
	    			
	    			if (viewCol != column) {
		    			for (int n = column; n < viewCol; n++) {
		    				aColumn = columnModel.getColumn (n);
		    				columnWidth = aColumn.getWidth();
		    	            if (!leftToRight) {
		    	                cellRect.x -= (column == n ? 0 : columnWidth);
		    	            }
		    	            else {
		    	            	cellRect.x += columnWidth;
		    	            }
		    			}
	    			}
	    			aColumn = columnModel.getColumn (viewCol);
	    			columnWidth = aColumn.getWidth();
                    cellRect.width = Math.max (1, columnWidth - columnMargin);
                    
                    if (aColumn != draggedColumn && lastDrawnColumn != aColumn) {
                    	if (!leftToRight) {
                    		cellRect.x -= columnWidth;
                    	}
                        paintCell (graphics, cellRect, row, viewCol);
                        lastDrawnColumn = aColumn;
                    }
                    
	    			column = viewCol;
	    		}
    		}
    	}
    }
    
    
    
    private void paintCell (final Graphics graphics, final Rectangle cellRect, 
    		final int row, final int column) {
        if (table.isEditing() && table.getEditingRow()==row &&
                                 table.getEditingColumn()==column) {
        	final Component component = table.getEditorComponent();
            component.setBounds (cellRect);
            component.validate();
        }
        else {
        	
        	if (table instanceof JMatrix) {
        		final Component component = ((JMatrix)table).getAndPrepareCellRenderer (row, column);
        		rendererPane.paintComponent (graphics, component, table, cellRect.x, cellRect.y,
                        cellRect.width, cellRect.height, false);
        	} else {
        		final TableCellRenderer renderer = table.getCellRenderer (row, column);
        		final Component component = table.prepareRenderer (renderer, row, column);
	            rendererPane.paintComponent (graphics, component, table, cellRect.x, cellRect.y,
	                                        cellRect.width, cellRect.height, false);
        	}

        }
    }
    
    
    private void paintDraggedArea (final Graphics graphics, final int rMin, final int rMax, 
    		final TableColumn draggedColumn, final int distance) {
    	final int draggedColumnIndex = table.convertColumnIndexToView (draggedColumn.getModelIndex());
    	//int draggedColumnIndex = viewIndexForColumn(draggedColumn);

    	final Rectangle minCell = table.getCellRect(rMin, draggedColumnIndex, true);
    	final Rectangle maxCell = table.getCellRect(rMax, draggedColumnIndex, true);
	
		Rectangle vacatedColumnRect = minCell.union(maxCell);
	
		// Paint a gray well in place of the moving column.
		graphics.setColor(table.getParent().getBackground());
		graphics.fillRect(vacatedColumnRect.x, vacatedColumnRect.y,
			   vacatedColumnRect.width, vacatedColumnRect.height);
	
		// Move to the where the cell has been dragged.
		vacatedColumnRect.x += distance;
	
		// Fill the background.
		graphics.setColor(table.getBackground());
		graphics.fillRect(vacatedColumnRect.x, vacatedColumnRect.y,
			   vacatedColumnRect.width, vacatedColumnRect.height);
	
		// Paint the vertical grid lines if necessary.
		if (table.getShowVerticalLines()) {
		    graphics.setColor(table.getGridColor());
		    final int x1 = vacatedColumnRect.x;
		    final int y1 = vacatedColumnRect.y;
		    final int x2 = x1 + vacatedColumnRect.width - 1;
		    final int y2 = y1 + vacatedColumnRect.height - 1;
		    // Left
		    graphics.drawLine (x1 - 1, y1, x1 - 1, y2);
		    // Right
		    graphics.drawLine (x2, y1, x2, y2);
		}
	
		for (int row = rMin; row <= rMax; row++) {
		    // Render the cell value
		    Rectangle rect = table.getCellRect(row, draggedColumnIndex, false);
		    rect.x += distance;
		    paintCell (graphics, rect, row, draggedColumnIndex);
	
		    // Paint the (lower) horizontal grid line if necessary.
		    if (table.getShowHorizontalLines()) {
				graphics.setColor(table.getGridColor());
				Rectangle rcr = table.getCellRect(row, draggedColumnIndex, true);
				rcr.x += distance;
				final int x1 = rcr.x;
				final int y1 = rcr.y;
				final int x2 = x1 + rcr.width - 1;
				final int y2 = y1 + rcr.height - 1;
				graphics.drawLine (x1, y2, x2, y2);
			}
		}
    }

    
    
    protected void scaleSize (final double scalePropIn, final Point point) {
		
    	double scaleProp = scalePropIn;
    	final Dimension size = table.getSize();		
    	final Dimension newSize = new Dimension (size);
		newSize.setSize ((int)Math.round(newSize.getWidth() * scaleProp), (int)Math.round(newSize.getHeight() * scaleProp));
		
		// Maybe remove size settings below and just change by row height / columnWidth and scale factor? 
		// Stop it shrinking smaller than the viewport size
		table.computeVisibleRect (visibleRect);
		final Dimension visRectSize = visibleRect.getSize(); 
		double hratio = (double)visRectSize.width / (double)newSize.width;
		double vratio = (double)visRectSize.height / (double)newSize.height;
		
		if (hratio > 1.0 && vratio > 1.0) {
			if (hratio > vratio) {
				scaleProp /= hratio;
				vratio /= hratio;
				hratio = 1.0;
				newSize.setSize (size.getWidth(), newSize.getHeight() / vratio);		
			}
			else if (vratio > hratio) {	
				scaleProp /= vratio;
				hratio /= vratio;
				vratio = 1.0;
				newSize.setSize (newSize.getWidth() / hratio, size.getHeight());
			}
		}
		
		final int newRowHeight = (int)(newSize.getHeight() / table.getRowCount());
		
		int x = table.getColumnModel().getTotalColumnWidth();
		final double colProp = newSize.getWidth() / (double)x;
		for (int n = 0; n < table.getColumnCount(); n++) {
			final TableColumn tableColumn = table.getColumnModel().getColumn (n);
			final int colWidth = tableColumn.getWidth();
			final int colWidth2 = Math.max (1, (int)(colWidth * colProp));
			//x += colWidth2;
			tableColumn.setMinWidth (colWidth2);
			tableColumn.setMaxWidth (colWidth2);
			tableColumn.setPreferredWidth (colWidth2);
			tableColumn.setWidth (colWidth2);
		} 
									
		x = table.getColumnModel().getTotalColumnWidth();
		
		table.setRowHeight (Math.max (1, newRowHeight));
		
		if (table instanceof JMatrix) {
			final JMatrix matrix = (JMatrix)table;
			final double smoothVert = ((double)newSize.getHeight() / (table.getRowCount() * newRowHeight));
			final double smoothHoriz = ((double)newSize.getWidth() / (double)x);
			final Point2D newScale = new Point2D.Double (smoothHoriz, smoothVert);
			matrix.setScale (newScale);	
			
			final JComponent[] fixSizedComponents = {matrix.getRowHeader(), matrix.getColumnHeader(), table.getTableHeader()};
			for (JComponent fixSizedComp : fixSizedComponents) {
				setSizeRigidly (fixSizedComp, newSize);
			}
		}
		
		table.getTableHeader().repaint ();
		
		setSizeRigidly (table, newSize);
		
		final double midx = (visibleRect.getX() + point.getX()) / size.getWidth() * newSize.getWidth();
		final double midy = (visibleRect.getY() + point.getY()) / size.getHeight() * newSize.getHeight();
		final double newX = midx - point.getX();
		final double newY = midy - point.getY();
		
		
		
		table.setLocation ((int)Math.round(-newX), (int)Math.round(-newY));
		scaleSlider.setLocation ((int)Math.round(newX + 20), (int)Math.round(newY + 20));
		scale *= scaleProp;
		
		scaleSlider.removeChangeListener (sliderListener);
		table.firePropertyChange ("scale", ZERO, Double.valueOf (scale));
		scaleSlider.addChangeListener (sliderListener);
    }

    
    public void setSizeRigidly (final JComponent comp, final Dimension newSize) {
    	if (comp != null) {
			comp.setMinimumSize (newSize);
			comp.setMaximumSize (newSize);
			comp.setPreferredSize (newSize);
			comp.setSize (newSize);
    	}
    }
    
    
    
    class ViewColumnSorter implements Comparator<Edge> {

		@Override
		public int compare (final Edge edge1, final Edge edge2) {
			final MatrixTableModel mtm = ((MatrixTableModel)table.getModel());
			final int colViewIndex1 = table.convertColumnIndexToView (mtm.getColumnIndex (edge1.getNode2()));
			final int colViewIndex2 = table.convertColumnIndexToView (mtm.getColumnIndex (edge2.getNode2()));
			return colViewIndex1 - colViewIndex2;
		}
    }
    
    
    
    class EdgeOppositeNodeSorter implements Comparator<Edge> {

    	protected Object node;
    	
    	public void setObject (final Object newNode) {
    		node = newNode;
    	}
    	
		@Override
		public int compare (final Edge edge1, final Edge edge2) {
			final MatrixTableModel mtm = ((MatrixTableModel)table.getModel());
			final Object otherNode1 = getOppNode (edge1);
			final Object otherNode2 = getOppNode (edge2);
			final int colViewIndex1 = table.convertColumnIndexToView (mtm.getColumnIndex (otherNode1));
			final int colViewIndex2 = table.convertColumnIndexToView (mtm.getColumnIndex (otherNode2));
			return colViewIndex1 - colViewIndex2;
		}
		
		public Object getOppNode (final Edge edge) {
			// == is quicker if possible
			if (edge.getNode1() == node) {
				return edge.getNode2();
			}
			if (edge.getNode2() == node) {
				return edge.getNode1();
			}
			return (edge.getNode1().equals (node) ? edge.getNode2() : edge.getNode1());
		}
    }
    
    
    
	class MouseOverListener extends MouseAdapter {
		
		@Override
		public void mouseExited (final MouseEvent mEvent) {
    		ToolTipManager.sharedInstance().setInitialDelay (500);
		}
		
		@Override
		public void mouseEntered (final MouseEvent mEvent) {
			mEvent.getComponent().requestFocusInWindow();
    		ToolTipManager.sharedInstance().setInitialDelay (0);
		}
		
		@Override
		public void mouseWheelMoved (final MouseWheelEvent mwEvent) {
			table.computeVisibleRect (visibleRect);
			final Point topLeft = visibleRect.getLocation();
			final Point p = mwEvent.getPoint ();
			p.translate (-topLeft.x, -topLeft.y);
			scaleSize (1.00 + (mwEvent.getWheelRotation() * 0.02), p);
		}	
	}
	
	
	class ScaleListener implements ChangeListener {
		
		@Override
		public void stateChanged (final ChangeEvent cEvent) {
			final JLogSlider jsl = (JLogSlider)cEvent.getSource();
			final double sliderScale = jsl.getScaleFromValue ();
			table.computeVisibleRect (visibleRect);
			scaleSize (sliderScale / scale, new Point ((int)visibleRect.getWidth() / 2, (int)visibleRect.getHeight() / 2));
			scale = sliderScale;
		}
	}
	
	
	class MatrixComponentListener extends ComponentAdapter {
		Rectangle bounds = new Rectangle ();
		
		@Override
		public void componentMoved (final ComponentEvent compEvent) {
			table.getBounds (bounds);
	        scaleSlider.setLocation (-bounds.x + 20, -bounds.y + 20);
		}
	}
	
}