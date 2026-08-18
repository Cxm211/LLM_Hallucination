    protected Object convertKey(Object key) {
        if (key == null) {
            return AbstractHashedMap.NULL;
        } else if (key instanceof String) {
            return ((String) key).toLowerCase();
        } else {
            return key;
        }
    }