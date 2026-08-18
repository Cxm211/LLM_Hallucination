public void putAll(int index, final Map<? extends K, ? extends V> map) {
    for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
        final boolean keyExisted = containsKey(entry.getKey());
        put(index, entry.getKey(), entry.getValue());
        if (!keyExisted) {
            // if no key was replaced, increment the index
            index++;
        } else {
            // otherwise put the next item after the currently inserted key
            index = indexOf(entry.getKey()) + 1;
        }
    }
}