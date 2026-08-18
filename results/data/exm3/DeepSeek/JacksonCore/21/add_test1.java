// com/fasterxml/jackson/core/filter/TokenVerifyingParserFiltering330Test.java
public void testSingleMatchWithPathNestedArray() throws Exception
{
    String json = "{\"arr\":[{\"value\":1}, {\"other\":2}]}";
    JsonParser p0 = JSON_F.createParser(json);
    JsonParser p = new FilteringParserDelegate(p0,
           new NameMatchFilter(\"value\"),
               true, // includePath
               false // multipleMatches
            );

    assertToken(JsonToken.START_OBJECT, p.nextToken());
    assertToken(JsonToken.FIELD_NAME, p.nextToken());
    assertEquals(\"arr\", p.getCurrentName());
    assertToken(JsonToken.START_ARRAY, p.nextToken());
    assertToken(JsonToken.START_OBJECT, p.nextToken());
    assertToken(JsonToken.FIELD_NAME, p.nextToken());
    assertEquals(\"value\", p.getCurrentName());
    assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    assertEquals(1, p.getIntValue());
    assertToken(JsonToken.END_OBJECT, p.nextToken());
    assertToken(JsonToken.END_ARRAY, p.nextToken());
    assertToken(JsonToken.END_OBJECT, p.nextToken());
    assertNull(p.nextToken());
    p.close();
}
