// com/fasterxml/jackson/databind/type/RecursiveTypeTest.java
public void testRecursiveGenericType() throws Exception {
    // Local class with self-referential generic
    class SelfRef<T extends SelfRef<T>> {
        public T child;
    }
    TypeFactory tf = objectMapper().getTypeFactory();
    // This should not throw IllegalStateException
    JavaType type = tf.constructType(SelfRef.class);
    assertNotNull(type);
    // Additional check: the generic bound should be resolved
    JavaType bound = type.containedType(0);
    assertNotNull(bound);
}
