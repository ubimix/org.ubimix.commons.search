/**
 * 
 */
package org.ubimix.commons.search;

import java.util.Iterator;

/**
 * Instances of this type are used as a source of documents to index.
 * 
 * @author kotelnikov
 */
public interface IDocumentProvider extends Iterable<IDocument> {
    /**
     * Closes the document iterator.
     * 
     * @param iterator the iterator to close.
     */
    void closeIterator(Iterator<IDocument> iterator);
}
