// com/fasterxml/jackson/databind/node/TestJsonNode.java
public void testDefaultTypingNonConcreteAndArraysWithTreeNodeArray() throws Exception {
    class Pojo {
        public JsonNode[] nodes;
        public ObjectNode[] objNodes;
    }
    ObjectMapper mapper = new ObjectMapper()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
    ObjectNode node1 = mapper.createObjectNode();
    node1.put("a", 1);
    ObjectNode node2 = mapper.createObjectNode();
    node2.put("b", 2);
    ObjectNode objNode1 = mapper.createObjectNode();
    objNode1.put("x", 10);
    ObjectNode objNode2 = mapper.createObjectNode();
    objNode2.put("y", 20);
    Pojo pojo = new Pojo();
    pojo.nodes = new JsonNode[] { node1, node2 };
    pojo.objNodes = new ObjectNode[] { objNode1, objNode2 };
    String json = mapper.writeValueAsString(pojo);
    JsonNode tree = mapper.readTree(json);
    JsonNode nodesField = tree.get("nodes");
    assertNotNull(nodesField);
    assertTrue(nodesField.isArray());
    assertEquals(2, nodesField.size());
    for (JsonNode elem : nodesField) {
        assertFalse(elem.has("@class"));
    }
    JsonNode objNodesField = tree.get("objNodes");
    assertNotNull(objNodesField);
    assertTrue(objNodesField.isArray());
    assertEquals(2, objNodesField.size());
    for (JsonNode elem : objNodesField) {
        assertFalse(elem.has("@class"));
    }
}
