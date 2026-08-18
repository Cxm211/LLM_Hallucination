// com/fasterxml/jackson/databind/type/RecursiveTypeTest.java
public void testMultipleConstructionOfSameRecursiveType() {
    TypeFactory tf = objectMapper().getTypeFactory();
    JavaType first = tf.constructType(Base.class);
    JavaType second = tf.constructType(Base.class);
    assertNotNull(first);
    assertNotNull(second);
    assertEquals(first, second);
}