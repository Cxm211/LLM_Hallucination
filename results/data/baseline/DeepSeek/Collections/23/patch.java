public static <K, V> UnmodifiableTrie<K, V> unmodifiableTrie(final Trie<K, ? extends V> trie) {
        return new UnmodifiableTrie<K, V>((Trie<K, V>) trie);
    }