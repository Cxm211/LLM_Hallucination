    private boolean _hasCustomHandlers(JavaType t) {
        if (t.isContainerType()) {
            // Check container's own handlers
            if (t.getValueHandler() != null || t.getTypeHandler() != null) {
                return true;
            }
            // Check content type handlers
            JavaType ct = t.getContentType();
            if (ct != null) {
                if (ct.getValueHandler() != null || ct.getTypeHandler() != null) {
                    return true;
                }
            }
            // For map-like types, check key type's value handler
            if (t.isMapLikeType()) {
                JavaType kt = t.getKeyType();
                if (kt != null && kt.getValueHandler() != null) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }