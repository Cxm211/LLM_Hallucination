// com/fasterxml/jackson/databind/creators/TestCreatorsDelegating.java
public void testDelegateWithTokenBufferStartArray() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    Value592 value = mapper.readValue("[1,2,3]", Value592.class);
    assertNotNull(value);
    Object ob = value.stuff;
    assertEquals(TokenBuffer.class, ob.getClass());
    JsonParser jp = ((TokenBuffer) ob).asParser();
    assertToken(JsonToken.START_ARRAY, jp.nextToken());
    assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
    assertEquals(1, jp.getIntValue());
    assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
    assertEquals(2, jp.getIntValue());
    assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
    assertEquals(3, jp.getIntValue());
    assertToken(JsonToken.END_ARRAY, jp.nextToken());
    jp.close();
}