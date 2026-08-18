// com/fasterxml/jackson/databind/type/TestTypeFactory.java
public void testCanonicalNamesAdditional1()
{
    TypeFactory tf = TypeFactory.defaultInstance();
    // Test with Optional which is another ReferenceType
    JavaType t = tf.constructType(new TypeReference<java.util.Optional<Integer>>() { });
    String can = t.toCanonical();
    assertEquals("java.util.Optional<java.lang.Integer>", can);
    assertEquals(t, tf.constructFromCanonical(can));
}