// com/fasterxml/jackson/core/json/async/AsyncLocationTest.java
public void testLocationNonContiguousWithNewline() throws Exception
    {
        JsonParser parser = DEFAULT_F.createNonBlockingByteArrayParser();
        ByteArrayFeeder feeder = (ByteArrayFeeder) parser.getNonBlockingInputFeeder();

        byte[] input = utf8Bytes("a\nb");

        feeder.feedInput(input, 0, 2); // 'a' and '\n'
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(1, parser.getCurrentLocation().getByteOffset());
        assertEquals(1, parser.getTokenLocation().getByteOffset());
        assertEquals(1, parser.getCurrentLocation().getColumnNr());
        assertEquals(1, parser.getTokenLocation().getColumnNr());

        feeder.feedInput(input, 2, 3); // 'b'
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(3, parser.getCurrentLocation().getByteOffset());
        assertEquals(3, parser.getTokenLocation().getByteOffset());
        assertEquals(1, parser.getCurrentLocation().getColumnNr());
        assertEquals(1, parser.getTokenLocation().getColumnNr());
        parser.close();
    }
