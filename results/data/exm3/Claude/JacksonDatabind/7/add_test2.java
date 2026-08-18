// com/fasterxml/jackson/databind/creators/TestCreatorsDelegating.java
public void testDelegateWithTokenBufferNestedObject() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    Value592 value = mapper.readValue("{\"outer\":{\"inner\":123}}", Value592.class);
    assertNotNull(value);
    Object ob = value.stuff;
    assertEquals(TokenBuffer.class, ob.getClass());
    JsonParser jp = ((TokenBuffer) ob).asParser();
    assertToken(JsonToken.START_OBJECT, jp.nextToken());
    assertToken(JsonToken.FIELD_NAME, jp.nextToken());
    assertEquals("outer", jp.getCurrentName());
    assertToken(JsonToken.START_OBJECT, jp.nextToken());
    assertToken(JsonToken.FIELD_NAME, jp.nextToken());
    assertEquals("inner", jp.getCurrentName());
    assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
    assertEquals(123, jp.getIntValue());
    assertToken(JsonToken.END_OBJECT, jp.nextToken());
    assertToken(JsonToken.END_OBJECT, jp.nextToken());
    jp.close();
}