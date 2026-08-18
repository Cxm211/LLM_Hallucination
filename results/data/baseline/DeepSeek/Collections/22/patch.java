public void putAll(int index, final Map<? extends K, ? extends V> map) {
    for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
        final K key = entry.getKey();
        final V value = entry.getValue();
        if (containsKey(key)) {
            final V old = put(index, key, value);
            index = indexOf(key) + 1;
        } else {
            put(index, key, value);
            index++;
        }
    }
}