// com/fasterxml/jackson/core/json/async/AsyncLocationTest.java::testLocationOffsets2
public void testLocationOffsets2() throws Exception
    {
        JsonParser parser = DEFAULT_F.createNonBlockingByteArrayParser();
        ByteArrayFeeder feeder = (ByteArrayFeeder) parser.getNonBlockingInputFeeder();

        byte[] input = utf8Bytes("[[[");

        // Feed two bytes from non-zero start
        feeder.feedInput(input, 1, 3);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(1, parser.getCurrentLocation().getByteOffset());
        assertEquals(1, parser.getTokenLocation().getByteOffset());
        assertEquals(1, parser.getCurrentLocation().getLineNr());
        assertEquals(1, parser.getTokenLocation().getLineNr());
        assertEquals(2, parser.getCurrentLocation().getColumnNr());
        assertEquals(1, parser.getTokenLocation().getColumnNr());

        // Second token from same chunk
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(2, parser.getCurrentLocation().getByteOffset());
        assertEquals(2, parser.getTokenLocation().getByteOffset());
        assertEquals(1, parser.getCurrentLocation().getLineNr());
        assertEquals(1, parser.getTokenLocation().getLineNr());
        assertEquals(3, parser.getCurrentLocation().getColumnNr());
        assertEquals(2, parser.getTokenLocation().getColumnNr());

        // Feed remaining first byte
        feeder.feedInput(input, 0, 1);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(3, parser.getCurrentLocation().getByteOffset());
        assertEquals(3, parser.getTokenLocation().getByteOffset());
        assertEquals(1, parser.getCurrentLocation().getLineNr());
        assertEquals(1, parser.getTokenLocation().getLineNr());
        assertEquals(4, parser.getCurrentLocation().getColumnNr());
        assertEquals(3, parser.getTokenLocation().getColumnNr());

        parser.close();
    }