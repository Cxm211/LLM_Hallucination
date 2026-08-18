public static <K, V> UnmodifiableTrie<K, V> unmodifiableTrie(final Trie<K, ? extends V> trie) {
        if (trie == null) {
            throw new IllegalArgumentException("Trie must not be null");
        }
        if (trie instanceof UnmodifiableTrie) {
            @SuppressWarnings("unchecked")
            final UnmodifiableTrie<K, V> unmod = (UnmodifiableTrie<K, V>) trie;
            return unmod;
        }
        @SuppressWarnings("unchecked")
        final Trie<K, V> cast = (Trie<K, V>) trie;
        return new UnmodifiableTrie<K, V>(cast);
    }