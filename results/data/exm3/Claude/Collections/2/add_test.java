// org/apache/commons/collections/TestExtendedProperties.java
public void testIncludeEmptyStringReturnsNull() {
    ExtendedProperties props = new ExtendedProperties();
    props.setInclude("");
    assertNull(props.getInclude());
}