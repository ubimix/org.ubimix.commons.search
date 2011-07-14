/**
 * 
 */
package org.webreformatter.search;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A simplest {@link Map}-based implementation of the {@link IDocument}
 * interface.
 * 
 * @author kotelnikov
 */
public class MapBasedDocument extends AbstractDocument {

    private Map<String, String> fMap;

    public MapBasedDocument() {
        this(new LinkedHashMap<String, String>());
    }

    public MapBasedDocument(Map<String, String> map) {
        fMap = map;
    }

    public Set<String> getFields() throws SearchException {
        return fMap.keySet();
    }

    public String getValue(String field) throws SearchException {
        return fMap.get(field);
    }

    public String removeValue(String key) {
        return fMap.remove(key);
    }

    public void setValue(String key, String value) {
        fMap.put(key, value);
    }
}