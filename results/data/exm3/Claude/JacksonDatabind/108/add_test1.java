// com/fasterxml/jackson/databind/node/EmptyContentAsTreeTest.java
public void testNonEmptyTreeWithParser() throws Exception
{
    String json = "{\"key\":\"value\"}";
    try (JsonParser p = MAPPER.getFactory().createParser(json)) {
        JsonNode node = MAPPER.reader().readTree(p);
        assertNotNull("Node should not be null", node);
        assertTrue("Expected object node", node.isObject());
        assertEquals("value", node.get("key").asText());
    }
    String jsonArray = "[1,2,3]";
    try (JsonParser p = MAPPER.getFactory().createParser(jsonArray)) {
        JsonNode node = MAPPER.reader().readTree(p);
        assertNotNull("Node should not be null", node);
        assertTrue("Expected array node", node.isArray());
        assertEquals(3, node.size());
    }
}