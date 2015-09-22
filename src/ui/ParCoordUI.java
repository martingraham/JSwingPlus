package ui;
/*
 * @(#)BasicTableUI.java	1.160 06/12/07
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use inputStream subject to license terms.
 */


import util.GraphicsUtil;
import util.Messages;
import util.colour.ColorUtilities;
import util.threads.Executioner;
import util.ui.UIUtils;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.datatransfer.Transferable;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.CellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.plaf.TableUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTableHeaderUI;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;


import model.shared.SortedTableColumn;
import model.shared.selection.TemporarySelectionModel;


import sun.swing.DefaultLookup;
import sun.swing.SwingUtilities2;
import sun.swing.UIAction;

import swingPlus.parcoord.JParCoord;
import swingPlus.parcoord.RowCoordData;


/**
 * BasicTableUI implementation
 *
 * @version 1.160 12/07/06
 * @author Philip Milne
 * @author Shannon Hickey (drag and drop)
 */
public class ParCoordUI extends TableUI {
    private static final StringBuilder BASELINE_COMPONENT_KEY =
        new StringBuilder ("Table.baselineComponent");
    
    final private static Class<ParCoordUI> CLASS_OBJ = ParCoordUI.class;
    private final static Logger LOGGER = Logger.getLogger (CLASS_OBJ);
    
	//
	// Instance Variables
	//
    protected Rectangle visibleRect = new Rectangle ();
    protected Insets insets = new Insets (0, 0, 0, 0);
    protected Rectangle visibleRectNoInsets = new Rectangle ();
    
    protected Timer antiAliasTimer = new Timer (1000, new AntiAliasActionListener ());
    protected boolean antiAlias = false;
    protected transient boolean nonBrushedLastDrawnAntiAliased = false;
    protected AWTEventListener cancelAntiAlias = new CancelAntiAliasListener ();
    
    protected Dimension lastBrush = new Dimension ();
    protected Timer brushOn;
    protected Timer brushOff;
    protected boolean brushing = false;
    protected transient boolean brushingCancelled = false;
    protected boolean isDragging = false;
    protected boolean moveNullsOutside = true;
    
    
    // ints for describing layer types and object (row selection) types
    protected final static int BACKGROUND = 0, SELECTED = 1, UBER_SELECTED = 2, 
    	BRUSHED_SELECTED = 3, BRUSHED = 4, END = 5, BRUSHED_LAYER = 3, LEGEND = 4;
    // table that decides which layer a type of row selection gets painted to
    protected final static int[] SELECTION_LAYER_TABLE = {BACKGROUND, SELECTED, 
    	UBER_SELECTED, BRUSHED_LAYER, BRUSHED_LAYER};
    protected BufferedImage[] img = new BufferedImage [5];
    protected BitSet drawingStates = new BitSet (END);
    protected Graphics[] drawingAreas = new Graphics [END];
    protected Color[] drawColours = new Color [END * 2];
    
    protected DrawRunnable drawingRunnable;
    protected Thread drawThread;
    protected transient long paintTimeOut = (long) (75 * 1E6); // milliseconds * 1E6 = nanoseconds    
    
    public enum Combinator {ADD, FILTER, EXCLUDE};
   
    // The JTable that is delegating the painting to this UI.
    protected JTable table;

    // Listeners that are attached to the JTable
    protected KeyListener keyListener;
    protected MouseInputListener mouseInputListener;
    protected ComponentListener componentListener;
    protected Handler handler;
    


     


	
    protected final static String SELECT = Messages.getString (CLASS_OBJ, "Select");
    protected final static String FILTER = Messages.getString (CLASS_OBJ, "Filter_on");
	
    protected final MessageFormat toolTipFormat = new MessageFormat (Messages.getString (CLASS_OBJ, "tooltipTemplate"));
	protected final Object[] formatValues = new Object [4];
	
	protected final static Cursor ADD_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor (
			GraphicsUtil.loadBufferedImage (GraphicsUtil.makeFilename ("AddCursor")), 
			new Point (0, 0),
			"AddCursor");
	
	protected final static Cursor FILTER_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor (
			GraphicsUtil.loadBufferedImage (GraphicsUtil.makeFilename ("FilterCursor")), 
			new Point (0, 0),
			"FilterCursor");	
	
	//
	//  Helper class for keyboard actions
	//
    static class Actions2 extends UIAction {
	    private static final String CANCEL_EDITING = "cancel";
	    private static final String SELECT_ALL = "selectAll";
	    private static final String CLEAR_SELECTION = "clearSelection";
	    private static final String START_EDITING = "startEditing";
	
	    private static final String NEXT_ROW = "selectNextRow";
	    private static final String NEXT_ROW_CELL = "selectNextRowCell";
		private static final String NEXT_ROW_EXTEND_SELECTION = "selectNextRowExtendSelection";
	    private static final String NEXT_ROW_CHANGE_LEAD = "selectNextRowChangeLead";
	    private static final String PREVIOUS_ROW = "selectPreviousRow";
	    private static final String PREVIOUS_ROW_CELL = "selectPreviousRowCell";
		private static final String PREVIOUS_ROW_EXTEND_SELECTION = "selectPreviousRowExtendSelection";
	    private static final String PREVIOUS_ROW_CHANGE_LEAD = "selectPreviousRowChangeLead";
	
	    private static final String NEXT_COLUMN = "selectNextColumn";
	    private static final String NEXT_COLUMN_CELL = "selectNextColumnCell";
	    private static final String NEXT_COLUMN_CHANGE_LEAD = "selectNextColumnChangeLead";
	    private static final String PREVIOUS_COLUMN = "selectPreviousColumn";
	    private static final String PREVIOUS_COLUMN_CELL = "selectPreviousColumnCell";
	    private static final String PREVIOUS_COLUMN_CHANGE_LEAD = "selectPreviousColumnChangeLead";
	
		private static final String SCROLL_UP_CHANGE_SELECTION = "scrollUpChangeSelection";
	    private static final String SCROLL_UP_EXTEND_SELECTION = "scrollUpExtendSelection";
	    private static final String SCROLL_DOWN_CHANGE_SELECTION = "scrollDownChangeSelection";
	    private static final String SCROLL_DOWN_EXTEND_SELECTION = "scrollDownExtendSelection";
	
	    private static final String FIRST_COLUMN = "selectFirstColumn";
	    private static final String LAST_COLUMN = "selectLastColumn";
	
	    private static final String FIRST_ROW = "selectFirstRow";
	    private static final String FIRST_ROW_EXTEND_SELECTION = "selectFirstRowExtendSelection";
	    private static final String LAST_ROW = "selectLastRow";
	    private static final String LAST_ROW_EXTEND_SELECTION = "selectLastRowExtendSelection";
	
	    // add the lead item to the selection without changing lead or anchor
	    private static final String ADD_TO_SELECTION = "addToSelection";
	
	    // toggle the selected state of the lead item and move the anchor to it
	    private static final String TOGGLE_AND_ANCHOR = "toggleAndAnchor";
	
	    // extend the selection to the lead item
	    private static final String EXTEND_TO = "extendTo";
	
	    // move the anchor to the lead and ensure only that item is selected
	    private static final String MOVE_SELECTION_TO = "moveSelectionTo";
	
	    // give focus to the JTableHeader, if one exists
	    private static final String FOCUS_HEADER = "focusHeader";
	    
	    protected int dx;
	    protected int dy;
		protected boolean extend;
		protected boolean inSelection;
	    protected boolean forwards;
	    protected boolean vertically;
	    protected boolean toLimit;
	
		protected int leadRow;
		protected int leadColumn;
	
        Actions2 (final String name) {
            super(name);
        }

        Actions2 (final String name, int dx, int dy, final boolean extend,
                final boolean inSelection) {
            super(name);

            // Actions2 specifying true for "inSelection" are
            // fairly sensitive to bad parameter values. They require
            // that one of dx and dy be 0 and the other be -1 or 1.
            // Bogus parameter values could cause an infinite loop.
            // To prevent any problems we massage the params here
            // and complain if we get something we can't deal with.
            if (inSelection) {
                this.inSelection = true;

                // look at the sign of dx and dy only
                dx = sign(dx);
                dy = sign(dy);

                // make sure one is zero, but not both
                assert (dx == 0 || dy == 0) && !(dx == 0 && dy == 0);
            }

            this.dx = dx;
            this.dy = dy;
            this.extend = extend;
        }

        Actions2 (final String name, final boolean extend, final boolean forwards,
        		final boolean vertically, final boolean toLimit) {
            this (name, 0, 0, extend, false);
            this.forwards = forwards;
            this.vertically = vertically;
            this.toLimit = toLimit;
        }
	
		private static int clipToRange (final int i, final int a, final int b) {
		    return Math.min (Math.max (i, a), b - 1);
		}
	
		private void moveWithinTableRange (final JTable table, final int dx, final int dy) {
			
			leadColumn = clipToRange (leadColumn + dx, 0, table.getColumnCount());
			leadRow = clipToRange (leadRow, 0, table.getRowCount());
			if (dx != 0) {
				table.getTableHeader().repaint ();
			}
			int atIndex = leadRow;
			final SortedTableColumn<?> stc = (SortedTableColumn<?>) table.getColumnModel().getColumn (leadColumn);
			LOGGER.debug ("lead row: "+leadRow+", lead column: "+leadColumn+", dx: "+dx+", dy: "+dy);
			
			if (dy != 0) {
				final int sortedIndex = findSortedColumnIndex (table, stc, leadRow);
				if (sortedIndex >= 0) {
					atIndex = sortedIndex;
				}
			}
			
			final int newIndex = clipToRange (atIndex + dy, 0, table.getRowCount());
			final int modelIndex = stc.getFilteredOrderedList().get(newIndex);
			final int viewIndex = table.convertRowIndexToView (modelIndex);

			leadRow = (dy == 0 ? leadRow : viewIndex);
			LOGGER.debug ("new lead row: "+leadRow+", val: "+table.getValueAt(leadRow, leadColumn)+", modIndex: "+modelIndex+", modVal: "+table.getModel().getValueAt(modelIndex, leadColumn));
		}

       private static int sign (final int num) {
            return (num < 0) ? -1 : ((num == 0) ? 0 : 1);
       }

        /**
         * Called to move within the selected range of the given JTable.
         * This method uses the table's notion of selection, which is
         * important to allow the user to navigate between items visually
         * selected on screen. This notion may or may not be the same as
         * what could be determined by directly querying the selection models.
         * It depends on certain table properties (such as whether or not
         * row or column selection is allowed). When performing modifications,
         * it is recommended that caution be taken in order to preserve
         * the intent of this method, especially when deciding whether to
         * query the selection models or interact with JTable directly.
         */
       private boolean moveWithinSelectedRange (final JTable table, final int dx, final int dy,
    		   final ListSelectionModel rsm, final ListSelectionModel csm) {

            // Note: The Actions2 constructor ensures that only one of
            // dx and dy is 0, and the other is either -1 or 1

            // find out how many items the table is showing as selected
            // and the range of items to navigate through
            int totalCount;
            int minX, maxX, minY, maxY;

            final boolean rs = table.getRowSelectionAllowed();
            final boolean cs = table.getColumnSelectionAllowed();

            // both column and row selection
            if (rs && cs) {
                totalCount = table.getSelectedRowCount() * table.getSelectedColumnCount();
                minX = csm.getMinSelectionIndex();
                maxX = csm.getMaxSelectionIndex();
                minY = rsm.getMinSelectionIndex();
                maxY = rsm.getMaxSelectionIndex();
            // row selection only
            } else if (rs) {
                totalCount = table.getSelectedRowCount();
                minX = 0;
                maxX = table.getColumnCount() - 1;
                minY = rsm.getMinSelectionIndex();
                maxY = rsm.getMaxSelectionIndex();
            // column selection only
            } else if (cs) {
                totalCount = table.getSelectedColumnCount();
                minX = csm.getMinSelectionIndex();
                maxX = csm.getMaxSelectionIndex();
                minY = 0;
                maxY = table.getRowCount() - 1;
            // no selection allowed
            } else {
                totalCount = 0;
                // A bogus assignment to stop javac from complaining
                // about unitialized values. In this case, these
                // won't even be used.
                minX = maxX = minY = maxY = 0;
            }

            // For some cases, there is no point in trying to stay within the
            // selected area. Instead, move outside the selection, wrapping at
            // the table boundaries. The cases are:
            boolean stayInSelection;

            // - nothing selected
            if (totalCount == 0 ||
                    // - one item selected, and the lead is already selected
                    (totalCount == 1 && table.isCellSelected(leadRow, leadColumn))) {

                stayInSelection = false;

                maxX = table.getColumnCount() - 1;
                maxY = table.getRowCount() - 1;

                // the mins are calculated like this in case the max is -1
                minX = Math.min(0, maxX);
                minY = Math.min(0, maxY);
            } else {
                stayInSelection = true;
            }

            // the algorithm below isn't prepared to deal with -1 lead/anchor
            // so massage appropriately here first
            if (dy == 1 && leadColumn == -1) {
                leadColumn = minX;
                leadRow = -1;
            } else if (dx == 1 && leadRow == -1) {
                leadRow = minY;
                leadColumn = -1;
            } else if (dy == -1 && leadColumn == -1) {
                leadColumn = maxX;
                leadRow = maxY + 1;
            } else if (dx == -1 && leadRow == -1) {
                leadRow = maxY;
                leadColumn = maxX + 1;
            }

            // In cases where the lead is not within the search range,
            // we need to bring it within one cell for the the search
            // to work properly. Check these here.
            leadRow = Math.min(Math.max(leadRow, minY - 1), maxY + 1);
            leadColumn = Math.min(Math.max(leadColumn, minX - 1), maxX + 1);

            // find the next position, possibly looping until it is selected
            do {
                calcNextPos(dx, minX, maxX, dy, minY, maxY);
            } while (stayInSelection && !table.isCellSelected(leadRow, leadColumn));

            return stayInSelection;
		}

