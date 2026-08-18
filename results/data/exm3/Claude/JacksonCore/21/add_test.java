// com/fasterxml/jackson/core/filter/TokenVerifyingParserFiltering330Test.java
public void testNestedStructEndAfterMatch() throws Exception
{
    // Test nested structure: {"outer":{"inner":{"value":42}}}
    String json = "{\"outer\":{\"inner\":{\"value\":42}}}";
    JsonParser p0 = JSON_F.createParser(json);
    JsonParser p = new FilteringParserDelegate(p0,
           new NameMatchFilter("value"),
               true, // includePath
               false // multipleMatches
            );

    // Should emit: {"outer":{"inner":{"value":42}}}
    assertToken(JsonToken.START_OBJECT, p.nextToken());
    assertToken(JsonToken.FIELD_NAME, p.nextToken());
    assertEquals("outer", p.getCurrentName());
    assertToken(JsonToken.START_OBJECT, p.nextToken());
    assertToken(JsonToken.FIELD_NAME, p.nextToken());
    assertEquals("inner", p.getCurrentName());
    assertToken(JsonToken.START_OBJECT, p.nextToken());
    assertToken(JsonToken.FIELD_NAME, p.nextToken());
    assertEquals("value", p.getCurrentName());
    assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    assertEquals(42, p.getIntValue());
    assertToken(JsonToken.END_OBJECT, p.nextToken());
    assertToken(JsonToken.END_OBJECT, p.nextToken());
    assertToken(JsonToken.END_OBJECT, p.nextToken());
    assertNull(p.nextToken());
    
    p.close();
}