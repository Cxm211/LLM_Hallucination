private boolean _hasCustomHandlers(JavaType t) {
        if (t == null) {
            return false;
        }
        // First: type itself may have value/type handlers
        if ((t.getValueHandler() != null) || (t.getTypeHandler() != null)) {
            return true;
        }
        if (t.isContainerType()) {
            // Value types may have both value and type handlers
            JavaType ct = t.getContentType();
            if (ct != null) {
                if ((ct.getValueHandler() != null) || (ct.getTypeHandler() != null)) {
                    return true;
                }
            }
            // Map(-like) types may have value handler for key (but not type; keys are untyped)
            if (t.isMapLikeType()) {
                JavaType kt = t.getKeyType();
                if (kt != null && kt.getValueHandler() != null) {
                    return true;
                }
            }
        }
        return false;
    }