        /**
         * Find the next lead row and column based on the given
         * dx/dy and max/min values.
         */
        private void calcNextPos (final int dx, final int minX, final int maxX,
        							final int dy, final int minY, final int maxY) {

            if (dx != 0) {
                leadColumn += dx;
                if (leadColumn > maxX) {
                    leadColumn = minX;
                    leadRow++;
                    if (leadRow > maxY) {
                        leadRow = minY;
                    }
                } else if (leadColumn < minX) {
                    leadColumn = maxX;
                    leadRow--;
                    if (leadRow < minY) {
                        leadRow = maxY;
                    }
                }
            } else {
                leadRow += dy;
                if (leadRow > maxY) {
                    leadRow = minY;
                    leadColumn++;
                    if (leadColumn > maxX) {
                        leadColumn = minX;
                    }
                } else if (leadRow < minY) {
                    leadRow = maxY;
                    leadColumn--;
                    if (leadColumn < minX) {
                        leadColumn = maxX;
                    }
                }
            }
        }

        public void actionPerformed (final ActionEvent aEvent) {
        	final String key = getName();
        	final JTable table = (JTable)aEvent.getSource();

            LOGGER.debug ("-------------------/nResponding to key action./nname: "+key);
            //UIUtils.printActionMap (table.getActionMap());
            //System.err.println ("Focus component: "+FocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
            
            final ListSelectionModel rsm = table.getSelectionModel();
            leadRow = getAdjustedLead (table, true, rsm);

            final ListSelectionModel csm = table.getColumnModel().getSelectionModel();
            leadColumn = getAdjustedLead (table, false, csm);

            if (!table.getComponentOrientation().isLeftToRight()) {
                forwards = true;
            }

            if (key == SCROLL_UP_CHANGE_SELECTION ||
                    key == SCROLL_UP_EXTEND_SELECTION ||
                    key == SCROLL_DOWN_CHANGE_SELECTION ||
                    key == SCROLL_DOWN_EXTEND_SELECTION ||
                    key == FIRST_COLUMN ||
                    key == FIRST_ROW ||
                    key == FIRST_ROW_EXTEND_SELECTION ||
                    key == LAST_COLUMN ||
                    key == LAST_ROW ||
                    key == LAST_ROW_EXTEND_SELECTION) {
                if (toLimit) {
                    if (vertically) {
                    	final int rowCount = table.getRowCount();
                        this.dx = 0;
                        this.dy = forwards ? rowCount : -rowCount;
                    }
                    else {
                    	final int colCount = table.getColumnCount();
                        this.dx = forwards ? colCount : -colCount;
                        this.dy = 0;
                    }
                }
                else {
                    if (!(table.getParent().getParent() instanceof
                            JScrollPane)) {
                        return;
                    }
    
                    final Dimension delta = table.getParent().getSize();

                    if (vertically) {
                        this.dx = 0;
                        final int pageJump = (int)Math.sqrt (table.getRowCount());
                        int newRow = leadRow + (forwards ? pageJump : -pageJump);
                        newRow = clipToRange (newRow, 0, table.getRowCount());
                        this.dy = newRow - leadRow;
                    }
                    else {
                    	final Rectangle r = table.getCellRect(0, leadColumn, true);
                        r.x += forwards ? delta.width : -delta.width;
                        int newColumn = table.columnAtPoint(r.getLocation());
                        if (newColumn == -1 && forwards) {
                            newColumn = table.getColumnCount();
                        }
                        this.dx = newColumn - leadColumn;
                        this.dy = 0;
                    }
                }
            }
            if (key == NEXT_ROW ||  // Navigate Actions2
                    key == NEXT_ROW_CELL ||
                    key == NEXT_ROW_EXTEND_SELECTION ||
                    key == NEXT_ROW_CHANGE_LEAD ||
                    key == NEXT_COLUMN ||
                    key == NEXT_COLUMN_CELL ||
                    key == NEXT_COLUMN_CHANGE_LEAD ||
                    key == PREVIOUS_ROW ||
                    key == PREVIOUS_ROW_CELL ||
                    key == PREVIOUS_ROW_EXTEND_SELECTION ||
                    key == PREVIOUS_ROW_CHANGE_LEAD ||
                    key == PREVIOUS_COLUMN ||
                    key == PREVIOUS_COLUMN_CELL ||
                    key == PREVIOUS_COLUMN_CHANGE_LEAD ||
                    // Paging Actions2.
                    key == SCROLL_UP_CHANGE_SELECTION ||
                    key == SCROLL_UP_EXTEND_SELECTION ||
                    key == SCROLL_DOWN_CHANGE_SELECTION ||
                    key == SCROLL_DOWN_EXTEND_SELECTION ||
                    key == FIRST_COLUMN ||
                    key == FIRST_ROW ||
                    key == FIRST_ROW_EXTEND_SELECTION ||
                    key == LAST_COLUMN ||
                    key == LAST_ROW ||
                    key == LAST_ROW_EXTEND_SELECTION) {

                if (table.isEditing() &&
                        !table.getCellEditor().stopCellEditing()) {
                    return;
                }
    
                // Unfortunately, this strategy introduces bugs because
                // of the asynchronous nature of requestFocus() call below.
                // Introducing a delay with invokeLater() makes this work
                // in the typical case though race conditions then allow
                // focus to disappear altogether. The right solution appears
                // to be to fix requestFocus() so that it queues a request
                // for the focus regardless of who owns the focus at the
                // time the call to requestFocus() is made. The optimisation
                // to ignore the call to requestFocus() when the component
                // already has focus may ligitimately be made as the
                // request focus event is dequeued, not before.
    
                // boolean wasEditingWithFocus = table.isEditing() &&
                // table.getEditorComponent().isFocusOwner();

                boolean changeLead = false;
                if (key == NEXT_ROW_CHANGE_LEAD || key == PREVIOUS_ROW_CHANGE_LEAD) {
                    changeLead = (rsm.getSelectionMode()
                                     == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                } else if (key == NEXT_COLUMN_CHANGE_LEAD || key == PREVIOUS_COLUMN_CHANGE_LEAD) {
                    changeLead = (csm.getSelectionMode()
                                     == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                }

                if (changeLead) {
                    moveWithinTableRange (table, dx, dy);
                    if (dy != 0) {
                        // casting should be safe since the action is only enabled
                        // for DefaultListSelectionModel
                        ((DefaultListSelectionModel)rsm).moveLeadSelectionIndex(leadRow);
                        if (getAdjustedLead(table, false, csm) == -1
                                && table.getColumnCount() > 0) {
                            ((DefaultListSelectionModel)csm).moveLeadSelectionIndex(0);
                        }
                    } else {
                        // casting should be safe since the action is only enabled
                        // for DefaultListSelectionModel
                        ((DefaultListSelectionModel)csm).moveLeadSelectionIndex(leadColumn);
                        if (getAdjustedLead(table, true, rsm) == -1
                                && table.getRowCount() > 0) {
                            ((DefaultListSelectionModel)rsm).moveLeadSelectionIndex(0);
                        }
                    }

                    final Rectangle cellRect = table.getCellRect(leadRow, leadColumn, false);
                    if (cellRect != null) {
                        table.scrollRectToVisible(cellRect);
                    }
                } else if (!inSelection) {
                    moveWithinTableRange (table, dx, dy);
                    changeSelection(table, leadRow, leadColumn, false, extend);
                }
                else {
                    if (table.getRowCount() <= 0 || table.getColumnCount() <= 0) {
                        // bail - don't try to move selection on an empty table
                        return;
                    }

                    if (moveWithinSelectedRange (table, dx, dy, rsm, csm)) {
                        // this is the only way we have to set both the lead
                        // and the anchor without changing the selection
                        if (rsm.isSelectedIndex(leadRow)) {
                            rsm.addSelectionInterval(leadRow, leadRow);
                        } else {
                            rsm.removeSelectionInterval(leadRow, leadRow);
                        }

                        if (csm.isSelectedIndex(leadColumn)) {
                            csm.addSelectionInterval(leadColumn, leadColumn);
                        } else {
                            csm.removeSelectionInterval(leadColumn, leadColumn);
                        }

                        final Rectangle cellRect = table.getCellRect(leadRow, leadColumn, false);
                        if (cellRect != null) {
                            table.scrollRectToVisible(cellRect);
                        }
                    }
                    else {
                        changeSelection (table, leadRow, leadColumn, false, false);
                    }
                }
    
                /*
                if (wasEditingWithFocus) {
                    table.editCellAt(leadRow, leadColumn);
                    final Component editorComp = table.getEditorComponent();
                    if (editorComp != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                editorComp.requestFocus();
                            }
                        });
                    }
                }
                */
            } else if (key == CANCEL_EDITING) {
                table.removeEditor();
            } else if (key == SELECT_ALL) {
                table.selectAll();
            } else if (key == CLEAR_SELECTION) {
                table.clearSelection();
            } else if (key == START_EDITING) {
                if (!table.hasFocus()) {
                	final CellEditor cellEditor = table.getCellEditor();
                    if (cellEditor != null && !cellEditor.stopCellEditing()) {
                        return;
                    }
                    table.requestFocus();
                    return;
                }
                table.editCellAt(leadRow, leadColumn, aEvent);
                final Component editorComp = table.getEditorComponent();
                if (editorComp != null) {
                    editorComp.requestFocus();
                }
            } else if (key == ADD_TO_SELECTION) {
                if (!table.isCellSelected(leadRow, leadColumn)) {
                	final int oldAnchorRow = rsm.getAnchorSelectionIndex();
                	final int oldAnchorColumn = csm.getAnchorSelectionIndex();
                    rsm.setValueIsAdjusting(true);
                    csm.setValueIsAdjusting(true);
                    changeSelection (table, leadRow, leadColumn, true, false);
                    rsm.setAnchorSelectionIndex(oldAnchorRow);
                    csm.setAnchorSelectionIndex(oldAnchorColumn);
                    rsm.setValueIsAdjusting(false);
                    csm.setValueIsAdjusting(false);
                }
            } else if (key == TOGGLE_AND_ANCHOR) {
                changeSelection (table, leadRow, leadColumn, true, false);
            } else if (key == EXTEND_TO) {
                changeSelection (table, leadRow, leadColumn, false, true);
            } else if (key == MOVE_SELECTION_TO) {
                changeSelection (table, leadRow, leadColumn, false, false);
            } else if (key == FOCUS_HEADER) {
                JTableHeader th = table.getTableHeader();
                if (th != null) {
                    //Set the header's selected column to match the table.
                	final int col = table.getSelectedColumn();
                    if (col >= 0) {
                    	final TableHeaderUI thUI = th.getUI();
                        if (thUI instanceof BasicTableHeaderUI) {
                            //((BasicTableHeaderUI)thUI).selectColumn(col);
                        }
                    }
                    
                    //Then give the header the focus.
                    th.requestFocusInWindow();
                }
            }
        }

        @Override
		public boolean isEnabled (final Object sender) {
        	final String key = getName();

            if (key == CANCEL_EDITING && sender instanceof JTable) {
                return ((JTable)sender).isEditing();
            } else if (key == NEXT_ROW_CHANGE_LEAD ||
                       key == PREVIOUS_ROW_CHANGE_LEAD) {
                // discontinuous selection actions are only enabled for
                // DefaultListSelectionModel
                return sender != null &&
                       ((JTable)sender).getSelectionModel()
                           instanceof DefaultListSelectionModel;
            } else if (key == NEXT_COLUMN_CHANGE_LEAD ||
                       key == PREVIOUS_COLUMN_CHANGE_LEAD) {
                // discontinuous selection actions are only enabled for
                // DefaultListSelectionModel
                return sender != null &&
                       ((JTable)sender).getColumnModel().getSelectionModel()
                           instanceof DefaultListSelectionModel;
            } else if (key == ADD_TO_SELECTION && sender instanceof JTable) {
                // This action is typically bound to SPACE.
                // If the table is already in an editing mode, SPACE should
                // simply enter a space character into the table, and not
                // select a cell. Likewise, if the lead cell is already selected
                // then hitting SPACE should just enter a space character
                // into the cell and begin editing. In both of these cases
                // this action will be disabled.
            	final JTable table = (JTable)sender;
                final int leadRow = getAdjustedLead(table, true);
                final int leadCol = getAdjustedLead(table, false);
                return !(table.isEditing() || table.isCellSelected(leadRow, leadCol));
            } else if (key == FOCUS_HEADER && sender instanceof JTable) {
            	final JTable table = (JTable)sender;
                return table.getTableHeader() != null;
            }

            return true;
        }
        
        
        
        void changeSelection (final JTable table, final int rowIndex, final int columnIndex,
        		final boolean toggle, final boolean extend) {
        	final ListSelectionModel rsm = table.getSelectionModel();
			final int anchorIndex = clipToRange (rsm.getAnchorSelectionIndex(), 0, table.getRowCount());
        	final boolean anchorSelected = rsm.isSelectedIndex (anchorIndex);
        	final SortedTableColumn<?> stc = (SortedTableColumn<?>)table.getColumnModel().getColumn (leadColumn);
        	LOGGER.debug ("Anchor index before sel change: "+anchorIndex);
        	LOGGER.debug ("Lead index before sel change: "+leadRow);
        	
        	if (extend) {
        		rsm.setValueIsAdjusting (true);
        		if (toggle) {
        			if (anchorSelected) {
        				setSelection (table, rsm, anchorIndex, rowIndex, stc, true);
        			} else {
        				setSelection (table, rsm, anchorIndex, rowIndex, stc, false);
        			}
        		} else {
        			rsm.clearSelection();
        			setSelection (table, rsm, anchorIndex, rowIndex, stc, true);
        		}
        		rsm.setAnchorSelectionIndex (anchorIndex);
        		rsm.setValueIsAdjusting (false);
        	} else {
        		table.changeSelection (rowIndex, columnIndex, toggle, extend);
        	}
        	
        	LOGGER.debug ("Anchor index after sel change: "+rsm.getAnchorSelectionIndex());
        	LOGGER.debug ("Lead index after sel change: "+rsm.getLeadSelectionIndex());
        }
        
        
        void setSelection (final JTable table, final ListSelectionModel selModel, final int anchorIndex, 
        		final int toIndex, final SortedTableColumn<?> stc, final boolean set) {

        	final int sortedColumnAnchorIndex = findSortedColumnIndex (table, stc, anchorIndex);
        	final int sortedColumnToIndex = findSortedColumnIndex (table, stc, toIndex);
        	final int step = sortedColumnAnchorIndex > sortedColumnToIndex ? -1 : 1;
        	final List<Integer> filteredList = stc.getFilteredOrderedList();
        	LOGGER.debug ("anchor sort list index: "+sortedColumnAnchorIndex+", to sort list index: "+sortedColumnToIndex);
        	
        	if (set) {
	         	for (int sortListIndex = sortedColumnAnchorIndex; 
	         			sortListIndex != sortedColumnToIndex + step; sortListIndex += step) {
	         		final int modelIndex = filteredList.get (sortListIndex);
	         		final int selIndex = table.convertRowIndexToView (modelIndex);
	        		selModel.addSelectionInterval (selIndex, selIndex);
	        	}
        	} else {
	         	for (int sortListIndex = sortedColumnAnchorIndex;
	         			sortListIndex != sortedColumnToIndex + step; sortListIndex += step) {
	         		final int modelIndex = filteredList.get (sortListIndex);
	         		final int selIndex = table.convertRowIndexToView (modelIndex);
	        		selModel.removeSelectionInterval (selIndex, selIndex);
	        	}
        	}
        }
        
     
        int findSortedColumnIndex (final JTable table, final SortedTableColumn<?> stc, final int viewIndex) {
			int atIndex = -1;
			
			if (stc != null && table != null) {
				long nano = System.nanoTime();
	        	final int modelIndex = table.convertRowIndexToModel (viewIndex);
	        	atIndex = stc.getSortedPosForModelIndex (Integer.valueOf (modelIndex));
				
	        	if (LOGGER.isDebugEnabled()) {
	        		nano = System.nanoTime() - nano;
					LOGGER.debug ("search for model index in column took "+(nano/1E6)+" ms.");
					if (atIndex >= 0) {
						final int oldModelIndex = stc.getFilteredOrderedList().get(atIndex);
						LOGGER.debug ("sort list index: "+atIndex+", model index: "+oldModelIndex+
								", back to view index: "+table.convertRowIndexToView (oldModelIndex));
					}
	        	}
			}
					
			return atIndex;
        }
    }
    
    





    protected class Handler implements MouseInputListener, PropertyChangeListener, KeyListener {

        // KeyListener
        public void keyPressed (final KeyEvent kEvent) { 
        	// Empty method
        }

        public void keyReleased (final KeyEvent kEvent) { 
        	// Empty method
        }

        public void keyTyped (final KeyEvent kEvent) {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(kEvent.getKeyChar(),
                    kEvent.getModifiers());

            // We register all actions using ANCESTOR_OF_FOCUSED_COMPONENT
            // which means that we might perform the appropriate action
            // in the table and then forward it to the editor if the editor
            // had focus. Make sure this doesn't happen by checking our
            // InputMaps.
            LOGGER.debug ("in Handler keyTyped key: "+kEvent);
		    InputMap map = table.getInputMap(JComponent.WHEN_FOCUSED);
		    LOGGER.debug ("Input Map: "+map);
		    if (map != null && map.get(keyStroke) != null) {
		    	return;
		    }
		    map = table.getInputMap(JComponent.
					  WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		    if (map != null && map.get(keyStroke) != null) {
		    	return;
		    }

		    keyStroke = KeyStroke.getKeyStrokeForEvent (kEvent);

            // The AWT seems to generate an unconsumed \r event when
            // ENTER (\n) inputStream pressed.
            if (kEvent.getKeyChar() == '\r') {
                return;
            }

            final int leadRow = getAdjustedLead(table, true);
            final int leadColumn = getAdjustedLead(table, false);
            if (leadRow != -1 && leadColumn != -1 && !table.isEditing()) {
                if (!table.editCellAt(leadRow, leadColumn)) {
                    return;
                }
            }

            // Forwarding events this way seems to put the component
            // in a state where it believes it has focus. In reality
            // the table retains focus - though it is difficult for
            // a user to tell, since the caret isvisible and flashing.

            // Calling table.requestFocus() here, to get the focus back to
            // the table, seems to have no effect.

            final Component editorComp = table.getEditorComponent();
            if (table.isEditing() && editorComp instanceof JComponent) {
            	final JComponent component = (JComponent)editorComp;
                map = component.getInputMap(JComponent.WHEN_FOCUSED);
                Object binding = (map != null) ? map.get(keyStroke) : null;
                if (binding == null) {
                	map = component.getInputMap(JComponent.
                			WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                	binding = (map != null) ? map.get(keyStroke) : null;
                }
                if (binding != null) {
                	final ActionMap am = component.getActionMap();
                	final Action action = (am != null) ? am.get(binding) : null;
                	if (action != null && SwingUtilities.
                		notifyAction(action, keyStroke, kEvent, component,
	    					kEvent.getModifiers())) {
                		kEvent.consume();
                	}
                }
            }
        }



   
	    // PropertyChangeListener
		public void propertyChange (final PropertyChangeEvent event) {
			final String changeName = event.getPropertyName();
		    LOGGER.debug ("event: "+changeName);
		    
		    if ("componentOrientation" == changeName) {
		    	final JTableHeader header = table.getTableHeader();
				if (header != null) {
				    header.setComponentOrientation(
		                            (ComponentOrientation)event.getNewValue());
				}
	        } else if ("dropLocation" == changeName) {
	        	final JTable.DropLocation oldValue = (JTable.DropLocation)event.getOldValue();
	            repaintDropLocation(oldValue);
	            repaintDropLocation(table.getDropLocation());
	        } else if ("foreground" == changeName || "background" == changeName
	        		|| "selectionForeground" == changeName || "selectionBackground" == changeName
	        		|| "uberSelectionForeground" == changeName || "brushSelection" == changeName
	        		|| "brushForeground" == changeName) {
	        	makeColourCache ();
	        	final boolean redrawAll = ("foreground" == changeName || "background" == changeName);
	        	((JParCoord)table).setRedrawAll (redrawAll);
	        	((JParCoord)table).setRedrawSelections (!redrawAll);
	        	table.repaint();
	        } else if ("brushing" == changeName) {
	        	if (event.getNewValue() == Boolean.TRUE) {
	        		startBrushing ();
	        	} else {
	        		stopBrushing ();
	        	}
	        } else if ("labelSeparation" == changeName) {
	        	table.repaint();
	        }
		}

        private void repaintDropLocation (final JTable.DropLocation loc) {
            if (loc == null) {
                return;
            }
            /*
            if (!loc.isInsertRow() && !loc.isInsertColumn()) {
                Rectangle rect = table.getCellRect(loc.getRow(), loc.getColumn(), false);
                if (rect != null) {
                    table.repaint(rect);
                }
                return;
            }

            if (loc.isInsertRow()) {
                Rectangle rect = extendRect(getHDropLineRect(loc), true);
                if (rect != null) {
                    table.repaint(rect);
                }
            }

            if (loc.isInsertColumn()) {
                Rectangle rect = extendRect(getVDropLineRect(loc), false);
                if (rect != null) {
                    table.repaint(rect);
                }
            }
            */
        }

        
        // MouseInputListener

        // The row and column where the press occurred and the
        // press event itself
        protected Object pressedValue, pressedValueUpper;
        protected int pressedCol;
        protected Dimension lastSortedRange = new Dimension ();
        protected TemporarySelectionModel tsm = new TemporarySelectionModel ();
        protected int initialModifiers = 0;
        protected int preserveAnchorIndex = -1;

        
		void clearPressed () {
			isDragging = false;
            pressedValue = null;
            pressedCol = -1;
            initialModifiers = 0;
            lastSortedRange.setSize (-1, -1);
            tsm.clear ();
		}
		
		       
		@Override
		public void mouseEntered (final MouseEvent mEvent) {
			table.requestFocusInWindow();
			if (!isDragging) {
				ToolTipManager.sharedInstance().setDismissDelay (5000);
	    		ToolTipManager.sharedInstance().setInitialDelay (0);
	    		//ToolTipManager.sharedInstance().setReshowDelay (0);
				clearPressed ();
				startBrushing ();
			}
		}

		
		@Override
		public void mouseExited (final MouseEvent mEvent) {
			if (!isDragging) {
				table.setToolTipText ("");
				ToolTipManager.sharedInstance().setInitialDelay (500);
				clearPressed ();	
				stopBrushing ();
				table.setCursor (Cursor.getDefaultCursor());
			}
		}

		@Override
		public void mouseReleased (final MouseEvent mEvent) {
			// if ALT key was depressed at start of drag then perform final filtering of current
			// drag selection with the table's row selection model via the temporary selection model
			final ListSelectionModel rsm = table.getSelectionModel ();
			
			if ((initialModifiers & Event.ALT_MASK) > 0) {
				final int dragCol = table.columnAtPoint (mEvent.getPoint());
				if (dragCol == pressedCol) {
	             	final SortedTableColumn<?> stc = (SortedTableColumn<?>) table.getColumnModel().getColumn (dragCol);
	             	rsm.clearSelection ();
	             	tsm.selectRange (lastSortedRange.width, lastSortedRange.height, table, stc, Combinator.FILTER);
				}
			}
			
			// Restore cursor and clear dragging variables
			// Restore initial anchor index (to emulate that the drag operation was one continuous selection)
			table.setCursor (Cursor.getDefaultCursor());
			rsm.setAnchorSelectionIndex (preserveAnchorIndex);
			rsm.setValueIsAdjusting (false);
	        clearPressed ();
	        
	        // If mouse released outside of component then fire the mouseExited
	        // method as well (this will have been blocked from firing whilst dragging
	        // was happening)
	        if (!visibleRect.contains (mEvent.getPoint())) {
	        	mouseExited (mEvent);
	        }
	        
	        // Repaint selection
	        //((JParCoord)table).setRedrawAll (true);
	        table.repaint ();
		}
	
	
		
		@Override
		public void mouseDragged (final MouseEvent mEvent) {
            final Point point = mEvent.getPoint();         
            final int dragCol = table.columnAtPoint (point);
            
            if (dragCol == pressedCol && isDragging) {
            	final Object val = getObject (point.y, dragCol);
            	final Object valUpper = getObject (point.y + 1, dragCol);
            	final Dimension sortedIndicesRange = new Dimension ();
             	final SortedTableColumn stc = (SortedTableColumn) table.getColumnModel().getColumn (dragCol);
             	stc.getRange (sortedIndicesRange, (Comparable)pressedValue, (Comparable)pressedValueUpper,
             			(Comparable)val, (Comparable)valUpper); 	
         	
             	final Combinator comb = (mEvent.isAltDown() ? Combinator.FILTER : Combinator.ADD);
            	if (lastSortedRange.width == -1 || !lastSortedRange.equals (sortedIndicesRange)) {
            		final String intro = (comb == Combinator.FILTER ? FILTER : SELECT);
            		ToolTipManager.sharedInstance().mouseMoved (mEvent);
            		setTooltip (intro, pressedValue, val, pressedCol, sortedIndicesRange);		
            	    fillBrushingSelectionModel (((JParCoord)table).getBrushModel(), stc, sortedIndicesRange);
            	    
            		if (lastSortedRange.width != sortedIndicesRange.width) {
            			final int start = Math.min (lastSortedRange.width, sortedIndicesRange.width);
            			final int end = Math.max (lastSortedRange.width, sortedIndicesRange.width);
            			final boolean select = sortedIndicesRange.width < lastSortedRange.width;
            			tsm.selectRange (start, end - 1, table, stc, select ? comb : Combinator.FILTER);
            		}
            		else if (lastSortedRange.height != sortedIndicesRange.height) {
            			final int start = Math.min (lastSortedRange.height, sortedIndicesRange.height);
            			final int end = Math.max (lastSortedRange.height, sortedIndicesRange.height);
            			final boolean select = sortedIndicesRange.height > lastSortedRange.height;
            			tsm.selectRange (start + 1, end, table, stc, select ? comb : Combinator.FILTER);
            		}
	            	lastSortedRange.setSize (sortedIndicesRange.width, sortedIndicesRange.height);
	            	
	            	//LOGGER.debug ("ldc brush: "+ lineDrawCounter.toString());
	            	//if ((lineDrawCounter.width == BACKGROUND && lineDrawCounter.height == 0)
	            	//		|| !(lineDrawCounter.width == BACKGROUND)) {
	            	//	lineDrawCounter.setSize (END - 1, 0);          		
	            		table.repaint ();
	            	//}
            	}	
            	table.getColumnModel().getSelectionModel().setLeadSelectionIndex (dragCol);
            }      
		}
	
		
		@Override
		public void mousePressed (final MouseEvent mEvent) {
            if (SwingUtilities2.shouldIgnore (mEvent, table)) {
                return;
            }
            
            tsm.clear ();	// Clear temporary selection model
            final ListSelectionModel sModel = table.getSelectionModel ();
            final Point point = mEvent.getPoint();
            pressedCol = table.columnAtPoint (point);	// record column mouse drag originated on
            final Object val = getObject (point.y, pressedCol);
    		final Object valUpper = getObject (point.y + 1, pressedCol);
    		final int anchorIndex = sModel.getAnchorSelectionIndex();
    		
            
    		// If SHIFT key depressed, take pressed values from anchor (i.e. extend selection from previous)
    		// otherwise just set them to the same as val and valUpper
            if (mEvent.isShiftDown()) {
            	if (anchorIndex == -1) {
            		point.y = 0;
            		pressedValue = val;
            		pressedValueUpper = valUpper;
            	} else {
            		pressedValue = table.getValueAt (anchorIndex, pressedCol);
            		pressedValueUpper = pressedValue;
            	}
            } else {
        		pressedValue = val;
        		pressedValueUpper = valUpper;
            }
            
            sModel.setValueIsAdjusting (true);

            // If neither CTRL nor ALT held down this selection will replace current selections
            if (!mEvent.isControlDown() && !mEvent.isAltDown()) {
            	sModel.clearSelection();
            }
            // Display appropriate mouse cursor
            table.setCursor (!(mEvent.isAltDown() ^ mEvent.isControlDown()) ? Cursor.getDefaultCursor()
            		: (mEvent.isAltDown() ? FILTER_CURSOR : ADD_CURSOR));

        	
            // Mark column as selected column and repaint header
            final ListSelectionModel columnSelectionModel = table.getColumnModel().getSelectionModel();
            if (columnSelectionModel.getMinSelectionIndex() != columnSelectionModel.getMaxSelectionIndex()
            		|| columnSelectionModel.getMinSelectionIndex() != pressedCol) {
            	columnSelectionModel.setSelectionInterval (pressedCol, pressedCol);
            	table.getTableHeader().repaint ();
            }
                
            tsm.copy (sModel); // Copy current row selection model to temporary selection model
            initialModifiers = mEvent.getModifiers ();	// record original key selection modifiers    
            isDragging = true;	// set dragging to true
            ((JParCoord)table).setRedrawSelections (true);
            
            // Deduce the range of indices in the sorted column list that the current press range covers
            // (in large ranges, even one pixel could cover a range of values)
            final Dimension sortedIndicesRange = new Dimension ();
         	final SortedTableColumn stc = (SortedTableColumn) table.getColumnModel().getColumn (pressedCol);
         	stc.getRange (sortedIndicesRange, (Comparable)pressedValue, (Comparable)pressedValueUpper,
         			(Comparable)val, (Comparable)valUpper); 
         	
         	// Make a copy of current anchor index to restore after drag
         	if (!mEvent.isShiftDown()) {
         		final int modelMin = ((List<Integer>)stc.getFilteredOrderedList()).get(sortedIndicesRange.width).intValue();
         		final int viewMin = table.convertRowIndexToView (modelMin);
         		sModel.setAnchorSelectionIndex (viewMin);
         		preserveAnchorIndex = viewMin;
         	} else {
         		preserveAnchorIndex = anchorIndex;
         	}
         	
         	// Combine pressed selection with existing row selection via the temporary selection model
         	final Combinator comb = (mEvent.isAltDown() ? Combinator.FILTER : Combinator.ADD);
         	tsm.selectRange (sortedIndicesRange.width, sortedIndicesRange.height, table, stc, comb);
                  
            lastSortedRange.setSize (sortedIndicesRange);	// set range of last drag operation in terms of sorted column indices
        }

		

		@Override
		public void mouseClicked (final MouseEvent mEvent) {
			// Empty method	
		}
		

		@Override
        public void mouseMoved (final MouseEvent mEvent) { 
			if (isTableActive()) {
				doBrushing (mEvent.getPoint ());
			}
		}
    }
    
    
	protected boolean isTableActive () {
		return (table.isEnabled() && table.getModel() != null && (table.getRowCount() * table.getColumnCount() > 0));
	}

    
	void setTooltip (final String intro, final Object val, final Object valUpper, 
			final int viewColIndex, final Dimension range) {
    	final SortedTableColumn stc = (SortedTableColumn) table.getColumnModel().getColumn (viewColIndex);
    	LOGGER.debug ("setTooltip val: "+val+", vu: "+valUpper) ;
    	
    	final String label1 = toString2 (val, stc, viewColIndex);
    	final String label2 = (val == null || val.equals (valUpper)) ? "" : " - "+toString2 (valUpper, stc, viewColIndex);
		formatValues [0] = intro + ("".equals (intro) ? "" : " ") + table.getColumnName(viewColIndex);
		formatValues [1] = label1 + label2;
		final int rval = range.height - range.width + 1;
		formatValues [2] = Integer.valueOf (rval);
		formatValues [3] = rval == 1 ? "" : "s";
		final String tipText = toolTipFormat.format (formatValues);
		
		//System.err.println (tipText);
		table.setToolTipText (tipText);
	} 
    
    Object getObject (final Point point) {
    	final int column = table.columnAtPoint (point);
    	return getValue (point.y, column, visibleRect);
    }
    
    Object getObject (final double y, final int column) {
    	return getValue (y, column, visibleRect);
    }

//
//  Factory methods for the Listeners
//

    protected Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    /**
     * Creates the key listener for handling keyboard navigation in the JTable.
     */
    protected KeyListener createKeyListener() {
    	return getHandler();
    }


    /**
     * Creates the mouse listener for the JTable.
     */
    protected MouseInputListener createMouseInputListener() {
        return getHandler();
    }
    
//
//  The installation/uninstall procedures and support
//

    public static ComponentUI createUI (final JComponent comp) {
        return new ParCoordUI();
    }

//  Installation

    @Override
	public void installUI (final JComponent comp) {
        table = (JTable)comp;
        getActiveBounds();

        installDefaults();
        installDefaults2();
        installListeners();
        installKeyboardActions();
    }

    /**
     * Initialize JTable properties, e.g. font, foreground, and background.
     * The font, foreground, and background properties are only set if their
     * current value is either null or a UIResource, other properties are set
     * if the current value is null.
     *
     * @see #installUI
     */
    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(table, "Table.background",
                                         "Table.foreground", "Table.font");
        // JTable's original row height is 16.  To correctly display the
        // contents on Linux we should have set it to 18, Windows 19 and
        // Solaris 20.  As these values vary so much it's too hard to
        // be backward compatible and try to update the row height, we're
        // therefore NOT going to adjust the row height based on font.  If the
        // developer changes the font, it's there responsibility to update
        // the row height.

        LookAndFeel.installProperty(table, "opaque", Boolean.FALSE);
        
        final Color sbg = table.getSelectionBackground();
        if (sbg == null || sbg instanceof UIResource) {
            table.setSelectionBackground(UIManager.getColor("Table.selectionBackground"));
        }

        final Color sfg = table.getSelectionForeground();
        if (sfg == null || sfg instanceof UIResource) {
            table.setSelectionForeground(UIManager.getColor("Table.selectionForeground"));
        }

        final Color gridColor = table.getGridColor();
        if (gridColor == null || gridColor instanceof UIResource) {
            table.setGridColor(UIManager.getColor("Table.gridColor"));
        }
        
        final Color brushColour = ((JParCoord)table).getBrushForegroundColour();
        if (brushColour == null || brushColour instanceof UIResource) {
            ((JParCoord)table).setBrushForegroundColour (UIManager.getColor ("ParCoord.brushForeground"));
        }
        
        final Color brushSelectionColour = ((JParCoord)table).getBrushSelectionColour();
        if (brushSelectionColour == null || brushSelectionColour instanceof UIResource) {
            ((JParCoord)table).setBrushSelectionColour (UIManager.getColor ("ParCoord.brushSelection"));
        }
        
     
        // install the scrollpane border
        Container parent = table.getParent();  // should be viewport
        if (parent != null) {
            parent = parent.getParent();  // should be the scrollpane
            if (parent instanceof JScrollPane) {
                LookAndFeel.installBorder((JScrollPane)parent, "Table.scrollPaneBorder");
            }
        }
        
        makeColourCache ();
    }

    private void installDefaults2() {
    	final TransferHandler tHandler = table.getTransferHandler();
    	if (tHandler == null || tHandler instanceof UIResource) {
    		table.setTransferHandler(DEFAULT_TRANSFER_HANDLER);
            // default TransferHandler doesn't support drop
            // so we don't want drop handling
            if (table.getDropTarget() instanceof UIResource) {
                table.setDropTarget(null);
            }
		}
    }

    /**
     * Attaches listeners to the JTable.
     */
    protected void installListeners() {
        keyListener = createKeyListener();
        mouseInputListener = createMouseInputListener();
        componentListener = new MyComponentListener ();

        table.addKeyListener (keyListener);
        table.addMouseListener (mouseInputListener);
        table.addMouseMotionListener (mouseInputListener);
        table.addPropertyChangeListener (getHandler());
        table.addComponentListener (componentListener);
        
        LOGGER.info ("Row count: "+table.getRowCount()+", col count: "+table.getColumnCount());
        antiAlias = (table.getRowCount() * table.getColumnCount()) < 2000;
        antiAliasTimer.setInitialDelay ((table.getRowCount() * 4) / 3);
        antiAliasTimer.setDelay ((table.getRowCount() * 4) / 3);
        antiAliasTimer.setRepeats (false);
        if (!antiAlias) {
        	antiAliasTimer.start ();      
        }
        
        Toolkit.getDefaultToolkit().addAWTEventListener (
        		cancelAntiAlias,
        		AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK
        		| AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK
        		| AWTEvent.ADJUSTMENT_EVENT_MASK
        		);    
        
        final ActionListener brushListener = new BrushListener ();
        brushOn = new Timer (750, brushListener);
        brushOn.setRepeats (false);
        brushOff = new Timer (5000, brushListener);
        brushOff.setRepeats (false);
    }

    /**
     * Register all keyboard actions on the JTable.
     */
    protected void installKeyboardActions() {
        LazyActionMap.installLazyActionMap ((JParCoord)table, ParCoordUI.class, "JParCoord.actionMap");

        final InputMap inputMap = getInputMap (JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
		if (LOGGER.isInfoEnabled()) {
			final KeyStroke keys[] = UIUtils.sortKeyStrokesByInputActionName (inputMap);
	    	for (KeyStroke k : keys) {
	    		LOGGER.info ("sorted k: "+k+" == "+inputMap.get(k));
	    	}
		}
		
		SwingUtilities.replaceUIInputMap ((JParCoord)table,
					JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
					inputMap);
	}

    
    InputMap getInputMap (final int condition) {
        if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
        	final InputMap keyMap =
                (InputMap)DefaultLookup.get((JParCoord)table, this,
                                            "Table.ancestorInputMap");
            return keyMap;
        }
        return null;
    }

    static void loadActionMap (final LazyActionMap map) {
        // IMPORTANT: There is a very close coupling between the parameters
        // passed to the Actions2 constructor. Only certain parameter
        // combinations are supported. For example, the following Action would
        // not work as expected:
        //     new Actions2(Actions2.NEXT_ROW_CELL, 1, 4, false, true)
        // Actions2 which move within the selection only (having a true
        // inSelection parameter) require that one of dx or dy be
        // zero and the other be -1 or 1. The point of this warning is
        // that you should be very careful about making sure a particular
        // combination of parameters is supported before changing or
        // adding anything here.
        
        map.put(new Actions2(Actions2.NEXT_COLUMN, 1, 0, false, false));
        map.put(new Actions2(Actions2.NEXT_COLUMN_CHANGE_LEAD, 1, 0, false, false));
        map.put(new Actions2(Actions2.PREVIOUS_COLUMN, -1, 0, false, false));
        map.put(new Actions2(Actions2.PREVIOUS_COLUMN_CHANGE_LEAD, -1, 0, false, false));
        map.put(new Actions2(Actions2.NEXT_ROW, 0, 1, false, false));
        map.put(new Actions2(Actions2.NEXT_ROW_CHANGE_LEAD, 0, 1, false, false));
        map.put(new Actions2(Actions2.PREVIOUS_ROW, 0, -1, false, false));
        map.put(new Actions2(Actions2.PREVIOUS_ROW_CHANGE_LEAD, 0, -1, false, false));
        map.put(new Actions2(Actions2.NEXT_ROW_EXTEND_SELECTION, 0, 1, true, false));
        map.put(new Actions2(Actions2.PREVIOUS_ROW_EXTEND_SELECTION, 0, -1, true, false));
        
        map.put(new Actions2(Actions2.SCROLL_UP_CHANGE_SELECTION, false, false, true, false));
        map.put(new Actions2(Actions2.SCROLL_DOWN_CHANGE_SELECTION, false, true, true, false));
        map.put(new Actions2(Actions2.FIRST_COLUMN, false, false, false, true));
        map.put(new Actions2(Actions2.LAST_COLUMN, false, true, false, true));
        map.put(new Actions2(Actions2.SCROLL_UP_EXTEND_SELECTION, true, false, true, false));
        map.put(new Actions2(Actions2.SCROLL_DOWN_EXTEND_SELECTION, true, true, true, false));
		map.put(new Actions2(Actions2.FIRST_ROW, false, false, true, true));
		map.put(new Actions2(Actions2.LAST_ROW, false, true, true, true));
		map.put(new Actions2(Actions2.FIRST_ROW_EXTEND_SELECTION, true, false, true, true));
		map.put(new Actions2(Actions2.LAST_ROW_EXTEND_SELECTION, true, true, true, true));
	
		map.put(new Actions2(Actions2.NEXT_COLUMN_CELL, 1, 0, false, true));
		map.put(new Actions2(Actions2.PREVIOUS_COLUMN_CELL, -1, 0, false, true));
		map.put(new Actions2(Actions2.NEXT_ROW_CELL, 0, 1, false, true));
		map.put(new Actions2(Actions2.PREVIOUS_ROW_CELL, 0, -1, false, true));
	
		map.put(new Actions2(Actions2.SELECT_ALL));
	    map.put(new Actions2(Actions2.CLEAR_SELECTION));
		map.put(new Actions2(Actions2.CANCEL_EDITING));
		map.put(new Actions2(Actions2.START_EDITING));

        map.put(TransferHandler.getCutAction().getValue(Action.NAME),
                TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());

        map.put(new Actions2(Actions2.ADD_TO_SELECTION));
        map.put(new Actions2(Actions2.TOGGLE_AND_ANCHOR));
        map.put(new Actions2(Actions2.EXTEND_TO));
        map.put(new Actions2(Actions2.MOVE_SELECTION_TO));
        map.put(new Actions2(Actions2.FOCUS_HEADER));
    }

//  Uninstallation

    @Override
	public void uninstallUI (final JComponent comp) {
        uninstallDefaults();
        uninstallListeners();
        uninstallKeyboardActions();

        table = null;
    }

    protected void uninstallDefaults() {
    	if (table.getTransferHandler() instanceof UIResource) {
    		table.setTransferHandler(null);
    	}
    }

    protected void uninstallListeners() {
        table.removeKeyListener(keyListener);
        table.removeMouseListener(mouseInputListener);
        table.removeMouseMotionListener(mouseInputListener);
        table.removePropertyChangeListener(getHandler());
        table.removeComponentListener (componentListener);
        
        Toolkit.getDefaultToolkit().removeAWTEventListener (cancelAntiAlias);
        
        keyListener = null;
        mouseInputListener = null;
        handler = null;
        antiAliasTimer.stop ();
    }

    protected void uninstallKeyboardActions() {
	SwingUtilities.replaceUIInputMap(table, JComponent.
				   WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
        SwingUtilities.replaceUIActionMap(table, null);
    }

    /**
     * Returns the baseline.
     *
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * @see javax.swing.JComponent#getBaseline(int, int)
     * @since 1.6
     */
    @Override
	public int getBaseline (final JComponent comp, final int width, final int height) {
        super.getBaseline (comp, width, height);
        final UIDefaults lafDefaults = UIManager.getLookAndFeelDefaults();
        Component renderer = (Component)lafDefaults.get(
                BASELINE_COMPONENT_KEY);
        if (renderer == null) {
        	final DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
            renderer = tcr.getTableCellRendererComponent(
                    table, "a", false, false, -1, -1);
            lafDefaults.put(BASELINE_COMPONENT_KEY, renderer);
        }
        renderer.setFont(table.getFont());
        final int rowMargin = table.getRowMargin();
        return renderer.getBaseline(Integer.MAX_VALUE, table.getRowHeight() -
                                    rowMargin) + rowMargin / 2;
    }

    /**
     * Returns an enum indicating how the baseline of the component
     * changes as the size changes.
     *
     * @throws NullPointerException {@inheritDoc}
     * @see javax.swing.JComponent#getBaseline(int, int)
     * @since 1.6
     */
    @Override
	public Component.BaselineResizeBehavior getBaselineResizeBehavior (final JComponent comp) {
        super.getBaselineResizeBehavior (comp);
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }

	//
	// Size Methods
	//

    private Dimension createTableSize (final long width) {
		int height = 0;
		final int rowCount = table.getRowCount();
		if (rowCount > 0 && table.getColumnCount() > 0) {
		    final Rectangle rect = table.getCellRect (rowCount-1, 0, true);
		    height = rect.y + rect.height;
		}
		if (table.getParent() instanceof JViewport) {
			final JViewport jview = (JViewport)table.getParent();
			height = jview.getHeight();
		} else {
			height = table.getHeight();
		}
		// Width is always positive. The call to abs() is a workaround for
		// a bug in the 1.1.6 JIT on Windows.
		long tmp = Math.abs(width);
	    if (tmp > Integer.MAX_VALUE) {
	        tmp = Integer.MAX_VALUE;
	    }
		return new Dimension((int)tmp, height);
    }

    /**
     * Return the minimum size of the table. The minimum height is the
     * row height times the number of rows.
     * The minimum width is the sum of the minimum widths of each column.
     */
    @Override
	public Dimension getMinimumSize (final JComponent comp) {
        long width = 0;
        final Enumeration<TableColumn> enumeration = table.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
        	final TableColumn aColumn = (TableColumn)enumeration.nextElement();
            width = width + aColumn.getMinWidth();
        }
		long tmp = Math.abs(width);
	    if (tmp > Integer.MAX_VALUE) {
	        tmp = Integer.MAX_VALUE;
	    }
        return new Dimension ((int)tmp, 100);
    }

    /**
     * Return the preferred size of the table. The preferred height is the
     * row height times the number of rows.
     * The preferred width is the sum of the preferred widths of each column.
     */
    @Override
	public Dimension getPreferredSize (final JComponent comp) {
        long width = 0;
        final Enumeration<TableColumn> enumeration = table.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
        	final TableColumn aColumn = (TableColumn)enumeration.nextElement();
            width = width + aColumn.getPreferredWidth();
        }
        return createTableSize (width);
    }

    /**
     * Return the maximum size of the table. The maximum height is the
     * row height times the number of rows.
     * The maximum width is the sum of the maximum widths of each column.
     */
    @Override
	public Dimension getMaximumSize (final JComponent comp) {
        long width = 0;
        final Enumeration<TableColumn> enumeration = table.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
        	final TableColumn aColumn = (TableColumn)enumeration.nextElement();
            width = width + aColumn.getMaxWidth();
        }
        return createTableSize(width);
    }

    
    
