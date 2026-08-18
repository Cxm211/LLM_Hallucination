// com/fasterxml/jackson/databind/convert/TestUpdateValue.java
public void testBindAsTreeWithNullNode() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();
        JsonNode nullNode = mapper.getNodeFactory().nullNode();
        JsonParser parser = nullNode.traverse();
        // This calls _bindAsTree, should handle VALUE_NULL
        JsonNode result = reader.readTree(parser);
        assertNotNull(result);
        assertTrue(result.isNull());
    }
