// com/fasterxml/jackson/databind/node/TestConversions.java
public void testConversionOfList() throws Exception
    {
        java.util.List<Integer> input = java.util.Arrays.asList(1, 2);
        JsonNode tree = MAPPER.valueToTree(input);
        assertTrue("Expected Array, got "+tree.getNodeType(), tree.isArray());
        assertEquals("[1,2]", MAPPER.writeValueAsString(tree));
    }