    public ListSelectionModel getSelectionModel () {
    	return table.getSelectionModel();
    }
    
    public boolean uberSelectionExists () {
    	return (((JParCoord)table).getUberSelection() != null && !((JParCoord)table).getUberSelection().isSelectionEmpty());
    }
    
    /**
     * Brushing routines
     */
    
    protected void doBrushing (final Point point) {

    	if (((JParCoord)table).isBrushing()) {
    		final int brushedCol = table.columnAtPoint (point);
			
			if (brushedCol != -1) {
				final Object val = getObject (point.y, brushedCol);
	        	Object valUpper = getObject (point.y + 1, brushedCol);
	        	if (LOGGER.isDebugEnabled()) {
	        		LOGGER.debug ("val: "+val+", valUpper: "+valUpper);
	        	}

	        	final Dimension range = new Dimension ();
	         	final SortedTableColumn stc = (SortedTableColumn) table.getColumnModel().getColumn (brushedCol);
	         	if (!stc.isEmpty ()) {
		         	stc.getRange (range, (Comparable)val, (Comparable)valUpper, 
		         			(Comparable)val, (Comparable)valUpper); 
		         	LOGGER.debug ("range: "+range);
		         	
		         	// If the end of the range is the last indexed value of val then valUpper should be ignored
		         	// when the tooltip is drawn
		         	if (stc.getLastIndexOf((Comparable)val) == range.height && range.height - range.width >= 0) {
		         		valUpper = val;
		         	}
		         	
		         	if (!range.equals (lastBrush)) {
		         		setTooltip ("", val, valUpper, brushedCol, range);
		         		lastBrush.setSize (range);
		         		final ListSelectionModel brushModel = ((JParCoord)table).getBrushModel();
			        	calcAndDrawBrushing (brushModel, stc, range);
		         	}
	         	}
			}
    	}
    }
    
    
    protected void calcAndDrawBrushing (final ListSelectionModel brushModel, final SortedTableColumn<?> stc, final Dimension range) {
    	LOGGER.info ("calcAndDrawBrushing");
    	final boolean wasEmpty = fillBrushingSelectionModel (brushModel, stc, lastBrush);
    	drawBrushing (brushModel, wasEmpty);
    }
    
