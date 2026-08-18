// com/fasterxml/jackson/databind/type/TestTypeFactory.java
public void testEnumMapWithGenericValue() {
    TypeFactory tf = TypeFactory.defaultInstance();
    JavaType keyType = tf.constructType(EnumForCanonical.class);
    JavaType valueType = tf.constructParametricType(List.class, String.class);
    JavaType mapType = tf.constructParametricType(Map.class, keyType, valueType);
    JavaType specialized = tf.constructSpecializedType(mapType, EnumMap.class);
    assertEquals(EnumMap.class, specialized.getRawClass());
    assertEquals(EnumForCanonical.class, specialized.getKeyType().getRawClass());
    JavaType vt = specialized.getContentType();
    assertEquals(List.class, vt.getRawClass());
    assertEquals(String.class, vt.getContentType().getRawClass());
}
