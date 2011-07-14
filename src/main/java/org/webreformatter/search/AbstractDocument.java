/**
 * 
 */
package org.webreformatter.search;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author kotelnikov
 */
public abstract class AbstractDocument implements IDocument {

    private static final Logger log = Logger.getLogger(AbstractDocument.class
        .getName());

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractDocument)) {
            return false;
        }
        AbstractDocument o = (AbstractDocument) obj;
        try {
            Set<String> fields = getFields();
            if (!fields.equals(o.getFields())) {
                return false;
            }
            for (String field : fields) {
                String first = getValue(field);
                String second = o.getValue(field);
                if (!equals(first, second)) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            throw handleError("Can not get the fields", e);
        }
    }

    private boolean equals(String first, String second) {
        return first != null && second != null
            ? first.equals(second)
            : first == second;
    }

    private RuntimeException handleError(String message, Throwable t) {
        log.log(Level.WARNING, message, t);
        return new RuntimeException(t);
    }

    @Override
    public int hashCode() {
        try {
            Set<String> fields = getFields();
            return fields.hashCode();
        } catch (IOException e) {
            throw handleError("Can not get fields", e);
        }
    }

    @Override
    public String toString() {
        try {
            StringBuilder buf = new StringBuilder();
            buf.append("{");
            Set<String> fields = getFields();
            for (String field : fields) {
                if (buf.length() > 0) {
                    buf.append(",");
                }
                String value = getValue(field);
                buf.append(field);
                buf.append("=");
                buf.append(value);
            }
            buf.append("}");
            return buf.toString();
        } catch (IOException e) {
            throw handleError("Can not get fields", e);
        }
    }

}