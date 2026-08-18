// org/jsoup/nodes/AttributeTest.java
@Test public void setValueOnOrphanAttributeSettingEmpty() {
        Attribute attr = new Attribute("key", "old");
        String oldVal = attr.setValue("");
        assertEquals("old", oldVal);
        assertEquals("", attr.getValue());
        assertEquals(null, attr.parent);
    }
