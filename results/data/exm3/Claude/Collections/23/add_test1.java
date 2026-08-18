// org/apache/commons/collections4/trie/UnmodifiableTrieTest.java
public void testDecorateFactoryWithPopulatedTrie() {
    final Trie<String, V> trie = new PatriciaTrie<V>();
    trie.put("test1", (V) "value1");
    trie.put("test2", (V) "value2");
    
    final Trie<String, V> unmodTrie = UnmodifiableTrie.unmodifiableTrie(trie);
    assertTrue("Should be UnmodifiableTrie", unmodTrie instanceof UnmodifiableTrie);
    
    final Trie<String, V> doubleWrapped = UnmodifiableTrie.unmodifiableTrie(unmodTrie);
    assertSame("Should not double-wrap", unmodTrie, doubleWrapped);
}