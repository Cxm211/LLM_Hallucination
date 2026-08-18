// org/apache/commons/collections/map/TestFlat3Map.java::testCollections261_NullKeyRemovals
public void testCollections261_NullKeyRemovals() {
        Flat3Map m = new Flat3Map();
        // size=3, null key in middle (slot 2)
        m.put(new Integer(1), new Integer(1));
        m.put(null, new Integer(0));
        m.put(new Integer(2), new Integer(2));
        assertEquals(new Integer(0), m.remove(null)); // buggy code would return value3 (2)
        // size=2, null key in slot 1
        m.clear();
        m.put(null, new Integer(0));
        m.put(new Integer(1), new Integer(1));
        assertEquals(new Integer(0), m.remove(null)); // buggy code would return value2 (1)
    }