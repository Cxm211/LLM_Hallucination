// com/fasterxml/jackson/databind/objectid/Objecid1083Test.java
public void testNarrowWithEmptyArray() throws Exception {
    final ObjectMapper mapper = new ObjectMapper();
    final String json = aposToQuotes("{'schemas': []}");
    JsonRoot result = mapper.readValue(json, JsonRoot.class);
    assertNotNull(result);
    assertNotNull(result.schemas);
    assertEquals(0, result.schemas.size());
}