// org/apache/commons/collections4/map/ListOrderedMapTest.java
@Test
public void testPutAllWithIndex_MixedNullAndNonNullValues() {
    Object key1 = new Object();
    Object key2 = new Object();
    Object key3 = new Object();
    HashMap<Object, Object> hmap = new HashMap<Object, Object>();
    hmap.put(key1, "value1");
    hmap.put(key2, null);
    hmap.put(key3, "value3");
    
    ListOrderedMap<Object, Object> listMap = new ListOrderedMap<Object, Object>();
    listMap.put(key1, null);
    listMap.put(key2, "oldValue2");
    
    assertEquals("Should have two elements before putAll", 2, listMap.size());
    listMap.putAll(2, hmap);
    assertEquals("Should have four elements after putAll", 4, listMap.size());
}