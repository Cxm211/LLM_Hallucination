// com/fasterxml/jackson/databind/jsontype/TypeRefinementForMap1215Test.java
public void testMapRefinementWithEnumMap() throws Exception
{
    ObjectMapper m = new ObjectMapper();
    
    JavaType baseType = m.getTypeFactory().constructMapType(Map.class,
            m.getTypeFactory().constructType(String.class),
            m.getTypeFactory().constructType(String.class));
    JavaType refinedType = m.getTypeFactory().constructSpecializedType(baseType, EnumMap.class);
    
    assertNotNull(refinedType);
    assertEquals(EnumMap.class, refinedType.getRawClass());
    assertEquals(String.class, refinedType.getKeyType().getRawClass());
    assertEquals(String.class, refinedType.getContentType().getRawClass());
}