package org.webreformatter.search;

import java.util.Map;

/**
 * @author kotelnikov
 */
public interface IDocumentIndexer {

    /**
     * Instances of this type are used to
     * 
     * @author kotelnikov
     */
    public static class FieldDescription {

        /**
         * The builder for {@link FieldDescription} instances.
         * 
         * @author kotelnikov
         */
        public static class Builder extends FieldDescription {

            /**
             * Returns a newly created field description .
             * 
             * @return a newly created field description
             */
            public FieldDescription build() {
                return new FieldDescription(this);
            }

            /**
             * @param analyze the analyze to set
             * @return this instance
             */
            public Builder setAnalyze(boolean analyze) {
                fAnalyzed = analyze;
                return this;
            }

            /**
             * @param boostFactor the boostFactor to set
             * @return this instance
             */
            public Builder setBoostFactor(float boostFactor) {
                fBoostFactor = boostFactor;
                return this;
            }

            /**
             * @param identifier the identifier to set
             * @return this instance
             */
            public Builder setIdentifier(boolean identifier) {
                fIdentifier = identifier;
                if (fIdentifier) {
                    setAnalyze(false);
                }
                return this;
            }

            /**
             * Defines if the content of this field is appended to the full
             * content and searchable in global queries (over all fields). If
             * the specified parameter is <code>Boolean.TRUE</code> or
             * <code>Boolean.FALSE</code> then this field is explicitly allowed
             * or forbidded. If the parameter is <code>null</code> then this
             * field became searchable only if this field is analyzed (see
             * {@link #isAnalyzed()}).
             * 
             * @param inFullContent
             */
            public void setInFullContent(Boolean inFullContent) {
                fSearchableInFullIndex = inFullContent;
            }
        }

        /**
         * The field description containing default parameters.
         */
        public final static FieldDescription DEFAULT = new FieldDescription();

        public static FieldDescription.Builder builder() {
            return new Builder();
        }

        /**
         * This field defines if the indexed field should be analyzed or not.
         */
        protected boolean fAnalyzed = true;

        /**
         * The boost factor for this field
         */
        protected float fBoostFactor = 1;

        /**
         * The flag defines if the corresponding field is an identifier.
         */
        protected boolean fIdentifier = false;

        /**
         * If this flag is <code>true</code> then the content of this field is
         * searchable in the full content search.
         */
        protected Boolean fSearchableInFullIndex;

        /**
         * The default constructor
         */
        protected FieldDescription() {
        }

        /**
         * A copy constructor. Used to build instances of this type from
         * builders.
         * 
         * @param description the source of field values.
         */
        public FieldDescription(FieldDescription description) {
            fBoostFactor = description.fBoostFactor;
            fAnalyzed = description.fAnalyzed;
            fIdentifier = description.fIdentifier;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof FieldDescription)) {
                return false;
            }
            FieldDescription o = (FieldDescription) obj;
            return fBoostFactor == o.fBoostFactor && fAnalyzed == o.fAnalyzed;
        }

        /**
         * Returns the boost factor for this field
         * 
         * @return the boost factor for this field
         */
        public float getBoostFactor() {
            return fBoostFactor;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            int a = fAnalyzed ? 1 : 0;
            int b = Float.floatToIntBits(fBoostFactor);
            return a ^ b;
        }

        /**
         * @return the analyze
         */
        public boolean isAnalyzed() {
            return fAnalyzed;
        }

        /**
         * @return the identifier
         */
        public boolean isIdentifier() {
            return fIdentifier;
        }

        /**
         * @return <code>true</code> if the content of this field should be
         *         searchable in the full content search
         */
        public boolean isSearchableInFullIndex() {
            if (fSearchableInFullIndex != null) {
                return fSearchableInFullIndex;
            }
            if (isIdentifier()) {
                return false;
            }
            return isAnalyzed();
        }

        @Override
        public String toString() {
            return "(boost="
                + fBoostFactor
                + ";analyzed="
                + fAnalyzed
                + ";searchable="
                + isSearchableInFullIndex()
                + ")";
        }
    }

    /**
     * Closes the writer and fries all associated resources..
     * 
     * @throws SearchException
     */
    void close() throws SearchException;

    /**
     * Adds the document to the index.
     * 
     * @param doc the document to add in the index
     * @throws SearchException
     */
    void index(IDocument doc) throws SearchException;

    /**
     * This method indexes multiple documents in one "transaction".
     * 
     * @param documents an iterator over all documents to index
     * @throws SearchException
     */
    void index(IDocumentProvider documents) throws SearchException;

    /**
     * Adds the provided document to the index.
     * 
     * @param fieldDescriptors this map contains field names and the
     *        corresponding descriptions; it is an optional parameter; it should
     *        contain descriptions only for fields having specific indexing
     *        parameters (like specific field boost factors etc).
     * @param doc the document to add in the list
     * @throws SearchException
     */
    void index(Map<String, FieldDescription> fieldDescriptors, IDocument doc)
        throws SearchException;

    /**
     * Indexes multiple documents in one "transaction".
     * 
     * @param fieldDescriptors list of field descriptors
     * @param documents documents to inded
     * @throws SearchException
     */
    void index(
        Map<String, FieldDescription> fieldDescriptors,
        IDocumentProvider documents) throws SearchException;

}