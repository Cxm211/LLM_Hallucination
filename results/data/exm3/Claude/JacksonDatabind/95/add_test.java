// com/fasterxml/jackson/databind/type/TestTypeFactory.java
public void testConstructSpecializedTypeForEnumSet()
{
    TypeFactory tf = TypeFactory.defaultInstance();
    // Test EnumSet specialization when subclass is EnumSet itself
    JavaType baseType = tf.constructCollectionType(EnumSet.class, EnumForCanonical.class);
    JavaType specialized = tf.constructSpecializedType(baseType, EnumSet.class);
    assertEquals(baseType, specialized);
}