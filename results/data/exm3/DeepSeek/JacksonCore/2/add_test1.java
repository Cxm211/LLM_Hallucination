// com/fasterxml/jackson/core/json/TestParserErrorHandling.java
public void testLeadingZeroFractionChars() throws Exception {
    JsonParser p = createParserUsingReader("0.1");
    assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
    assertEquals(0.1, p.getDoubleValue());
    p.close();
}
