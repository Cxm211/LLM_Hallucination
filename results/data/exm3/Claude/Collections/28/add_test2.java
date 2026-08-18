// org/apache/commons/collections4/trie/PatriciaTrieTest.java
public void testPrefixMapPutAll() {
    Trie<String, Integer> trie = new PatriciaTrie<Integer>();
    trie.put("Anna", 1);
    trie.put("Andreas", 4);
    SortedMap<String, Integer> prefixMap = trie.prefixMap("And");
    
    Map<String, Integer> newEntries = new HashMap<String, Integer>();
    newEntries.put("Andrea", 5);
    newEntries.put("Andres", 6);
    prefixMap.putAll(newEntries);
    
    assertEquals(2, prefixMap.size());
    assertEquals(Integer.valueOf(5), prefixMap.get("Andrea"));
    assertEquals(Integer.valueOf(6), prefixMap.get("Andres"));
    assertEquals(3, trie.size());
}