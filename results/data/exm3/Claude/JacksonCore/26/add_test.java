// com/fasterxml/jackson/core/json/async/AsyncLocationTest.java
public void testLocationOffsetsWithEmptyFirstChunk() throws Exception
{
    JsonParser parser = DEFAULT_F.createNonBlockingByteArrayParser();
    ByteArrayFeeder feeder = (ByteArrayFeeder) parser.getNonBlockingInputFeeder();

    byte[] input = utf8Bytes("[{}");

    feeder.feedInput(input, 1, 1);
    assertNull(parser.nextToken());

    feeder.feedInput(input, 0, 2);
    assertEquals(JsonToken.START_ARRAY, parser.nextToken());
    assertEquals(1, parser.getCurrentLocation().getByteOffset());
    assertEquals(1, parser.getTokenLocation().getByteOffset());
    assertEquals(1, parser.getCurrentLocation().getLineNr());
    assertEquals(1, parser.getTokenLocation().getLineNr());
    assertEquals(2, parser.getCurrentLocation().getColumnNr());
    assertEquals(1, parser.getTokenLocation().getColumnNr());
    parser.close();
}