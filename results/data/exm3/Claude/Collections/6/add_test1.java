// org/apache/commons/collections/map/TestFlat3Map.java
public void testRemoveMiddleKeyFromSize3() {
        Flat3Map m = new Flat3Map();
        m.put("a", "valueA");
        m.put("b", "valueB");
        m.put("c", "valueC");
        assertEquals("valueB", m.remove("b"));
        assertEquals("valueA", m.remove("a"));
        assertEquals("valueC", m.remove("c"));
    }