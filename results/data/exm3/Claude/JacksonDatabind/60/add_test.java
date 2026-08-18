// com/fasterxml/jackson/databind/jsontype/TestDefaultWithCreators.java
public void testWithCreatorAndJsonValueNullValue() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        String json = mapper.writeValueAsString(new Bean1385Wrapper(null));
        Bean1385Wrapper result = mapper.readValue(json, Bean1385Wrapper.class);
        assertNotNull(result);
        assertNull(result.value);
    }