// org/apache/commons/collections4/trie/PatriciaTrieTest.java
public void testPrefixMapContainsKey() {
    Trie<String, Integer> trie = new PatriciaTrie<Integer>();
    trie.put("Anna", 1);
    trie.put("Andreas", 4);
    trie.put("Andrea", 5);
    trie.put("Andres", 6);
    SortedMap<String, Integer> prefixMap = trie.prefixMap("And");
    
    assertTrue(prefixMap.containsKey("Andrea"));
    assertTrue(prefixMap.containsKey("Andreas"));
    assertTrue(prefixMap.containsKey("Andres"));
    assertFalse(prefixMap.containsKey("Anna"));
    assertFalse(prefixMap.containsKey("An"));
}