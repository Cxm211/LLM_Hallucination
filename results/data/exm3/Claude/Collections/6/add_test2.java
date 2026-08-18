// org/apache/commons/collections/map/TestFlat3Map.java
public void testRemoveFirstKeyFromSize2() {
        Flat3Map m = new Flat3Map();
        m.put("x", "valueX");
        m.put("y", "valueY");
        assertEquals("valueX", m.remove("x"));
        assertEquals("valueY", m.remove("y"));
    }