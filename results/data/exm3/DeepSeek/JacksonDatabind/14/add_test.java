// com/fasterxml/jackson/databind/convert/TestUpdateValue.java
public void testBindAsTreeWithObjectNode() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();
        // Create a simple object node
        JsonNode node = mapper.valueToTree(new DataB());
        JsonParser parser = node.traverse();
        // This calls _bindAsTree internally
        JsonNode result = reader.readTree(parser);
        assertNotNull(result);
        assertEquals(node, result);
    }
