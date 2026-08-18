    public void putAll(int index, final Map<? extends K, ? extends V> map) {
        for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            final boolean existed = containsKey(entry.getKey());
            final V old = put(index, entry.getKey(), entry.getValue());
            if (!existed) {
                index++;
            } else {
                index = indexOf(entry.getKey()) + 1;
            }
        }
    }