/**
 * 
 */
package org.ubimix.commons.search;

import java.util.Set;

/**
 * Instances of this type provides access to field values of indexed documents.
 * 
 * @author kotelnikov
 */
public interface IDocument {

    /**
     * Returns a set of all field names of this document.
     * 
     * @return a set of all field names associated with this document
     * @throws SearchException
     */
    Set<String> getFields() throws SearchException;

    /**
     * Returns the value of the field with the specified name.
     * 
     * @param field the name of the field
     * @return the value of the field with the specified name
     * @throws SearchException
     */
    String getValue(String field) throws SearchException;

}
