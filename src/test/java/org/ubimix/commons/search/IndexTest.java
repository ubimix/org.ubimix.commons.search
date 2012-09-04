/**
 * 
 */
package org.ubimix.commons.search;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.ubimix.commons.search.IDocument;
import org.ubimix.commons.search.IDocumentIndexer;
import org.ubimix.commons.search.IDocumentSearcher;
import org.ubimix.commons.search.MapBasedDocument;
import org.ubimix.commons.search.SearchException;
import org.ubimix.commons.search.IDocumentIndexer.FieldDescription;
import org.ubimix.commons.search.IDocumentSearcher.ISearchResult;
import org.ubimix.commons.search.IDocumentSearcher.ISearchResultCollector;
import org.ubimix.commons.search.lucene.DocumentIndexer;
import org.ubimix.commons.search.lucene.DocumentSearcher;

/**
 * @author kotelnikov
 */
public class IndexTest extends TestCase {

    protected Analyzer fAnalyzer;

    protected Directory fDir;

    /**
     * @param name
     */
    public IndexTest(String name) {
        super(name);
    }

    protected Map<String, String> getMap(String... strs) {
        HashMap<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < strs.length;) {
            String key = strs[i++];
            String value = i < strs.length ? strs[i++] : null;
            result.put(key, value);
        }
        return result;
    }

    private void index(MapBasedDocument doc) throws SearchException {
        IDocumentIndexer indexer = new DocumentIndexer(fDir, fAnalyzer);
        Map<String, FieldDescription> fields = new HashMap<String, IDocumentIndexer.FieldDescription>();
        fields.put("id", FieldDescription
            .builder()
            .setAnalyze(false)
            .setIdentifier(true));
        indexer.index(fields, doc);
        indexer.close();
    }

    protected SimpleAnalyzer newAnalyzer() {
        return new SimpleAnalyzer(Version.LUCENE_33);
    }

    protected RAMDirectory newDirectory() {
        return new RAMDirectory();
    }

    protected MapBasedDocument newDocument(String... strings) {
        return new MapBasedDocument(getMap(strings));
    }

    private void search(String query, String result) throws SearchException {
        final StringBuilder buf = new StringBuilder();
        IDocumentSearcher searcher = new DocumentSearcher(fDir, fAnalyzer);
        searcher.search(query, new ISearchResultCollector() {
            public int getMaxResultNumber() {
                return 100;
            }

            public void onSearchResult(ISearchResult result)
                throws SearchException {
                IDocument doc = result.getDocument();
                assertNotNull(doc);
                if (buf.length() > 0) {
                    buf.append(",");
                }
                buf.append(doc.getValue("id"));
            }
        });
        searcher.close();
        assertEquals(result, buf.toString());
    }

    @Override
    protected void setUp() throws Exception {
        fDir = newDirectory();
        fAnalyzer = newAnalyzer();
    }

    public void test() throws SearchException {
        MapBasedDocument doc = newDocument(
            "id",
            "123",
            "firstName",
            "John",
            "lastName",
            "Smith");
        index(doc);

        // search
        search("John", "123");
        search("firstName:John", "123");
        search("lastName:John", "");

        // Set a new document with the same ID
        doc = newDocument(
            "id",
            "123",
            "title",
            "Lorem ipsum dolor sit amet",
            "summary",
            ""
                + "consectetur adipiscing elit. Sed at est sit amet ipsum accumsan "
                + "bibendum. Mauris quis tempus enim. Vivamus auctor nunc a tellus "
                + "varius in varius ipsum cursus. Praesent interdum euismod molestie. ",
            "content",
            ""
                + "Mauris vulputate purus blandit lorem sollicitudin luctus."
                + "Praesent suscipit consectetur libero, eu imperdiet dui ornare "
                + "Phasellus bibendum augue sed ipsum congue sit amet mattis "
                + "eros fermentum. Aliquam erat volutpat. Maecenas turpis diam, "
                + "vestibulum vulputate interdum quis, commodo vel sem. Donec "
                + "consequat metus sit amet dolor bibendum lobortis. Phasellus "
                + "molestie vulputate dui a rutrum. Etiam tempor porta libero at "
                + "venenatis. In hac habitasse platea dictumst. Sed est eros, "
                + "ornare non congue in, rhoncus sit amet leo. Aenean nibh lectus, "
                + "accumsan ac congue non, rutrum eget mi. Aliquam erat volutpat. "
                + "Nunc euismod, neque et congue tincidunt, ligula libero adipiscing "
                + "mauris, pulvinar tempus purus nulla non tortor. Nullam eget "
                + "augue tellus, ac luctus leo.");
        index(doc);
        // Check that there is no fields from the previous indexing
        search("John", "");
        search("firstName:John", "");
        search("lastName:John", "");

        search("Vivamus", "123");
        search("content:Vivamus", ""); // This field don't have this word
        search("summary:Vivamus", "123");

        search("Praesent", "123");
        search("title:Praesent", "");
        search("content:Praesent", "123");
        search("summary:Praesent", "123"); // This field don't have this word

    }

}
