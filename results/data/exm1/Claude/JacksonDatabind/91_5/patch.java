private boolean _hasCustomHandlers(JavaType t) {
        if (t.isContainerType()) {
            // First: check if container type itself has custom handlers
            if (t.getValueHandler() != null || t.getTypeHandler() != null) {
                return true;
            }
            // Second: value types may have both value and type handlers
            JavaType ct = t.getContentType();
            if (ct != null) {
                if ((ct.getValueHandler() != null) || (ct.getTypeHandler() != null)) {
                    return true;
                }
            }
            // Third: map(-like) types may have value handler for key (but not type; keys are untyped)
            if (t.isMapLikeType()) {
                JavaType kt = t.getKeyType();
                if (kt != null && kt.getValueHandler() != null) {
                    return true;
                }
            }
        }
        return false;
    }