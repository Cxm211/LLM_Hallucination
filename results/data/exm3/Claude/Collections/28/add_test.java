// org/apache/commons/collections4/trie/PatriciaTrieTest.java
public void testPrefixMapSize() {
    Trie<String, Integer> trie = new PatriciaTrie<Integer>();
    trie.put("Anna", 1);
    trie.put("Anael", 2);
    trie.put("Analu", 3);
    trie.put("Andreas", 4);
    trie.put("Andrea", 5);
    trie.put("Andres", 6);
    trie.put("Anatole", 7);
    SortedMap<String, Integer> prefixMap = trie.prefixMap("And");
    assertEquals(3, prefixMap.size());
    
    prefixMap.remove("Andrea");
    assertEquals(2, prefixMap.size());
    assertEquals(6, trie.size());
    assertEquals(new HashSet<String>(Arrays.asList("Anael", "Analu", "Anatole", "Anna", "Andreas", "Andres")), trie.keySet());
}