// com/fasterxml/jackson/databind/deser/TestEnumDeserialization.java
public void testEnumWithNonBundleAnnotation() throws Exception {
    // Test enum with a standard annotation that is not a bundle
    @SuppressWarnings("deprecation")
    class TestClass {
        @Deprecated
        public void method() {}
    }
    try {
        java.lang.reflect.Method method = TestClass.class.getMethod("method");
        Deprecated deprecated = method.getAnnotation(Deprecated.class);
        com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector introspector = new com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector();
        boolean result = introspector.isAnnotationBundle(deprecated);
        assertFalse(result);
    } catch (NoSuchMethodException e) {
        fail("Method not found");
    }
}