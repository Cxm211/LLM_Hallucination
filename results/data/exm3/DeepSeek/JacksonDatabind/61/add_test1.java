// com/fasterxml/jackson/databind/jsontype/DefaultTypingWithPrimitivesTest.java
public void testPrimitiveArrayWithNonConcreteAndArrays() throws Exception {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("intArray", new int[]{1, 2, 3});
    ObjectMapper mapper = new ObjectMapper();
    StdTypeResolverBuilder resolver = new StdTypeResolverBuilder();
    resolver.init(JsonTypeInfo.Id.CLASS, null);
    resolver.inclusion(JsonTypeInfo.As.PROPERTY);
    resolver.typing(TypeResolverBuilder.Typing.NON_CONCRETE_AND_ARRAYS);
    resolver.typeProperty("__t");
    mapper.setDefaultTyping(resolver);
    String json = mapper.writeValueAsString(map);
    Map<?,?> result = mapper.readValue(json, Map.class);
    assertNotNull(result);
    assertEquals(1, result.size());
}
