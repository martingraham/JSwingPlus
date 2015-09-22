package util.collections;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;


public class PropertyChangeArrayList<E> extends ArrayList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -16834039148592997L;

	
	PropertyChangeSupport pcs = new PropertyChangeSupport (this);
	
    public PropertyChangeArrayList (final int initialCapacity) {
    	super (initialCapacity);
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public PropertyChangeArrayList() {
	 	super ();
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public PropertyChangeArrayList (final Collection<? extends E> coll) {
		super (coll);
    }


    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
	public E set (final int index, final E element) {
		final E returnElement = super.set (index, element);
		pcs.firePropertyChange ("listSet", returnElement, element);
		return returnElement;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param element element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    @Override
	public boolean add (final E element) {
		final boolean successAdd = super.add (element);
		pcs.firePropertyChange ("listAdd", null, element);
		return successAdd;
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
	public void add (final int index, final E element) {
		super.add (index, element);
		pcs.firePropertyChange ("listAdd", null, element);
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
	public E remove (final int index) {
		final E returnElement = super.remove (index);
		pcs.firePropertyChange ("listRemove", returnElement, null);
		return returnElement;
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If the list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * <tt>insets</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(insets)==null&nbsp;:&nbsp;o.equals(get(insets)))</tt>
     * (if such an element exists).  Returns <tt>true</tt> if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * @param obj element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     */
    @Override
	public boolean remove (final Object obj) {
		final boolean successRemove = super.remove (obj);
		if (successRemove) {
			pcs.firePropertyChange ("listRemove", successRemove ? obj : null, null);
		}
		return successRemove;
    }


    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    @Override
	public void clear() {
		super.clear ();
		pcs.firePropertyChange ("listClear", null, null);
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the
     * specified collection's Iterator.  The behaviour of this operation is
     * undefined if the specified collection is modified while the operation
     * is in progress.  (This implies that the behaviour of this call is
     * undefined if the specified collection is this list, and this
     * list is nonempty.)
     *
     * @param coll collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    @Override
	public boolean addAll (final Collection<? extends E> coll) {
		final boolean successAddAll = super.addAll (coll);
		if (successAddAll) {
			pcs.firePropertyChange ("listAdd", null, coll);
		}
		return successAddAll;
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param coll collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */
    @Override
	public boolean addAll (final int index, final Collection<? extends E> coll) {
		final boolean successAddAll = super.addAll (index, coll);
		if (successAddAll) {
			pcs.firePropertyChange ("listAdd", null, coll);
		}
		return successAddAll;
    }
    
    
    /**
     * Add a PropertyChangeListener to the listener list.
     * The listener is registered for all properties.
     * The same listener object may be added more than once, and will be called
     * as many times as it is added.
     * If <code>listener</code> is null, no exception is thrown and no action
     * is taken.
     *
     * @param listener  The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener (final PropertyChangeListener listener) {
		pcs.addPropertyChangeListener (listener);
    }

    /**
     * Remove a PropertyChangeListener from the listener list.
     * This removes a PropertyChangeListener that was registered
     * for all properties.
     * If <code>listener</code> was added more than once to the same event
     * source, it will be notified one less time after being removed.
     * If <code>listener</code> inputStream null, or was never added, no exception is
     * thrown and no action is taken.
     *
     * @param listener  The PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener (final PropertyChangeListener listener) {
		pcs.removePropertyChangeListener (listener);
    }
}
