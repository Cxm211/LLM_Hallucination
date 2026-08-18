// org/apache/commons/collections4/map/ListOrderedMapTest.java
public void testPutAllExistingKeysAtStart() {
    ListOrderedMap<String, String> map = new ListOrderedMap<>();
    map.put("K1", "V1");
    map.put("K2", "V2");
    map.put("K3", "V3");
    Map<String, String> toInsert = new HashMap<>();
    toInsert.put("K1", "NewV1");
    toInsert.put("K2", "NewV2");
    map.putAll(3, toInsert);
    assertEquals(3, map.size());
    List<String> keys = new ArrayList<>(map.keySet());
    assertEquals(Arrays.asList("K3", "K1", "K2"), keys);
    assertEquals("NewV1", map.get("K1"));
    assertEquals("NewV2", map.get("K2"));
    assertEquals("V3", map.get("K3"));
}
