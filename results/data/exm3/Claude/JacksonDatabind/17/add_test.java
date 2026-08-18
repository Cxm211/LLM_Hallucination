// com/fasterxml/jackson/databind/node/TestJsonNode.java
public void testNestedArrayWithDefaultTyping() throws Exception
{
    ObjectMapper mapper = new ObjectMapper()
        .enableDefaultTyping();

    JsonNode nested = mapper.readTree("[[1, 2], [3, 4]]");
    assertTrue(nested.isArray());
    assertEquals(2, nested.size());
    assertTrue(nested.get(0).isArray());
    assertEquals(2, nested.get(0).size());
}