/**
 * 
 */
package org.webreformatter.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.webreformatter.search.IDocumentSearcher.ISearchResult;

public abstract class AbstractSearchResultGroups {

    protected String fGroupFieldName;

    final Map<String, List<ISearchResult>> fGroups = new LinkedHashMap<String, List<ISearchResult>>();

    protected Collection<String> fSortFields;

    public AbstractSearchResultGroups(
        String groupFieldName,
        Collection<String> sortFields) {
        fGroupFieldName = groupFieldName;
        fSortFields = sortFields;
    }

    public AbstractSearchResultGroups(
        String groupFieldName,
        String... sortFields) {
        fGroupFieldName = groupFieldName;
        fSortFields = new ArrayList<String>();
        for (String field : sortFields) {
            fSortFields.add(field);
        }
    }

    public void addSearchResult(ISearchResult result) throws SearchException {
        IDocument doc = result.getDocument();
        String groupFieldValue = doc.getValue(fGroupFieldName);
        groupFieldValue = normalizeGroupFieldValue(groupFieldValue);
        List<ISearchResult> list = fGroups.get(groupFieldValue);
        if (list == null) {
            list = new ArrayList<ISearchResult>();
            fGroups.put(groupFieldValue, list);
        }
        list.add(result);
    }

    protected String normalizeGroupFieldValue(String value) {
        return value.toLowerCase();
    }

    public void show() throws IOException {
        for (Map.Entry<String, List<ISearchResult>> entry : fGroups.entrySet()) {
            String name = entry.getKey();
            List<ISearchResult> list = entry.getValue();
            if (fSortFields != null && !fSortFields.isEmpty()) {
                Collections.sort(list, new Comparator<ISearchResult>() {
                    public int compare(ISearchResult o1, ISearchResult o2) {
                        try {
                            IDocument d1 = o1.getDocument();
                            IDocument d2 = o2.getDocument();
                            for (String field : fSortFields) {
                                String first = d1.getValue(field);
                                String second = d2.getValue(field);
                                int result = first.compareTo(second);
                                if (result != 0) {
                                    return result;
                                }
                            }
                        } catch (IOException e) {
                        }
                        return 0;
                    }
                });
            }
            showResultGroup(name, list);
        }
    }

    protected abstract void showResultGroup(
        String name,
        List<ISearchResult> list) throws IOException;

}