package org.ubimix.commons.search;

import java.util.Collection;

/**
 * This interface provides access to search functionalities.
 * 
 * @author kotelnikov
 */
public interface IDocumentSearcher {

    /**
     * Individual search result. Gives access to the indexed document and to the
     * highlighted text.
     * 
     * @author kotelnikov
     */
    public interface ISearchResult {

        /**
         * Returns the indexed document. It contains only stored field.
         * 
         * @return the indexed document
         */
        IDocument getDocument();

        /**
         * Returns the highlighted snippet of the document.
         * 
         * @return the highlighted snippet of the document
         * @throws SearchException
         */
        String getHighlight() throws SearchException;

        /**
         * @return the score of this search result
         */
        float getScore();
    }

    /**
     * Objects of this type should be provided by the client to accumulate
     * search results.
     * 
     * @author kotelnikov
     */
    public interface ISearchResultCollector {

        /**
         * @return the maximal number of
         */
        int getMaxResultNumber();

        /**
         * @param result
         * @throws SearchException
         */
        void onSearchResult(ISearchResult result) throws SearchException;
    }

    /**
     * @throws SearchException
     */
    void close() throws SearchException;

    /**
     * @param q
     * @param fields
     * @param collector
     * @throws SearchException
     */
    void search(
        String q,
        Collection<String> fields,
        ISearchResultCollector collector) throws SearchException;

    /**
     * @param q
     * @param collector
     * @throws SearchException
     */
    void search(String q, ISearchResultCollector collector)
        throws SearchException;

}