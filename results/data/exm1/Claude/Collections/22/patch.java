public void putAll(int index, final Map<? extends K, ? extends V> map) {
        for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            final K key = entry.getKey();
            final boolean containedKey = containsKey(key);
            put(index, key, entry.getValue());
            if (!containedKey) {
                index++;
            } else {
                index = indexOf(key) + 1;
            }
        }
    }