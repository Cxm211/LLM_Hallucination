// com/fasterxml/jackson/databind/type/TestTypeFactory.java
public void testConstructSpecializedTypeForArrayList()
{
    TypeFactory tf = TypeFactory.defaultInstance();
    // Test ArrayList specialization
    JavaType baseType = tf.constructCollectionType(Collection.class, String.class);
    JavaType specialized = tf.constructSpecializedType(baseType, ArrayList.class);
    assertEquals(ArrayList.class, specialized.getRawClass());
    assertEquals(String.class, specialized.getContentType().getRawClass());
}