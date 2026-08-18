// com/fasterxml/jackson/core/json/LocationInObjectTest.java
public void testOffsetWithNestedArrays() throws Exception
{
    final JsonFactory f = new JsonFactory();
    char[] c = "[[1,2],[3,4]]".toCharArray();
    JsonParser p = f.createParser(c);

    assertEquals(JsonToken.START_ARRAY, p.nextToken());
    assertEquals(0L, p.getTokenLocation().getCharOffset());

    assertEquals(JsonToken.START_ARRAY, p.nextToken());
    assertEquals(1L, p.getTokenLocation().getCharOffset());

    assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    assertEquals(2L, p.getTokenLocation().getCharOffset());

    assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    assertEquals(4L, p.getTokenLocation().getCharOffset());

    assertEquals(JsonToken.END_ARRAY, p.nextToken());

    assertEquals(JsonToken.START_ARRAY, p.nextToken());
    assertEquals(7L, p.getTokenLocation().getCharOffset());

    assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    assertEquals(8L, p.getTokenLocation().getCharOffset());

    assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    assertEquals(10L, p.getTokenLocation().getCharOffset());

    assertEquals(JsonToken.END_ARRAY, p.nextToken());
    assertEquals(JsonToken.END_ARRAY, p.nextToken());

    p.close();
}