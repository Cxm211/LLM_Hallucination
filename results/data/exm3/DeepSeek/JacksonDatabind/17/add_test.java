// com/fasterxml/jackson/databind/node/TestJsonNode.java
public void testDefaultTypingWithTreeNode() throws Exception {
    class Pojo {
        public JsonNode node;
        public ObjectNode objNode;
    }
    ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();
    ObjectNode node = mapper.createObjectNode();
    node.put("value", 42);
    ObjectNode objNode = mapper.createObjectNode();
    objNode.put("field", "test");
    Pojo pojo = new Pojo();
    pojo.node = node;
    pojo.objNode = objNode;
    String json = mapper.writeValueAsString(pojo);
    JsonNode tree = mapper.readTree(json);
    JsonNode nodeField = tree.get("node");
    assertNotNull(nodeField);
    assertFalse(nodeField.has("@class"));
    assertEquals(42, nodeField.get("value").asInt());
    JsonNode objNodeField = tree.get("objNode");
    assertNotNull(objNodeField);
    assertFalse(objNodeField.has("@class"));
    assertEquals("test", objNodeField.get("field").asText());
}
