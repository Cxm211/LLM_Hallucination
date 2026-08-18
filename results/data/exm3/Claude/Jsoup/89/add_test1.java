// org/jsoup/nodes/AttributeTest.java
@Test public void setValueMultipleTimesOnOrphanAttribute() {
    Attribute attr = new Attribute("testKey", "initialValue");
    String oldVal1 = attr.setValue("secondValue");
    assertEquals("initialValue", oldVal1);
    assertEquals("secondValue", attr.getValue());
    String oldVal2 = attr.setValue("thirdValue");
    assertEquals("secondValue", oldVal2);
    assertEquals("thirdValue", attr.getValue());
}