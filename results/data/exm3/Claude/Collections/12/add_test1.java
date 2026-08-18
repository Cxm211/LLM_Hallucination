// org/apache/commons/collections/TestExtendedProperties.java
public void testCombineWithSpecialCharacters() {
    ExtendedProperties props1 = new ExtendedProperties();
    props1.setProperty("path", "C:\\\\Program Files\\\\App");
    
    ExtendedProperties props2 = new ExtendedProperties();
    props2.combine(props1);
    
    assertEquals("C:\\Program Files\\App", props2.getProperty("path"));
}