// org/apache/commons/collections/map/TestFlat3Map.java
public void testRemoveNullKey() {
    Flat3Map m = new Flat3Map();
    m.put(null, "nullVal");
    m.put("x", "xVal");
    assertEquals("nullVal", m.remove(null));
    assertEquals("xVal", m.get("x"));
    assertEquals(1, m.size());
    m.clear();
    m.put(null, "null1");
    m.put("a", "aVal");
    m.put("b", "bVal");
    assertEquals("null1", m.remove(null));
    assertEquals("aVal", m.get("a"));
    assertEquals("bVal", m.get("b"));
}
