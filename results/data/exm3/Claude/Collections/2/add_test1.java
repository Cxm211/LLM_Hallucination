// org/apache/commons/collections/TestExtendedProperties.java
public void testIncludeNonEmptyStringReturnsValue() {
    ExtendedProperties props = new ExtendedProperties();
    props.setInclude("customInclude");
    assertEquals("customInclude", props.getInclude());
}