// com/fasterxml/jackson/databind/struct/SingleValueAsArrayTest.java
public void testSuccessfulDeserializationWithArrayDelegateAndInjectables() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Bean1421A result = mapper.readValue(JSON, Bean1421A.class);
        assertNotNull(result);
    }