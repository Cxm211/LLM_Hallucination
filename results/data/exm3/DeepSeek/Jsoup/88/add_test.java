// org/jsoup/nodes/AttributeTest.java
@Test public void attributeWithNullValueReturnsEmptyString() {
        Attribute attr = new Attribute("key", null);
        assertEquals("", attr.getValue());
    }
