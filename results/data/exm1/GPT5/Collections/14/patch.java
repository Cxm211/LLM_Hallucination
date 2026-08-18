protected Object convertKey(Object key) {
        if (key != null) {
            return key.toString().toLowerCase(java.util.Locale.ENGLISH);
        } else {
            return AbstractHashedMap.NULL;
        }
    }