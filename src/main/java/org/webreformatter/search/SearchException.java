/**
 * 
 */
package org.webreformatter.search;

import java.io.IOException;

/**
 * @author kotelnikov
 */
public class SearchException extends IOException {
    private static final long serialVersionUID = -4686631947955530687L;

    /**
     * 
     */
    public SearchException() {
    }

    /**
     * @param message
     */
    public SearchException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public SearchException(Throwable cause) {
        super(cause);
    }

}
