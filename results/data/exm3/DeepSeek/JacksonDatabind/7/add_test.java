// com/fasterxml/jackson/databind/creators/TestCreatorsDelegating.java
public void testDeserializeTokenBufferFromFieldName() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    JsonParser jp = mapper.getFactory().createParser("{\"a\":1,\"b\":2,\"c\":{\"d\":3}}");
    // Position parser at first FIELD_NAME
    jp.nextToken(); // START_OBJECT
    jp.nextToken(); // FIELD_NAME "a"
    TokenBuffer tb = mapper.readValue(jp, TokenBuffer.class);
    JsonParser tbParser = tb.asParser();
    assertToken(JsonToken.START_OBJECT, tbParser.nextToken());
    assertToken(JsonToken.FIELD_NAME, tbParser.nextToken());
    assertEquals("a", tbParser.getCurrentName());
    assertToken(JsonToken.VALUE_NUMBER_INT, tbParser.nextToken());
    assertEquals(1, tbParser.getIntValue());
    assertToken(JsonToken.FIELD_NAME, tbParser.nextToken());
    assertEquals("b", tbParser.getCurrentName());
    assertToken(JsonToken.VALUE_NUMBER_INT, tbParser.nextToken());
    assertEquals(2, tbParser.getIntValue());
    assertToken(JsonToken.FIELD_NAME, tbParser.nextToken());
    assertEquals("c", tbParser.getCurrentName());
    assertToken(JsonToken.START_OBJECT, tbParser.nextToken());
    assertToken(JsonToken.FIELD_NAME, tbParser.nextToken());
    assertEquals("d", tbParser.getCurrentName());
    assertToken(JsonToken.VALUE_NUMBER_INT, tbParser.nextToken());
    assertEquals(3, tbParser.getIntValue());
    assertToken(JsonToken.END_OBJECT, tbParser.nextToken());
    assertToken(JsonToken.END_OBJECT, tbParser.nextToken());
    tbParser.close();
    // Ensure original parser is at end of object
    assertNull(jp.nextToken());
    jp.close();
}
