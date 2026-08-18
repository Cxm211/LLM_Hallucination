// com/fasterxml/jackson/databind/jsontype/TestPolymorphicWithDefaultImpl.java
public void testWithNonEmptyStringWithFeature() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        AsPropertyWrapper wrapper = mapper.readValue("{ \"value\": \"test\" }", AsPropertyWrapper.class);
        assertEquals("test", wrapper.value);
    }
