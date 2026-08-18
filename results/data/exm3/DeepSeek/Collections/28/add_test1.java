// org/apache/commons/collections4/trie/PatriciaTrieTest.java
public void testHeadMapClear() {
    Trie<String, Integer> trie = new PatriciaTrie<Integer>();
    trie.put("A", 1);
    trie.put("B", 2);
    trie.put("C", 3);
    trie.put("D", 4);
    SortedMap<String, Integer> headMap = trie.headMap("C", true);
    assertEquals(new HashSet<String>(Arrays.asList("A", "B", "C")), headMap.keySet());
    headMap.clear();
    assertTrue(headMap.isEmpty());
    assertEquals(new HashSet<String>(Arrays.asList("D")), trie.keySet());
}
