// com/fasterxml/jackson/databind/interop/DeprecatedTypeHandling1102Test.java
public void testDeprecatedTypeResolutionWithPrimitives() throws Exception
{
    TypeFactory tf = MAPPER.getTypeFactory();

    // Test with primitive type and null context
    JavaType t1 = tf.constructType(int.class, (Class<?>) null);
    assertEquals(int.class, t1.getRawClass());

    // Test with array type and null context
    JavaType t2 = tf.constructType(String[].class, (Class<?>) null);
    assertEquals(String[].class, t2.getRawClass());

    // Test with void type and null context
    JavaType t3 = tf.constructType(void.class, (Class<?>) null);
    assertEquals(void.class, t3.getRawClass());
}