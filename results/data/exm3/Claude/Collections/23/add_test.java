// org/apache/commons/collections4/TrieUtilsTest.java
public void testUnmodifiableTrieWithNestedUnmodifiable() {
    Trie<String, Object> originalTrie = new PatriciaTrie<Object>();
    originalTrie.put("key", "value");
    
    Trie<String, Object> firstUnmod = TrieUtils.unmodifiableTrie(originalTrie);
    Trie<String, Object> secondUnmod = TrieUtils.unmodifiableTrie(firstUnmod);
    
    assertSame("Double wrapping should return the same UnmodifiableTrie", firstUnmod, secondUnmod);
    assertTrue("Should still be UnmodifiableTrie", secondUnmod instanceof UnmodifiableTrie);
}