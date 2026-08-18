// org/jsoup/nodes/AttributeTest.java
@Test public void setValueOnOrphanAttributeWithNullPrevious() {
        Attribute attr = new Attribute("one", null);
        String oldVal = attr.setValue("four");
        assertEquals("", oldVal);
        assertEquals("four", attr.getValue());
        assertEquals(null, attr.parent);
    }