// org/jsoup/nodes/AttributeTest.java
@Test public void setValueOnAttributeWithParent() {
    Attributes attrs = new Attributes();
    attrs.put("key1", "value1");
    Attribute attr = null;
    for (Attribute a : attrs) {
        if (a.getKey().equals("key1")) {
            attr = a;
            break;
        }
    }
    assertNotNull(attr);
    String oldVal = attr.setValue("newValue");
    assertEquals("value1", oldVal);
    assertEquals("newValue", attr.getValue());
    assertEquals("newValue", attrs.get("key1"));
}