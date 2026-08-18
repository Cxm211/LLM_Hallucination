// com/fasterxml/jackson/databind/objectid/Objecid1083Test.java
public void testNarrowWithSubclass() throws Exception {
    final ObjectMapper mapper = new ObjectMapper();
    final String json = aposToQuotes("{'schemas': [{'name': 'SubFoodMart'}]}");
    JsonRoot result = mapper.readValue(json, JsonRoot.class);
    assertNotNull(result);
    assertNotNull(result.schemas);
    assertEquals(1, result.schemas.size());
}