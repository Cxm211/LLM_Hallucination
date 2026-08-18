    public void putAll(int index, final Map<? extends K, ? extends V> map) {
        for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            final K key = entry.getKey();
            final V value = entry.getValue();
            final boolean existed = containsKey(key);
            int insertIndex = index;
            if (existed) {
                int pos = indexOf(key);
                if (pos < index) {
                    insertIndex = index - 1;
                }
            }
            put(insertIndex, key, value);
            index = insertIndex + 1;
        }
    }