// org/jsoup/nodes/AttributeTest.java
@Test public void nonEmptyValuePreserved() {
    Attribute attr = new Attribute("test", "value");
    assertEquals("value", attr.getValue());
}