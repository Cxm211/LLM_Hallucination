protected SortedMap<K, V> createRangeMap(final K fromKey, final boolean fromInclusive,
                                             final K toKey, final boolean toInclusive) {
            if (fromKey == null && toKey == null) {
                return this;
            }
            return new RangeEntryMap(fromKey, fromInclusive, toKey, toInclusive);
        }