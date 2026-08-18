// com/fasterxml/jackson/databind/node/TestConversions.java
public void testWriteSimpleObject() throws Exception {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeObject(Integer.valueOf(42));
        JsonParser parser = buffer.asParser();
        try {
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(42, parser.getIntValue());
        } finally {
            parser.close();
        }
        buffer.close();
    }
