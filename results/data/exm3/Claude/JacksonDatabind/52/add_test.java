// com/fasterxml/jackson/databind/jsontype/ext/ExternalTypeId999Test.java
public void testExternalTypeIdWithMultipleProperties() throws Exception
    {
        TypeReference<?> type = new TypeReference<Message<FooPayload>>() { };

        // Test with multiple external type id properties in sequence
        Message<?> msg = MAPPER.readValue(aposToQuotes("{ 'type':'foo', 'payload': {}, 'type':'foo' }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);
    }