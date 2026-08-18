// com/fasterxml/jackson/databind/filter/ProblemHandlerTest.java
public void testWeirdStringHandlingNullReturnFromDeserialize() throws Exception
{
    ObjectMapper mapper = new ObjectMapper()
        .addHandler(new DeserializationProblemHandler() {
            @Override
            public Object handleWeirdStringValue(DeserializationContext ctxt,
                    Class<?> targetType, String valueToConvert, String failureMsg)
                    throws IOException {
                // Return null to indicate we want null as the result
                return null;
            }
        });
    // Test that null return from handler is properly handled
    java.net.URL result = mapper.readValue(quote("not a valid URL"), java.net.URL.class);
    assertNull(result);
}