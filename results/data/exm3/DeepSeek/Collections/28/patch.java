        protected SortedMap<K, V> createRangeMap(final K fromKey, final boolean fromInclusive,
                                                 final K toKey, final boolean toInclusive) {
            return new RangeEntryMap(fromKey, fromInclusive, toKey, toInclusive) {
                @Override
                public void clear() {
                    // Use iterator to remove each key, ensuring proper removal from backing map.
                    java.util.Iterator<K> it = keySet().iterator();
                    while (it.hasNext()) {
                        it.next();
                        it.remove();
                    }
                }
            };
        }