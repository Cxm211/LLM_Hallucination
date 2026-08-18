// org/jsoup/nodes/AttributeTest.java
@Test public void explicitEmptyStringValuePreserved() {
    Attribute attr = new Attribute("test", "");
    assertEquals("", attr.getValue());
    assertNotNull(attr.getValue());
}