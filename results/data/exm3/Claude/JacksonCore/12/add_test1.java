// com/fasterxml/jackson/core/json/LocationInObjectTest.java
public void testOffsetWithMixedContent() throws Exception
{
    final JsonFactory f = new JsonFactory();
    char[] c = "{\"a\":true,\"b\":null}".toCharArray();
    JsonParser p = f.createParser(c);

    assertEquals(JsonToken.START_OBJECT, p.nextToken());

    assertEquals(JsonToken.FIELD_NAME, p.nextToken());
    assertEquals(1L, p.getTokenLocation().getCharOffset());

    assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
    assertEquals(6L, p.getTokenLocation().getCharOffset());

    assertEquals(JsonToken.FIELD_NAME, p.nextToken());
    assertEquals(11L, p.getTokenLocation().getCharOffset());

    assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    assertEquals(16L, p.getTokenLocation().getCharOffset());

    assertEquals(JsonToken.END_OBJECT, p.nextToken());

    p.close();
}