protected Object _deserializeFromEmptyString() throws IOException {
            // As per [databind#398], URI requires special handling
            if (_kind == STD_URI) {
                // fix: empty URI should return null to avoid exception
                return null;
            }
            // As per [databind#1123], Locale too
            if (_kind == STD_LOCALE) {
                return Locale.ROOT;
            }
            return super._deserializeFromEmptyString();
        }