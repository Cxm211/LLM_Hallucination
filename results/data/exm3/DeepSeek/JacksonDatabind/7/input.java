// buggy function
    public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
            copyCurrentStructure(jp);
        /* 28-Oct-2014, tatu: As per #592, need to support a special case of starting from
         *    FIELD_NAME, which is taken to mean that we are missing START_OBJECT, but need
         *    to assume one did exist.
         */
        return this;
    }

// trigger testcase
// com/fasterxml/jackson/databind/creators/TestCreatorsDelegating.java::testDelegateWithTokenBuffer
public void testDelegateWithTokenBuffer() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Value592 value = mapper.readValue("{\"a\":1,\"b\":2}", Value592.class);
        assertNotNull(value);
        Object ob = value.stuff;
        assertEquals(TokenBuffer.class, ob.getClass());
        JsonParser jp = ((TokenBuffer) ob).asParser();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("a", jp.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(1, jp.getIntValue());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("b", jp.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(2, jp.getIntValue());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
    }
