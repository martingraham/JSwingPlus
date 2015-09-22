package util.swing;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputAdapter;

import model.shared.ArrayListModel;

// Adapted from noah.w & Todd Corley in java.sun.com forums
// http://forum.java.sun.com/thread.jspa?forumID=57&threadID=260100
// http://forum.java.sun.com/thread.jspa?forumID=57&threadID=527239

public class DragDropList2 extends JList {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6117460588076333219L;
	private int from, initial;
    //private final Border dragBorder = BorderFactory.createMatteBorder (1, 0, 1, 0, Color.black);

	
	public DragDropList2 () {
		this (Collections.EMPTY_LIST); // To stop null errors in the model
	}
	
    public DragDropList2 (final List aList) {
    	
    	super ();
		setModel (aList);

		//setFixedCellWidth (120);
		setFixedCellHeight (13);
		//setBorder (BorderFactory.createMatteBorder (1, 1, 1, 1, Color.decode(Messages.getString (GraphicsUtil.GRAPHICPROPS, "myBorderPanel.borderColour")).brighter()));
		setBorder (BorderFactory.createMatteBorder (1, 1, 1, 1, getBackground().darker()));
		setSelectionMode (ListSelectionModel.SINGLE_SELECTION);

        final DragDropListInputAdapter mouseInput = new DragDropListInputAdapter ();
        addMouseListener (mouseInput);
        addMouseMotionListener (mouseInput);
        setCellRenderer (new OrderCellRenderer ());
    }

 
    public final void setModel (final List aList) {
    	setModel (new ArrayListModel (aList));
    }

    
    /**
     * Constructs a read-only <code>ListModel</code> from an array of objects,
     * and calls {@code setModel} with this model.
     * 
     * Attempts to pass a {@code null} value to this method results in
     * undefined behavior and, most likely, exceptions. The created model
     * references the given array directly. Attempts to modify the array
     * after invoking this method results in undefined behavior.
     *
     * @param listData an array of {@code Objects} containing the items to
     *        display in the list
     * @see #setModel
     */
    public void setListData (final Object[] listData) {
        setModel (new ArrayListModel (new ArrayList<Object> (Arrays.asList(listData))));
    }


    /**
     * Constructs a read-only <code>ListModel</code> from a <code>Vector</code>
     * and calls {@code setModel} with this model.
     * 
     * Attempts to pass a {@code null} value to this method results in
     * undefined behavior and, most likely, exceptions. The created model
     * references the given {@code Vector} directly. Attempts to modify the
     * {@code Vector} after invoking this method results in undefined behavior.
     *
     * @param listData a <code>Vector</code> containing the items to
     *						display in the list
     * @see #setModel
     */
    @Override
    public void setListData (final Vector listData) {
        setModel (new ArrayListModel (new ArrayList<Object> (listData)));
    }

    class DragDropListInputAdapter extends MouseInputAdapter {

        @Override
		public void mousePressed (final MouseEvent event) {
			setValueIsAdjusting (true);
            from = getSelectedIndex();
			initial = from;
		}

		@Override
		public void mouseReleased (final MouseEvent event) {
		    if (initial != from) {
                setValueIsAdjusting (false);
                fireSelectionValueChanged (getModel().getSize(), getModel().getSize(), false);
            }
		}

        @Override
		public void mouseDragged (final MouseEvent event) {
			final int to = getSelectedIndex();
			if (to == from) {
				return;
			}

			if (getModel() instanceof ArrayListModel) {
				final ArrayListModel alm = (ArrayListModel) getModel();
				alm.swap (to, from);
				from = to;
			}
	    }
    }

    
    static class OrderCellRenderer extends DefaultListCellRenderer {

        /**
		 * 
		 */
		private static final long serialVersionUID = -1624873329794231872L;

		@Override
		public Component getListCellRendererComponent (final JList list,
		        final Object value, // value to display
		        final int index,    // cell index
		        final boolean iss,  // inputStream selected
		        final boolean chf)  // cell has focus?
	    {
	         super.getListCellRendererComponent (list, value, index, iss, chf);
	         setText (Integer.toString(index+1)+" "+getText());
	         return this;
	    }
	}
}