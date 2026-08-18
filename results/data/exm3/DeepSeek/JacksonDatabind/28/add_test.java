// com/fasterxml/jackson/databind/node/TestObjectNode.java
public void testDeserializeWithEndObject() throws Exception {
    JsonParser parser = MAPPER.getFactory().createParser("{}");
    parser.nextToken(); // START_OBJECT
    parser.nextToken(); // END_OBJECT
    ObjectNode node = MAPPER.readValue(parser, ObjectNode.class);
    assertNotNull(node);
    assertEquals(0, node.size());
}
