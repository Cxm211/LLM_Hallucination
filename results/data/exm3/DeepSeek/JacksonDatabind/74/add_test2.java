// com/fasterxml/jackson/databind/jsontype/TestPolymorphicWithDefaultImpl.java
public void testWithInteger() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        AsPropertyWrapper wrapper = mapper.readValue("{ \"value\": 42 }", AsPropertyWrapper.class);
        assertEquals(42, wrapper.value);
    }
