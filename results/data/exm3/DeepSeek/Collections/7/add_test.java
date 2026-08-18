// org/apache/commons/collections/TestExtendedProperties.java
public void testPutAllWithNonExtendedProperties() {
        ExtendedProperties p = new ExtendedProperties();
        Map m = new LinkedHashMap();
        m.put("x", "value1");
        m.put("y", "value2");
        p.putAll(m);
        Iterator it = p.getKeys();
        assertEquals("x", (String) it.next());
        assertEquals("y", (String) it.next());
        assertFalse(it.hasNext());
    }
