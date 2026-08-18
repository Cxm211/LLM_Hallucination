public void putAll(int index, final Map<? extends K, ? extends V> map) {
        for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            final K key = entry.getKey();
            final boolean existing = containsKey(key);
            put(index, key, entry.getValue());
            if (existing) {
                index = indexOf(key) + 1;
            } else {
                index++;
            }
        }
    }