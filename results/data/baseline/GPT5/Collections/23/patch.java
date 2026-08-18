public static <K, V> UnmodifiableTrie<K, V> unmodifiableTrie(final Trie<K, ? extends V> trie) {
        @SuppressWarnings("unchecked")
        final Trie<K, V> casted = (Trie<K, V>) trie;
        return new UnmodifiableTrie<K, V>(casted);
    }