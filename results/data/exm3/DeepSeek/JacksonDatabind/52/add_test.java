// com/fasterxml/jackson/databind/jsontype/ext/ExternalTypeId999Test.java
public void testExternalTypeIdWithCreator() throws Exception
    {
        // Define a simple class with a creator and external type id
        ObjectMapper mapper = MAPPER;
        mapper.registerSubtypes(new com.fasterxml.jackson.databind.jsontype.NamedType(FooPayload.class, "foo"));
        
        // Use a static nested class for the test
        class CreatorMessage {
            @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
            public Object payload;
            public String type;
            
            @JsonCreator
            public CreatorMessage(@JsonProperty("payload") Object payload, @JsonProperty("type") String type) {
                this.payload = payload;
                this.type = type;
            }
        }
        
        String json = aposToQuotes("{ 'type':'foo', 'payload': {} }");
        CreatorMessage msg = mapper.readValue(json, CreatorMessage.class);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);
        assertTrue(msg.payload instanceof FooPayload);
    }
