public static <K, V> UnmodifiableTrie<K, V> unmodifiableTrie(final Trie<K, ? extends V> trie) {
    if (trie == null) {
        throw new IllegalArgumentException("Trie must not be null");
    }
    return (trie instanceof UnmodifiableTrie) ? (UnmodifiableTrie<K, V>) trie : new UnmodifiableTrie<K, V>(trie);
}