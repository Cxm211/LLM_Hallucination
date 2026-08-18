// org/apache/commons/collections/TestExtendedProperties.java
public void testKeySet6() {
        ExtendedProperties q = new ExtendedProperties();
        q.addProperty("a", "foo");
        q.addProperty("b", "bar");

        ExtendedProperties p = new ExtendedProperties();
        p.addProperty("a", "existing");
        p.putAll(q);

        Iterator it = p.getKeys();
        assertEquals("a", (String) it.next());
        assertEquals("b", (String) it.next());
        assertFalse(it.hasNext());
    }