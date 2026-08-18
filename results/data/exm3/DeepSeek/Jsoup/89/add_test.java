// org/jsoup/nodes/AttributeTest.java
@Test public void setValueOnOrphanAttributeWithEmptyInitialValue() {
        Attribute attr = new Attribute("key", "");
        String oldVal = attr.setValue("new");
        assertEquals("", oldVal);
        assertEquals("new", attr.getValue());
        assertEquals(null, attr.parent);
    }
