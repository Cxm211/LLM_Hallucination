public static <K, V> UnmodifiableTrie<K, V> unmodifiableTrie(final Trie<K, ? extends V> trie) {
    if (trie instanceof UnmodifiableTrie) {
        @SuppressWarnings("unchecked")
        final UnmodifiableTrie<K, V> unmodifiableTrie = (UnmodifiableTrie<K, V>) trie;
        return unmodifiableTrie;
    }
    return new UnmodifiableTrie<K, V>(trie);
}