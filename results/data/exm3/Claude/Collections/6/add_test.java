// org/apache/commons/collections/map/TestFlat3Map.java
public void testRemoveWithNullKeys() {
        Flat3Map m = new Flat3Map();
        m.put(null, "nullValue1");
        m.put("key2", "value2");
        m.put("key3", "value3");
        assertEquals("nullValue1", m.remove(null));
        assertEquals("value2", m.remove("key2"));
        assertEquals("value3", m.remove("key3"));
    }