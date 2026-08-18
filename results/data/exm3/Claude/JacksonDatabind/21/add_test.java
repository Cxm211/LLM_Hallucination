// com/fasterxml/jackson/databind/deser/TestEnumDeserialization.java
public void testEnumWithNullAnnotation() throws Exception {
    // Test that isAnnotationBundle handles null annotation gracefully
    com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector introspector = new com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector();
    boolean result = introspector.isAnnotationBundle(null);
    assertFalse(result);
}