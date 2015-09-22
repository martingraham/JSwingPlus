package util.collections;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CircularQueue<E> extends AbstractQueue<E> {

    int head = 0;
    int length = 0;
    private final List<E> objects;

    public CircularQueue () {
        this (10);
    }

    public CircularQueue (final int size) {
    	super ();
        objects = new ArrayList<E> (size);
        for (int n = 0; n < size; n++) {
        	objects.add (null);
        }
    }

    
    @Override
    public E poll () {
        return (length > 0) ? removeAt (0) : null;
    }

    
    @Override
    public E peek () {
        return (length > 0) ? objects.get (convertToList (0)) : null;
    }

    
    @Override
    public boolean offer (final E element) {
        if (length < objects.size()) {
            objects.set (convertToList (length), element);
            length++;
        }

        return (length <= objects.size());
    }

    @Override
	public boolean isEmpty () {
        return (length == 0);
    }

    @Override
	public int size() { return length; }

    
    
    
    private E removeAt (final int index) {
        if (index <= length) {
           final E element = objects.get (convertToList (index));
           head = (head + 1) % objects.size();
           length--;
           return element;
        }
      
        return null;
    }
    
    
    private int convertToList (final int index) {
        return (index + head) % objects.size();
    }


    /**
     * Returns an iterator over the elements in this queue in proper sequence.
     *
     * @return an iterator over the elements in this queue in proper sequence.
     */
    @Override
	public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * Iterator for ArrayBlockingQueue
     */
    private class Itr implements Iterator<E> {

        private int nextIndex;

        Itr() {
            nextIndex = length;
        }

        public boolean hasNext() {
            return nextIndex > 0;
        }

        public E next() {
            final int index = convertToList (--nextIndex);
            return objects.get (index);
        }

        public void remove() {
            removeAt (nextIndex - 1);
        }
    }
}