private boolean _hasCustomHandlers(JavaType t) {
        // First: direct handlers for the type itself
        if ((t.getValueHandler() != null) || (t.getTypeHandler() != null)) {
            return true;
        }
        if (t.isContainerType()) {
            // Value/content types may have both value and type handlers
            JavaType ct = t.getContentType();
            if (ct != null) {
                if ((ct.getValueHandler() != null) || (ct.getTypeHandler() != null)) {
                    return true;
                }
            }
            // Map(-like) types may have value handler for key (but not type; keys are untyped)
            if (t.isMapLikeType()) {
                JavaType kt = ((com.fasterxml.jackson.databind.type.MapLikeType) t).getKeyType();
                if (kt != null && kt.getValueHandler() != null) {
                    return true;
                }
            }
        }
        return false;
    }