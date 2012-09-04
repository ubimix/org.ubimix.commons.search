/**
 * 
 */
package org.ubimix.commons.search.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.ubimix.commons.search.AbstractDocument;
import org.ubimix.commons.search.IDocument;
import org.ubimix.commons.search.IDocumentSearcher;
import org.ubimix.commons.search.IFieldConst;
import org.ubimix.commons.search.SearchException;

/**
 * @author kotelnikov
 */
public class DocumentSearcher implements IDocumentSearcher {

    protected static class InternalSearchResults implements ISearchResult {

        private Analyzer fAnalyzer;

        private final Document fDoc;

        private IDocument fDocument;

        private final Highlighter fHighlighter;

        private float fScore;

        private final Collection<String> fSearchFields;

        protected InternalSearchResults(
            Document doc,
            Analyzer analyzer,
            Highlighter highlighter,
            Collection<String> searchFields,
            float score) {
            fDoc = doc;
            fAnalyzer = analyzer;
            fHighlighter = highlighter;
            fSearchFields = searchFields;
            fScore = score;
        }

        public IDocument getDocument() {
            if (fDocument == null) {
                fDocument = new AbstractDocument() {
                    public Set<String> getFields() throws SearchException {
                        List<Fieldable> fields = fDoc.getFields();
                        Set<String> set = new LinkedHashSet<String>();
                        for (Fieldable field : fields) {
                            String fieldName = field.name();
                            set.add(fieldName);
                        }
                        set.remove(IFieldConst.FULL_CONTENT);
                        return set;
                    }

                    public String getValue(String fieldName)
                        throws SearchException {
                        String value = fDoc.get(fieldName);
                        return value;
                    }
                };
            }
            return fDocument;
        }

        public String getHighlight() throws SearchException {
            try {
                IDocument document = getDocument();
                StringBuilder buf = new StringBuilder();
                for (String field : fSearchFields) {
                    String value = document.getValue(field);
                    TokenStream tokenStream = fAnalyzer.tokenStream(
                        field,
                        new StringReader(value));
                    String formattedValue = fHighlighter.getBestFragments(
                        tokenStream,
                        value,
                        3,
                        "...");
                    if (formattedValue.length() > 0) {
                        buf.append(" ... ");
                        buf.append(formattedValue);
                    }
                }
                if (buf.length() > 0) {
                    buf.append(" ... ");
                }
                return buf.toString();
            } catch (Throwable t) {
                throw handleError(
                    "Can not return highlights for search results.",
                    t);
            }
        }

        public float getScore() {
            return fScore;
        }
    }

    final static Logger log = Logger
        .getLogger(DocumentSearcher.class.getName());

    private static SearchException handleError(String message, Throwable e) {
        log.log(Level.WARNING, message, e);
        if (e instanceof SearchException) {
            return (SearchException) e;
        }
        return new SearchException(message, e);
    }

    private Analyzer fAnalyzer;

    private Directory fDir;

    private IndexReader fReader;

    public DocumentSearcher(Directory dir, Analyzer analyzer)
        throws SearchException {
        fAnalyzer = analyzer;
        fDir = dir;
    }

    /**
     * @see org.ubimix.commons.search.IDocumentSearcher#close()
     */
    public void close() throws SearchException {
        try {
            IndexReader reader = getReader(false);
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            throw handleError("Can not close the Lucene reader", e);
        }
    }

    private Query getQuery(
        Collection<String> fields,
        Analyzer analyzer,
        String q) throws SearchException {
        try {
            BooleanQuery booleanQuery = new BooleanQuery();
            for (String field : fields) {
                QueryParser parser = new QueryParser(
                    Version.LUCENE_33,
                    field,
                    analyzer);
                Query query = parser.parse(q);
                booleanQuery.add(query, Occur.SHOULD);
            }
            return booleanQuery;
        } catch (Throwable t) {
            throw handleError("Parse error", t);
        }
    }

    private IndexReader getReader(boolean open) throws SearchException {
        try {
            if (fReader == null && open) {
                fReader = IndexReader.open(fDir);
            }
            return fReader;
        } catch (IOException e) {
            throw handleError("Can not create a DocumentSearcher instance.", e);
        }
    }

    /**
     * @see org.ubimix.commons.search.IDocumentSearcher#search(java.lang.String,
     *      java.util.Collection,
     *      org.ubimix.commons.search.DocumentSearcher.ISearchResultCollector)
     */
    public void search(
        String q,
        Collection<String> fields,
        final ISearchResultCollector collector) throws SearchException {
        try {
            if (fields == null || fields.isEmpty()) {
                fields = new HashSet<String>();
                fields.add(IFieldConst.FULL_CONTENT);
            }
            IndexReader reader = getReader(true);
            final Collection<String> searchFields = fields;
            Query query = getQuery(searchFields, fAnalyzer, q);
            IndexSearcher searcher = new IndexSearcher(reader);
            Formatter formatter = new SimpleHTMLFormatter();
            final Highlighter highlighter = new Highlighter(
                formatter,
                new QueryScorer(query));
            int maxCount = collector.getMaxResultNumber();
            TopDocs hits = searcher.search(query, maxCount);
            if (hits != null) {
                for (ScoreDoc hit : hits.scoreDocs) {
                    final Document doc = reader.document(hit.doc);
                    collector.onSearchResult(new InternalSearchResults(
                        doc,
                        fAnalyzer,
                        highlighter,
                        searchFields,
                        hit.score));
                }
            }
        } catch (Throwable t) {
            throw handleError("Can not perform a search operation. Query: '"
                + q
                + "'.", t);
        }
    }

    /**
     * @see org.ubimix.commons.search.IDocumentSearcher#search(java.lang.String,
     *      java.util.Collection,
     *      org.ubimix.commons.search.DocumentSearcher.ISearchResultCollector)
     */
    public void search(String q, final ISearchResultCollector collector)
        throws SearchException {
        search(q, null, collector);
    }
}
