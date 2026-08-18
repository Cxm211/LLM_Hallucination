// com/fasterxml/jackson/databind/jsontype/TestPolymorphicWithDefaultImpl.java
public void testWithNonEmptyStringWithoutDefaultImpl() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        try {
            mapper.readValue("{ \"value\": \"someValue\" }", AsPropertyWrapper.class);
            fail("Should have thrown exception");
        } catch (Exception e) {
            // Expected: missing type property
        }
    }