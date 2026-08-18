// org/apache/commons/collections/TestExtendedProperties.java
public void testCombineEmpty() {
    ExtendedProperties props1 = new ExtendedProperties();
    props1.setProperty("key1", "value1");
    
    ExtendedProperties props2 = new ExtendedProperties();
    props2.combine(new ExtendedProperties());
    
    assertNull(props2.getProperty("key1"));
    
    props2.combine(props1);
    assertEquals("value1", props2.getProperty("key1"));
}