    protected boolean fillBrushingSelectionModel (final ListSelectionModel brushModel, final SortedTableColumn stc, final Dimension range) { 	
    	final boolean empty = brushModel.isSelectionEmpty();
    	
    	if (((JParCoord)table).isBrushing()) {
	    	brushOn.restart ();
	 		brushOff.restart ();
	 		final List<Integer> columnOrder = stc.getFilteredOrderedList();
	    	
	    	brushModel.setValueIsAdjusting (true);
	    	brushModel.clearSelection();
	    	if (brushing && range.width != -1) {
				for (int n = range.width; n <= range.height; n++) {
					final int primaryModelIndex = columnOrder.get(n).intValue();
					final int primaryViewIndex = table.convertRowIndexToView (primaryModelIndex);
	        		brushModel.addSelectionInterval (primaryViewIndex, primaryViewIndex);
				}
	    	}
			brushModel.setValueIsAdjusting (false);
    	}
		return empty;
    }
    
    
    protected void startBrushing () {
    	if (((JParCoord)table).isBrushing()) {
	    	brushOn.restart ();
	    	brushOff.restart ();
    	}
    }
    
    protected void stopBrushing () {
    	brushOn.stop ();
    	brushOff.stop ();
    	clearBrushing ();
    }
    
    protected void clearBrushing () {
    	final ListSelectionModel brushModel = ((JParCoord)table).getBrushModel();
    	final boolean wasEmpty = brushModel.isSelectionEmpty();
    	brushModel.clearSelection ();
    	lastBrush.setSize (-1, -1);
    	brushingCancelled = true;
    	drawBrushing (brushModel, wasEmpty);
    }
    
