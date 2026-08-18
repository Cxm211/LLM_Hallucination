// org/apache/commons/collections/map/TestMultiValueMap.java
public void testPutAllEmptyMap() {
    MultiValueMap map = new MultiValueMap();
    Collection coll = Arrays.asList(new Object[] {"A", "B"});
    
    assertEquals(true, map.putAll("key1", coll));
    assertEquals(2, map.size("key1"));
    assertEquals(true, map.containsValue("key1", "A"));
    assertEquals(true, map.containsValue("key1", "B"));
}