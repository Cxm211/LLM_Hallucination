// com/fasterxml/jackson/databind/type/TestTypeFactory.java
public void testPropertiesAsMapValue() throws Exception
{
    TypeFactory tf = TypeFactory.defaultInstance();
    JavaType mapType = tf.constructParametricType(Map.class, String.class, Properties.class);
    // value type should be Properties with String key/value
    JavaType valueType = mapType.getContentType();
    assertEquals(MapType.class, valueType.getClass());
    MapType propsType = (MapType) valueType;
    assertSame(String.class, propsType.getKeyType().getRawClass());
    assertSame(String.class, propsType.getContentType().getRawClass());
}