    protected void drawBrushing (final ListSelectionModel brushModel, final boolean wasEmpty) {
    	if (!brushModel.isSelectionEmpty() || !wasEmpty) {
    		drawingStates.set (BRUSHED, brushing);
    		drawingStates.set (BRUSHED_SELECTED, brushing);
		    table.repaint ();
		}
    }
    
    
    /**
     * Paint routines
     */
    
    
    @Override
	public void update (final Graphics graphics, final JComponent comp) {
    	//super.update(g, c);
    	getActiveBounds ();
    	paint (graphics, comp);
    }
    
    
    @Override
	public void paint (final Graphics graphics, final JComponent comp) {
    	
    	if (LOGGER.isDebugEnabled()) {
	    	LOGGER.debug ("---- UI PAINT ------");
	    	LOGGER.debug (getMilliFormatTime()+", antiAlias: "+antiAlias+", aadelay: "+antiAliasTimer.getInitialDelay());
    	}

    	antiAliasTimer.stop(); // Stop antialias timer now we're redrawing, antialised or not
       	
    	final Rectangle clip = graphics.getClipBounds();
    	//Dimension cSize = visibleRectNoInsets.getSize();
    	final Dimension cSize = comp.getSize ();
    	
    	// Basically if the clip is the visible area of the component we assume the entire
    	// component needs redrawn, even the currently off-screen bits
    	// It would be more efficient memory-wise to keep the image size to the visible area
    	// but it would make dragging the parcoord in a scrollpane torturously slow and flickery.
    	final Rectangle compZeroedBounds = new Rectangle (0, 0, cSize.width, cSize.height);
    	final boolean clipIsVisibleArea = visibleRectNoInsets.getSize().equals (clip.getSize());
    	final Rectangle redrawArea = clipIsVisibleArea ? compZeroedBounds : clip;
    	final Dimension cRange = getColumnRangeFromClip (graphics, redrawArea, comp);

    	//LOGGER.info ("vrni: "+visibleRectNoInsets+", clip: "+clip+", cRange: "+cRange);
    	final JTable jTable = (JTable)comp;
    	final JParCoord jParCoord = (JParCoord)comp;
    	
    	// Possible batik SVG or screenshot output
     	final boolean svgOutput = GraphicsUtil.isSVGGraphics (graphics);
    	final boolean nonScreenOutput = svgOutput | GraphicsUtil.isNonScreenImage ((Graphics2D)graphics);
    	
    	if (nonScreenOutput) {
    		jParCoord.setRedrawAll (true);
    		graphics.setColor (comp.getBackground());
    	    graphics.fillRect (0, 0, visibleRectNoInsets.width, visibleRectNoInsets.height);
    	}
    	
    	final boolean selAdjusting = jTable.getSelectionModel().getValueIsAdjusting();
    	final boolean redrawAll = jParCoord.isRedrawAll();	
    	final boolean redrawSelections = jParCoord.isRedrawSelections();

    	    	
    	if (redrawSelections) {
        	drawingStates.set (SELECTED, true);
	    	drawingStates.set (UBER_SELECTED, true);
	    	paintLegendLayer (graphics, cRange, redrawArea);
    		((JParCoord)table).setRedrawSelections (false);
    	}
    	
	    // Reset image buffers if everything to be redrawn (usually after a resize)
    	if (redrawAll) {
    		clearDrawParameters ();
    		
    		for (int layer = 0; layer < img.length; layer++) {
    	       	img[layer] = GraphicsUtil.initImage (img[layer], (Component)comp, cSize, layer == 0 ? Transparency.OPAQUE : Transparency.TRANSLUCENT);
    		}

	       	if (img[BACKGROUND] != null) {
	       		final Graphics gImg = GraphicsUtil.primeImageGraphics (graphics, redrawArea, img, table.getBackground(), BACKGROUND);
	       		//Graphics gImg = GraphicsUtil.primeImageGraphics (graphics, graphics.getClipBounds(), img, table.getBackground(), UIConstants.ALL);
	    		// Slap column lines on ALL image as very first thing drawn
	       		translateDrawingArea (gImg, visibleRectNoInsets, false);
	    		paintGrid (gImg, 0, table.getRowCount(), cRange.width, cRange.height);
	    		translateDrawingArea (gImg, visibleRectNoInsets, true);
	    	}
	       	
	       	if (!redrawSelections) {
	       		paintLegendLayer (graphics, cRange, redrawArea);
	       	}
	    	drawingStates.set (BACKGROUND, true);
	    	drawingStates.set (SELECTED, true);
	    	drawingStates.set (UBER_SELECTED, true);
	    	jParCoord.setRedrawAll (false);
    	}
    	
    	// if just brushing (should this be true) or if label separation has changed and caused a repaint
    	if (!redrawSelections && !redrawAll) {
    		paintLegendLayer (graphics, cRange, redrawArea);
    	}
    	
    	// What's to redraw?
    	drawingStates.set (SELECTED, drawingStates.get (SELECTED) || selAdjusting /*|| isDragging*/);
    	drawingStates.set (UBER_SELECTED, drawingStates.get (UBER_SELECTED) || selAdjusting /*|| isDragging*/);
    	
    	// These are set in drawBrushing that calls repaint(), this stops repeated calling of the drawing runnable
    	// caused by these being set to true on every repaint call
		//drawingStates.set (BRUSHED, brushing);
		//drawingStates.set (BRUSHED_SELECTED, brushing);
    		
    	
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug ("graphics class name: "+graphics.getClass().getCanonicalName()+", svg: "+svgOutput);
    		LOGGER.debug ("ra: "+redrawAll+", rs: "+redrawSelections);
    		LOGGER.debug ("ds: "+drawingStates);
    	}
    	
    	
		for (int selectionType = 0; selectionType < END; selectionType++) {
			Image image = img [SELECTION_LAYER_TABLE [selectionType]];
			final Graphics layerGraphics = (image == null ? null : image.getGraphics());
			drawingAreas [selectionType] = (layerGraphics == null || svgOutput) ? graphics : layerGraphics;
		}
		
