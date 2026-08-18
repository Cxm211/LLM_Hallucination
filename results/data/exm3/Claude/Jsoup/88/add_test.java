// org/jsoup/nodes/AttributeTest.java
@Test public void nullValueAttributesReturnEmptyString() {
    Attribute attr = new Attribute("test", null);
    assertEquals("", attr.getValue());
    assertNotNull(attr.getValue());
}