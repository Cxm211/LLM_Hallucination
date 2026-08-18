// com/fasterxml/jackson/databind/jsontype/DefaultTypingWithPrimitivesTest.java
public void testPrimitiveBooleanWithObjectAndNonConcrete() throws Exception {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("boolValue", true);
    ObjectMapper mapper = new ObjectMapper();
    StdTypeResolverBuilder resolver = new StdTypeResolverBuilder();
    resolver.init(JsonTypeInfo.Id.CLASS, null);
    resolver.inclusion(JsonTypeInfo.As.PROPERTY);
    resolver.typing(TypeResolverBuilder.Typing.OBJECT_AND_NON_CONCRETE);
    resolver.typeProperty("__t");
    mapper.setDefaultTyping(resolver);
    String json = mapper.writeValueAsString(map);
    Map<?,?> result = mapper.readValue(json, Map.class);
    assertNotNull(result);
    assertEquals(1, result.size());
}