     	drawData (graphics, cRange.width, cRange.height);
    	
        if (!antiAlias) {
          	LOGGER.debug (getMilliFormatTime()+", restarting antialias timer");
        	antiAliasTimer.restart(); // start antialiasing up again if drawing wasn't antialiased
        } 
    	LOGGER.debug ("Leaving PAINT()");
    }
    
    
    void paintLegendLayer (final Graphics graphics, final Dimension cRange, final Rectangle redrawArea) {
       	if (img[LEGEND] != null) {
	    	GraphicsUtil.primeImageGraphics (graphics, redrawArea, img, table.getBackground(), LEGEND);
	    	final Graphics legendLayerGraphics = img[LEGEND].getGraphics();
	    	final Rectangle oldClip = legendLayerGraphics.getClipBounds();
	       	legendLayerGraphics.setClip (redrawArea);
	       	((Graphics2D)legendLayerGraphics).setRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    	paintLegend (legendLayerGraphics, cRange.width, cRange.height);
	       	((Graphics2D)legendLayerGraphics).setRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	    	legendLayerGraphics.setClip (oldClip);
       	}
    }
    
    
    protected void clearDrawParameters () {
    	drawingStates.clear ();
    	for (int n = 0; n < drawingAreas.length; n++) {
    		drawingAreas [n] = null;
    	}
    }
    

    private String getMilliFormatTime () {
    	long time = System.currentTimeMillis();
    	time %= 1e7;
    	time /= 1e2;
    	return Long.toString (time);
    }
    
    
	//
	//  Paint methods and support
	//
    
    public void drawData (final Graphics graphics, final int cMin, final int cMax) {

    	if (!drawingStates.isEmpty()) {
    		LOGGER.debug ("drawingStates: "+drawingStates.cardinality()+", "+drawingStates);
	    	if (drawingRunnable == null) {
	    		drawingRunnable = new DrawRunnable ();
	    	}
	    	drawingRunnable.setup (graphics, cMin, cMax, drawingStates);
	    	if (!drawingRunnable.isActive()) {
	    		Executioner.getInstance().getExecutorService().execute (drawingRunnable);
	    	};
	    	
    	}
    	
    	paintLayers (graphics);  
    	
    	if (table.getColumnModel().getColumnCount() > 0 && drawingRunnable.isActive()) {
	        final Rectangle clip = new Rectangle (graphics.getClipBounds ());
	        table.repaint (50, clip.x, clip.y, clip.width, clip.height);
    	}
    }
    
    public Dimension getColumnRangeFromClip (final Graphics graphics, Rectangle clip, final JComponent comp) {
        final Dimension cRange = new Dimension ();
    	
        if (clip == null) {
        	//clip = new Rectangle (0, 0, cSize.width, cSize.height);
        	clip = new Rectangle (visibleRectNoInsets);
        }

        final Rectangle bounds = table.getBounds();
        LOGGER.info ("clip: "+clip+", bounds: "+bounds);
        // account for the fact that the graphics has already been translated
        // into the table's bounds
        bounds.x = bounds.y = 0;

		if (table.getRowCount() <= 0 || table.getColumnCount() <= 0 ||
            // this check prevents us from painting the entire table
            // when the clip doesn't intersect our bounds at all
            clip == null || !bounds.intersects(clip)) {
	
	        paintDropLines(graphics);
		    return cRange;
		}

		final boolean ltr = table.getComponentOrientation().isLeftToRight();

        Point upperLeft = clip.getLocation();
        if (!ltr) {
            upperLeft.x++;
        }

        final Point lowerRight = new Point(clip.x + clip.width - (ltr ? 1 : 0),
                                     clip.y + clip.height);

        int cMin = table.columnAtPoint (ltr ? upperLeft : lowerRight); 
        int cMax = table.columnAtPoint (ltr ? lowerRight : upperLeft);        
        // This should never happen.
        if (cMin == -1) {
        	cMin = 0;
        }
        // If the table does not have enough columns to fill the view we'll get -1.
        // Replace this with the index of the last column.
        if (cMax == -1) {
        	cMax = table.getColumnCount()-1;
        }
        
        cRange.setSize (cMin, cMax);
        return cRange;
    }
    

	
	void calcRowCoordData (final RowCoordData rowCoordData, final TableModel tModel, 
			final int modelRow, final int cMin, final int cMax) {
		
		rowCoordData.setContinuousPath (true);
		rowCoordData.setActiveColumns (0);
		
		final Object[] dataRow = rowCoordData.getData();
		final BitSet nullColumnFlags = rowCoordData.getNullColumnFlags();
		final int[] yCoords = rowCoordData.getYCoords();
		boolean nullAfterValueFound = false, valueFound = false;
			
		for (int c = 0; c < dataRow.length; c++) {
			dataRow [c] = tModel.getValueAt (modelRow, table.convertColumnIndexToModel (c));
			
			if (dataRow [c] != null) {
				yCoords [c] = getY (dataRow [c], c, visibleRect);
				
				if (nullAfterValueFound) {
					rowCoordData.setContinuousPath (false);
				}
				rowCoordData.setActiveColumns (rowCoordData.getActiveColumns() + 1);
				nullColumnFlags.clear (c);
				valueFound = true;
			}
			else {
				yCoords [c] = -1;
				nullColumnFlags.set (c);
				if (valueFound) {
					nullAfterValueFound = true;
				}
				//LOGGER.debug ("Null @ Row: "+viewRow+", Col: "+tModel.getColumnName(c));
			}
		}
		
		nullColumnFlags.clear (dataRow.length);
		
		
		// Fill in y values for nulls dependent on neighbouring values
		if (nullColumnFlags.nextSetBit (0) >= 0) {
			int lasty = (int) visibleRect.getCenterY();
			int fillFromColumn = -1;
			
			for (int c = 0; c <= dataRow.length; c++) {
				boolean last = (c == dataRow.length);
					
				if (nullColumnFlags.get (c) && fillFromColumn == -1) {
					fillFromColumn = c;
				}	
				
				if (!nullColumnFlags.get (c)) {
					final int y = (last ? (int) visibleRect.getCenterY() : yCoords [c]);

					if (fillFromColumn != -1) {
						final int nully = (y + lasty) / 2 > (int)visibleRect.getCenterY() ?
								(int)table.getHeight() - 5 : 5;
									
						for (int cc = fillFromColumn; cc < c; cc++) {
							yCoords [cc] = nully;
						}
						
						fillFromColumn = -1;
					}
					
					lasty = y;	
				}
			}
		}
	}
	
	

	
	void translateDrawingAreas (final Graphics[] drawingAreas, final Rectangle visibleArea, final boolean reverse) {
		for (Graphics graphics : drawingAreas) {
			translateDrawingArea (graphics, visibleArea, reverse);
		}
	}
	
