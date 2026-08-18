public void removeValue(Comparable key) {
        if (key == null) {
            throw new IllegalArgumentException("Null 'key' not permitted.");
        }
        int index = getIndex(key);
        if (index < 0) {
            throw new UnknownKeyException("Key not found: " + key);
        }
        removeValue(index);
    }