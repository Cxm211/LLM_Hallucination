protected Object convertKey(Object key) {
        if (key == null) {
            return AbstractHashedMap.NULL;
        }
        if (key instanceof String) {
            return ((String) key).toLowerCase(java.util.Locale.ROOT);
        }
        return key;
    }