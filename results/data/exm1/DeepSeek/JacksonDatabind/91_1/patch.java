private boolean _hasCustomHandlers(JavaType t) {
    if (t.isContainerType()) {
        JavaType ct = t.getContentType();
        if (ct != null) {
            if ((ct.getValueHandler() != null) || (ct.getTypeHandler() != null)) {
                return true;
            }
        }
        JavaType keyType = t.getKeyType();
        if (keyType != null) {
            if ((keyType.getValueHandler() != null) || (keyType.getTypeHandler() != null)) {
                return true;
            }
        }
    }
    return false;
}