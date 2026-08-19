// ===== FIXED org.apache.commons.collections4.trie.UnmodifiableTrie :: unmodifiableTrie(Trie) [lines 54-61] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-23-fixed/src/main/java/org/apache/commons/collections4/trie/UnmodifiableTrie.java =====
    public static <K, V> Trie<K, V> unmodifiableTrie(final Trie<K, ? extends V> trie) {
        if (trie instanceof Unmodifiable) {
            @SuppressWarnings("unchecked") // safe to upcast
            final Trie<K, V> tmpTrie = (Trie<K, V>) trie;
            return tmpTrie;
        }
        return new UnmodifiableTrie<K, V>(trie);
    }
