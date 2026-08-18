// org/apache/commons/collections/TestExtendedProperties.java
public void testCombineWithMultipleValues() {
    ExtendedProperties props1 = new ExtendedProperties();
    props1.setProperty("list.item", "value1");
    props1.addProperty("list.item", "value2");
    
    ExtendedProperties props2 = new ExtendedProperties();
    props2.combine(props1);
    
    assertEquals("value1", props2.getString("list.item"));
    Object value = props2.get("list.item");
    assertTrue(value instanceof java.util.Vector || value instanceof java.util.List);
}