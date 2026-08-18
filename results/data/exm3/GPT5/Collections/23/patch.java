public static <K, V> UnmodifiableTrie<K, V> unmodifiableTrie(final Trie<K, ? extends V> trie) {
        if (trie == null) {
            throw new IllegalArgumentException("Trie must not be null");
        }
        if (trie instanceof UnmodifiableTrie) {
            @SuppressWarnings("unchecked")
            final UnmodifiableTrie<K, V> unmodifiable = (UnmodifiableTrie<K, V>) trie;
            return unmodifiable;
        }
        return new UnmodifiableTrie<K, V>(trie);
    }