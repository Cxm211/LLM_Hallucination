// org/apache/commons/collections/TestExtendedProperties.java
public void testCombineEmptyProperties() {
    ExtendedProperties props1 = new ExtendedProperties();
    
    ExtendedProperties props2 = new ExtendedProperties();
    props2.setProperty("existing", "value");
    props2.combine(props1);
    
    assertEquals("value", props2.getProperty("existing"));
    assertNull(props2.getProperty("nonexistent"));
}