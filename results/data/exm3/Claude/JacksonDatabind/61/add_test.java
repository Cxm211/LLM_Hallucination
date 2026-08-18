// com/fasterxml/jackson/databind/jsontype/DefaultTypingWithPrimitivesTest.java
public void testDefaultTypingWithInt() throws Exception
{
    Map<String, Object> mapData = new HashMap<String, Object>();
    mapData.put("intValue", 42);

    ObjectMapper mapper = new ObjectMapper();
    StdTypeResolverBuilder resolver = new StdTypeResolverBuilder();
    resolver.init(JsonTypeInfo.Id.CLASS, null);
    resolver.inclusion(JsonTypeInfo.As.PROPERTY);
    resolver.typeProperty("__t");
    mapper.setDefaultTyping(resolver);

    String json = mapper.writeValueAsString(mapData);
    Map<?,?> result = mapper.readValue(json, Map.class);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(42, result.get("intValue"));
}