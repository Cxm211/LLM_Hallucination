// ===== FIXED org.apache.commons.collections4.map.ListOrderedMap :: putAll(int, Map) [lines 244-259] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-22-fixed/src/main/java/org/apache/commons/collections4/map/ListOrderedMap.java =====
    public void putAll(int index, final Map<? extends K, ? extends V> map) {
        for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            final K key = entry.getKey();
            final boolean contains = containsKey(key);
            // The return value of put is null if the key did not exist OR the value was null
            // so it cannot be used to determine whether the key was added
            put(index, entry.getKey(), entry.getValue());
            if (!contains) {
                // if no key was replaced, increment the index
                index++;
            } else {
                // otherwise put the next item after the currently inserted key
                index = indexOf(entry.getKey()) + 1;
            }
        }
    }
