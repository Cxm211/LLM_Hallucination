// org/jsoup/nodes/AttributeTest.java::nullValueAttributeReturnsEmptyString
@Test public void nullValueAttributeReturnsEmptyString() {
        Attribute a = new Attribute("hidden", null);
        assertEquals("", a.getValue());
    }