	void translateDrawingArea (final Graphics drawingArea, final Rectangle visibleArea, final boolean reverse) {
		if (drawingArea != null) {
			//drawingArea.translate (reverse ? visibleArea.x : -visibleArea.x, reverse ? visibleArea.y : -visibleArea.y);
		}
	}
	
	
	void paintLayers (final Graphics screenGraphics) {
		final boolean svgOutput = GraphicsUtil.isSVGGraphics (screenGraphics);
		if (!svgOutput) {
			final ListSelectionModel selectModel = table.getSelectionModel();
			final ListSelectionModel brushModel = ((JParCoord)table).getBrushModel();
			final ListSelectionModel uberSelectionModel = ((JParCoord)table).getUberSelection();
			
			final BitSet layerSet = new BitSet ();
			layerSet.set (BACKGROUND, true);
			layerSet.set (SELECTED, !selectModel.isSelectionEmpty());
			layerSet.set (UBER_SELECTED, uberSelectionModel != null && !uberSelectionModel.isSelectionEmpty());
			layerSet.set (BRUSHED_LAYER, !brushModel.isSelectionEmpty());
			layerSet.set (LEGEND, true);
			
			translateDrawingArea (screenGraphics, visibleRectNoInsets, true);
			for (int bit = layerSet.nextSetBit(0); bit >= 0; bit = layerSet.nextSetBit (bit + 1)) {
				screenGraphics.drawImage (img[bit], 0, 0, table);
			}

			translateDrawingArea (screenGraphics, visibleRectNoInsets, false);
		}
	}
	

