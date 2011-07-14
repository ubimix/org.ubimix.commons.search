/**
 * 
 */
package org.webreformatter.search;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author kotelnikov
 */
public class DocumentProvider implements IDocumentProvider {

    private Iterable<IDocument> fDocuments;

    /**
     * 
     */
    public DocumentProvider(IDocument... documents) {
        this(Arrays.asList(documents));
    }

    public DocumentProvider(Iterable<IDocument> documents) {
        fDocuments = documents;
    }

    /**
     * @see org.webreformatter.search.IDocumentProvider#closeIterator(java.util.Iterator)
     */
    public void closeIterator(Iterator<IDocument> iterator) {
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<IDocument> iterator() {
        return fDocuments.iterator();
    }

}
