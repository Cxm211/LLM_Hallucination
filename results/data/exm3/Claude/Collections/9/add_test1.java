// org/apache/commons/collections/TestExtendedProperties.java
public void testCombineOverwritesExistingProperties() {
    ExtendedProperties props1 = new ExtendedProperties();
    props1.setProperty("test", "\\\\path\\one");
    
    ExtendedProperties props2 = new ExtendedProperties();
    props2.setProperty("test", "oldValue");
    props2.setProperty("other", "keepThis");
    props2.combine(props1);
    
    assertEquals("\\\\path\\one", props2.getProperty("test"));
    assertEquals("keepThis", props2.getProperty("other"));
}