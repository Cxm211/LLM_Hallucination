// com/fasterxml/jackson/databind/creators/TestCreatorsDelegating.java::testDelegateWithTokenBufferSingleField
public void testDelegateWithTokenBufferSingleField() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Value592 value = mapper.readValue("{\"a\":1}", Value592.class);
        assertNotNull(value);
        Object ob = value.stuff;
        assertEquals(TokenBuffer.class, ob.getClass());
        JsonParser jp = ((TokenBuffer) ob).asParser();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("a", jp.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(1, jp.getIntValue());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
    }