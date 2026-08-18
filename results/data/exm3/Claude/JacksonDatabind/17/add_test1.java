// com/fasterxml/jackson/databind/node/TestJsonNode.java
public void testObjectNodeWithDefaultTyping() throws Exception
{
    ObjectMapper mapper = new ObjectMapper()
        .enableDefaultTyping();

    JsonNode node = mapper.readTree("{\"nested\": {\"value\": 42}}");
    assertTrue(node.isObject());
    assertTrue(node.get("nested").isObject());
    assertEquals(42, node.get("nested").get("value").asInt());
}