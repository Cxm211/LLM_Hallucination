// com/fasterxml/jackson/databind/filter/ProblemHandlerTest.java
public void testWeirdStringHandlingWithValidValue() throws Exception
{
    ObjectMapper mapper = new ObjectMapper()
        .addHandler(new DeserializationProblemHandler() {
            @Override
            public Object handleWeirdStringValue(DeserializationContext ctxt,
                    Class<?> targetType, String valueToConvert, String failureMsg)
                    throws IOException {
                // This handler should NOT be called for valid values
                fail("Handler should not be invoked for valid deserialization");
                return null;
            }
        });
    // Test with a valid UUID that should deserialize normally
    UUID validUuid = UUID.randomUUID();
    UUID result = mapper.readValue(quote(validUuid.toString()), UUID.class);
    assertEquals(validUuid, result);
}