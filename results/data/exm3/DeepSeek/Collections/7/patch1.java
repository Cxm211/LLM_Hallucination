    private void addPropertyDirect(String key, Object value) {
        // safety check
        if (!containsKey(key)) {
            keysAsListed.add(key);
        }
        put(key, value);
    }