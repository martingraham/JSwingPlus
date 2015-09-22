package model.shared;

import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

public class ArrayListModel extends AbstractListModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1764480350440632426L;
	private final List<Object> list;

    public ArrayListModel (final List<Object> list) {
    	super ();
        this.list = (list == null ? Collections.EMPTY_LIST : list);
    }

    public int getSize() { return list.size(); }

    public Object getElementAt (final int index) { return list.get(index); }

    public Object get (final int index) { return getElementAt (index); }

    public void add (final Object obj ) {
        list.add (obj);
        fireContentsChanged (this, this.getSize(), this.getSize());
    }

    public void remove (final int index ) {
        if (!list.isEmpty()) {
            list.remove(index);
            fireContentsChanged (this, 0, 0);
        }
    }

    public void set (final int index, final Object value) {
        list.set (index, value);
        fireContentsChanged (this, index, index);
    }
    
    
    public void clear() {
    	final int index1 = list.size() - 1;
    	list.clear ();
    	if (index1 >= 0) {
    	    fireIntervalRemoved (this, 0, index1);
    	}
    }

    
    public void swap (final int to, final int from) {
    	Collections.swap (list, to, from);
    }
    
    public List<Object> getList () { return list; }
}
