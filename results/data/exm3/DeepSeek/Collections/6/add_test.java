// org/apache/commons/collections/map/TestFlat3Map.java
public void testRemoveMiddleAndFirst() {
    Flat3Map m = new Flat3Map();
    m.put("a", "aVal");
    m.put("b", "bVal");
    m.put("c", "cVal");
    assertEquals("bVal", m.remove("b"));
    assertEquals("aVal", m.remove("a"));
    assertEquals("cVal", m.remove("c"));
    assertTrue(m.isEmpty());
}
