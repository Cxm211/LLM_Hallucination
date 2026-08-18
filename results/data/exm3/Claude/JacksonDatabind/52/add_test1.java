// com/fasterxml/jackson/databind/jsontype/ext/ExternalTypeId999Test.java
public void testExternalTypeIdWithEmptyPayload() throws Exception
    {
        TypeReference<?> type = new TypeReference<Message<FooPayload>>() { };

        // Test edge case with minimal payload first
        Message<?> msg = MAPPER.readValue(aposToQuotes("{'payload': {}, 'type':'foo' }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);
        
        // Test with type first
        msg = MAPPER.readValue(aposToQuotes("{ 'type':'foo', 'payload': {} }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);
    }