private boolean _hasCustomHandlers(JavaType t) {
    if (t.isContainerType()) {
        JavaType ct = t.getContentType();
        if (ct != null) {
            if (ct.getValueHandler() != null || ct.getTypeHandler() != null) {
                return true;
            }
        }
        if (t.isMapLikeType()) {
            JavaType kt = t.getKeyType();
            if (kt != null) {
                if (kt.getValueHandler() != null || kt.getTypeHandler() != null) {
                    return true;
                }
            }
        }
    }
    return false;
}