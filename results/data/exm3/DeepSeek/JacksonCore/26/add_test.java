// com/fasterxml/jackson/core/json/async/AsyncLocationTest.java
public void testLocationContiguousFeed() throws Exception
    {
        JsonParser parser = DEFAULT_F.createNonBlockingByteArrayParser();
        ByteArrayFeeder feeder = (ByteArrayFeeder) parser.getNonBlockingInputFeeder();

        byte[] input = utf8Bytes("[]");

        feeder.feedInput(input, 0, 1);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(1, parser.getCurrentLocation().getByteOffset());
        assertEquals(1, parser.getTokenLocation().getByteOffset());
        assertEquals(1, parser.getCurrentLocation().getColumnNr());
        assertEquals(1, parser.getTokenLocation().getColumnNr());

        feeder.feedInput(input, 1, 2);
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(2, parser.getCurrentLocation().getByteOffset());
        assertEquals(2, parser.getTokenLocation().getByteOffset());
        assertEquals(2, parser.getCurrentLocation().getColumnNr());
        assertEquals(2, parser.getTokenLocation().getColumnNr());
        parser.close();
    }
