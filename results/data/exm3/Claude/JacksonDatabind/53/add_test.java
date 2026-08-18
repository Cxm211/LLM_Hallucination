// com/fasterxml/jackson/databind/jsontype/TypeRefinementForMap1215Test.java
public void testMapRefinementWithTreeMap() throws Exception
{
    String ID1 = "test-id-1";
    String ID2 = "test-id-2";
    String json = aposToQuotes(
"{'id':'" + ID1 + "','items':{'" + ID2 + "':{'id':'" + ID2 + "','property':'value'}}}");

    ObjectMapper m = new ObjectMapper();
    
    JavaType baseType = m.getTypeFactory().constructMapType(Map.class,
            m.getTypeFactory().constructType(String.class),
            m.getTypeFactory().constructType(Item.class));
    JavaType refinedType = m.getTypeFactory().constructSpecializedType(baseType, TreeMap.class);
    
    assertNotNull(refinedType);
    assertEquals(TreeMap.class, refinedType.getRawClass());
    assertEquals(String.class, refinedType.getKeyType().getRawClass());
    assertEquals(Item.class, refinedType.getContentType().getRawClass());
}