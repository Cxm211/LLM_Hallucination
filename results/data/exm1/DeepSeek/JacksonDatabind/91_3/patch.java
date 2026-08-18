private boolean _hasCustomHandlers(JavaType t) {
    if (t.isContainerType()) {
        // First: value types may have both value and type handlers
        JavaType ct = t.getContentType();
        if (ct != null) {
            if (ct.getValueHandler() != null || ct.getTypeHandler() != null) {
                return true;
            }
        }
        // Second: check if map(-like) types have custom key handlers
        if (t.isMapLikeType()) {
            JavaType kt = t.getKeyType();
            if (kt != null && kt.getValueHandler() != null) {
                return true;
            }
        }
    }
    return false;
}