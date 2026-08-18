// org/apache/commons/collections/map/TestFlat3Map.java
public void testMapIteratorSetValue1() throws Exception {
    Flat3Map map = new Flat3Map();
    map.put(ONE, TEN);
    map.put(TWO, TWENTY);
    map.put(THREE, THIRTY);
    
    MapIterator it = map.mapIterator();
    it.next();
    it.setValue("NewValue");
    assertEquals(3, map.size());
    assertEquals(true, map.containsKey(ONE));
    assertEquals(true, map.containsKey(TWO));
    assertEquals(true, map.containsKey(THREE));
    assertEquals("NewValue", map.get(ONE));
    assertEquals(TWENTY, map.get(TWO));
    assertEquals(THIRTY, map.get(THREE));
}