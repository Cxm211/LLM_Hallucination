protected Object convertKey(Object key) {
        if (key == null) {
            return AbstractHashedMap.NULL;
        }
        return key.toString().toLowerCase(java.util.Locale.ENGLISH);
    }