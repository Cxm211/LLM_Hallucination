// com/fasterxml/jackson/core/json/TestParserErrorHandling.java
public void testZeroAlone() throws Exception {
    String json = "[0]";
    JsonParser p = createParserUsingReader(json);
    assertToken(JsonToken.START_ARRAY, p.nextToken());
    assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    assertEquals(0, p.getIntValue());
    assertToken(JsonToken.END_ARRAY, p.nextToken());
    p.close();

    p = createParserUsingStream(json);
    assertToken(JsonToken.START_ARRAY, p.nextToken());
    assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    assertEquals(0, p.getIntValue());
    assertToken(JsonToken.END_ARRAY, p.nextToken());
    p.close();
}