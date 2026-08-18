// com/fasterxml/jackson/core/json/TestParserErrorHandling.java
public void testNegativeLeadingZero() throws Exception {
    String json = "[-01]";
    JsonParser p = createParserUsingReader(json);
    assertToken(JsonToken.START_ARRAY, p.nextToken());
    try {
        p.nextToken();
        fail("Expected an exception for leading zero after minus");
    } catch (JsonParseException e) {
        verifyException(e, "leading zero");
    }
    p.close();

    p = createParserUsingStream(json);
    assertToken(JsonToken.START_ARRAY, p.nextToken());
    try {
        p.nextToken();
        fail("Expected an exception for leading zero after minus");
    } catch (JsonParseException e) {
        verifyException(e, "leading zero");
    }
    p.close();
}