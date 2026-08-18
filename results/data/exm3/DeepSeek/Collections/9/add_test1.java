// org/apache/commons/collections/TestExtendedProperties.java
public void testCombinePreservesExisting() {
        ExtendedProperties target = new ExtendedProperties();
        target.addProperty("key", "original");
        ExtendedProperties source = new ExtendedProperties();
        source.addProperty("key", "new");
        target.combine(source);
        Vector values = target.getVector("key");
        assertEquals(2, values.size());
        assertEquals("original", values.elementAt(0));
        assertEquals("new", values.elementAt(1));
    }
