// com/fasterxml/jackson/databind/interop/DeprecatedTypeHandling1102Test.java
public void testDeprecatedTypeResolutionWithGenerics() throws Exception
{
    TypeFactory tf = MAPPER.getTypeFactory();

    // Test with parameterized type and null context
    JavaType listType = tf.constructParametricType(java.util.List.class, String.class);
    JavaType t1 = tf.constructType(listType, (Class<?>) null);
    assertEquals(java.util.List.class, t1.getRawClass());

    // Test with map type and null context
    JavaType mapType = tf.constructParametricType(java.util.Map.class, String.class, Integer.class);
    JavaType t2 = tf.constructType(mapType, (Class<?>) null);
    assertEquals(java.util.Map.class, t2.getRawClass());
}