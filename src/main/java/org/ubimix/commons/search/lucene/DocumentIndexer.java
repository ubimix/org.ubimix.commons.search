package org.ubimix.commons.search.lucene;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MergeScheduler;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.ubimix.commons.search.DocumentProvider;
import org.ubimix.commons.search.IDocument;
import org.ubimix.commons.search.IDocumentIndexer;
import org.ubimix.commons.search.IDocumentProvider;
import org.ubimix.commons.search.IFieldConst;
import org.ubimix.commons.search.SearchException;

/**
 * This class is used to index objects of the {@link IDocument} type. Indexing
 * parameters for individual fields could be re-defined using the
 * {@link FieldDescription} instances.
 * 
 * @author kotelnikov
 */
public class DocumentIndexer implements IDocumentIndexer {

    /**
     * The internal logger
     */
    private final static Logger log = Logger.getLogger(DocumentIndexer.class
        .getName());

    /**
     * The text analyzer (used by Lucene).
     */
    private Analyzer fAnalyzer;

    private IndexWriter fWriter;

    /**
     * @param dir the Lucene directory
     * @param analyzer the analyzer
     */
    public DocumentIndexer(Directory dir, Analyzer analyzer) {
        try {
            fAnalyzer = analyzer;
            IndexWriterConfig config = new IndexWriterConfig(
                Version.LUCENE_33,
                fAnalyzer);
            MergeScheduler mergeScheduler = new SerialMergeScheduler();
            config.setMergeScheduler(mergeScheduler);
            fWriter = new IndexWriter(dir, config);
        } catch (Throwable t) {
            throw handleErrror("Can not open the index.", t);
        }
    }

    /**
     * @see org.ubimix.commons.search.IDocumentIndexer#close()
     */
    public void close() throws SearchException {
        try {
            fWriter.optimize();
            fWriter.close(true);
        } catch (Throwable t) {
            throw handleError("Can not close the indexer", t);
        }
    }

    private SearchException handleError(String msg, Throwable e) {
        log.log(Level.FINE, msg, e);
        return new SearchException(msg, e);
    }

    private RuntimeException handleErrror(String message, Throwable t) {
        log.log(Level.WARNING, message, t);
        return new RuntimeException(t);
    }

    /**
     * @see org.ubimix.commons.search.IDocumentIndexer#index(org.ubimix.commons.search.IDocument)
     */
    public void index(IDocument doc) throws SearchException {
        index(null, doc);
    }

    public void index(IDocumentProvider documents) throws SearchException {
        index(null, documents);
    }

    /**
     * @see org.ubimix.commons.search.IDocumentIndexer#index(java.util.Map,
     *      org.ubimix.commons.search.IDocument)
     */
    public void index(
        Map<String, FieldDescription> fieldDescriptors,
        IDocument doc) throws SearchException {
        IDocumentProvider provider = new DocumentProvider(doc);
        index(fieldDescriptors, provider);
    }

    public void index(
        Map<String, FieldDescription> fieldDescriptors,
        IDocumentProvider documents) throws SearchException {
        try {
            if (fieldDescriptors == null) {
                fieldDescriptors = Collections.emptyMap();
            }
            // Gets all index fields of documents to index.
            Map<String, QueryParser> indexFields = new HashMap<String, QueryParser>();
            for (Map.Entry<String, FieldDescription> entry : fieldDescriptors
                .entrySet()) {
                FieldDescription value = entry.getValue();
                if (value.isIdentifier()) {
                    String field = entry.getKey();
                    if (value.isAnalyzed()) {
                        QueryParser parser = new QueryParser(
                            Version.LUCENE_33,
                            field,
                            fAnalyzer);
                        indexFields.put(field, parser);
                    } else {
                        indexFields.put(field, null);
                    }
                }
            }
            for (IDocument document : documents) {
                // Remove previous version of the document from the index
                BooleanQuery booleanQuery = new BooleanQuery();
                for (Map.Entry<String, QueryParser> entry : indexFields
                    .entrySet()) {
                    String field = entry.getKey();
                    String value = document.getValue(field);
                    if (value != null) {
                        Query query = null;
                        QueryParser parser = entry.getValue();
                        if (parser == null) {
                            Term term = new Term(field, value);
                            query = new TermQuery(term);
                        } else {
                            query = parser.parse(value);
                        }
                        booleanQuery.add(query, Occur.SHOULD);
                    }
                }
                fWriter.deleteDocuments(booleanQuery);

                // Add a new version of the document
                Document luceneDoc = newDocument(document, fieldDescriptors);
                fWriter.addDocument(luceneDoc);

            }
        } catch (Throwable t) {
            throw handleError("Can not index a document", t);
        }

    }

    /**
     * Create a new Lucene document to index based on the provided fields and
     * field descriptions.
     * 
     * @param doc the document to transform into a Lucene document
     * @param fieldDescriptions the map containing descriptions of specified
     *        field configurations
     * @return
     * @throws SearchException
     */
    private Document newDocument(
        IDocument doc,
        Map<String, FieldDescription> fieldDescriptions) throws SearchException {
        Document result = new Document();
        StringBuffer fullContent = new StringBuffer();
        for (String fieldName : doc.getFields()) {
            String str = doc.getValue(fieldName);
            FieldDescription descr = fieldDescriptions.get(fieldName);
            if (descr == null) {
                descr = FieldDescription.DEFAULT;
            }
            Index analyze;
            if (descr.isSearchableInFullIndex()) {
                analyze = Field.Index.ANALYZED;
                fullContent.append(" ").append(str);
            } else {
                analyze = Field.Index.NOT_ANALYZED;
            }
            Field field = new Field(fieldName, str, Field.Store.YES, analyze);
            float boost = descr.getBoostFactor();
            field.setBoost(boost);
            result.add(field);
        }
        Field field = new Field(
            IFieldConst.FULL_CONTENT,
            fullContent.toString(),
            Field.Store.YES,
            Field.Index.ANALYZED);
        result.add(field);
        return result;
    }

}