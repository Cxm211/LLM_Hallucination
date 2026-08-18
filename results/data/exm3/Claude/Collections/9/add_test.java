// org/apache/commons/collections/TestExtendedProperties.java
public void testCombineWithMultipleProperties() {
    ExtendedProperties props1 = new ExtendedProperties();
    props1.setProperty("key1", "\\\\server\\share");
    props1.setProperty("key2", "normalValue");
    props1.setProperty("key3", "\\t\\n\\r");
    
    ExtendedProperties props2 = new ExtendedProperties();
    props2.combine(props1);
    
    assertEquals("\\\\server\\share", props2.getProperty("key1"));
    assertEquals("normalValue", props2.getProperty("key2"));
    assertEquals("\\t\\n\\r", props2.getProperty("key3"));
}