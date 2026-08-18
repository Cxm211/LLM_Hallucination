// com/fasterxml/jackson/databind/node/TestConversions.java
public void testWriteTreeNode() throws Exception {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        JsonNodeFactory nf = MAPPER.getNodeFactory();
        TextNode textNode = nf.textNode("hello");
        buffer.writeTree(textNode);
        JsonParser parser = buffer.asParser();
        try {
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("hello", parser.getText());
        } finally {
            parser.close();
        }
        buffer.close();
    }
