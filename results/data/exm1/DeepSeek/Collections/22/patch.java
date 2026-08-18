public void putAll(int index, final Map<? extends K, ? extends V> map) {
    for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
        final K key = entry.getKey();
        final V value = entry.getValue();
        if (containsKey(key)) {
            put(key, value);
        } else {
            put(index, key, value);
            index++;
        }
    }
}