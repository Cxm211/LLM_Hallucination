// org/apache/commons/collections4/trie/PatriciaTrieTest.java
public void testSubMapClear() {
    Trie<String, Integer> trie = new PatriciaTrie<Integer>();
    trie.put("A", 1);
    trie.put("B", 2);
    trie.put("C", 3);
    trie.put("D", 4);
    SortedMap<String, Integer> subMap = trie.subMap("B", false, "D", true);
    assertEquals(new HashSet<String>(Arrays.asList("C", "D")), subMap.keySet());
    subMap.clear();
    assertTrue(subMap.isEmpty());
    assertEquals(new HashSet<String>(Arrays.asList("A", "B")), trie.keySet());
}
