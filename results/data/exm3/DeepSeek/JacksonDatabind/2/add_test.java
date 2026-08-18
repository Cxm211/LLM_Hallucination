// com/fasterxml/jackson/databind/node/TestConversions.java
public void testWriteNull() throws Exception {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeObject(null);
        JsonParser parser = buffer.asParser();
        try {
            assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        } finally {
            parser.close();
        }
        buffer.close();
        
        TokenBuffer buffer2 = new TokenBuffer(MAPPER, false);
        buffer2.writeTree(null);
        JsonParser parser2 = buffer2.asParser();
        try {
            assertEquals(JsonToken.VALUE_NULL, parser2.nextToken());
        } finally {
            parser2.close();
        }
        buffer2.close();
    }
