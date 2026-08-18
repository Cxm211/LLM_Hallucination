// com/fasterxml/jackson/core/json/async/AsyncLocationTest.java
public void testLocationOffsetsMultipleFeedsLineNumber() throws Exception
{
    JsonParser parser = DEFAULT_F.createNonBlockingByteArrayParser();
    ByteArrayFeeder feeder = (ByteArrayFeeder) parser.getNonBlockingInputFeeder();

    byte[] input1 = utf8Bytes("[");
    byte[] input2 = utf8Bytes("\n]");

    feeder.feedInput(input1, 0, 1);
    assertEquals(JsonToken.START_ARRAY, parser.nextToken());
    assertEquals(1, parser.getCurrentLocation().getLineNr());

    feeder.feedInput(input2, 0, 2);
    assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    assertEquals(2, parser.getCurrentLocation().getLineNr());
    assertEquals(2, parser.getTokenLocation().getLineNr());
    parser.close();
}