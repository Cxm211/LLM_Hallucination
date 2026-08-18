// com/fasterxml/jackson/databind/jsontype/DefaultTypingWithPrimitivesTest.java
public void testDefaultTypingWithDouble() throws Exception
{
    Map<String, Object> mapData = new HashMap<String, Object>();
    mapData.put("doubleValue", 3.14);

    ObjectMapper mapper = new ObjectMapper();
    StdTypeResolverBuilder resolver = new StdTypeResolverBuilder();
    resolver.init(JsonTypeInfo.Id.CLASS, null);
    resolver.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
    mapper.setDefaultTyping(resolver);

    String json = mapper.writeValueAsString(mapData);
    Map<?,?> result = mapper.readValue(json, Map.class);
    assertNotNull(result);
    assertEquals(1, result.size());
}