private boolean _hasCustomHandlers(JavaType t) {
        // Check handlers directly on the type itself
        if ((t.getValueHandler() != null) || (t.getTypeHandler() != null)) {
            return true;
        }
        if (t.isContainerType()) {
            // First: value types may have both value and type handlers
            JavaType ct = t.getContentType();
            if (ct != null) {
                if ((ct.getValueHandler() != null) || (ct.getTypeHandler() != null)) {
                    return true;
                }
            }
            // Second: map(-like) types may have value handler for key (but not type; keys are untyped)
            if (t.isMapLikeType()) {
                JavaType kt = t.getKeyType();
                if (kt != null && kt.getValueHandler() != null) {
                    return true;
                }
            }
        }
        return false;
    }