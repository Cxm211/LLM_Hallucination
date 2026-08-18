// org/apache/commons/collections4/map/ListOrderedMapTest.java
public void testPutAllMixNewAndExisting() {
    ListOrderedMap<String, String> map = new ListOrderedMap<>();
    map.put("K1", "V1");
    map.put("K2", "V2");
    Map<String, String> toInsert = new HashMap<>();
    toInsert.put("K2", "NewV2");
    toInsert.put("K3", "V3");
    map.putAll(2, toInsert);
    assertEquals(3, map.size());
    List<String> keys = new ArrayList<>(map.keySet());
    assertEquals(Arrays.asList("K1", "K2", "K3"), keys);
    assertEquals("V1", map.get("K1"));
    assertEquals("NewV2", map.get("K2"));
    assertEquals("V3", map.get("K3"));
}
