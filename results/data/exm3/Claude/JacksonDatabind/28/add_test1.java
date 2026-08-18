// com/fasterxml/jackson/databind/node/TestObjectNode.java
public void testEmptyObjectNestedInArray() throws Exception
{
    String json = "[{}]";
    JsonNode[] nodes = MAPPER.readValue(json, JsonNode[].class);
    assertNotNull(nodes);
    assertEquals(1, nodes.length);
    assertTrue(nodes[0].isObject());
    assertEquals(0, nodes[0].size());
}