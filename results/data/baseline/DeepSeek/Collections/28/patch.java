protected SortedMap<K, V> createRangeMap(final K fromKey, final boolean fromInclusive,
                                         final K toKey, final boolean toInclusive) {
    if (fromKey != null && toKey != null) {
        @SuppressWarnings("unchecked")
        int cmp = ((Comparable<? super K>) fromKey).compareTo(toKey);
        if (cmp > 0) {
            throw new IllegalArgumentException("fromKey must be <= toKey");
        }
    }
    return new RangeEntryMap(fromKey, fromInclusive, toKey, toInclusive);
}