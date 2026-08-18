// com/fasterxml/jackson/databind/deser/filter/ProblemHandlerUnknownTypeId2221Test.java
public void testWithoutDeserializationProblemHandler() throws Exception {
        final ObjectMapper mapper = new ObjectMapper()
                .enableDefaultTyping();
        try {
            GenericContent processableContent = mapper.readValue(JSON, GenericContent.class);
            fail("Should have thrown exception");
        } catch (InvalidTypeIdException e) {
            verifyException(e, "Could not resolve type id");
        }
    }