package util.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;

public class SingleItemSet<E> implements Set<E> {
	
	private static final Logger LOGGER = Logger.getLogger (SingleItemSet.class);
	
	
	E item;	
	SingleItemIterator<E> reusableIterator = new SingleItemIterator<E> ();
	
	@Override
	public boolean add (final E elem) {
		if (this.size() == 0) {
			item = elem;
			return true;
		}
		return false;
	}

	@Override
	public boolean addAll (final Collection<? extends E> coll) {
		if (coll.size() + size() > 1) {
			return false;
		}
		if (!coll.isEmpty()) {
			final Iterator<? extends E> iter = coll.iterator();
			final E obj = iter.next();
			add (obj);
		}
		return true;
	}

	@Override
	public void clear() {
		item = null;	
	}

	@Override
	public boolean contains (final Object obj) {
		return item == obj && obj != null;
	}

	@Override
	public boolean containsAll (final Collection<?> coll) {
		if (isEmpty() || coll.size() != 1) {
			return false;
		}
		final Iterator<?> iter = coll.iterator();
		return contains (iter.next());
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public Iterator<E> iterator() {
		return new SingleItemIterator<E> ();
		
		//Below doesn't work with java for-each syntax, iterator() gets called constantly, resetting the index each time
		// resulting in an infinite loop...
		/*
		iterator.index = 0;
		System.err.println ("iterator retreived!");
		return iterator;
		*/
		
	}

	@Override
	public boolean remove (final Object obj) {
		if (item == obj && obj != null) {
			item = null;
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll (final Collection<?> coll) {
		if (isEmpty() || coll.size() != 1) {
			return false;
		}
		final Iterator<?> iter = coll.iterator();
		return remove (iter.next());
	}

	@Override
	public boolean retainAll (final Collection<?> coll) {
		// No effect
		if (isEmpty() || coll.isEmpty()) {
			return false;
		}
		final Iterator<?> iter = coll.iterator();
		while (iter.hasNext()) {
			remove (iter.next());
		}
		return (item == null);
	}

	@Override
	public int size() {
		return item == null ? 0 : 1;
	}
	
	private void fillArray (Object[] array) {
		final Iterator<E> iter = iterator();
		int index = 0;
		while (iter.hasNext()) {
			array[index] = iter.next();
			index++;
		}
		if (array.length > index) {
			array[index] = null;
		}
	}

	@Override
	public Object[] toArray() {
		final Object[] array = new Object [1];
		fillArray (array);
		return array;
	}

	@Override
	public <T> T[] toArray(T[] array) {
		if (array.length < size()) {
			array = (T[]) new Object [size()];
		}
		fillArray (array);
		return array;
	}
	
	
	class SingleItemIterator<E> implements Iterator<E> {

		int index = 0;
		
		@Override
		public boolean hasNext() {
			return index < size();
		}

		@Override
		public E next() {
		    if (index == 0 && !isEmpty()) {
		    	index++;
				return (E)item;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			SingleItemSet.this.clear();
			index = 0;
		}
	}
	
	public static void main (final String args[]) {
		final Collection<String> sisTest = new SingleItemSet<String> ();
		sisTest.add ("Hello World");
		
		for (String str : sisTest) {
			LOGGER.info (str);
		}
		
		
		sisTest.clear ();
		sisTest.add ("Hello World Again");
		
		for (String str : sisTest) {
			LOGGER.info (str);
		}
		
		LOGGER.info ("-- fin --");
	}
}
