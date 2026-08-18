// com/fasterxml/jackson/databind/exc/BasicExceptionTest.java
public void testExceptionMessageWithNullCause() throws Exception
    {
        try {
            MAPPER.readValue("{\"value\":\"test\"}",
                    new TypeReference<Map<String, CustomTypeWithNullMessage>>() { });
            fail("Should not pass");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertFalse("Should not contain 'null' as message", msg.contains(": null"));
        }
    }

    static class CustomTypeWithNullMessage {
        public CustomTypeWithNullMessage(String value) {
            throw new RuntimeException((String)null);
        }
    }