// org/apache/commons/collections4/map/ListOrderedMapTest.java
@Test
public void testPutAllWithIndex_ReplacingNonNullWithNull() {
    Object key1 = new Object();
    Object key2 = new Object();
    HashMap<Object, Object> hmap = new HashMap<Object, Object>();
    hmap.put(key1, null);
    
    ListOrderedMap<Object, Object> listMap = new ListOrderedMap<Object, Object>();
    listMap.put(key1, "existingValue");
    listMap.put(key2, "value2");
    
    assertEquals("Should have two elements before putAll", 2, listMap.size());
    listMap.putAll(1, hmap);
    assertEquals("Should still have two elements after putAll", 2, listMap.size());
    assertNull("Key1 should now have null value", listMap.get(key1));
}