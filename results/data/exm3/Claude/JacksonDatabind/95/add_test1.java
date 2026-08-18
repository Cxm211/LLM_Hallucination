// com/fasterxml/jackson/databind/type/TestTypeFactory.java
public void testConstructSpecializedTypeForHashMap()
{
    TypeFactory tf = TypeFactory.defaultInstance();
    // Test HashMap specialization
    JavaType baseType = tf.constructMapType(Map.class, String.class, Integer.class);
    JavaType specialized = tf.constructSpecializedType(baseType, HashMap.class);
    assertEquals(HashMap.class, specialized.getRawClass());
    assertEquals(String.class, specialized.getKeyType().getRawClass());
    assertEquals(Integer.class, specialized.getContentType().getRawClass());
}