// com/fasterxml/jackson/databind/node/EmptyContentAsTreeTest.java
public void testNullNodeFromExplicitNullWithParser() throws Exception
{
    String jsonNull = "null";
    try (JsonParser p = MAPPER.getFactory().createParser(jsonNull)) {
        JsonNode node = MAPPER.reader().readTree(p);
        assertTrue("Expected null node", node.isNull());
    }
    try (JsonParser p = MAPPER.getFactory().createParser(new StringReader(jsonNull))) {
        JsonNode node = MAPPER.reader().readTree(p);
        assertTrue("Expected null node", node.isNull());
    }
    try (JsonParser p = MAPPER.getFactory().createParser(jsonNull.getBytes())) {
        JsonNode node = MAPPER.reader().readTree(p);
        assertTrue("Expected null node", node.isNull());
    }
}