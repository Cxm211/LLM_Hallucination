public void putAll(int index, final Map<? extends K, ? extends V> map) {
    for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
        final K key = entry.getKey();
        final boolean keyExists = containsKey(key);
        final V old = put(index, key, entry.getValue());
        if (!keyExists) {
            // if no key was replaced, increment the index
            index++;
        } else {
            // otherwise put the next item after the currently inserted key
            index = indexOf(key) + 1;
        }
    }
}