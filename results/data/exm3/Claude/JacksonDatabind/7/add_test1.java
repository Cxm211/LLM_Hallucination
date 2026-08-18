// com/fasterxml/jackson/databind/creators/TestCreatorsDelegating.java
public void testDelegateWithTokenBufferEmptyObject() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    Value592 value = mapper.readValue("{}", Value592.class);
    assertNotNull(value);
    Object ob = value.stuff;
    assertEquals(TokenBuffer.class, ob.getClass());
    JsonParser jp = ((TokenBuffer) ob).asParser();
    assertToken(JsonToken.START_OBJECT, jp.nextToken());
    assertToken(JsonToken.END_OBJECT, jp.nextToken());
    jp.close();
}