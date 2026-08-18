// org/apache/commons/collections/TestExtendedProperties.java
public void testPutAllWithExistingKey() {
        ExtendedProperties p = new ExtendedProperties();
        p.put("a", "foo");
        Map m = new HashMap();
        m.put("a", "bar");
        m.put("b", "baz");
        p.putAll(m);
        Iterator it = p.getKeys();
        assertEquals("a", (String) it.next());
        assertEquals("b", (String) it.next());
        assertFalse(it.hasNext());
        assertEquals("bar", p.get("a"));
    }