	Paint setPaintFromGradientCache (final Graphics2D gDrawable, 
			final Map <Integer, Map <Integer, Paint>> gradients, 
			final int start, final int end, final int startx, final int endx,
			final Color fadeColour) {
		
		final Paint oldPaint = gDrawable.getPaint ();
		if (end - start > 1) {
			final Integer startInt = Integer.valueOf (start);
			final Integer endInt = Integer.valueOf (end);
			Map<Integer, Paint> gradientsByStartIndex = gradients.get (startInt);
			if (gradientsByStartIndex == null) {
				gradientsByStartIndex = new HashMap <Integer, Paint> ();
				gradients.put (startInt, gradientsByStartIndex);
			}
			Paint paint = gradientsByStartIndex.get (endInt);
			if (paint == null) {
				paint = new GradientPaint (startx, 0, gDrawable.getColor(), 
						startx + ((endx - startx) / 2), 0, fadeColour, true);
				gradientsByStartIndex.put (endInt, paint);
			}
			gDrawable.setPaint (paint);
		}
		return oldPaint;
	}
	
    
    protected int getY (final Object valT, final int viewIndex, final Rectangle rect) {
    	final SortedTableColumn<?> column = (SortedTableColumn<?>)table.getColumnModel().getColumn (viewIndex);
		int deltay = column.getY (valT, rect.height);
    	if (column.getCurrentOrder() == SortOrder.DESCENDING) {
			deltay = rect.height - deltay;
		}
    	return rect.y + deltay;
    }
    
    
    protected Object getValue (final double y, final int viewIndex, final Rectangle rect) {
    	final SortedTableColumn<?> column = (SortedTableColumn<?>)table.getColumnModel().getColumn (viewIndex);
		double ratio = ((double)(y - rect.y) / (double)(rect.height));
		//System.err.println ("y: "+y+", ratio: "+ratio+", rect: "+rect);
		if (column.getCurrentOrder() == SortOrder.DESCENDING) {
			ratio = 1.0 - ratio;
		}
		return column.getValue (ratio);
    }
    

    
    void paintLegend (final Graphics gLegend, final int cMin, final int cMax) {
    	if (gLegend != null) {   		
			paintAxes (gLegend, cMin, cMax);        
        	paintDropLines (gLegend);     	
    	}
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

        Rectangle rect = getHDropLineRect(loc);
        if (rect != null) {
        	final int x = rect.x;
        	final int width = rect.width;
            if (color != null) {
                extendRect(rect, true);
                graphics.setColor(color);
                graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
            }
            if (!loc.isInsertColumn() && shortColor != null) {
                graphics.setColor(shortColor);
                graphics.fillRect(x, rect.y, width, rect.height);
            }
        }

        rect = getVDropLineRect(loc);
        if (rect != null) {
        	final int y = rect.y;
        	final int height = rect.height;
            if (color != null) {
                extendRect(rect, false);
                graphics.setColor(color);
                graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
            }
            if (!loc.isInsertRow() && shortColor != null) {
                graphics.setColor(shortColor);
                graphics.fillRect(rect.x, y, rect.width, height);
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

            if (table.getRowCount() != 0) {
            	final Rectangle lastRect = table.getCellRect(table.getRowCount() - 1, 0, true);
                rect.height = lastRect.y + lastRect.height;
            } else {
                rect.height = table.getHeight();
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
    private void paintAxes (final Graphics graphics, final int cMin, final int cMax) {
        graphics.setColor (table.getGridColor());
        graphics.setFont (table.getFont());

        final TableColumnModel columnModel = table.getColumnModel();
        final int columns = columnModel.getColumnCount();
	    int x = 0;
	    //Rectangle clipRect = g.getClipBounds();
	    
	    final boolean lToR = table.getComponentOrientation().isLeftToRight();
	    final int columnStart = lToR ? 0 : columns;
	    final int columnEnd = lToR ? columns : -1;
	    final int columnIncrement = lToR ? 1 : -1;
	    
		for (int column = columnStart; column != columnEnd; column += columnIncrement) {
			final int colWidth = columnModel.getColumn(column).getWidth();
		    if (column >= cMin && column <= cMax) {
			    //g.setClip (x - 1 + (w / 2), visibleRect.y - insets.top, 
			    //		w - 4, visibleRect.height + insets.top + insets.bottom);
			    graphics.drawLine (x - 1 + (colWidth / 2), visibleRect.y, x - 1 + (colWidth / 2), (int)visibleRect.getMaxY() - 1);
			    paintLabels (graphics, column, x - 1 + (colWidth / 2));
		    }
		    x += colWidth;
		}
	    
	    //g.setClip (clipRect);
    }
    
    
    
    private void paintLabels (final Graphics graphics, final int column, final int x) {

    	final SortedTableColumn<?> stc = (SortedTableColumn<?>)table.getColumnModel().getColumn (column);
    	final int colWidth = stc.getWidth();
    	int nextColWidth = 8;
    	
    	if (column < table.getColumnModel().getColumnCount() - 1) {
    		final TableColumn tcNext = table.getColumnModel().getColumn (column + 1);
    		nextColWidth = tcNext.getWidth();
    	}

    	final Rectangle clipRect = graphics.getClipBounds();
    	graphics.setClip (x, visibleRectNoInsets.y,
    			((colWidth + nextColWidth) / 2) - 4,
    			visibleRectNoInsets.height);
    	
    	final int disSize = stc.getDiscreteRange();
    	
    	int approxSep = -1; 
    	// if numerical scale, calculate appropriate label separation
    	if (disSize == 0 && table instanceof JParCoord) {
    		approxSep = ((JParCoord)table).getSuggestedLabelSeparation();
    		double labelCount = (double)visibleRect.height / approxSep;
    		int labelCountI = (int)Math.round (labelCount);
    		approxSep = visibleRect.height / Math.max(1, labelCountI);
    	}

    	final double jump = (disSize == 0 ? 
    			Math.max (table.getRowHeight(), approxSep) : 
    			Math.max (table.getRowHeight(), (double)visibleRect.height / (double)Math.max (1, disSize - 1))
    	);
    	final int xx = x;

    	Object lastObj = null;
    	for (double y = visibleRect.y; y <= visibleRect.getMaxY(); y += jump) {
    		final Object obj = getValue ((int)Math.round(y), column, visibleRect);
    		if (obj != null && obj != lastObj) {
    			final String labelString = toString2 (obj, stc, column);
	    		graphics.drawString (labelString, xx + 1, (int)Math.round(y) + 6);
	    		lastObj = obj;
    		}
    	}
    	
    	graphics.setClip (clipRect);
    }
    
    
    String toString2 (final Object val, final SortedTableColumn<?> stc, final int viewColIndex) {
    	String labelString = null;
    	
    	if (stc != null) {
    		final Class<?> columnType = stc.getColumnClass();
	    	
	    	if (columnType == Date.class || columnType == Double.class) {
	    		final TableCellRenderer tcr = stc.getCellRenderer();
    			if (tcr instanceof DefaultTableCellRenderer) {
    				final DefaultTableCellRenderer dtcr = (DefaultTableCellRenderer)tcr;
    				tcr.getTableCellRendererComponent (table, val, false, false, 0, viewColIndex);
        			labelString = dtcr.getText();
    			}
	    	}
    	}
    	
    	if (labelString == null && val != null) {
    		labelString = val.toString();
    	}
    	return labelString;
    }
    
	String getLabelAt (final double y, final int column) {
		final Object val = getValue (y, column, visibleRect);
		final SortedTableColumn<?> stc = (SortedTableColumn<?>)table.getColumnModel().getColumn (column);
    	return toString2 (val, stc, column);
	}
    
	String getLabelAt (final Point point) {
		final int column = table.columnAtPoint (point);
    	return getLabelAt (point.y, column);
	}
    
    
    void getActiveBounds () {
    	table.computeVisibleRect (visibleRect);
    	visibleRectNoInsets.setBounds (visibleRect);
    	table.getInsets (insets);
    	GraphicsUtil.adjustRectangle (visibleRect, insets);
    }
    
    
    /*
     * Paints the grid lines within <I>aRect</I>, using the grid
     * color set with <I>setGridColor</I>. Paints vertical lines
     * if <code>getShowVerticalLines()</code> returns true and paints
     * horizontal lines if <code>getShowHorizontalLines()</code>
     * returns true.
     */
    private void paintGrid (final Graphics graphics, final int rMin, final int rMax, final int cMin, final int cMax) {
        graphics.setColor (ColorUtilities.mixColours (table.getGridColor(), table.getBackground(), 0.25f));

        final Rectangle minCell = table.getCellRect (rMin, cMin, true);
        final Rectangle maxCell = table.getCellRect (rMax, cMax, true);
        final Rectangle damagedArea = minCell.union (maxCell);
        LOGGER.info ("darea: "+damagedArea);
        
        if (table.getShowVerticalLines()) {
        	final TableColumnModel columnModel = table.getColumnModel();
		    if (columnModel.getColumnCount () > 0) {
			    int x;
			    if (table.getComponentOrientation().isLeftToRight()) {
					x = damagedArea.x;
					for (int column = cMin; column <= cMax; column++) {
						final int colWidth = columnModel.getColumn(column).getWidth();
					    x += colWidth;
					    graphics.drawLine(x - 1, visibleRect.y, x - 1, (int)visibleRect.getMaxY());
					}
			    } else {
					x = damagedArea.x;
					for (int column = cMax; column >= cMin; column--) {
					    final int colWidth = columnModel.getColumn(column).getWidth();
					    x += colWidth;
					    graphics.drawLine(x - 1, visibleRect.y, x - 1, (int)visibleRect.getMaxY());
					}
			    }
		    }
		}
    }

    private static int getAdjustedLead (final JTable table,
    		final boolean row,
    		final ListSelectionModel model) {

    	final int index = model.getLeadSelectionIndex();
    	final int compare = row ? table.getRowCount() : table.getColumnCount();
        return index < compare ? index : -1;
    }

    private static int getAdjustedLead (final JTable table, final boolean row) {
        return row ? getAdjustedLead (table, row, table.getSelectionModel())
                   : getAdjustedLead (table, row, table.getColumnModel().getSelectionModel());
    }


    
    void makeColourCache () {
    	drawColours [BACKGROUND] = table.getForeground();
    	drawColours [SELECTED] = table.getSelectionForeground();
    	drawColours [UBER_SELECTED] = ((JParCoord)table).getUberSelectedColour();
    	drawColours [BRUSHED] = ((JParCoord)table).getBrushForegroundColour();
    	drawColours [BRUSHED_SELECTED] = ((JParCoord)table).getBrushSelectionColour();
    	
    	for (int n = 0; n < END; n++) {
    		drawColours [n + END] = ColorUtilities.mixColours (drawColours [n], table.getBackground(), 0.5f);
    	}
    }
    
    
    
    /**
     * Class used to draw to offscreen buffers outside of the swing thread
     * @author cs22
     *
     */
    class DrawRunnable implements Runnable {

    	Graphics graphics;
    	int cMin, cMax;
    	BitSet drawStates;
    	Rectangle clipBounds;
    	boolean active, restart; // restart used to interrupt paintParCoords() to restart drawing afresh if part way through
    	boolean isSVGGraphics, nonScreenOutput;
    	
    	public void setup (final Graphics graphics, int cMin, int cMax, final BitSet drawStates) {
    		synchronized (this) {
	    		this.graphics = graphics;
	    		this.clipBounds = new Rectangle (graphics.getClipBounds());
	    		this.isSVGGraphics = GraphicsUtil.isSVGGraphics (graphics);
	    		this.nonScreenOutput = GraphicsUtil.isNonScreenImage ((Graphics2D)graphics) || isSVGGraphics;
	    		this.cMin = cMin;
	    		this.cMax = cMax;
	    		this.drawStates = (BitSet)drawStates.clone();
	    		drawStates.clear ();
	    		
	    		setRestart (isActive());
    		}
    	}
		@Override
		public void run() {
			paintParCoords ();
		}
		
		protected void paintParCoords () {

			do {
				restart = false;
				active = true;
				final long nanoTime = System.nanoTime();
		        translateDrawingAreas (drawingAreas, visibleRectNoInsets, false);
		        
				final Stroke stroke = ((JParCoord)table).getStroke();
				final Stroke selectedStroke = ((JParCoord)table).getSelectedStroke();
				
				final int COORDS = table.getColumnCount();
				final int size = table.getRowCount ();
				
				boolean brushed, selected, uberSelected;
	
				final RowCoordData rowCoordData = new RowCoordData (new Object[COORDS], new int [COORDS], new int [COORDS], new BitSet());
				int widthTotal = 0;
				final TableColumnModel tcm = table.getColumnModel();
				int[] xcoords = rowCoordData.getXCoords();
				for (int n = 0; n < COORDS; n++) {
					final int inc = tcm.getColumn(n).getWidth();
					xcoords [n] = widthTotal + (inc >> 1);
					widthTotal += inc;
				}
				
				final TableModel tModel = table.getModel();
				final ListSelectionModel selectModel = table.getSelectionModel();
				final ListSelectionModel brushModel = ((JParCoord)table).getBrushModel();
				final ListSelectionModel uberSelectionModel = ((JParCoord)table).getUberSelection();
				final boolean emptyBrush = brushModel.isSelectionEmpty();
				final boolean emptySelect = selectModel.isSelectionEmpty();
				final boolean emptyUberSelect = !ParCoordUI.this.uberSelectionExists();
	
				final boolean fullRefresh = visibleRectNoInsets.equals (clipBounds);
				
				final BitSet displayLayers = new BitSet();		
				displayLayers.set (BACKGROUND, ((JParCoord)table).isDrawUnselectedItems());
				displayLayers.set (SELECTED, !emptySelect);
				displayLayers.set (UBER_SELECTED, !emptyUberSelect);
				displayLayers.set (BRUSHED_SELECTED, !emptyBrush);
				displayLayers.set (BRUSHED, !emptyBrush);
				displayLayers.and (drawStates);
				
				final BitSet rowPresentLayers = new BitSet ();
				
				final Rectangle compZeroedBounds = new Rectangle (0, 0, table.getWidth(), table.getHeight());
				final int[] primeImagePrompts = {SELECTED, UBER_SELECTED, BRUSHED};
				
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug ("drawStates: "+drawStates);
					LOGGER.debug ("displaylayers: "+displayLayers);
				}
				
				try {
					for (int cycle = BRUSHED; cycle >= 0 && !restart; cycle--) {
						
						for (int pCounter = 0; pCounter < primeImagePrompts.length; pCounter++) {
							final int promptStage = primeImagePrompts [pCounter];
							if (cycle == promptStage && displayLayers.get (promptStage)) {
								GraphicsUtil.primeImageGraphics (graphics, fullRefresh ? compZeroedBounds : graphics.getClipBounds(), img, 
										table.getBackground(), SELECTION_LAYER_TABLE [promptStage]);
							}
						}
		
						// Righty-ho, cycle2...
						// basically for drawing to the screen we draw the stuff we want to see first
						// (ie selected, brushed) so we can display it quicker and then fill in the 
						// background lines on a separate image.
						// However for outputting to svg, it's all to one graphics context,
						// and as we want these lines to be on top, we have to draw
						// the background first and draw selected/brushed lines last.
						// Flipping the cycle2 variable dependent on svgOutput does this
						final int cycle2 = isSVGGraphics ? (END - 1) - cycle : cycle;
						final Graphics2D gDrawable = (Graphics2D) drawingAreas [cycle2];
		
						if (!isSVGGraphics) {
							gDrawable.setRenderingHint (RenderingHints.KEY_ANTIALIASING, antiAlias | nonScreenOutput ? 
								RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
						}
		
						if (displayLayers.get (cycle2) && gDrawable != null) {
							final Color drawColour = drawColours [cycle2];
							//final Color drawColour2 = ColorUtilities.addAlpha (drawColour, 32);
							//Color drawColour2 = ColorUtilities.mixColours (drawColour, table.getBackground(), 0.1f);
							final Stroke sOld = gDrawable.getStroke();
							gDrawable.setColor (drawColour);		
							//final Map <Integer, Map <Integer, Paint>> gradients = new HashMap <Integer, Map <Integer, Paint>> ();
							
							for (int viewRow = 0; viewRow < size && !restart; viewRow++) {
								synchronized (this) {
									if (!restart) {
										selected = selectModel.isSelectedIndex (viewRow); 
										brushed = brushModel.isSelectedIndex (viewRow);// && drawStates.get(2);
										final int modeln = table.convertRowIndexToModel (viewRow);
										uberSelected = emptyUberSelect ? false : uberSelectionModel.isSelectedIndex (modeln);
				
										rowPresentLayers.clear();
										rowPresentLayers.set (BACKGROUND);
										rowPresentLayers.set (UBER_SELECTED, uberSelected);
										rowPresentLayers.set (SELECTED, selected);
										rowPresentLayers.set (selected ? BRUSHED_SELECTED : BRUSHED, brushed);
										
										if (rowPresentLayers.get (cycle2)) {				
											// This bit collects data for the row, puts it in an index and also sets up an
											// index that skips columns with null values in.
											calcRowCoordData (rowCoordData, tModel, modeln, cMin, cMax);
											gDrawable.setStroke (selected || uberSelected ? selectedStroke : stroke);
											
											if (rowCoordData.getActiveColumns() >= 0) {
												((JParCoord)table).getDefaultRenderer().renderRow ((JParCoord)table, gDrawable, rowCoordData, cMin, cMax);
											}
										}
									}
								}
							}
							gDrawable.setStroke (sOld);
						}
					}
				}	
				catch (Exception ex) {
					restart = true;
				}
			} while (restart);
			
			LOGGER.debug ("Finished painting polylines");
			active = false;
	        final Rectangle clip = new Rectangle (clipBounds);
	        ParCoordUI.this.table.repaint (clip.x, clip.y, clip.width, clip.height);
		}
		
		
		public void setRestart (final boolean state) {
			restart = state;
		}
		
		public boolean getRestart () {
			return restart;
		}
		
		public boolean isActive () {
			return active;
		}
    }
    
    
    
    class MyComponentListener extends ComponentAdapter {
    	@Override
    	public void componentResized (final ComponentEvent cEvent) {
    		LOGGER.debug ("height: "+cEvent.getComponent().getHeight());
    		clearDrawParameters ();
    		((JParCoord)table).setRedrawAll (true);
    		getActiveBounds ();
    	}
    }
    
    
    /*
     * Class to stop costly anti-alias rendering if user input is
     * occurring somewhere
     */
    class CancelAntiAliasListener implements AWTEventListener {
		@Override
		public void eventDispatched (final AWTEvent event) {
			antiAlias = (table.getRowCount() * table.getColumnCount()) < 2000;
			if (antiAliasTimer.isRunning()) {
				antiAliasTimer.restart ();
			}
		}
    }
    
    /**
     * 
     * @author cs22
     * Timer class that redraws with antialiasing on
     * Generally waits a few seconds since last full non-antialiased redraw
     * Antialiasing can be cancelled by above class
     */
    class AntiAliasActionListener implements ActionListener {	
		@Override
		public void actionPerformed (final ActionEvent aEvent) {
			if (!antiAlias) {
				//antiAlias = true;
				//brushingCancelled = false;
				//((JParCoord)table).setRedrawAll (true);
				//((JParCoord)table).repaint ();
			}
		}	
    }
    
    
	class BrushListener implements ActionListener {
		@Override
		public void actionPerformed (final ActionEvent aEvent) {
			final Timer brushTimer = (Timer) aEvent.getSource ();
			final boolean oldBrushing = brushing;
			brushing = (brushTimer == brushOn ? true : (brushTimer == brushOff ? false : brushing));
			
			if (oldBrushing != brushing) {		
				if (!brushing) {
					clearBrushing ();
				}
				else {
					final Point point = MouseInfo.getPointerInfo().getLocation();
					SwingUtilities.convertPointFromScreen (point, table);
					doBrushing (point);
				}
			}
		}	
	};
	
	
	
	// Little convenience class that does the same job as Dimension, but renames the fields
	// to be more appropriate
	static protected class YRange {
		public int top, bottom;
		
		public YRange () {
			this (0, 0);
		}
		
		public YRange (final int top, final int bottom) {
			this.top = top;
			this.bottom = bottom;
		}
	}
    
	
	
	private static final TransferHandler DEFAULT_TRANSFER_HANDLER = new TableTransferHandler(); 
    
    static class TableTransferHandler extends TransferHandler implements UIResource {

	/**
		 * 
		 */
		private static final long serialVersionUID = -8707594408931825740L;

	/**
	 * Create a Transferable to use as the source for a data transfer.
	 *
	 * @param comp  The component holding the data to be transfered.  This
	 *  argument is provided to enable sharing of TransferHandlers by
	 *  multiple components.
	 * @return  The representation of the data to be transfered.
	 *
	 */
        @Override
		protected Transferable createTransferable (final JComponent comp) {
        	if (comp instanceof JTable) {
	    	final JTable table = (JTable) comp;
	    	int[] rows;
	    	int[] cols;
		
			if (!table.getRowSelectionAllowed() && !table.getColumnSelectionAllowed()) {
			    return null;
			}
			
                if (!table.getRowSelectionAllowed()) {
                	final int rowCount = table.getRowCount();

                    rows = new int[rowCount];
                    for (int counter = 0; counter < rowCount; counter++) {
                        rows[counter] = counter;
                    }
                } else {
		    rows = table.getSelectedRows();
            }
		
                if (!table.getColumnSelectionAllowed()) {
                	final int colCount = table.getColumnCount();

                    cols = new int[colCount];
                    for (int counter = 0; counter < colCount; counter++) {
                        cols[counter] = counter;
                    }
                } else {
		    cols = table.getSelectedColumns();
		}
                
		if (rows == null || cols == null || rows.length == 0 || cols.length == 0) {
		    return null;
		}
                
		final StringBuffer plainBuf = new StringBuffer ();
		final StringBuffer htmlBuf = new StringBuffer (100);
                
                htmlBuf.append("<html>\n<body>\n<table>\n");
                
                for (int row = 0; row < rows.length; row++) {
                    htmlBuf.append("<tr>\n");
                    for (int col = 0; col < cols.length; col++) {
                    	final Object obj = table.getValueAt (rows[row], cols[col]);
                    	final String val = ((obj == null) ? "" : obj.toString());
                        plainBuf.append(val).append('\t');
                        htmlBuf.append("  <td>" + val + "</td>\n");
                    }
                    // we want a newline at the end of each line and not a tab
                    plainBuf.deleteCharAt(plainBuf.length() - 1).append("\n");
                    htmlBuf.append("</tr>\n");
                }

                // remove the last newline
                plainBuf.deleteCharAt(plainBuf.length() - 1);
                htmlBuf.append("</table>\n</body>\n</html>");
                
                return new BasicTransferable(plainBuf.toString(), htmlBuf.toString());
	    }

	    return null;
	}

    @Override
	public int getSourceActions (final JComponent comp) {
    	return COPY;
    }

    }
}  // End of Class BasicTableUI
