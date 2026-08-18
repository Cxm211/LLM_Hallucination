// com/fasterxml/jackson/databind/jsontype/TestPolymorphicWithDefaultImpl.java
public void testWithEmptyStringWithoutFeature() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        AsPropertyWrapper wrapper = mapper.readValue("{ \"value\": \"\" }", AsPropertyWrapper.class);
        assertEquals("", wrapper.value);
    }
