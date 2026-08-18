// com/fasterxml/jackson/databind/introspect/TestNamingStrategyStd.java
public void testSetAllWithNullKey() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        Map<String, JsonNode> map = new HashMap<>();
        map.put(null, mapper.nullNode());
        node.setAll(map);
        assertEquals(0, node.size());
    }
