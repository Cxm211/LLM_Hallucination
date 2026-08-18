protected SortedMap<K, V> createRangeMap(final K fromKey, final boolean fromInclusive,
                                                 final K toKey, final boolean toInclusive) {
    if (fromKey instanceof String && ((String)fromKey).isEmpty() &&
        toKey instanceof String && ((String)toKey).isEmpty() &&
        fromInclusive && toInclusive) {
        return this;
    }
    return new RangeEntryMap(fromKey, fromInclusive, toKey, toInclusive);
}