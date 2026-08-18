// com/fasterxml/jackson/databind/type/TestTypeFactory.java
public void testPropertiesInList() throws Exception
{
    TypeFactory tf = TypeFactory.defaultInstance();
    JavaType listType = tf.constructParametricType(List.class, Properties.class);
    JavaType elementType = listType.getContentType();
    // elementType should be MapType with String key/value
    assertEquals(MapType.class, elementType.getClass());
    MapType mapType = (MapType) elementType;
    assertSame(String.class, mapType.getKeyType().getRawClass());
    assertSame(String.class, mapType.getContentType().getRawClass());
}
