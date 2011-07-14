/**
 * 
 */
package org.webreformatter.search;

import java.util.Iterator;

/**
 * @author kotelnikov
 *
 */

/**
 * Shift iterator returns objects until the returned objects are not
 * <code>null</code>.
 * 
 * @author kotelnikov
 * @param <T> the type of objects
 */
public abstract class ShiftIterator<T> implements Iterator<T> {

    protected T fObject;

    private boolean fReady;

    /**
     * Constructor for ShiftIterator.
     */
    public ShiftIterator() {
        this(null);
    }

    /**
     * Constructor for ShiftIterator.
     * 
     * @param firstObject the first object returned by this iterator item source
     */
    public ShiftIterator(T firstObject) {
        reset(firstObject);
    }

    /**
     * @return the current object of this iterator
     */
    public T getObject() {
        return fObject;
    }

    /**
     * @return <code>true</code> if there is at least one object to return.
     */
    public boolean hasNext() {
        if (!fReady) {
            fObject = shiftItem();
        }
        fReady = true;
        return (fObject != null);
    }

    /**
     * Returns the next object.
     * 
     * @return the next object.
     */
    public T next() {
        if (!fReady) {
            fObject = shiftItem();
        }
        fReady = false;
        return fObject;
    }

    /**
     * @throws UnsupportedOperationException - this is an unallowed operation.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Method reset.
     */
    public void reset() {
        reset(null);
    }

    /**
     * Method reset.
     * 
     * @param object
     */
    public void reset(T object) {
        fObject = object;
        fReady = (fObject != null);
    }

    protected abstract T shiftItem();

}
